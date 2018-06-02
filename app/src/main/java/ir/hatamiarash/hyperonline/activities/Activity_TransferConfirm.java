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
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;
import static ir.hatamiarash.hyperonline.helpers.PriceHelper.formatPrice;

public class Activity_TransferConfirm extends AppCompatActivity {
	private static final String CLASS = Activity_TransferConfirm.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.tPrice)
	TextView tPrice;
	@BindView(R.id.cWallet)
	TextView cWallet;
	@BindView(R.id.tUser)
	TextView tUser;
	@BindView(R.id.cPrice)
	TextView cPrice;
	@BindView(R.id.aPrice)
	TextView aPrice;
	@BindView(R.id.message)
	TextView message;
	@BindView(R.id.confirm)
	TextView confirm;
	
	String transferDest;
	String transferSrc;
	String transferUser;
	String transferPrice;
	String currentPrice;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_transfer_confirm);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		setSupportActionBar(toolbar);
		try {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_action_bar_title, null);
			ActionBar.LayoutParams p = new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.END);
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "تایید انتقال"));
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
		
		transferDest = getIntent().getStringExtra(TAGs.CODE);
		transferSrc = getIntent().getStringExtra("src_code");
		transferUser = getIntent().getStringExtra(TAGs.NAME);
		transferPrice = getIntent().getStringExtra("tPrice");
		currentPrice = getIntent().getStringExtra(TAGs.PRICE);
		
		message.setVisibility(View.GONE);
		tPrice.setText(formatPrice(transferPrice) + " تومان");
		cPrice.setText(formatPrice(currentPrice) + " تومان");
		final int afterPrice = Integer.valueOf(currentPrice) - Integer.valueOf(transferPrice);
		aPrice.setText(formatPrice(String.valueOf(afterPrice)) + " تومان");
		cWallet.setText("اصلی");
		tUser.setText(transferUser);
		
		if (afterPrice < 0) {
			message.setVisibility(View.VISIBLE);
			confirm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));
		}
		
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				if (afterPrice >= 0) {
					transferMoney(transferSrc, transferDest, getIntent().getStringExtra(TAGs.UID), transferPrice);
				} else {
					Helper.MakeToast(getApplicationContext(), "موجودی کافی نیست", TAGs.ERROR);
				}
			}
		});
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				hideDialog();
				try {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					JSONObject object = new JSONObject(response);
					Boolean error = object.getBoolean(TAGs.ERROR);
					if (!error) {
						Intent intent = new Intent(getApplicationContext(), Activity_Main.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						startActivity(new Intent(getApplicationContext(), Activity_Transactions.class));
						Helper.MakeToast(getApplicationContext(), "انتقال موفقیت آمیز بود", TAGs.SUCCESS);
						finish();
					} else {
						String errorMsg = object.getString(TAGs.ERROR_MSG);
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
	
	private void transferMoney(String src_code, String des_code, String user_id, String price) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "transferMoney";
			JSONObject params = new JSONObject();
			params.put("dest_code", des_code);
			params.put("src_code", src_code);
			params.put("user_id", user_id);
			params.put("price", price);
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
