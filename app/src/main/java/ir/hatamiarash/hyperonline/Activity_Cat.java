/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mikepenz.materialdrawer.Drawer;
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
import helper.EndlessScrollListener;
import helper.FontHelper;
import helper.Helper;
import helper.SQLiteHandler;
import ir.hatamiarash.adapters.CategoryAdapter_All;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Category;

public class Activity_Cat extends AppCompatActivity implements CardBadge {
	private Vibrator vibrator;
	static Typeface persianTypeface;
	public Drawer result = null;
	SweetAlertDialog progressDialog;
	public static SQLiteHandler db_user;
	
	private List<Category> categoryList;
	private CategoryAdapter_All categoryAdapter;
	
	@BindView(R.id.toolbar)
	public Toolbar toolbar;
	@BindView(R.id.list)
	public RecyclerView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_simple);
		ButterKnife.bind(this);
		
		db_user = new SQLiteHandler(getApplicationContext());
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		persianTypeface = Typeface.createFromAsset(getAssets(), FontHelper.FontPath);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText("لطفا منتظر بمانید");
		
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
			e.printStackTrace();
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
		
		loadCategories(1);
	}
	
	private void loadCategories(int page) {
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = URLs.base_URL + "categories";
			JSONObject params = new JSONObject();
			params.put("index", String.valueOf(page));
			final String mRequestBody = params.toString();
			showDialog();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					hideDialog();
					Log.i("LOG_VOLLEY R", response);
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							JSONArray categories = jObj.getJSONArray("cat");
							
							for (int i = 0; i < categories.length(); i++) {
								JSONObject category = categories.getJSONObject(i);
								
								categoryList.add(new Category(
												category.getString("unique_id"),
												category.getString("name"),
												category.getString("image"),
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
						e.printStackTrace();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					hideDialog();
					Log.e("LOG_VOLLEY E", error.toString());
				}
			}) {
				@NonNull
				@Contract(pure = true)
				@Override
				public String getBodyContentType() {
					return "application/json; charset=utf-8";
				}
				
				@Nullable
				@Override
				public byte[] getBody() throws AuthFailureError {
					try {
						return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException uee) {
						VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
						return null;
					}
				}
			};
			requestQueue.add(stringRequest);
		} catch (Exception e) {
			hideDialog();
			e.printStackTrace();
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