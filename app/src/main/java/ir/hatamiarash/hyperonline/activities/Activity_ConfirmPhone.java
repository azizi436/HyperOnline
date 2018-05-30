/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.concurrent.TimeUnit;

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
import ir.hatamiarash.hyperonline.helpers.PermissionHelper;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.interfaces.SmsListener;
import ir.hatamiarash.hyperonline.receivers.SmsReceiver;
import ir.hatamiarash.hyperonline.utils.TAGs;
import ir.hatamiarash.hyperonline.utils.Values;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_ConfirmPhone extends AppCompatActivity implements SmsListener {
	private static final String CLASS = Activity_ConfirmPhone.class.getSimpleName();
	
	SweetAlertDialog progressDialog;
	ConfirmManager confirmManager;
	SQLiteHandler db_user;
	SQLiteHandlerItem db_item;
	SQLiteHandlerSupport db_support;
	SessionManager session;
	SmsReceiver receiver;
	Vibrator vibrator;
	Response.Listener<String> requestListener;
	Response.Listener<String> syncListener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.button0)
	Button button0;
	@BindView(R.id.button1)
	Button button1;
	@BindView(R.id.button2)
	Button button2;
	@BindView(R.id.button3)
	Button button3;
	@BindView(R.id.button4)
	Button button4;
	@BindView(R.id.button5)
	Button button5;
	@BindView(R.id.button6)
	Button button6;
	@BindView(R.id.button7)
	Button button7;
	@BindView(R.id.button8)
	Button button8;
	@BindView(R.id.button9)
	Button button9;
	@BindView(R.id.editText)
	EditText passwordInput;
	@BindView(R.id.time)
	TextView time;
	@BindView(R.id.help)
	FancyButton help;
	@BindView(R.id.logout)
	FancyButton logout;
	@BindView(R.id.phone)
	TextView phone;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	
	boolean keyPadLockedFlag = false;
	boolean WaitFlag = false;
	String userEntered;
	String phoneNumber;
	String confirmCode;
	String FORMAT = "%01d:%02d";
	int PIN_LENGTH = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_phone);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		confirmManager = new ConfirmManager(this);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		session = new SessionManager(getApplicationContext());
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		db_user = new SQLiteHandler(getApplicationContext());
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		
		phoneNumber = getIntent().getStringExtra(TAGs.PHONE);
		phone.setText(phoneNumber);
		
		setSupportActionBar(toolbar);
		try {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_action_bar_title, null);
			ActionBar.LayoutParams p = new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.END);
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "تایید تلفن همراه"));
			getSupportActionBar().setCustomView(v, p);
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
		}
		
		userEntered = "";
		
		button0.setOnClickListener(pinButtonHandler);
		button1.setOnClickListener(pinButtonHandler);
		button2.setOnClickListener(pinButtonHandler);
		button3.setOnClickListener(pinButtonHandler);
		button4.setOnClickListener(pinButtonHandler);
		button5.setOnClickListener(pinButtonHandler);
		button6.setOnClickListener(pinButtonHandler);
		button7.setOnClickListener(pinButtonHandler);
		button8.setOnClickListener(pinButtonHandler);
		button9.setOnClickListener(pinButtonHandler);
		
		time.setVisibility(View.VISIBLE);
		help.setVisibility(View.GONE);
		logout.setVisibility(View.GONE);
		
		requestListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						String code = jObj.getString("code");
						confirmCode = String.valueOf(Integer.valueOf(code) - getResources().getInteger(R.integer.SMSCODE));
						Timer();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
						finish();
					}
				} catch (JSONException e) {
					Crashlytics.logException(e);
					hideDialog();
					finish();
				}
			}
		};
		
		syncListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						confirmManager.setPhoneConfirm(true);
						Helper.MakeToast(getApplicationContext(), "حساب شما فعال شد", TAGs.SUCCESS);
						Intent i = new Intent(Activity_ConfirmPhone.this, Activity_Main.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
						finish();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
					}
				} catch (JSONException e) {
					Crashlytics.logException(e);
					hideDialog();
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
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			getPermission(Activity_ConfirmPhone.this);
		else
			RequestCode();
		
		help.setCustomTextFont("sans.ttf");
		logout.setCustomTextFont("sans.ttf");
		
		help.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + Values.phoneNumber));
				startActivity(intent);
			}
		});
		
		logout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
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
		});
		
		analyticsReport();
	}
	
	private void Timer() {
		new CountDownTimer(90000, 1000) {
			public void onTick(long millisUntilFinished) {
				time.setText("" + String.format(
						FORMAT,
						TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
						TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
						)
				);
			}
			
			public void onFinish() {
				help.setVisibility(View.VISIBLE);
				logout.setVisibility(View.VISIBLE);
				time.setText("کد فعالسازی ارسال شده است. در صورت عدم دریافت کد ، پس از بررسی وضعیت تلفن همراه خود با ما در تماس باشید.");
				time.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
			}
		}.start();
	}
	
	@Override
	public void onBackPressed() {
		//App not allowed to go back to Parent activity until correct pin entered. comment following code
		//super.onBackPressed();
	}
	
	private void RequestCode() {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "verifyPhone";
			JSONObject params = new JSONObject();
			params.put(TAGs.PHONE, phoneNumber);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, requestListener, errorListener) {
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
	
	private void syncServer() {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "verifyPhoneOK";
			JSONObject params = new JSONObject();
			params.put(TAGs.PHONE, phoneNumber);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, syncListener, errorListener) {
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
	
	private class LockKeyPadOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Crashlytics.logException(e);
			}
			return "Executed";
		}
		
		@Override
		protected void onPostExecute(String result) {
			hideDialog();
			passwordInput.setText("");
			userEntered = "";
			keyPadLockedFlag = false;
			if (WaitFlag) {
				time.setVisibility(View.VISIBLE);
				Timer();
				WaitFlag = false;
			}
		}
	}
	
	public void getPermission(final Activity activity) {
		if (PermissionHelper.checkSMSPermission(Activity_ConfirmPhone.this))
			new MaterialStyledDialog.Builder(activity)
					.setTitle(FontHelper.getSpannedString(activity, "تایید پیامکی"))
					.setDescription(FontHelper.getSpannedString(activity, "جهت تایید خودکار شماره تلفن هایپرآنلاین نیاز به دسترسی دارد"))
					.setStyle(Style.HEADER_WITH_TITLE)
					.withDarkerOverlay(true)
					.withDialogAnimation(true)
					.setCancelable(false)
					.setPositiveText("باشه")
					.setNegativeText("خیر")
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							getPermission();
						}
					})
					.onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							RequestCode();
						}
					})
					.show();
		else {
			registerSmsListener();
			RequestCode();
		}
	}
	
	@Override
	public void handleSms(String sender, String message) {
		String sms_code = message.replaceAll("[^0-9]", "");
		if (sms_code.equals(confirmCode)) {
			syncServer();
			unregisterReceiver(receiver);
		}
	}
	
	private void registerSmsListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(2147483647);
		receiver = new SmsReceiver(this);
		registerReceiver(receiver, filter);
	}
	
	private void getPermission() {
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 4621);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case 4621: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					registerSmsListener();
					RequestCode();
				} else {
					RequestCode();
				}
			}
		}
	}
	
	private View.OnClickListener pinButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			vibrator.vibrate(50);
			if (keyPadLockedFlag)
				return;
			Button pressedButton = (Button) v;
			if (userEntered.length() < PIN_LENGTH) {
				userEntered = userEntered + pressedButton.getText();
				passwordInput.setText(userEntered);
				passwordInput.setSelection(passwordInput.getText().toString().length());
				if (userEntered.length() == PIN_LENGTH)
					if (userEntered.equals(confirmCode)) {
						syncServer();
					} else {
						Helper.MakeToast(getApplicationContext(), "کد وارد شده اشتباه است", TAGs.ERROR);
						new LockKeyPadOperation().execute("");
					}
			} else {
				passwordInput.setText("");
				userEntered = "";
				userEntered = userEntered + pressedButton.getText();
				passwordInput.setText("8");
			}
		}
	};
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	private void showDialog() {
		if (!progressDialog.isShowing())
			progressDialog.show();
	}
	
	private void hideDialog() {
		if (progressDialog.isShowing())
			progressDialog.dismiss();
	}
	
	private void analyticsReport() {
		analytics.reportScreen(CLASS);
		analytics.reportEvent("Open " + CLASS);
	}
}