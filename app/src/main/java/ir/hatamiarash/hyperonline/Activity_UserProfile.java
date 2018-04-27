/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import helper.ConfirmManager;
import helper.Helper;
import helper.PriceHelper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSupport;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;

public class Activity_UserProfile extends AppCompatActivity {
	SQLiteHandler db_user;
	SQLiteHandlerItem db_item;
	SQLiteHandlerSupport db_support;
	SessionManager session;
	ConfirmManager confirmManager;
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.btnLogout)
	FancyButton btnLogout;
	@BindView(R.id.btnEdit)
	FancyButton btnEdit;
	@BindView(R.id.name)
	TextView User_Name;
	@BindView(R.id.address)
	TextView User_Address;
	@BindView(R.id.phone)
	TextView User_Phone;
	@BindView(R.id.tPrice)
	TextView tPrice;
	@BindView(R.id.tCount)
	TextView tCount;
	@BindView(R.id.image)
	ImageView User_Photo;
	@BindView(R.id.progress_bar)
	ProgressBar progressBar;
	@BindView(R.id.user_info)
	RelativeLayout user_info;
	@BindView(R.id.order_info)
	RelativeLayout order_info;
	
	private static String HOST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);
		ButterKnife.bind(this);
		
		progressBar.setVisibility(View.GONE);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		db_user = new SQLiteHandler(getApplicationContext());
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		session = new SessionManager(getApplicationContext());
		confirmManager = new ConfirmManager(getApplicationContext());
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		
		HOST = getResources().getString(R.string.url_host);
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		if (!session.isLoggedIn()) logoutUser();
		
		user_info.setVisibility(View.INVISIBLE);
		order_info.setVisibility(View.INVISIBLE);
		
		btnEdit.setCustomTextFont("sans.ttf");
		btnLogout.setCustomTextFont("sans.ttf");
		
		btnLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logoutUser();
			}
		});
		
		btnEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				Intent i = new Intent(getApplicationContext(), Activity_EditProfile.class);
				startActivityForResult(i, 100);
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
						user_info.setVisibility(View.VISIBLE);
						order_info.setVisibility(View.VISIBLE);
						
						JSONObject user = jObj.getJSONObject(TAGs.USER);
						User_Name.setText(user.getString(TAGs.NAME));
						User_Address.setText(user.getString(TAGs.ADDRESS));
						User_Phone.setText(user.getString(TAGs.PHONE));
						if (!user.getString(TAGs.IMAGE).equals(TAGs.NULL)) {
							progressBar.setVisibility(View.VISIBLE);
							Picasso
									.with(getApplicationContext())
									.load(getResources().getString(R.string.url_image, HOST) + user.getString(TAGs.IMAGE))
									.networkPolicy(NetworkPolicy.NO_CACHE)
									.memoryPolicy(MemoryPolicy.NO_CACHE)
									.into(User_Photo, new com.squareup.picasso.Callback() {
										@Override
										public void onSuccess() {
											progressBar.setVisibility(View.GONE);
										}
										
										@Override
										public void onError() {
											progressBar.setVisibility(View.GONE);
										}
									});
						}
						
						JSONObject orders = jObj.getJSONObject("orders");
						tCount.setText(orders.getString(TAGs.COUNT));
						tPrice.setText(PriceHelper.formatPrice(orders.getString(TAGs.PRICE)));
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
						finish();
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
				finish();
			}
		};
		
		GetUser(db_user.getUserDetails().get(TAGs.UID));
	}
	
	private void logoutUser() {
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
	
	private void GetUser(final String uid) {
		showDialog();
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "users/" + uid;
			JSONObject params = new JSONObject();
			params.put(TAGs.UNIQUE_ID, uid);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, listener, errorListener) {
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100)
			if (resultCode == 1)
				GetUser(db_user.getUserDetails().get(TAGs.UID));
	}
}