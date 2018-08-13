/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.adapters.RequestAdapter;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_RequestList extends AppCompatActivity {
	private static final String CLASS = Activity_RequestList.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.list)
	RecyclerView list;
	@BindView(R.id.add)
	FancyButton add;
	@BindView(R.id.confirm)
	FancyButton confirm;
	
	List<String> requests;
	RequestAdapter requestAdapter;
	List<ir.hatamiarash.hyperonline.models.Request> requestList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_request_list);
		ButterKnife.bind(this);
		
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		add.setCustomTextFont("sans.ttf");
		confirm.setCustomTextFont("sans.ttf");
		
		requests = new ArrayList<>();
		requestList = new ArrayList<>();
		requests.add("1");
		requests.add("2");
		requests.add("3");
		requestAdapter = new RequestAdapter(requests);
		requestAdapter.setHasStableIds(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		list.setItemAnimator(new DefaultItemAnimator());
		list.setAdapter(requestAdapter);
		
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
						new MaterialStyledDialog.Builder(Activity_RequestList.this)
								.setTitle(FontHelper.getSpannedString(getApplicationContext(), "سفارش خارج از برنامه"))
								.setDescription(FontHelper.getSpannedString(getApplicationContext(), "با تشکر از انتخاب شما... سفارش ثبت شد !!"))
								.setStyle(Style.HEADER_WITH_TITLE)
								.setHeaderColor(R.color.green)
								.withDarkerOverlay(true)
								.withDialogAnimation(true)
								.setCancelable(false)
								.setPositiveText("باشه")
								.onPositive(new MaterialDialog.SingleButtonCallback() {
									@Override
									public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
										Intent intent = new Intent(getApplicationContext(), Activity_Main.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}
								})
								.show();
					} else {
						String error_msg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), error_msg, TAGs.ERROR);
					}
				} catch (Exception e) {
					analytics.reportException(e);
					Helper.MakeToast(getApplicationContext(), "مشکلی پیش آمده است", TAGs.ERROR);
				}
			}
		};
		
		errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				hideDialog();
				analytics.reportException(error);
				Helper.MakeToast(getApplicationContext(), "مشکلی پیش آمده است", TAGs.ERROR);
			}
		};
		
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addItem(String.valueOf(requests.size() + 1));
			}
		});
		
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < requests.size(); i++) {
					View requestItem = list.getLayoutManager().findViewByPosition(i);
					EditText title = requestItem.findViewById(R.id.title);
					EditText count = requestItem.findViewById(R.id.count);
					EditText description = requestItem.findViewById(R.id.description);
					
					String itemTitle, itemDescription, itemCount;
					itemTitle = title.getText().toString();
					itemDescription = description.getText().toString();
					itemCount = count.getText().toString();
					
					requestList.add(new ir.hatamiarash.hyperonline.models.Request(itemTitle, itemCount, itemDescription));
				}
				sendRequest(requestList);
			}
		});
		
		analyticsReport();
	}
	
	private void sendRequest(List<ir.hatamiarash.hyperonline.models.Request> list) {
		showDialog();
		try {
			StringBuilder finalList = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				ir.hatamiarash.hyperonline.models.Request request = list.get(i);
				finalList.append(request.getTitle()).append(",").append(request.getCount()).append(",").append(request.getDescription()).append(":");
			}
			JSONObject params = new JSONObject();
			params.put(TAGs.BODY, finalList.substring(0, finalList.length() - 1));
			params.put(TAGs.UID, getIntent().getStringExtra(TAGs.UID));
			final String body = params.toString();
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
						analytics.reportException(e);
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
			analytics.reportException(e);
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
	
	private void addItem(String item) {
		requests.add(item);
		requestAdapter.notifyDataSetChanged();
	}
	
	private void analyticsReport() {
		analytics.reportScreen(CLASS);
		analytics.reportEvent("Open " + CLASS);
	}
}

