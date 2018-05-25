/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.databases.SQLiteHandler;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerSupport;
import ir.hatamiarash.hyperonline.helpers.ConfirmManager;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.IconEditText;
import ir.hatamiarash.hyperonline.utils.TAGs;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_EditPassword extends AppCompatActivity {
	private static final String CLASS = Activity_EditPassword.class.getSimpleName();
	
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	SQLiteHandlerItem db_item;
	SQLiteHandlerSupport db_support;
	SQLiteHandler db_user;
	SessionManager session;
	ConfirmManager confirmManager;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.new_password)
	IconEditText new_password;
	@BindView(R.id.new_password_2)
	IconEditText new_password_2;
	@BindView(R.id.btnConfirm)
	Button btnConfirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_password);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());
		session = new SessionManager(getApplicationContext());
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		confirmManager = new ConfirmManager(getApplicationContext());
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						new MaterialStyledDialog.Builder(Activity_EditPassword.this)
								.setTitle(FontHelper.getSpannedString(Activity_EditPassword.this, "رمز عبور"))
								.setDescription(FontHelper.getSpannedString(Activity_EditPassword.this, "رمز عبور شما با موفقیت تغییر کرد. نیاز به ورود مجدد می باشد."))
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
						Helper.MakeToast(Activity_EditPassword.this, errorMsg, TAGs.ERROR);
					}
				} catch (JSONException e) {
					Crashlytics.logException(e);
					hideDialog();
					finish();
				}
			}
		};
		
		errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				hideDialog();
				finish();
			}
		};
		
		analyticsReport();
	}
	
	private void UpdatePassword(String pass) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "updatePassword";
			JSONObject params = new JSONObject();
			params.put(TAGs.PASSWORD, pass);
			params.put(TAGs.UID, db_user.getUserDetails().get(TAGs.UID));
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, listener, errorListener) {
				@NonNull
				@Contract(pure = true)
				@Override
				public String getBodyContentType() {
					return "application/json; charset=utf-8";
				}
				
				@Nullable
				@Override
				public byte[] getBody() {
					try {
						return mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException uee) {
						hideDialog();
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
			application.addToRequestQueue(stringRequest);
		} catch (JSONException e) {
			Crashlytics.logException(e);
			hideDialog();
		}
	}
	
	private void logoutUser() {
		vibrator.vibrate(50);
		progressDialog.setTitleText(getResources().getString(R.string.wait));
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
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	private void analyticsReport() {
		analytics.reportScreen(CLASS);
		analytics.reportEvent("Open " + CLASS);
	}
}