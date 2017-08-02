/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;

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
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import volley.AppController;

public class EditPassword extends AppCompatActivity {
    private static final String TAG = EditPassword.class.getSimpleName();
    
    @InjectView(R.id.current_password)
    IconEditText current_password;
    @InjectView(R.id.new_password)
    IconEditText new_password;
    @InjectView(R.id.new_password_2)
    IconEditText new_password_2;
    @InjectView(R.id.btnConfirm)
    Button btnConfirm;
    
    private SweetAlertDialog progressDialog;
    private SQLiteHandler db_user;
    private SessionManager session;
    private String pass, phone;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_password);

        ButterKnife.inject(this);
        db_user = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));

        phone = db_user.getUserDetails().get(TAGs.PHONE);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cp = current_password.getText().toString();
                String np = new_password.getText().toString();
                String np2 = new_password_2.getText().toString();
                if (cp.equals(pass))
                    if (np.equals(np2))
                        if (Helper.isValidPassword(np))
                            MakeQuestion("تغییر شماره تلفن", "اطلاعات ورود شما نیز تغییر خواهد کرد", phone, np);
                        else
                            Helper.MakeToast(getApplicationContext(), "رمز عبور جدید معتبر نیست", TAGs.ERROR);
                    else
                        Helper.MakeToast(getApplicationContext(), "رمز های عبور مطابقت ندارند", TAGs.ERROR);
                else
                    Helper.MakeToast(getApplicationContext(), "رمز عبور وارد شده اشتباه است", TAGs.ERROR);
            }
        });

        GetUser(phone);
    }

    private void GetUser(final String phone) {
        String string_req = "req_get";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Fetch Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        JSONObject user = jObj.getJSONObject(TAGs.USER);
                        pass = user.getString(TAGs.PASSWORD);
                    } else {
                        String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                        Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    finish();
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
                finish();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_get_password");
                params.put(TAGs.PHONE, phone);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void UpdatePassword(final String pass, final String phone) {
        String string_req = "req_update";
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Updating Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        Helper.MakeToast(getApplicationContext(), "کلمه عبور تغییر کرد", TAGs.SUCCESS);
                    } else {
                        String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                        Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
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
                finish();
            }
        }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_update_password");
                params.put(TAGs.PASSWORD, pass);
                params.put(TAGs.PHONE, phone);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void MakeQuestion(String Title, String Message, final String phone, final String password) {
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctw);
        dialog.setTitle(Title);
        dialog.setMessage(Message);
        dialog.setIcon(R.drawable.ic_alert);
        dialog.setPositiveButton("باشه !", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                progressDialog.setTitleText("لطفا منتظر بمانید");
                showDialog();
                UpdatePassword(password, phone);
                logoutUser();
            }
        });
        dialog.setNegativeButton("بیخیال", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void logoutUser() {
        session.setLogin(false);
        db_user.deleteUsers();
        hideDialog();
        Intent i = new Intent(getApplicationContext(), Lobby.class);
        Activity_Main.pointer.finish();
        startActivity(i);
        finish();
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