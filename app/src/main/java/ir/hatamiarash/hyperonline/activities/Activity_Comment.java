/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
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
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Comment extends AppCompatActivity {
	private static final String CLASS = Activity_Comment.class.getSimpleName();
	
	static SQLiteHandler db_user;
	Vibrator vibrator;
	SessionManager session;
	SweetAlertDialog progressDialog;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.comment_body)
	EditText body;
	@BindView(R.id.comment_send)
	FancyButton send;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		session = new SessionManager(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		send.setCustomTextFont("sans.ttf");
		
		setSupportActionBar(toolbar);
		try {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_action_bar_title, null);
			ActionBar.LayoutParams p = new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.END);
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "ارسال نظر"));
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
		
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				if (Helper.CheckInternet(getApplicationContext())) {
					if (session.isLoggedIn()) {
						if (body.length() > 0) {
							String msg_body = body.getText().toString();
							Send(
									msg_body,
									db_user.getUserDetails().get(TAGs.UID)
							);
						} else
							Helper.MakeToast(getApplicationContext(), "لطفا متن پیام را وادر نمایید", TAGs.WARNING);
					} else {
						Helper.MakeToast(getApplicationContext(), "لطفا برای ارسال نظر ، وارد شوید", TAGs.WARNING);
						Intent i = new Intent(getApplicationContext(), Activity_Login.class);
						startActivity(i);
						finish();
					}
				}
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
						Helper.MakeToast(getApplicationContext(), "پیام شما با موفقیت ارسال شد ، متشکریم !", TAGs.SUCCESS);
						Intent i = new Intent(getApplicationContext(), Activity_Main.class);
						startActivity(i);
						finish();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(Activity_Comment.this, errorMsg, TAGs.ERROR);
					}
				} catch (JSONException e) {
					Crashlytics.logException(e);
					finish();
				}
			}
		};
		
		errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crashlytics.logException(error);
				hideDialog();
				finish();
			}
		};
		
		analyticsReport();
	}
	
	private void Send(final String body, final String sender) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "comments";
			JSONObject params = new JSONObject();
			params.put(TAGs.BODY, body);
			params.put(TAGs.SENDER, sender);
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
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
						hideDialog();
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
		} catch (Exception e) {
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