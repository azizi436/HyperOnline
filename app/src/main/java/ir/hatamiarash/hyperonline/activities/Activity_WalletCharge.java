/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.preferences.WalletManager;
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_WalletCharge extends AppCompatActivity {
	private static final String CLASS = Activity_WalletCharge.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	Vibrator vibrator;
	WalletManager walletManager;
	
	@BindView(R.id.btnConfirm)
	FancyButton btnConfirm;
	@BindView(R.id.price)
	EditText chargePrice;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_wallet_charge);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		walletManager = new WalletManager(Activity_WalletCharge.this);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		btnConfirm.setCustomTextFont("sans.ttf");
		
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
						Pay(jObj.getString(TAGs.CODE));
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
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				String price = chargePrice.getText().toString();
				if (!price.equals("")) {
					if (Integer.valueOf(price) >= 1000) {
						if (Helper.isValidNumber(price)) {
							requestCharge(getIntent().getStringExtra(TAGs.UID), price);
						} else {
							Helper.MakeToast(getApplicationContext(), "مبلغ وارد شده اشتباه است", TAGs.ERROR);
						}
					} else {
						Helper.MakeToast(getApplicationContext(), "حداقل مبلغ شارژ 1000 تومان می باشد", TAGs.ERROR);
					}
				} else {
					Helper.MakeToast(getApplicationContext(), "مبلغ را وارد نمایید", TAGs.ERROR);
				}
			}
		});
		
		if (walletManager.isWalletFirstUse()) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			final View customView = inflater.inflate(R.layout.dialog_wallet_policy, null);
			final TextView policies = customView.findViewById(R.id.policies);
			policies.setMovementMethod(new ScrollingMovementMethod());
			policies.setText(getResources().getString(R.string.policy_wallet).replace("\\n", System.getProperty("line.separator")));
			new MaterialStyledDialog.Builder(Activity_WalletCharge.this)
					.setTitle(FontHelper.getSpannedString(Activity_WalletCharge.this, "کیف پول"))
					.setDescription(FontHelper.getSpannedString(Activity_WalletCharge.this, "لطفا قبل از استفاده از کیف پول موارد زیر را خوانده و سپس تایید کنید :"))
					.setStyle(Style.HEADER_WITH_TITLE)
					.setHeaderColor(R.color.green)
					.setCustomView(customView, 5, 5, 5, 5)
					.withDarkerOverlay(true)
					.withDialogAnimation(true)
					.setCancelable(false)
					.setPositiveText("می پذیرم")
					.setNegativeText("نمی پذیرم")
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							analytics.reportEvent("Wallet - Accept Policies");
							walletManager.setWalletFirstUse(false);
						}
					})
					.onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							analytics.reportEvent("Wallet - Decline Policies");
							finish();
						}
					})
					.show();
		}
		
		analyticsReport();
	}
	
	private void Pay(String ID) {
		String Address = getResources().getString(R.string.url_charge, HOST) + ID;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Address));
		startActivityForResult(i, 200);
	}
	
	private void requestCharge(String id, String price) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "chargeWalletTemp";
			JSONObject params = new JSONObject();
			params.put(TAGs.UID, id);
			params.put(TAGs.PRICE, price);
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
