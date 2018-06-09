/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.blikoon.qrcodescanner.QrCodeActivity;
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
import ir.hatamiarash.hyperonline.helpers.PermissionHelper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Transfer extends AppCompatActivity {
	private static final String CLASS = Activity_Transfer.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	Response.Listener<String> getListener;
	Response.Listener<String> confirmListener;
	Response.ErrorListener errorListener;
	Vibrator vibrator;
	
	@BindView(R.id.btnConfirm)
	FancyButton btnConfirm;
	@BindView(R.id.camera)
	ImageView scanQR;
	@BindView(R.id.code)
	EditText transferDest;
	@BindView(R.id.price)
	EditText transferPrice;
	
	private static final int REQUEST_CODE_QR_SCAN = 4511;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_transfer);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		btnConfirm.setCustomTextFont("sans.ttf");
		
		scanQR.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				if (PermissionHelper.checkCameraPermission(Activity_Transfer.this)) {
					Intent i = new Intent(Activity_Transfer.this, QrCodeActivity.class);
					startActivityForResult(i, REQUEST_CODE_QR_SCAN);
				} else {
					PermissionHelper.getCameraPermission(Activity_Transfer.this);
					if (PermissionHelper.checkCameraPermission(Activity_Transfer.this)) {
						Intent i = new Intent(Activity_Transfer.this, QrCodeActivity.class);
						startActivityForResult(i, REQUEST_CODE_QR_SCAN);
					} else {
						new MaterialStyledDialog.Builder(Activity_Transfer.this)
								.setTitle(FontHelper.getSpannedString(Activity_Transfer.this, "اسکن"))
								.setDescription(FontHelper.getSpannedString(Activity_Transfer.this, "جهت اسکن ، هایپرآنلاین نیازمند دسترسی به دوربین می باشد."))
								.setStyle(Style.HEADER_WITH_TITLE)
								.withDarkerOverlay(true)
								.withDialogAnimation(true)
								.setCancelable(true)
								.setPositiveText("باشه")
								.show();
					}
				}
			}
		});
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				String code = transferDest.getText().toString();
				String price = transferPrice.getText().toString();
				if (!transferDest.getText().toString().equals(getIntent().getStringExtra(TAGs.CODE)))
					if (!code.equals("")) {
						if (!price.equals("")) {
							if (Helper.isValidNumber(price)) {
								getConfirmation(code);
							} else {
								Helper.MakeToast(getApplicationContext(), "مبلغ وارد شده اشتباه است", TAGs.ERROR);
							}
						} else {
							Helper.MakeToast(getApplicationContext(), "مبلغ را وارد نمایید", TAGs.ERROR);
						}
					} else {
						Helper.MakeToast(getApplicationContext(), "شماره کیف پول مقصد را وارد نمایید", TAGs.ERROR);
					}
				else
					Helper.MakeToast(getApplicationContext(), "شما نمی توانید مبلغی به کیف پول خود منتقل کنید", TAGs.ERROR);
			}
		});
		
		getListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						String code = jObj.getString(TAGs.CODE);
						transferDest.setText(code.substring(3));
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
		
		confirmListener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				response = fixResponse(response);
				Timber.tag(CLASS).d(response);
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						Intent intent = new Intent(Activity_Transfer.this, Activity_TransferConfirm.class);
						intent.putExtra(TAGs.NAME, jObj.getString(TAGs.USER));
						intent.putExtra(TAGs.PRICE, jObj.getString(TAGs.PRICE));
						intent.putExtra(TAGs.CODE, transferDest.getText().toString());
						intent.putExtra("tPrice", transferPrice.getText().toString());
						intent.putExtra(TAGs.UID, getIntent().getStringExtra(TAGs.UID));
						intent.putExtra("src_code", getIntent().getStringExtra(TAGs.CODE));
						startActivity(intent);
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
		
		analyticsReport();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			Timber.tag("QR").d("COULD NOT GET A GOOD RESULT.");
			if (data == null)
				return;
			String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
			if (result != null) {
				new MaterialStyledDialog.Builder(Activity_Transfer.this)
						.setTitle(FontHelper.getSpannedString(Activity_Transfer.this, "اسکن"))
						.setDescription(FontHelper.getSpannedString(Activity_Transfer.this, "خطایی رخ داده است. امکان اسکن بارکد وجود ندارد"))
						.setStyle(Style.HEADER_WITH_TITLE)
						.withDarkerOverlay(true)
						.withDialogAnimation(true)
						.setCancelable(true)
						.setPositiveText("باشه")
						.show();
			}
			return;
			
		}
		if (requestCode == REQUEST_CODE_QR_SCAN) {
			if (data == null)
				return;
			String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
			Timber.tag("QR").d("Scan result : %s", result);
			getWalletCode(result);
		}
	}
	
	private void getWalletCode(String id) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "getWalletCode";
			JSONObject params = new JSONObject();
			params.put(TAGs.UNIQUE_ID, id);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, getListener, errorListener) {
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
	
	private void getConfirmation(String code) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "getTransferConfirmationByCode";
			JSONObject params = new JSONObject();
			params.put("dest_code", code);
			params.put("src_code", getIntent().getStringExtra(TAGs.CODE));
			params.put("user_id", getIntent().getStringExtra(TAGs.UID));
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, confirmListener, errorListener) {
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
