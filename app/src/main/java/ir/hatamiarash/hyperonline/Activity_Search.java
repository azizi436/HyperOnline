/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import helper.FontHelper;
import helper.Helper;
import ir.hatamiarash.adapters.ProductAdapter_All;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.DataFish;
import models.Product;

public class Activity_Search extends AppCompatActivity implements CardBadge {
    SearchView searchView = null;
    private List<Product> productList;
    private ProductAdapter_All productAdapter;
    ProgressDialog pdLoading;
    
    @InjectView(R.id.list)
    public RecyclerView list;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        ButterKnife.inject(this);
        
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        setSupportActionBar(toolbar);
        
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter_All(this, productList);
        productAdapter.setHasStableIds(true);
        pdLoading = new ProgressDialog(Activity_Search.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(linearLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(productAdapter);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) Activity_Search.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null)
            searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
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
            loadProduct(query);
        }
    }
    
    private void loadProduct(String word) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "search";
            JSONObject params = new JSONObject();
            params.put("word", word);
            final String mRequestBody = params.toString();
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pdLoading.dismiss();
                    Log.i("LOG_VOLLEY R", response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            JSONArray products = jObj.getJSONArray("result");
                            
                            for (int i = 0; i < products.length(); i++) {
                                JSONObject product = products.getJSONObject(i);
                                
                                productList.add(new Product(
                                                product.getString("unique_id"),
                                                product.getString("name"),
                                                product.getString("image"),
                                                product.getString("price"),
                                                product.getInt("off"),
                                                product.getInt("count"),
                                                product.getDouble("point"),
                                                product.getInt("point_count"),
                                                product.getString("description")
                                        )
                                );
                            }
                            
                            productAdapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_Search.this, errorMsg, TAGs.ERROR);
                        }
                    } catch (JSONException e) {
                        pdLoading.dismiss();
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pdLoading.dismiss();
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
            pdLoading.dismiss();
            e.printStackTrace();
        }
    }
    
    @Override
    public void updateBadge() {
    }
}