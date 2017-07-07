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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import volley.AppController;

public class Login extends Activity {
    private static final String TAG = Login.class.getSimpleName(); // class's tag for log
    
    @InjectView(R.id.btnLogin)
    Button btnLogin;                          // login button
    @InjectView(R.id.btnLinkToRegisterScreen)
    Button btnLinkToRegister;                 // register activity button
    @InjectView(R.id.btnLinkToResetPassword)
    Button btnLinkToResetPassword;
    @InjectView(R.id.new_password_2)
    IconEditText inputPhone;                      // email input
    @InjectView(R.id.password)
    IconEditText inputPassword;                   // password input
    
    private SweetAlertDialog progressDialog;       // dialog window
    private SessionManager session;           // session for check user logged status
    private SQLiteHandler db;                 // users database
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.inject(this);
        
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        db = new SQLiteHandler(getApplicationContext());                         // users database
        db.CreateTable();                                                        // create users table
        session = new SessionManager(getApplicationContext());
        
        if (session.isLoggedIn()) {                                              // Check if user is already logged in or not
            Intent i = new Intent(Login.this, MainScreenActivity.class);
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
                    Log.i("LOG_VOLLEY R", response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {                          // Check for error node in json
                            session.setLogin(true);            // set login status true
                            JSONObject user = jObj.getJSONObject(TAGs.USER);
            
                            db.addUser(
                                    user.getString(TAGs.NAME),
                                    user.getString(TAGs.EMAIL),
                                    user.getString(TAGs.ADDRESS),
                                    user.getString(TAGs.PHONE),
                                    user.getString(TAGs.UNIQUE_ID),
                                    "Iran",
                                    user.getString(TAGs.STATE),
                                    user.getString(TAGs.CITY)
                            );
                            String msg = "سلام " + user.getString(TAGs.NAME);
                            Lobby.pointer.finish();
                            Helper.MakeToast(Login.this, msg, TAGs.SUCCESS);
                            Intent i = new Intent(Login.this, MainScreenActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // Error in login. Get the error message
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
                    Log.e("LOG_VOLLEY E", error.toString());
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
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void showDialog() { // show dialog
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    
    private void hideDialog() { // close dialog
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
    
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}