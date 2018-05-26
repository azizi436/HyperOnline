/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

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

import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.utils.TAGs;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_CheckTransaction extends AppCompatActivity {
	private static final String CLASS = Activity_CheckTransaction.class.getSimpleName();
	
	SQLiteHandlerItem db_item;
	SweetAlertDialog progressDialog;
	Uri uri;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		db_item = new SQLiteHandlerItem(getApplicationContext());
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		uri = getIntent().getData();
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						db_item.deleteItems();
						Intent i = new Intent(getApplicationContext(), Activity_Factor.class);
						i.putExtra("order_code", uri.getQueryParameter("code"));
						startActivity(i);
						finish();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
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
		
		try {
			int error = Integer.valueOf(uri.getQueryParameter("error"));
			if (error == 0) {
				new MaterialStyledDialog.Builder(Activity_CheckTransaction.this)
						.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
						.setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت موفقیت آمیز بود. با تشکر از انتخاب شما."))
						.setStyle(Style.HEADER_WITH_TITLE)
						.setHeaderColor(R.color.green)
						.withDarkerOverlay(true)
						.withDialogAnimation(true)
						.setCancelable(false)
						.setPositiveText("باشه")
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								CompleteOrder(uri.getQueryParameter("code"));
							}
						})
						.show();
			} else if (error == 1) {
				new MaterialStyledDialog.Builder(Activity_CheckTransaction.this)
						.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
						.setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت با مشکل مواجه شده است. در صورت کسر وجه ، با پشتیبانی تماس حاصل فرمایید. کد خطا : " + uri.getQueryParameter("er_code")))
						.setStyle(Style.HEADER_WITH_TITLE)
						.withDarkerOverlay(true)
						.withDialogAnimation(true)
						.setCancelable(false)
						.setPositiveText("باشه")
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								finish();
							}
						})
						.show();
			}
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
			finish();
		}
		
		analyticsReport();
	}
	
	private void CompleteOrder(final String id) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "complete_order";
			JSONObject params = new JSONObject();
			params.put("id", id);
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
