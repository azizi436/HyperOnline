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
import android.widget.ProgressBar;

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
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.IconEditText;
import ir.hatamiarash.hyperonline.preferences.ConfirmManager;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_EditProfile extends AppCompatActivity {
	private static final String CLASS = Activity_EditProfile.class.getSimpleName();
	
	SweetAlertDialog progressDialog;
	SQLiteHandler db_user;
	Vibrator vibrator;
	ConfirmManager confirmManager;
	Response.Listener<String> getUserListener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.name)
	IconEditText txtName;
	@BindView(R.id.address)
	IconEditText txtAddress;
	@BindView(R.id.btnConfirm)
	Button btnConfirm;
	@BindView(R.id.btnChangePassword)
	Button btnChangePassword;
	@BindView(R.id.progress_bar)
	ProgressBar progressBar;
	
	String uid, BACKUP_NAME, BACKUP_ADDRESS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		progressBar.setVisibility(View.GONE);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		confirmManager = new ConfirmManager(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());              // user local database
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {         // confirm button's event
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				String name = txtName.getText().toString();
				String address = txtAddress.getText().toString();
				if (!BACKUP_ADDRESS.equals(address) || !BACKUP_NAME.equals(name)) {
					if (Helper.CheckInternet(getApplicationContext()))
						if (!name.isEmpty() && !address.isEmpty())
							UpdateUser(name, address);
						else
							Helper.MakeToast(getApplicationContext(), "تمامی کادر ها را پر نمایید", TAGs.WARNING);
				} else {
					Intent data = new Intent();
					setResult(2, data);
					finish();
				}
				
			}
		});
		
		btnChangePassword.setOnClickListener(new View.OnClickListener() {         // confirm button's event
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), Activity_EditPassword.class);
				startActivity(i);
				finish();
			}
		});
		
		getUserListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {                          // Check for error node in json
						JSONObject user = jObj.getJSONObject(TAGs.USER);
						BACKUP_ADDRESS = user.getString(TAGs.ADDRESS);
						BACKUP_NAME = user.getString(TAGs.NAME);
						txtName.setText(getApplicationContext(), BACKUP_NAME);
						txtAddress.setText(getApplicationContext(), BACKUP_ADDRESS);
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR); // show error message
						finish();
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
				Crashlytics.logException(error);
				hideDialog();
			}
		};
		
		uid = db_user.getUserDetails().get(TAGs.UID);
		if (Helper.CheckInternet(getApplicationContext()))
			GetUser();
		else
			finish();
		
		analyticsReport();
	}
	
	private void GetUser() {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "users/" + uid;
			JSONObject params = new JSONObject();
			params.put(TAGs.UNIQUE_ID, uid);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, getUserListener, errorListener) {
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
			application.addToRequestQueue(stringRequest);
		} catch (JSONException e) {
			hideDialog();
		}
	}
	
	private void UpdateUser(final String name, final String address) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "user_update";
			JSONObject params = new JSONObject();
			params.put(TAGs.NAME, name);
			params.put(TAGs.ADDRESS, address);
			params.put(TAGs.UID, uid);
			params.put(TAGs.IMAGE, TAGs.NULL);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {                          // Check for error node in json
							db_user.updateUser(uid, name, address);
							new MaterialStyledDialog.Builder(Activity_EditProfile.this)
									.setTitle(FontHelper.getSpannedString(Activity_EditProfile.this, "ویرایش مشخصات"))
									.setDescription(FontHelper.getSpannedString(Activity_EditProfile.this, "اطلاعات شما با موفقیت تغییر کرد. حساب شما نیاز به تایید مجدد دارد"))
									.setStyle(Style.HEADER_WITH_TITLE)
									.withDarkerOverlay(true)
									.withDialogAnimation(true)
									.setCancelable(false)
									.setPositiveText("باشه")
									.onPositive(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											confirmManager.setInfoConfirm(false);
											Intent data = new Intent();
											setResult(1, data);
											finish();
										}
									})
									.show();
						} else {
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(Activity_EditProfile.this, errorMsg, TAGs.ERROR); // show error message
						}
					} catch (JSONException e) {
						Crashlytics.logException(e);
						hideDialog();
						finish();
					}
				}
			}, errorListener) {
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
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
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