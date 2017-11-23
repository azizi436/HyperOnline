/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import helper.ConfirmManager;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class Login extends Activity {
    private static final String TAG = Login.class.getSimpleName(); // class's tag for log
    
    @BindView(R.id.btnLogin)
    Button btnLogin;                          // login button
    @BindView(R.id.btnLinkToRegisterScreen)
    Button btnLinkToRegister;                 // register activity button
    @BindView(R.id.btnLinkToResetPassword)
    Button btnLinkToResetPassword;
    @BindView(R.id.new_password_2)
    IconEditText inputPhone;                      // email input
    @BindView(R.id.password)
    IconEditText inputPassword;                   // password input
    
    private SweetAlertDialog progressDialog;       // dialog window
    private SessionManager session;           // session for check user logged status
    private SQLiteHandler db;                 // users database
    private ConfirmManager confirmManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        db = new SQLiteHandler(getApplicationContext());                         // users database
        db.CreateTable();                                                        // create users table
        session = new SessionManager(getApplicationContext());
        confirmManager = new ConfirmManager(getApplicationContext());
        
        if (session.isLoggedIn()) {                                              // Check if user is already logged in or not
            Intent i = new Intent(Login.this, Activity_Main.class);
            startActivity(i);                                                    // start main activity
            finish();                                                            // close this activity
        }
        
        btnLogin.setOnClickListener(new View.OnClickListener() {                      // login button's event
            public void onClick(View view) {
                String phone = inputPhone.getText().toString();                       // get email from text input
                String password = inputPassword.getText().toString();                 // get password from text input
                if (Helper.CheckInternet(Login.this))                                 // check network connection status
                    if (Helper.isValidPhone(phone) && password.trim().length() > 0) // check empty fields
                        CheckLogin(phone, password);                                  // check user login request from server
                    else
                        Helper.MakeToast(Login.this, "مشخصات را بررسی نمایید", TAGs.WARNING);
            }
        });
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {             // register button's event
            public void onClick(View view) {
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
                finish();
            }
        });
        btnLinkToResetPassword.setOnClickListener(new View.OnClickListener() {        // register button's event
            public void onClick(View view) {
                Intent i = new Intent(Login.this, ResetPassword.class);
                startActivity(i);
                finish();
            }
        });
    }
    
    private void CheckLogin(final String phone, final String password) {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "login";
            JSONObject params = new JSONObject();
            params.put(TAGs.PHONE, phone);
            params.put(TAGs.PASSWORD, password);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("CheckLogin R", response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {                          // Check for error node in json
                            session.setLogin(true);            // set login status true
                            final JSONObject user = jObj.getJSONObject(TAGs.USER);
                            
                            db.addUser(
                                    user.getString(TAGs.NAME),
                                    "test@gmail.com",
                                    user.getString(TAGs.ADDRESS),
                                    user.getString(TAGs.PHONE),
                                    user.getString(TAGs.UNIQUE_ID),
                                    "Iran",
                                    user.getString(TAGs.STATE),
                                    user.getString(TAGs.CITY)
                            );
//                            if (Integer.valueOf(user.getString("confirmed_phone")) == 0) {
//                                confirmManager.setPhoneConfirm(true);
//                                confirmManager.setPhoneConfirm(false);
//                                new MaterialStyledDialog.Builder(Login.this)
//                                        .setTitle(FontHelper.getSpannedString(Login.this, "تایید حساب"))
//                                        .setDescription(FontHelper.getSpannedString(Login.this, "لطفا شماره تلفن خود را تایید کنید"))
//                                        .setStyle(Style.HEADER_WITH_TITLE)
//                                        .withDarkerOverlay(true)
//                                        .withDialogAnimation(true)
//                                        .setCancelable(true)
//                                        .setPositiveText("باشه")
//                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                            @Override
//                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                                try {
//                                                    Intent intent = new Intent(Login.this, Confirm_Phone.class);
//                                                    intent.putExtra(TAGs.PHONE, user.getString(TAGs.PHONE));
//                                                    startActivity(intent);
//                                                    finish();
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        })
//                                        .show();
//                            } else {
                            confirmManager.setPhoneConfirm(true);
                            if (Integer.valueOf(user.getString("confirmed_info")) == 0)
                                confirmManager.setInfoConfirm(false);
                            else
                                confirmManager.setInfoConfirm(true);
                            
                            String msg = "سلام " + user.getString(TAGs.NAME);
                            Helper.MakeToast(Login.this, msg, TAGs.SUCCESS);
                            Intent intent = new Intent(Login.this, Activity_Main.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
//                            }
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Login.this, errorMsg, TAGs.ERROR); // show error message
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
                    Log.e("CheckLogin E", error.toString());
                    hideDialog();
                    finish();
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
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
    
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}