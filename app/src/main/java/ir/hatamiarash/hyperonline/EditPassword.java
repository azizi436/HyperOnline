/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import helper.ConfirmManager;
import helper.FontHelper;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSupport;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class EditPassword extends AppCompatActivity {
	private SweetAlertDialog progressDialog;
	private Vibrator vibrator;
	private SQLiteHandlerItem db_item;
	private SQLiteHandlerSupport db_support;
	private SQLiteHandler db_user;
	private SessionManager session;
	private ConfirmManager confirmManager;
	
	@BindView(R.id.current_password)
	IconEditText current_password;
	@BindView(R.id.new_password)
	IconEditText new_password;
	@BindView(R.id.new_password_2)
	IconEditText new_password_2;
	@BindView(R.id.btnConfirm)
	Button btnConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_password);
		
		ButterKnife.bind(this);
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());
		session = new SessionManager(getApplicationContext());
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		confirmManager = new ConfirmManager(getApplicationContext());
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String cp = current_password.getText().toString();
				String np = new_password.getText().toString();
				String np2 = new_password_2.getText().toString();
				if (np.equals(np2))
					if (Helper.isValidPassword(np))
						UpdatePassword(np);
					else
						Helper.MakeToast(getApplicationContext(), "رمز عبور جدید معتبر نیست", TAGs.ERROR);
				else
					Helper.MakeToast(getApplicationContext(), "رمز های عبور مطابقت ندارند", TAGs.ERROR);
			}
		});
	}
	
	private void UpdatePassword(String pass) {
		progressDialog.setTitleText("لطفا منتظر بمانید");
		showDialog();
		
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = URLs.base_URL + "updatePassword";
			JSONObject params = new JSONObject();
			params.put(TAGs.PASSWORD, pass);
			params.put(TAGs.UID, db_user.getUserDetails().get(TAGs.UID));
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					Log.i("UpdatePassword R", response);
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							new MaterialStyledDialog.Builder(EditPassword.this)
									.setTitle(FontHelper.getSpannedString(EditPassword.this, "رمز عبور"))
									.setDescription(FontHelper.getSpannedString(EditPassword.this, "رمز عبور شما با موفقیت تغییر کرد. نیاز به ورود مجدد می باشد."))
									.setStyle(Style.HEADER_WITH_TITLE)
									.withDarkerOverlay(true)
									.withDialogAnimation(true)
									.setCancelable(false)
									.setPositiveText("باشه")
									.onPositive(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											logoutUser();
										}
									})
									.show();
						} else {
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(EditPassword.this, errorMsg, TAGs.ERROR);
						}
					} catch (JSONException e) {
						hideDialog();
						e.printStackTrace();
						finish();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e("UpdatePassword E", error.toString());
					hideDialog();
					finish();
				}
			}) {
				@NonNull
				@Contract(pure = true)
				@Override
				public String getBodyContentType() {
					return "application/json; charset=utf-8";
				}
				
				@Nullable
				@Override
				public byte[] getBody() throws AuthFailureError {
					try {
						return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException uee) {
						VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
						return null;
					}
				}
			};
			// add retry policy to prevent send request twice
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void logoutUser() {
		vibrator.vibrate(50);
		progressDialog.setTitleText("لطفا منتظر بمانید");
		showDialog();
		session.setLogin(false);
		confirmManager.setPhoneConfirm(false);
		confirmManager.setInfoConfirm(false);
		db_user.deleteUsers();
		db_item.deleteItems();
		db_support.deleteMessages();
		hideDialog();
		Helper.MakeToast(getApplicationContext(), "با موفقیت خارج شدید", TAGs.SUCCESS);
		Intent i = new Intent(getApplicationContext(), Activity_Main.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}
	
	private void showDialog() {
		if (!progressDialog.isShowing())
			progressDialog.show();
	}
	
	private void hideDialog() {
		if (progressDialog.isShowing())
			progressDialog.dismiss();
	}
}