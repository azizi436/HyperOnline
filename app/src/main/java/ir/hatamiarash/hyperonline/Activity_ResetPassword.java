/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import helper.Helper;
import ir.hatamiarash.utils.TAGs;

public class Activity_ResetPassword extends AppCompatActivity {
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.phone)
	EditText inputPhone;
	@BindView(R.id.btnSet)
	Button btnSet;
	
	private static String HOST;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_password);
		
		ButterKnife.bind(this);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		
		HOST = getResources().getString(R.string.url_host);
		
		btnSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				if (Helper.CheckInternet(getApplicationContext()))
					if (Helper.isValidPhone(inputPhone.getText().toString()))
						resetPassword(inputPhone.getText().toString());
					else
						Helper.MakeToast(getApplicationContext(), "شماره موبایل را بررسی نمایید", TAGs.WARNING);
			}
		});
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						MakeQuestion();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
					}
				} catch (JSONException e) {
					hideDialog();
					finish();
				}
			}
		};
		
		errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				hideDialog();
			}
		};
	}
	
	private void resetPassword(final String phone) {
		showDialog();
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "resetPassword";
			JSONObject params = new JSONObject();
			params.put(TAGs.PHONE, phone);
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
			requestQueue.add(stringRequest);
		} catch (JSONException e) {
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
	
	private void MakeQuestion() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(Activity_ResetPassword.this);
		dialog.setTitle("تعویض کلمه عبور");
		dialog.setMessage("کلمه عبور جدید به شماره شما ارسال شد");
		dialog.setIcon(R.drawable.ic_success);
		dialog.setPositiveButton("تایید", new DialogInterface.OnClickListener() { // negative answer
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				Intent i = new Intent(getApplicationContext(), Activity_Login.class);
				startActivity(i);
				finish();
			}
		});
		AlertDialog alert = dialog.create();
		alert.show();
	}
}