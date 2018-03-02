/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import helper.Address;
import helper.FontHelper;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;

public class Register extends AppCompatActivity {
	private static String HOST;
	SessionManager session;
	SweetAlertDialog progressDialog;
	SQLiteHandler db;
	@BindView(R.id.btnConfirm)
	Button btnRegister;
	@BindView(R.id.btnChangePassword)
	Button btnLinkToLogin;
	@BindView(R.id.current_password)
	IconEditText inputName;
	@BindView(R.id.password)
	IconEditText inputPassword;
	@BindView(R.id.password2)
	IconEditText inputPassword2;
	@BindView(R.id.address)
	IconEditText inputAddress;
	@BindView(R.id.new_password_2)
	IconEditText inputPhone;
	@BindView(R.id.province)
	Spinner inputProvince;
	@BindView(R.id.city)
	Spinner inputCity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		
		ButterKnife.bind(this);
		
		inputPhone.setError(Register.this, "همانند نمونه 09123456789");
		inputPassword.setError(Register.this, "حداقل 8 حرف");
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, Address.provinces);
		inputProvince.setAdapter(adapter);
		inputProvince.setEnabled(false);
		inputName.requestFocus();
		
		HOST = getResources().getString(R.string.url_host);
		
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
										if (inputCity.getSelectedItem() != null && !inputCity.getSelectedItem().toString().equals("انتخاب کنید"))
											registerUser(
													name,
													password,
													address,
													phone,
													inputProvince.getSelectedItem().toString(),
													inputCity.getSelectedItem().toString()
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
	
	private void registerUser(final String name, final String password, final String address, final String phone, final String state, final String city) {
		progressDialog.setTitleText("لطفا منتظر بمانید");
		showDialog();
		
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "users";
			JSONObject params = new JSONObject();
			params.put(TAGs.NAME, name);
			params.put(TAGs.ADDRESS, address);
			params.put(TAGs.PHONE, phone);
			params.put(TAGs.PASSWORD, password);
			params.put(TAGs.STATE, state);
			params.put(TAGs.CITY, city);
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
							hideDialog();
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
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			requestQueue.add(stringRequest);
		} catch (JSONException e) {
			e.printStackTrace();
			hideDialog();
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
}