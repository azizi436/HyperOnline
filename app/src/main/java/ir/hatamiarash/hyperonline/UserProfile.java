/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import helper.ConfirmManager;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerSupport;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class UserProfile extends Activity {
    private static final String TAG = UserProfile.class.getSimpleName();
    private SQLiteHandler db_user;
    private SQLiteHandlerItem db_item;
    private SQLiteHandlerSupport db_support;
    private SessionManager session;
    private ConfirmManager confirmManager;
    private SweetAlertDialog progressDialog;
    private Vibrator vibrator;
    
    @InjectView(R.id.btnLogout)
    public Button btnLogout;
    @InjectView(R.id.btnEdit)
    public Button btnEdit;
    @InjectView(R.id.profile_name)
    public TextView User_Name;
    @InjectView(R.id.profile_address)
    public TextView User_Address;
    @InjectView(R.id.profile_phone)
    public TextView User_Phone;
    @InjectView(R.id.image)
    public ImageView User_Photo;
    @InjectView(R.id.progress_bar)
    public ProgressBar progressBar;
    @InjectView(R.id.blurView)
    public BlurView blurView;
    @InjectView(R.id.main)
    public RelativeLayout main;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        ButterKnife.inject(this);
        
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
        
        if (!session.isLoggedIn()) logoutUser();
        
        final View decorView = getWindow().getDecorView();
        //Activity's root View. Can also be root View of your layout (preferably)
        final ViewGroup rootView = decorView.findViewById(android.R.id.content);
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
                vibrator.vibrate(50);
                Intent i = new Intent(getApplicationContext(), EditProfile.class);
                startActivityForResult(i, 100);
            }
        });
        
        GetUser(db_user.getUserDetails().get(TAGs.UID));
    }
    
    private void logoutUser() {
        vibrator.vibrate(50);
        progressDialog.setTitleText("لطفا منتظر بمانید");
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100)
            if (resultCode == 1)
                GetUser(db_user.getUserDetails().get(TAGs.UID));
    }
}