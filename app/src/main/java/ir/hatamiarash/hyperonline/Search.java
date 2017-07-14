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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import helper.FontHelper;
import ir.hatamiarash.adapters.SearchAdapter;
import models.DataFish;

public class Search extends AppCompatActivity {
    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    RecyclerView mRVFish;
    SearchAdapter mAdapter;
    SearchView searchView = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), getResources().getString(R.string.app_name_fa)));
        setSupportActionBar(toolbar);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // adds item to action bar
        getMenuInflater().inflate(R.menu.search, menu);
        // Get Search item from action bar and Get Search service
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) Search.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null)
            searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(Search.this.getComponentName()));
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
            new AsyncFetch(query).execute();
        }
    }
    
    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {
        
        ProgressDialog pdLoading = new ProgressDialog(Search.this);
        HttpURLConnection conn;
        URL url = null;
        String searchQuery;
        
        AsyncFetch(String searchQuery) {
            this.searchQuery = searchQuery;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
            
        }
        
        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL("http://arash-hatami.ir/test/search.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                
            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.toString();
            }
            
            try {
                
                int response_code = conn.getResponseCode();
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        result.append(line);
                    
                    return (result.toString());
                    
                } else
                    return ("Connection error");
            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            //this method will be running on UI thread
            pdLoading.dismiss();
            List<DataFish> data = new ArrayList<>();
            pdLoading.dismiss();
            if (result.equals("no rows")) {
                Toast.makeText(Search.this, "No Results found for entered query", Toast.LENGTH_LONG).show();
            } else {
                try {
                    JSONArray jArray = new JSONArray(result);
                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        DataFish fishData = new DataFish();
                        fishData.fishName = json_data.getString("fish_name");
                        fishData.catName = json_data.getString("cat_name");
                        fishData.sizeName = json_data.getString("size_name");
                        fishData.price = json_data.getInt("price");
                        data.add(fishData);
                    }
                    // Setup and Handover data to recyclerview
                    mRVFish = (RecyclerView) findViewById(R.id.fishPriceList);
                    mAdapter = new SearchAdapter(Search.this, data);
                    mRVFish.setAdapter(mAdapter);
                    mRVFish.setLayoutManager(new LinearLayoutManager(Search.this));
                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(Search.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(Search.this, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}