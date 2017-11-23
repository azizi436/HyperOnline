/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mikepenz.materialdrawer.Drawer;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import helper.FontHelper;
import helper.Helper;
import helper.SQLiteHandler;
import ir.hatamiarash.adapters.OrderAdapter;
import ir.hatamiarash.interfaces.CardBadge;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import models.Order;

public class Activity_UserOrders extends AppCompatActivity implements CardBadge {
    private Vibrator vibrator;
    static Typeface persianTypeface;
    public Drawer result = null;
    SweetAlertDialog progressDialog;
    public static SQLiteHandler db_user;
    
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    
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
        
        toolbar.setTitle(FontHelper.getSpannedString(getApplicationContext(), "لیست سفارشات"));
        setSupportActionBar(toolbar);
        
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        orderAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(linearLayoutManager);
        list.setItemAnimator(new DefaultItemAnimator());
        list.setAdapter(orderAdapter);
        
        loadOrders();
    }
    
    private void loadOrders() {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "user_orders";
            JSONObject params = new JSONObject();
            params.put("unique_id", db_user.getUserDetails().get(TAGs.UID));
            final String mRequestBody = params.toString();
            progressDialog.setTitleText("لطفا منتظر بمانید");
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
                            JSONArray orders = jObj.getJSONArray("orders");
                            
                            for (int i = 0; i < orders.length(); i++) {
                                JSONObject order = orders.getJSONObject(i);
                                
                                orderList.add(new Order(
                                                order.getString("unique_id"),
                                                order.getString("code"),
                                                order.getString("seller_name"),
                                                order.getString("stuffs"),
                                                order.getString("price"),
                                                order.getInt("hour"),
                                                order.getString("method"),
                                                order.getString("status"),
                                                order.getString("description"),
                                                order.getString("create_date")
                                        )
                                );
                            }
                            
                            orderAdapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Activity_UserOrders.this, errorMsg, TAGs.ERROR);
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