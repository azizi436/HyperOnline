/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.ronash.pushe.Pushe;
import helper.Address;
import helper.FontHelper;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import ir.hatamiarash.utils.URLs;

public class Register extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = Register.class.getSimpleName();
    
    @InjectView(R.id.btnConfirm)
    Button btnRegister;
    @InjectView(R.id.btnChangePassword)
    Button btnLinkToLogin;
    @InjectView(R.id.current_password)
    IconEditText inputName;
    @InjectView(R.id.password)
    IconEditText inputPassword;
    @InjectView(R.id.password2)
    IconEditText inputPassword2;
    @InjectView(R.id.address)
    IconEditText inputAddress;
    @InjectView(R.id.new_password_2)
    IconEditText inputPhone;
    @InjectView(R.id.province)
    Spinner inputProvince;
    @InjectView(R.id.city)
    Spinner inputCity;
    
    SessionManager session;
    SweetAlertDialog progressDialog;
    SQLiteHandler db;
    
    GoogleApiClient mGoogleApiClient;
    Location mLocation;
    LocationManager locationManager;
    LocationRequest mLocationRequest;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    public double latitude = 0, longitude = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        ButterKnife.inject(this);
        
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        inputPhone.setError(Register.this, "همانند نمونه 09123456789");
        inputPassword.setError(Register.this, "حداقل 8 حرف");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.provinces);
        inputProvince.setAdapter(adapter);
        inputName.requestFocus();
        
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
        session = new SessionManager(getApplicationContext());// Session manager
        db = new SQLiteHandler(getApplicationContext());      // SQLite database handler
        if (session.isLoggedIn()) { // Check if user is already logged in or not
            // User is already logged in. Take him to main activity
            Helper.MakeToast(getApplicationContext(), "شما قبلا وارد شده اید", TAGs.WARNING);
            startActivity(new Intent(getApplicationContext(), Activity_Main.class));
            finish();
        }
        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String password = inputPassword.getText().toString();
                String password2 = inputPassword2.getText().toString();
                String address = inputAddress.getText().toString();
                String phone = inputPhone.getText().toString();
                
                if (Helper.CheckInternet(Register.this))
                    if (!name.isEmpty() && !password.isEmpty() && !password2.isEmpty() && !address.isEmpty() && !phone.isEmpty())
                        if (Helper.isValidPhone(phone))
                            if (Helper.isValidPassword(password))
                                if (password.equals(password2))
                                    if (inputProvince.getSelectedItem() != null && !inputProvince.getSelectedItem().toString().equals(""))
                                        if (inputCity.getSelectedItem() != null)
                                            registerUser(
                                                    name,
                                                    password,
                                                    address,
                                                    phone,
                                                    inputProvince.getSelectedItem().toString(),
                                                    inputCity.getSelectedItem().toString(),
                                                    String.valueOf(latitude),
                                                    String.valueOf(longitude)
                                            );
                                        else
                                            Helper.MakeToast(Register.this, "شهر را انتخاب نمایید", TAGs.ERROR);
                                    else
                                        Helper.MakeToast(Register.this, "استان را انتخاب نمایید", TAGs.ERROR);
                                else
                                    Helper.MakeToast(Register.this, "کلمه عبور ها تطابق ندارند", TAGs.ERROR);
                            else
                                Helper.MakeToast(Register.this, "کلمه عبور معتبر نیست", TAGs.ERROR);
                        else
                            Helper.MakeToast(Register.this, "شماره موبایل را بررسی نمایید", TAGs.ERROR);
                    else
                        Helper.MakeToast(Register.this, "تمامی کادر ها را پر نمایید", TAGs.ERROR);
            }
        });
        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Login.class);
                startActivity(i);
                finish();
            }
        });
        inputProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LoadCity(inputProvince.getSelectedItem().toString());
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }
    
    private void registerUser(final String name, final String password, final String address, final String phone, final String state, final String city, final String loc_x, final String loc_y) {
        progressDialog.setTitleText("لطفا منتظر بمانید");
        showDialog();
        
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = URLs.base_URL + "users";
            JSONObject params = new JSONObject();
            params.put(TAGs.NAME, name);
            params.put(TAGs.ADDRESS, address);
            params.put(TAGs.PHONE, phone);
            params.put(TAGs.PASSWORD, password);
            params.put(TAGs.STATE, state);
            params.put(TAGs.CITY, city);
            params.put(TAGs.LOCATION_X, loc_x);
            params.put(TAGs.LOCATION_Y, loc_y);
            params.put("pushe", Pushe.getPusheId(getApplicationContext()));
            final String mRequestBody = params.toString();
            
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY R", response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean(TAGs.ERROR);
                        if (!error) {
                            MakeDialog("ثبت نام انجام شد", "نام کاربری شما تلفن همراهتان می باشد ، اکنون می توانید وارد شوید");
                        } else {
                            String errorMsg = jObj.getString(TAGs.ERROR_MSG);
                            Helper.MakeToast(Register.this, errorMsg, TAGs.ERROR); // show error message
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
                    Helper.MakeToast(Register.this, error.toString(), TAGs.ERROR);
                    hideDialog();
                    //finish();
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
    
    private void MakeDialog(String Title, String Message) {
        new MaterialStyledDialog.Builder(this)
                .setTitle(FontHelper.getSpannedString(this, Title))
                .setDescription(FontHelper.getSpannedString(this, Message))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
    
    private void MakeDialog2(String Title, String Message) {
        new MaterialStyledDialog.Builder(this)
                .setTitle(FontHelper.getSpannedString(this, Title))
                .setDescription(FontHelper.getSpannedString(this, Message))
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .withDialogAnimation(true)
                .setCancelable(true)
                .setPositiveText("باشه")
                .show();
    }
    
    private void LoadCity(String province) {
        ArrayAdapter<String> adapter;
        switch (province) {
            case "":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province0);
                inputCity.setAdapter(adapter);
                break;
            case "آذربایجان شرقی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province1);
                inputCity.setAdapter(adapter);
                break;
            case "آذربایجان غربی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province2);
                inputCity.setAdapter(adapter);
                break;
            case "اردبیل":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province3);
                inputCity.setAdapter(adapter);
                break;
            case "اصفهان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province4);
                inputCity.setAdapter(adapter);
                break;
            case "البرز":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province5);
                inputCity.setAdapter(adapter);
                break;
            case "ایلام":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province6);
                inputCity.setAdapter(adapter);
                break;
            case "بوشهر":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province7);
                inputCity.setAdapter(adapter);
                break;
            case "تهران":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province8);
                inputCity.setAdapter(adapter);
                break;
            case "چهارمحال و بختیاری":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province9);
                inputCity.setAdapter(adapter);
                break;
            case "خراسان جنوبی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province10);
                inputCity.setAdapter(adapter);
                break;
            case "خراسان رضوی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province11);
                inputCity.setAdapter(adapter);
                break;
            case "خراسان شمالی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province12);
                inputCity.setAdapter(adapter);
                break;
            case "خوزستان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province13);
                inputCity.setAdapter(adapter);
                break;
            case "زنجان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province14);
                inputCity.setAdapter(adapter);
                break;
            case "سمنان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province15);
                inputCity.setAdapter(adapter);
                break;
            case "سیستان و بلوچستان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province16);
                inputCity.setAdapter(adapter);
                break;
            case "فارس":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province17);
                inputCity.setAdapter(adapter);
                break;
            case "قزوین":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province18);
                inputCity.setAdapter(adapter);
                break;
            case "قم":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province19);
                inputCity.setAdapter(adapter);
                break;
            case "کردستان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province20);
                inputCity.setAdapter(adapter);
                break;
            case "کرمان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province21);
                inputCity.setAdapter(adapter);
                break;
            case "کرمانشاه":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province22);
                inputCity.setAdapter(adapter);
                break;
            case "کهگیلویه و بویراحمد":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province23);
                inputCity.setAdapter(adapter);
                break;
            case "گلستان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province24);
                inputCity.setAdapter(adapter);
                break;
            case "گیلان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province25);
                inputCity.setAdapter(adapter);
                break;
            case "لرستان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province26);
                inputCity.setAdapter(adapter);
                break;
            case "مازندران":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province27);
                inputCity.setAdapter(adapter);
                break;
            case "مرکزی":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province28);
                inputCity.setAdapter(adapter);
                break;
            case "هرمزگان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province29);
                inputCity.setAdapter(adapter);
                break;
            case "همدان":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province30);
                inputCity.setAdapter(adapter);
                break;
            case "یزد":
                adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.province31);
                inputCity.setAdapter(adapter);
                break;
        }
    }
    
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null)
            startLocationUpdates();
        if (mLocation != null) {
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            Log.e("Location Service", "Location Detected (" + String.valueOf(latitude) + ", " + String.valueOf(longitude) + ")");
        } else
            Log.e("Location Service", "Location not Detected");
    }
    
    protected void startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }
    
    @Override
    public void onLocationChanged(Location location) {
    }
}