/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.adapters.CategoryAdapter_All;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.interfaces.CardBadge;
import ir.hatamiarash.hyperonline.libraries.EndlessScrollListener;
import ir.hatamiarash.hyperonline.models.Category;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Cat extends AppCompatActivity implements CardBadge {
	private static final String CLASS = Activity_Cat.class.getSimpleName();
	
	SweetAlertDialog progressDialog;
	List<Category> categoryList;
	CategoryAdapter_All categoryAdapter;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.list)
	RecyclerView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_simple);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		setSupportActionBar(toolbar);
		try {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_action_bar_title, null);
			ActionBar.LayoutParams p = new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.END);
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "دسته بندی ها"));
			getSupportActionBar().setCustomView(v, p);
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
		}
		
		categoryList = new ArrayList<>();
		categoryAdapter = new CategoryAdapter_All(this, categoryList);
		categoryAdapter.setHasStableIds(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		EndlessScrollListener scrollListener = new EndlessScrollListener(linearLayoutManager) {
			@Override
			public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
				showDialog();
				loadCategories(page);
			}
		};
		list.addOnScrollListener(scrollListener);
		list.setItemAnimator(new DefaultItemAnimator());
		list.setAdapter(categoryAdapter);
		
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
						JSONArray categories = jObj.getJSONArray("cat");
						
						for (int i = 0; i < categories.length(); i++) {
							JSONObject category = categories.getJSONObject(i);
							
							categoryList.add(new Category(
											category.getString(TAGs.UNIQUE_ID),
											category.getString(TAGs.NAME),
											category.getString(TAGs.IMAGE),
											category.getDouble("point"),
											category.getInt("point_count"),
											category.getInt("off"),
											1
									)
							);
						}
						
						categoryAdapter.notifyDataSetChanged();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(Activity_Cat.this, errorMsg, TAGs.ERROR);
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
		
		loadCategories(1);
		analyticsReport();
	}
	
	private void loadCategories(int page) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "categories";
			JSONObject params = new JSONObject();
			params.put(TAGs.INDEX, String.valueOf(page));
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
	
	@Override
	public void updateBadge() {
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