/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.Helper;
import helper.SQLiteHandler;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import volley.AppController;

public class ResetPassword extends Activity {
    private static final String TAG = ResetPassword.class.getSimpleName();
    
    @InjectView(R.id.email)
    public EditText inputEmail;
    @InjectView(R.id.phone)
    public EditText inputPhone;
    @InjectView(R.id.btnSet)
    public Button btnSet;
    
    private SweetAlertDialog progressDialog;
    private boolean CheckEmail, CheckPhone;
    private SQLiteHandler db_user;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
        
        ButterKnife.inject(this);
        db_user = new SQLiteHandler(getApplicationContext());
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        CheckEmail = Helper.isValidEmail(inputEmail.getText().toString());
        CheckPhone = Helper.isValidPhone(inputPhone.getText().toString());
        
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!db_user.getUserDetails().get(TAGs.EMAIL).equals("NULL") ||
                        !db_user.getUserDetails().get(TAGs.EMAIL).equals("null"))
                    if (Helper.CheckInternet(getApplicationContext()))
                        if (CheckEmail)
                            if (CheckPhone)
                                ResetUserPassword(inputEmail.getText().toString(), inputPhone.getText().toString());
                            else
                                Helper.MakeToast(getApplicationContext(), "شماره موبایل را بررسی نمایید", TAGs.WARNING);
                        else
                            Helper.MakeToast(getApplicationContext(), "ایمیل را بررسی نمایید", TAGs.WARNING);
                    else
                        Helper.MakeToast(getApplicationContext(), "اتصال اینترنت را بررسی نمایید", TAGs.ERROR);
                else {
                    Helper.MakeToast(getApplicationContext(), "شما ایمیلی را در سیستم ثبت نکرده اید", TAGs.ERROR);
                    Helper.MakeToast(getApplicationContext(), "مشخصات خود را ویرایش کنید", TAGs.WARNING);
                }
            }
        });
    }
    
    private void ResetUserPassword(final String email, final String phone) {
        // Tag used to cancel the request
        String string_req = "req_reset";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Reset Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        MakeQuestion("تعویض کلمه عبور", "کلمه عبور جدید به ایمیل شما ارسال شد");
                    } else {
                        // Error occurred in registration. Get the error message
                        String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                        Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Fetch Error: " + error.getMessage());
                if (error.getMessage() != null) {
                    Helper.MakeToast(getApplicationContext(), error.getMessage(), TAGs.ERROR);
                } else
                    Helper.MakeToast(getApplicationContext(), "خطایی رخ داده است - اتصال به اینترنت را بررسی نمایید", TAGs.ERROR);
                hideDialog();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                // Posting params to register url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_reset_password");
                params.put(TAGs.EMAIL, email);
                params.put(TAGs.PHONE, phone);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, string_req);
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