/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import helper.ConfirmManager;
import helper.FontHelper;
import helper.Helper;
import helper.IconEditText;
import helper.SQLiteHandler;
import ir.hatamiarash.Image.PickerBuilder;
import ir.hatamiarash.utils.TAGs;
import volley.AppController;

public class EditProfile extends AppCompatActivity {
	private static final String TAG = EditProfile.class.getSimpleName();
	private static String HOST;
	ConfirmManager confirmManager;
	@BindView(R.id.name)
	IconEditText txtName;
	@BindView(R.id.address)
	IconEditText txtAddress;
	@BindView(R.id.btnConfirm)
	Button btnConfirm;
	@BindView(R.id.btnChangePassword)
	Button btnChangePassword;
	@BindView(R.id.image)
	ImageView image;
	@BindView(R.id.add_photo)
	RelativeLayout add_photo;
	@BindView(R.id.progress_bar)
	ProgressBar progressBar;
	private SweetAlertDialog progressDialog;
	private SQLiteHandler db_user;
	private Vibrator vibrator;
	private String uid, BACKUP_NAME, BACKUP_ADDRESS;
	private Uri filePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile);
		ButterKnife.bind(this);
		
		progressBar.setVisibility(View.GONE);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		add_photo.setVisibility(View.GONE);
		image.setVisibility(View.INVISIBLE);
		confirmManager = new ConfirmManager(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());              // user local database
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		
		HOST = getResources().getString(R.string.url_host);
		
		if (Helper.CheckInternet(getApplicationContext()))
			GetUser(db_user.getUserDetails().get(TAGs.UID));
		else
			finish();
		
		btnConfirm.setOnClickListener(new View.OnClickListener() {         // confirm button's event
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				String name = txtName.getText().toString();
				String address = txtAddress.getText().toString();
				if (!BACKUP_ADDRESS.equals(address) || !BACKUP_NAME.equals(name)) {
					if (Helper.CheckInternet(getApplicationContext()))
						if (!name.isEmpty() && !address.isEmpty())
							if (filePath == null)
								UpdateUser(name, address);
							else
								UpdateUserWithPicture(name, address, filePath);
						else
							Helper.MakeToast(getApplicationContext(), "تمامی کادر ها را پر نمایید", TAGs.WARNING);
				} else {
					Intent data = new Intent();
					setResult(2, data);
					finish();
				}
				
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
	
	private void GetUser(final String unique_id) {
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "users/" + unique_id;
			JSONObject params = new JSONObject();
			params.put(TAGs.UNIQUE_ID, unique_id);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {                          // Check for error node in json
							JSONObject user = jObj.getJSONObject(TAGs.USER);
							BACKUP_ADDRESS = user.getString(TAGs.ADDRESS);
							BACKUP_NAME = user.getString(TAGs.NAME);
							txtName.setText(getApplicationContext(), BACKUP_NAME);
							txtAddress.setText(getApplicationContext(), BACKUP_ADDRESS);
							uid = unique_id;
							if (!user.getString(TAGs.IMAGE).equals(TAGs.NULL)) {
								progressBar.setVisibility(View.VISIBLE);
								Picasso
										.with(getApplicationContext())
										.load(getResources().getString(R.string.url_image, HOST) + user.getString(TAGs.IMAGE))
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
								//add_photo.setVisibility(View.VISIBLE);
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
	
	private void UpdateUser(final String name, final String address) {
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "user_update";
			JSONObject params = new JSONObject();
			params.put(TAGs.NAME, name);
			params.put(TAGs.ADDRESS, address);
			params.put(TAGs.UID, uid);
			params.put(TAGs.IMAGE, TAGs.NULL);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					Log.i("UpdateUser R", response);
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {                          // Check for error node in json
							db_user.updateUser(uid, name, address);
							new MaterialStyledDialog.Builder(EditProfile.this)
									.setTitle(FontHelper.getSpannedString(EditProfile.this, "ویرایش مشخصات"))
									.setDescription(FontHelper.getSpannedString(EditProfile.this, "اطلاعات شما با موفقیت تغییر کرد. حساب شما نیاز به تایید مجدد دارد"))
									.setStyle(Style.HEADER_WITH_TITLE)
									.withDarkerOverlay(true)
									.withDialogAnimation(true)
									.setCancelable(false)
									.setPositiveText("باشه")
									.onPositive(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											confirmManager.setInfoConfirm(false);
											Intent data = new Intent();
											setResult(1, data);
											finish();
										}
									})
									.show();
						} else {
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(EditProfile.this, errorMsg, TAGs.ERROR); // show error message
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
					Log.e("UpdateUser E", error.toString());
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
	
	private void UpdateUserWithPicture(final String name, final String address, final Uri filePath) {
		// Tag used to cancel the request
		String string_req = "req_update";
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		StringRequest strReq = new StringRequest(Request.Method.POST, getResources().getString(R.string.url_api, HOST), new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Update Response: " + response);
				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean(TAGs.ERROR);
					if (!error) {
						Upload(filePath);
						// Inserting row in users table
						db_user.updateUser(uid, name, address);
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
				params.put(TAGs.ADDRESS, address);
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