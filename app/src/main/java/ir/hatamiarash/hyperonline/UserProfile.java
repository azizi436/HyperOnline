/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class UserProfile extends Activity {
    private static final String TAG = UserProfile.class.getSimpleName();
    Button btnLogout;
    Button btnEdit;
    Button btnCharge;
    private TextView User_Name;
    private TextView User_Address;
    private TextView User_Phone;
    private TextView User_Wallet;
    private TextView User_Shop;
    private ImageView User_Photo;
    private ProgressBar progressBar;
    private SQLiteHandler db_user;
    private SQLiteHandlerItem db_item;
    private SessionManager session;
    private SweetAlertDialog progressDialog;
    BlurView blurView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        
        User_Name = (TextView) findViewById(R.id.profile_name);
        User_Address = (TextView) findViewById(R.id.profile_address);
        User_Phone = (TextView) findViewById(R.id.profile_phone);
        User_Wallet = (TextView) findViewById(R.id.Wallet);
        User_Shop = (TextView) findViewById(R.id.Shop);
        User_Photo = (ImageView) findViewById(R.id.image);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnCharge = (Button) findViewById(R.id.btnCharge);
        blurView = (BlurView) findViewById(R.id.blurView);
        RelativeLayout main = (RelativeLayout) findViewById(R.id.main);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        
        progressBar.setVisibility(View.GONE);
        db_user = new SQLiteHandler(getApplicationContext());
        db_item = new SQLiteHandlerItem(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        
        if (!session.isLoggedIn()) logoutUser();
        
        final View decorView = getWindow().getDecorView();
        //Activity's root View. Can also be root View of your layout (preferably)
        final ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //set background, if your root layout doesn't have one
        final Drawable windowBackground = decorView.getBackground();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            blurView.setupWith(rootView)
                    .windowBackground(windowBackground)
                    .blurAlgorithm(new RenderScriptBlur(this))
                    .blurRadius(15);
        } else
            main.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background));
        
        User_Name.setText("");
        User_Address.setText("");
        User_Phone.setText("");
        blurView.setVisibility(View.INVISIBLE);
        
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EditProfile.class);
                startActivity(i);
                finish();
            }
        });
        
        btnCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.MakeToast(getApplicationContext(), "انتقال به صفحه شارژ", TAGs.WARNING);
            }
        });
        
        GetUser(db_user.getUserDetails().get(TAGs.UID));
    }
    
    private void logoutUser() {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        session.setLogin(false);
        db_user.deleteUsers();
        db_item.deleteItems();
        hideDialog();
        Intent i = new Intent(UserProfile.this, Lobby.class);
        Activity_Main.pointer.finish();
        startActivity(i);
        finish();
    }
    
    private void GetUser(final String uid) {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "users/" + uid;
            JSONObject params = new JSONObject();
            params.put(TAGs.UNIQUE_ID, uid);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY R", response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            blurView.setVisibility(View.VISIBLE);
                            JSONObject user = jObj.getJSONObject(TAGs.USER);
                            User_Name.setText(user.getString(TAGs.NAME));
                            User_Address.setText(user.getString(TAGs.ADDRESS));
                            User_Phone.setText(user.getString(TAGs.PHONE));
                            User_Wallet.setText(user.getString(TAGs.WALLET) + " تومان");
                            if (!user.getString(TAGs.IMAGE).equals(TAGs.NULL)) {
                                progressBar.setVisibility(View.VISIBLE);
                                Picasso
                                        .with(getApplicationContext())
                                        .load(URLs.image_URL + user.getString(TAGs.IMAGE))
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
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
                            finish();
                        }
                    } catch (JSONException e) {
                        hideDialog();
                        e.printStackTrace();
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_VOLLEY E", error.toString());
                    hideDialog();
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
        } catch (JSONException e) {
            e.printStackTrace();
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
}