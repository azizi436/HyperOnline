/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.Image.PickerBuilder;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;
import volley.AppController;

public class EditProfile extends AppCompatActivity {
    private static final String TAG = EditProfile.class.getSimpleName();

    @InjectView(R.id.name)
    IconEditText txtName;
    @InjectView(R.id.address)
    IconEditText txtAddress;
    @InjectView(R.id.phone)
    IconEditText txtPhone;
    @InjectView(R.id.email)
    IconEditText txtEmail;
    @InjectView(R.id.btnConfirm)
    Button btnConfirm;
    @InjectView(R.id.btnChangePassword)
    Button btnChangePassword;
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.add_photo)
    RelativeLayout add_photo;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;

    private SweetAlertDialog progressDialog;
    private SQLiteHandler db_user;
    private SessionManager session;

    private String Backup_Phone, uid;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        ButterKnife.inject(this);
        progressBar.setVisibility(View.GONE);
        add_photo.setVisibility(View.GONE);
        image.setVisibility(View.INVISIBLE);
        db_user = new SQLiteHandler(getApplicationContext());              // user local database
        session = new SessionManager(getApplicationContext());             // user session
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));

        if (Helper.CheckInternet(getApplicationContext()))
            GetUser(db_user.getUserDetails().get(TAGs.PHONE));             // get person detail from server
        else
            finish();

        btnConfirm.setOnClickListener(new View.OnClickListener() {         // confirm button's event
            @Override
            public void onClick(View v) {
                String name = txtName.getText().toString();
                String address = txtAddress.getText().toString();
                String phone = txtPhone.getText().toString();
                String email = txtEmail.getText().toString();
                if (Helper.CheckInternet(getApplicationContext()))
                    if (!name.isEmpty() && !address.isEmpty() && !phone.isEmpty())
                        if (Helper.isValidEmail(email))
                            if (Helper.isValidPhone(phone))
                                if (!phone.equals(Backup_Phone)) {
                                    if (filePath == null)
                                        MakeQuestion("تغییر شماره تلفن", "اطلاعات ورود شما نیز تغییر خواهد کرد", name, address, phone, email);
                                    else
                                        MakeQuestionPicture("تغییر شماره تلفن", "اطلاعات ورود شما نیز تغییر خواهد کرد", name, address, phone, email, filePath);
                                } else {
                                    if (filePath == null)
                                        UpdateUser(name, address, phone, email);
                                    else
                                        UpdateUserWithPicture(name, address, phone, email, filePath);
                                }
                            else
                                Helper.MakeToast(getApplicationContext(), "شماره موبایل را بررسی نمایید", TAGs.WARNING);
                        else
                            Helper.MakeToast(getApplicationContext(), "ایمیل را بررسی نمایید", TAGs.ERROR);
                    else
                        Helper.MakeToast(getApplicationContext(), "تمامی کادر ها را پر نمایید", TAGs.WARNING);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {         // confirm button's event
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EditPassword.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void GetUser(final String phone) {             // check login request from server
        String string_req = "req_get";                 // Tag used to cancel the request
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Fetch Response: " + response); // log server response
                hideDialog();                              // close dialog
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {                          // Check for error node in json
                        JSONObject user = jObj.getJSONObject(TAGs.USER);
                        txtName.setText(getApplicationContext(), user.getString(TAGs.NAME));
                        txtAddress.setText(getApplicationContext(), user.getString(TAGs.ADDRESS));
                        txtEmail.setText(getApplicationContext(), user.getString(TAGs.EMAIL));
                        Backup_Phone = user.getString(TAGs.PHONE);
                        txtPhone.setText(getApplicationContext(), Backup_Phone);
                        uid = user.getString(TAGs.UID);
                        if (!user.getString(TAGs.IMAGE).equals(TAGs.NULL)) {
                            progressBar.setVisibility(View.VISIBLE);
                            Picasso
                                    .with(getApplicationContext())
                                    .load(URLs.image_URL + user.getString(TAGs.IMAGE))
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .into(image, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            progressBar.setVisibility(View.GONE);
                                            image.setVisibility(View.VISIBLE);
                                            add_photo.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onError() {
                                            progressBar.setVisibility(View.GONE);
                                            image.setVisibility(View.VISIBLE);
                                            add_photo.setVisibility(View.VISIBLE);
                                        }
                                    });
                        } else {
                            add_photo.setVisibility(View.VISIBLE);
                            image.setVisibility(View.VISIBLE);
                        }
                    } else {
                        String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                        Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR); // show error message
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
            protected java.util.Map<String, String> getParams() { // Posting parameters to login url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_get");
                params.put(TAGs.PHONE, phone);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void UpdateUser(final String name, final String address, final String phone, final String email) {
        // Tag used to cancel the request
        String string_req = "req_update";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        // Inserting row in users table
                        db_user.updateUser(uid, name, email, address, phone);
                        Helper.MakeToast(getApplicationContext(), "اطلاعات شما به روزرسانی شد", TAGs.SUCCESS);
                        Intent i = new Intent(getApplicationContext(), UserProfile.class);
                        startActivity(i);
                        finish();
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
                Log.e(TAG, "Update Error: " + error.getMessage());
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
                // Posting params to register url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_update_details");
                params.put(TAGs.NAME, name);
                params.put(TAGs.EMAIL, email);
                params.put(TAGs.ADDRESS, address);
                params.put(TAGs.PHONE, phone);
                params.put(TAGs.UID, uid);
                params.put(TAGs.IMAGE, TAGs.NULL);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void UpdateUserWithPicture(final String name, final String address, final String phone, final String email, final Uri filePath) {
        // Tag used to cancel the request
        String string_req = "req_update";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        Upload(filePath);
                        // Inserting row in users table
                        db_user.updateUser(uid, name, email, address, phone);
                        Helper.MakeToast(getApplicationContext(), "اطلاعات شما به روزرسانی شد", TAGs.SUCCESS);
                        Intent i = new Intent(getApplicationContext(), UserProfile.class);
                        startActivity(i);
                        finish();
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
                Log.e(TAG, "Update Error: " + error.getMessage());
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
                // Posting params to register url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_update_details");
                params.put(TAGs.NAME, name);
                params.put(TAGs.EMAIL, email);
                params.put(TAGs.ADDRESS, address);
                params.put(TAGs.PHONE, phone);
                params.put(TAGs.UID, uid);
                params.put(TAGs.IMAGE, "user_" + db_user.getUserDetails().get(TAGs.UID) + ".jpg");
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void UpdateUserWithPhone(final String name, final String address, final String phone, final String Backup_Phone, final String email) {
        // Tag used to cancel the request
        String string_req = "req_update";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        // Inserting row in users table
                        db_user.updateUser(uid, name, email, address, phone);
                        Helper.MakeToast(getApplicationContext(), "اطلاعات شما به روزرسانی شد", TAGs.SUCCESS);
                        logoutUser();
                    } else {
                        // Error occurred in registration. Get the error message
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
                Log.e(TAG, "Update Error: " + error.getMessage());
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
                // Posting params to register url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_update_details_p");
                params.put(TAGs.NAME, name);
                params.put(TAGs.EMAIL, email);
                params.put(TAGs.ADDRESS, address);
                params.put(TAGs.PHONE, phone);
                params.put("backup", Backup_Phone);
                params.put(TAGs.UID, uid);
                params.put(TAGs.IMAGE, TAGs.NULL);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, string_req);
    }

    private void UpdateUserWithPhonePicture(final String name, final String address, final String phone, final String Backup_Phone, final String email, final Uri filePath) {
        // Tag used to cancel the request
        String string_req = "req_update";
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST, URLs.base_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean(TAGs.ERROR);
                    if (!error) {
                        // Inserting row in users table
                        db_user.updateUser(uid, name, email, address, phone);
                        Helper.MakeToast(getApplicationContext(), "اطلاعات شما به روزرسانی شد", TAGs.SUCCESS);
                        Upload(filePath);
                        logoutUser();
                    } else {
                        // Error occurred in registration. Get the error message
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
                Log.e(TAG, "Update Error: " + error.getMessage());
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
                // Posting params to register url
                java.util.Map<String, String> params = new HashMap<>();
                params.put(TAGs.TAG, "user_update_details_p");
                params.put(TAGs.NAME, name);
                params.put(TAGs.EMAIL, email);
                params.put(TAGs.ADDRESS, address);
                params.put(TAGs.PHONE, phone);
                params.put("backup", Backup_Phone);
                params.put(TAGs.UID, uid);
                params.put(TAGs.IMAGE, "user_" + db_user.getUserDetails().get(TAGs.UID) + ".jpg");
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

    private void MakeQuestion(String Title, String Message, final String name, final String address, final String phone, final String email) { // build and show an confirm window
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctw);
        dialog.setTitle(Title);
        dialog.setMessage(Message);
        dialog.setIcon(R.drawable.ic_alert);
        dialog.setPositiveButton("باشه !", new DialogInterface.OnClickListener() {  // positive answer
            public void onClick(DialogInterface dialog, int id) {
                UpdateUserWithPhone(name, address, phone, Backup_Phone, email);
                logoutUser();
            }
        });
        dialog.setNegativeButton("بیخیال", new DialogInterface.OnClickListener() { // negative answer
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();            // close dialog
            }
        });
        AlertDialog alert = dialog.create(); // create dialog
        alert.show();                        // show dialog
    }

    private void MakeQuestionPicture(String Title, String Message, final String name, final String address, final String phone, final String email, final Uri filePath) { // build and show an confirm window
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.AlertDialogCustom);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctw);
        dialog.setTitle(Title);
        dialog.setMessage(Message);
        dialog.setIcon(R.drawable.ic_alert);
        dialog.setPositiveButton("باشه !", new DialogInterface.OnClickListener() {  // positive answer
            public void onClick(DialogInterface dialog, int id) {
                UpdateUserWithPhonePicture(name, address, phone, Backup_Phone, email, filePath);
                logoutUser();
            }
        });
        dialog.setNegativeButton("بیخیال", new DialogInterface.OnClickListener() { // negative answer
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();            // close dialog
            }
        });
        AlertDialog alert = dialog.create(); // create dialog
        alert.show();                        // show dialog
    }

    private void logoutUser() {
        session.setLogin(false);
        db_user.deleteUsers();
        Intent i = new Intent(getApplicationContext(), Lobby.class);
        MainScreenActivity.pointer.finish();
        startActivity(i);
        finish();
    }

    public void AddPhoto(View view) {
        HashMap<String, String> user = db_user.getUserDetails();
        String file_name = "user_" + user.get(TAGs.UID);
        new PickerBuilder(EditProfile.this, PickerBuilder.SELECT_FROM_GALLERY)
                .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                    @Override
                    public void onImageReceived(Uri imageUri) {
                        Log.i("ImagePath", String.valueOf(imageUri));
                        image.setImageURI(imageUri);
                        filePath = imageUri;
                    }
                })
                .setImageName(file_name)
                .setImageFolderName("temp")
                .withTimeStamp(false)
                .setCropScreenColor(ContextCompat.getColor(getApplicationContext(), R.color.accent))
                .start();
    }

    private void Upload(final Uri uri) {
        try {
            File file = new File(uri.getPath());
            String file_name = "user_" + db_user.getUserDetails().get(TAGs.UID);
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName("ftp.zimia.ir"));
            ftpClient.login("zm@zimia.ir", "3920512197");
            ftpClient.changeWorkingDirectory("/images/");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
            ftpClient.enterLocalPassiveMode();
            ftpClient.storeFile(file_name + ".jpg", buffIn);
            buffIn.close();
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (NullPointerException | java.io.IOException e) {
            e.printStackTrace();
        }
        hideDialog();
    }
}