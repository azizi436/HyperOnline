
/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import helper.FontHelper;
import helper.Helper;
import ir.hatamiarash.adapters.ProductAdapter_All;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import models.Product;

public class Activity_Search extends AppCompatActivity implements CardBadge {
	SearchView searchView;
	SweetAlertDialog progressDialog;
	List<Product> productList;
	ProductAdapter_All productAdapter;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.list)
	public RecyclerView list;
	
	private static String HOST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		ButterKnife.bind(this);
		
		final Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
		setSupportActionBar(toolbar);
		
		HOST = getResources().getString(R.string.url_host);
		
		productList = new ArrayList<>();
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		list.setItemAnimator(new DefaultItemAnimator());
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				hideDialog();
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						JSONArray products = jObj.getJSONArray("result");
						
						for (int i = 0; i < products.length(); i++) {
							JSONObject product = products.getJSONObject(i);
							
							productList.add(new Product(
											product.getString(TAGs.UNIQUE_ID),
											product.getString(TAGs.NAME),
											product.getString(TAGs.IMAGE),
											product.getString(TAGs.PRICE),
											product.getInt("off"),
											product.getInt("count"),
											product.getDouble("point"),
											product.getInt("point_count"),
											product.getString(TAGs.DESCRIPTION)
									)
							);
						}
						
						productAdapter.notifyDataSetChanged();
					} else {
						String errorMsg = jObj.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(Activity_Search.this, errorMsg, TAGs.ERROR);
					}
				} catch (JSONException e) {
					hideDialog();
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
	
	private void loadProduct(String word) {
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "search";
			JSONObject params = new JSONObject();
			params.put("word", word);
			final String mRequestBody = params.toString();
			showDialog();
			
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
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			requestQueue.add(stringRequest);
		} catch (Exception e) {
			hideDialog();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchManager searchManager = (SearchManager) Activity_Search.this.getSystemService(Context.SEARCH_SERVICE);
		if (searchItem != null)
			searchView = (SearchView) searchItem.getActionView();
		if (searchView != null) {
			assert searchManager != null;
			searchView.setSearchableInfo(searchManager.getSearchableInfo(Activity_Search.this.getComponentName()));
			searchView.setIconified(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	// Every time when you press search button on keypad an Activity is recreated which in turn calls this function
	@Override
	protected void onNewIntent(Intent intent) {
		// Get search query and create object of class AsyncFetch
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			if (searchView != null)
				searchView.clearFocus();
			productList.clear();
			list.removeAllViewsInLayout();
			productAdapter = new ProductAdapter_All(this, productList);
			productAdapter.setHasStableIds(true);
			list.setAdapter(productAdapter);
			loadProduct(query);
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
}