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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.IconEditText;
import ir.hatamiarash.hyperonline.utils.Address;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Register extends AppCompatActivity {
	private static final String CLASS = Activity_Register.class.getSimpleName();
	
	SessionManager session;
	SQLiteHandler db;
	SweetAlertDialog progressDialog;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.btnConfirm)
	Button btnRegister;
	@BindView(R.id.btnChangePassword)
	Button btnLinkToLogin;
	@BindView(R.id.current_password)
	IconEditText inputName;
	@BindView(R.id.password)
	IconEditText inputPassword;
	@BindView(R.id.password2)
	IconEditText inputPassword2;
	@BindView(R.id.address)
	IconEditText inputAddress;
	@BindView(R.id.new_password_2)
	IconEditText inputPhone;
	@BindView(R.id.presenter)
	IconEditText inputPresenter;
	@BindView(R.id.province)
	Spinner inputProvince;
	@BindView(R.id.city)
	Spinner inputCity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		inputPhone.setError(Activity_Register.this, "همانند نمونه 09123456789");
		inputPassword.setError(Activity_Register.this, "حداقل 8 حرف");
		
		ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.provinces);
		ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.cities);
		inputProvince.setAdapter(provinceAdapter);
		inputProvince.setEnabled(false);
		inputCity.setAdapter(cityAdapter);
		inputName.requestFocus();
		
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		session = new SessionManager(getApplicationContext());
		db = new SQLiteHandler(getApplicationContext());
		
		if (session.isLoggedIn()) {
			Helper.MakeToast(getApplicationContext(), "شما قبلا وارد شده اید", TAGs.WARNING);
			startActivity(new Intent(getApplicationContext(), Activity_Main.class));
			finish();
		}
		
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String name = inputName.getText().toString();
				String password = inputPassword.getText().toString();
				String password2 = inputPassword2.getText().toString();
				String address = inputAddress.getText().toString();
				String phone = inputPhone.getText().toString();
				String presenter = inputPresenter.getText().toString();
				presenter = (presenter.isEmpty()) ? "" : presenter;
				
				if (Helper.CheckInternet(Activity_Register.this))
					if (!name.isEmpty() && !password.isEmpty() && !password2.isEmpty() && !address.isEmpty() && !phone.isEmpty())
						if (Helper.isValidPhone(phone))
							if (Helper.isValidPassword(password))
								if (password.equals(password2))
									if (inputProvince.getSelectedItem() != null && !inputProvince.getSelectedItem().toString().equals(""))
										if (inputCity.getSelectedItem() != null && !inputCity.getSelectedItem().toString().equals("انتخاب کنید"))
											prepareRequest(
													name,
													password,
													address,
													phone,
													inputProvince.getSelectedItem().toString(),
													inputCity.getSelectedItem().toString(),
													presenter
											);
										else
											Helper.MakeToast(Activity_Register.this, "شهر را انتخاب نمایید", TAGs.ERROR);
									else
										Helper.MakeToast(Activity_Register.this, "استان را انتخاب نمایید", TAGs.ERROR);
								else
									Helper.MakeToast(Activity_Register.this, "کلمه عبور ها تطابق ندارند", TAGs.ERROR);
							else
								Helper.MakeToast(Activity_Register.this, "کلمه عبور معتبر نیست", TAGs.ERROR);
						else
							Helper.MakeToast(Activity_Register.this, "شماره موبایل را بررسی نمایید", TAGs.ERROR);
					else
						Helper.MakeToast(Activity_Register.this, "تمامی کادر ها را پر نمایید", TAGs.ERROR);
			}
		});
		
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), Activity_Login.class);
				startActivity(i);
				finish();
			}
		});
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						MakeDialog();
					} else {
						hideDialog();
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(Activity_Register.this, errorMsg, TAGs.ERROR); // show error message
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
				Helper.MakeToast(Activity_Register.this, error.toString(), TAGs.ERROR);
				hideDialog();
			}
		};
		
		analyticsReport();
	}
	
	private void prepareRequest(final String name,
	                            final String password,
	                            final String address,
	                            final String phone,
	                            final String state,
	                            final String city,
	                            final String presenter) {
		showDialog();
		try {
			JSONObject params = new JSONObject();
			params.put(TAGs.NAME, name);
			params.put(TAGs.ADDRESS, address);
			params.put(TAGs.PHONE, phone);
			params.put(TAGs.PASSWORD, password);
			params.put(TAGs.STATE, state);
			params.put(TAGs.CITY, city);
			params.put("presenter", presenter);
			final String body = params.toString();
			registerUser(body);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}
	
	private void registerUser(final String body) {
		String URL = getResources().getString(R.string.url_api, HOST) + "users";
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
	
	private void MakeDialog() {
		new MaterialStyledDialog.Builder(this)
				.setTitle(FontHelper.getSpannedString(this, "ثبت نام انجام شد"))
				.setDescription(FontHelper.getSpannedString(this, "نام کاربری شما تلفن همراهتان می باشد ، اکنون می توانید وارد شوید"))
				.setStyle(Style.HEADER_WITH_TITLE)
				.withDarkerOverlay(true)
				.withDialogAnimation(true)
				.setCancelable(true)
				.setPositiveText("باشه")
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						Intent intent = new Intent(Activity_Register.this, Activity_Login.class);
						startActivity(intent);
						finish();
					}
				})
				.show();
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