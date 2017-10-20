/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.Helper;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class ResetPassword extends Activity {
    private static final String TAG = ResetPassword.class.getSimpleName();
    
    @InjectView(R.id.phone)
    public EditText inputPhone;
    @InjectView(R.id.btnSet)
    public Button btnSet;
    
    private SweetAlertDialog progressDialog;
    private Vibrator vibrator;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
        
        ButterKnife.inject(this);
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
                if (Helper.CheckInternet(getApplicationContext()))
                    if (Helper.isValidPhone(inputPhone.getText().toString()))
                        resetPassword(inputPhone.getText().toString());
                    else
                        Helper.MakeToast(getApplicationContext(), "شماره موبایل را بررسی نمایید", TAGs.WARNING);
            }
        });
    }
    
    private void resetPassword(final String phone) {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "resetPass";
            JSONObject params = new JSONObject();
            params.put(TAGs.PHONE, phone);
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("resetPassword R", response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            MakeQuestion("تعویض کلمه عبور", "کلمه عبور جدید به شماره شما ارسال شد");
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
                    Log.e("resetPassword E", error.toString());
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
            // add retry policy to prevent send request twice
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
    
    private void MakeQuestion(String Title, String Message) {                     // build and show an confirm window
        AlertDialog.Builder dialog = new AlertDialog.Builder(ResetPassword.this);
        dialog.setTitle(Title);                                                   // set title
        dialog.setMessage(Message);                                               // set message
        dialog.setIcon(R.drawable.ic_success);                                    // set icon
        dialog.setPositiveButton("تایید", new DialogInterface.OnClickListener() { // negative answer
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss(); // close dialog
                Intent i = new Intent(getApplicationContext(), Login.class);
                startActivity(i);
                finish();
            }
        });
        AlertDialog alert = dialog.create(); // create dialog
        alert.show();                        // show dialog
    }
}