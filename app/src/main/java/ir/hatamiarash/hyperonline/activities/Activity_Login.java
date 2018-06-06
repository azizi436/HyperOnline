/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amplitude.api.Amplitude;
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
import ir.hatamiarash.hyperonline.helpers.ConfirmManager;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.IconEditText;
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Login extends AppCompatActivity {
	private static final String CLASS = Activity_Login.class.getSimpleName();
	
	SweetAlertDialog progressDialog;       // dialog window
	SessionManager session;                // session for check user logged status
	SQLiteHandler db;                      // users database
	ConfirmManager confirmManager;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.btnLogin)
	FancyButton btnLogin;
	@BindView(R.id.btnLinkToRegisterScreen)
	FancyButton btnLinkToRegister;
	@BindView(R.id.btnLinkToResetPassword)
	FancyButton btnLinkToResetPassword;
	@BindView(R.id.new_password_2)
	IconEditText inputPhone;
	@BindView(R.id.password)
	IconEditText inputPassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		db = new SQLiteHandler(getApplicationContext());                         // users database
		db.CreateTable();                                                        // create users table
		session = new SessionManager(getApplicationContext());
		confirmManager = new ConfirmManager(getApplicationContext());
		btnLogin.setCustomTextFont("sans.ttf");
		btnLinkToRegister.setCustomTextFont("sans.ttf");
		btnLinkToResetPassword.setCustomTextFont("sans.ttf");
		
		if (session.isLoggedIn()) {                                              // Check if user is already logged in or not
			Intent i = new Intent(Activity_Login.this, Activity_Main.class);
			startActivity(i);                                                    // start main activity
			finish();                                                            // close this activity
		}
		
		btnLogin.setOnClickListener(new View.OnClickListener() {                      // login button's event
			public void onClick(View view) {
				String phone = inputPhone.getText().toString();                       // get email from text input
				String password = inputPassword.getText().toString();                 // get password from text input
				if (Helper.CheckInternet(Activity_Login.this))                                 // check network connection status
					if (Helper.isValidPhone(phone) && password.trim().length() > 0) // check empty fields
						prepareRequest(phone, password);                                  // check user login request from server
					else
						Helper.MakeToast(Activity_Login.this, "مشخصات را بررسی نمایید", TAGs.WARNING);
			}
		});
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {             // register button's event
			public void onClick(View view) {
				Intent i = new Intent(Activity_Login.this, Activity_Register.class);
				startActivity(i);
				finish();
			}
		});
		btnLinkToResetPassword.setOnClickListener(new View.OnClickListener() {        // register button's event
			public void onClick(View view) {
				Intent i = new Intent(Activity_Login.this, Activity_ResetPassword.class);
				startActivity(i);
				finish();
			}
		});
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						analytics.reportEvent("User - Login");
						analytics.reportLogin();
						session.setLogin(true);
						final JSONObject user = jObj.getJSONObject(TAGs.USER);
						String uid = user.getString(TAGs.UNIQUE_ID);
						Crashlytics.setUserIdentifier(uid);
						Crashlytics.setUserName(user.getString(TAGs.NAME));
						Amplitude.getInstance().setUserId(uid);
						
						db.addUser(
								user.getString(TAGs.NAME),
								"test@gmail.com",
								user.getString(TAGs.ADDRESS),
								user.getString(TAGs.PHONE),
								uid,
								"Iran",
								user.getString(TAGs.STATE),
								user.getString(TAGs.CITY)
						);
						if (Integer.valueOf(user.getString("confirmed_phone")) == 0) {
							confirmManager.setPhoneConfirm(true);
							confirmManager.setPhoneConfirm(false);
							new MaterialStyledDialog.Builder(Activity_Login.this)
									.setTitle(FontHelper.getSpannedString(Activity_Login.this, "تایید حساب"))
									.setDescription(FontHelper.getSpannedString(Activity_Login.this, "لطفا شماره تلفن خود را تایید کنید"))
									.setStyle(Style.HEADER_WITH_TITLE)
									.withDarkerOverlay(true)
									.withDialogAnimation(true)
									.setCancelable(false)
									.setPositiveText("باشه")
									.onPositive(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											try {
												Intent intent = new Intent(Activity_Login.this, Activity_ConfirmPhone.class);
												intent.putExtra(TAGs.PHONE, user.getString(TAGs.PHONE));
												startActivity(intent);
												finish();
											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									})
									.show();
						} else {
							confirmManager.setPhoneConfirm(true);
							
							if (Integer.valueOf(user.getString("confirmed_info")) == 0)
								confirmManager.setInfoConfirm(false);
							else
								confirmManager.setInfoConfirm(true);
							
							String msg = "سلام " + user.getString(TAGs.NAME);
							Helper.MakeToast(Activity_Login.this, msg, TAGs.SUCCESS);
							Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(Activity_Login.this, errorMsg, TAGs.ERROR); // show error message
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
				Helper.MakeToast(getApplicationContext(), "مشکلی پیش آمده است", TAGs.ERROR);
				finish();
			}
		};
		
		analyticsReport();
	}
	
	private void prepareRequest(final String phone, final String password) {
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		try {
			JSONObject params = new JSONObject();
			params.put(TAGs.PHONE, phone);
			params.put(TAGs.PASSWORD, password);
			final String body = params.toString();
			CheckLogin(body);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}
	
	private void CheckLogin(final String body) {
		String URL = getResources().getString(R.string.url_api, HOST) + "login";
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
					return body.getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					Crashlytics.logException(e);
					return null;
				}
			}
		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(
				0,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
		));
		application.addToRequestQueue(stringRequest);
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