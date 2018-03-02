/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ronash.pushe.Pushe;
import helper.ConfirmManager;
import helper.FontHelper;
import helper.FormatHelper;
import helper.Helper;
import helper.SQLiteHandler;
import helper.SQLiteHandlerItem;
import helper.SQLiteHandlerMain;
import helper.SessionManager;
import ir.hatamiarash.utils.TAGs;
import models.Product;
import volley.AppController;

public class ShopCard extends AppCompatActivity {
	final static private int CODE_PAYMENT = 100;
	public static SQLiteHandlerItem db_item;
	public static SQLiteHandlerMain db_main;
	public static SQLiteHandler db_user;
	private static String HOST;
	@BindView(R.id.recyclerView)
	public RecyclerView list;
	@BindView(R.id.CardPrice)
	public TextView total_price;
	@BindView(R.id.CardDiscount)
	public TextView total_off;
	@BindView(R.id.CardExtend)
	public TextView total_extend;
	@BindView(R.id.CardTotalPrice)
	public TextView total_pay;
	@BindView(R.id.status)
	public TextView status;
	@BindView(R.id.btnPay)
	public Button pay;
	@BindView(R.id.btnClear)
	public Button clear;
	@BindView(R.id.btnBack)
	public Button back;
	@BindView(R.id.empty)
	public RelativeLayout empty;
	@BindView(R.id.origin)
	public RelativeLayout origin;
	SessionManager session;
	ConfirmManager confirmManager;
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	List<String> Item;
	List<Product> Products_List;
	Adapter_Product adapter;
	private int CODE_STATUS = 0;
	private int tOff = 0;
	private int tPrice = 0;
	private int tExtend = 4000;
	private int check = 0;
	private int send_time;
	private String ORDER_CODE = "-1";
	private String ORDER_AMOUNT = "1000";
	private String ORDER_HOUR;
	private String STUFFS = "";
	private String STUFFS_ID = "";
	private String DESCRIPTION = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shop_card);
		ButterKnife.bind(this);
		
		db_main = new SQLiteHandlerMain(getApplicationContext());
		db_item = new SQLiteHandlerItem(getApplicationContext());
		db_user = new SQLiteHandler(getApplicationContext());
		session = new SessionManager(getApplicationContext());
		confirmManager = new ConfirmManager(getApplicationContext());
		list.setHasFixedSize(true);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(layoutManager);
		Products_List = new ArrayList<>();
		adapter = new Adapter_Product(Products_List);
		list.setAdapter(adapter);
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		
		HOST = getResources().getString(R.string.url_host);
		
		pay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!session.isLoggedIn()) {
					Helper.MakeToast(getApplicationContext(), "ابتدا وارد حساب کاربری شوید", TAGs.ERROR);
					startActivity(new Intent(ShopCard.this, Login.class));
				} else if (!confirmManager.isPhoneConfirm()) {
					new MaterialStyledDialog.Builder(ShopCard.this)
							.setTitle(FontHelper.getSpannedString(ShopCard.this, "تایید حساب"))
							.setDescription(FontHelper.getSpannedString(ShopCard.this, "لطفا شماره تلفن خود را تایید کنید."))
							.setStyle(Style.HEADER_WITH_TITLE)
							.withDarkerOverlay(true)
							.withDialogAnimation(true)
							.setCancelable(true)
							.setPositiveText("باشه")
							.onPositive(new MaterialDialog.SingleButtonCallback() {
								@Override
								public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
									Intent intent = new Intent(ShopCard.this, Confirm_Phone.class);
									intent.putExtra(TAGs.PHONE, db_user.getUserDetails().get(TAGs.PHONE));
									startActivity(intent);
									finish();
								}
							})
							.show();
				} else if (!confirmManager.isInfoConfirm()) {
					new MaterialStyledDialog.Builder(ShopCard.this)
							.setTitle(FontHelper.getSpannedString(ShopCard.this, "تایید حساب"))
							.setDescription(FontHelper.getSpannedString(ShopCard.this, "متاسفانه اطلاعات حساب شما تایید نشده است. جهت اطلاعات بیشتر صندوق پیام را بررسی کرده و در صورت هر گونه سوال با ما تماس بگیرید"))
							.setStyle(Style.HEADER_WITH_TITLE)
							.withDarkerOverlay(true)
							.withDialogAnimation(true)
							.setCancelable(false)
							.setPositiveText("باشه")
							.show();
				} else {
					vibrator.vibrate(50);
					if (tPrice > 0 && db_item.getRowCount() > 0) {
						String time = String.valueOf(times(getTime(1), getTime(2)));
						String time2 = String.valueOf(times(getTime(1), getTime(2)) + 1);
						String extend = "";
						if (send_time == 9 || send_time == 11)
							extend = " صبح";
						if (send_time == 16 || send_time == 18 || send_time == 19)
							extend = " عصر";
						ORDER_HOUR = time;
						String message;
						if (send_time == 19)
							message = "با توجه به زمان خدمات دهی شرکت ، سفارش شما از ساعت " + time + ":30 الی" + time2 + ":30 " + extend + " برای شما ارسال خواهد شد.";
						else
							message = "با توجه به زمان خدمات دهی شرکت ، سفارش شما از ساعت " + time + " الی " + time2 + extend + " برای شما ارسال خواهد شد.";
						LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						final View customView = inflater.inflate(R.layout.custom_dialog, null);
						final TextView edit_text = customView.findViewById(R.id.edit_text);
						final RadioGroup payMethod = customView.findViewById(R.id.payMethod);
						new MaterialStyledDialog.Builder(ShopCard.this)
								.setTitle(FontHelper.getSpannedString(getApplicationContext(), "تکمیل خرید"))
								.setDescription(FontHelper.getSpannedString(getApplicationContext(), message))
								.setStyle(Style.HEADER_WITH_TITLE)
								.setHeaderColor(R.color.green)
								.setCustomView(customView, 5, 5, 5, 5)
								.withDarkerOverlay(true)
								.withDialogAnimation(true)
								.setCancelable(true)
								.setPositiveText("تایید")
								.onPositive(new MaterialDialog.SingleButtonCallback() {
									@Override
									public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
										DESCRIPTION = edit_text.getText().toString();
										RadioButton rb = customView.findViewById(payMethod.getCheckedRadioButtonId());
										String tPay = total_pay.getText().toString();
										int final_price = Integer.valueOf(FormatHelper.toEnglishNumber(tPay.substring(0, tPay.length() - 6))) * 10;
										if (rb.getText().toString().equals("آنلاین")) {
											Pay(TAGs.API_KEY, String.valueOf(final_price));
										} else if (rb.getText().toString().equals("حضوری")) {
											SetOrder(Pushe.getPusheId(getApplicationContext()), 0);
										}
//                                        onPaySuccess();
									}
								})
								.show();
					} else
						Helper.MakeToast(getApplicationContext(), "سبد خرید خالی است", TAGs.ERROR);
				}
			}
		});
		
		clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				db_item.deleteItems();
				Products_List.clear();
				adapter.notifyDataSetChanged();
				list.setAdapter(adapter);
				CODE_STATUS = 1;
				empty.setVisibility(View.VISIBLE);
				origin.setVisibility(View.GONE);
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				vibrator.vibrate(50);
				Intent data = new Intent();
				setResult(CODE_STATUS, data);
				finish();
			}
		});
		
		List<String> main = db_main.getItemsDetails();
		tExtend = Integer.valueOf(main.get(0));
		FetchAllProducts();
		sendPrice(tPrice);
	}
	
	private void FetchAllProducts() {
		Item = db_item.getItemsDetails();
		// numbers must be same of database fields !!!!! all numbers Item.size() / n [] i * n + 1
		for (int i = 0; i < (Item.size() / 8); i++) {
			//String id = Item.get(i * 10);
			String uid = Item.get(i * 8 + 1);
			String name = Item.get(i * 8 + 2);
			String price = Item.get(i * 8 + 3);
			String info = Item.get(i * 8 + 4);
			String off = Item.get(i * 8 + 5);
			String count = Item.get(i * 8 + 6);
			String o_count = Item.get(i * 8 + 7);
			
			tOff += Integer.valueOf(off);
			tPrice += Integer.valueOf(price);
			
			// temporary use point count var for "original count"
			Products_List.add(new Product(uid, name, "", price, Integer.valueOf(off), Integer.valueOf(count), 0.0, Integer.valueOf(o_count), info));
			
			STUFFS += "," + name;
			STUFFS_ID += "," + uid;
		}
		if (tPrice == 0) {
			empty.setVisibility(View.VISIBLE);
			origin.setVisibility(View.GONE);
		} else {
			ORDER_AMOUNT = String.valueOf(tPrice + tExtend);
			total_off.setText(String.valueOf(tOff) + " تومان");
			total_price.setText(String.valueOf(tPrice + tOff) + " تومان");
			total_extend.setText(String.valueOf(tExtend) + " تومان");
			total_pay.setText(String.valueOf(tPrice + tExtend) + " تومان");
			pay.setText("پرداخت - " + FormatHelper.toPersianNumber(String.valueOf(tPrice + tExtend)) + " تومان");
			adapter.notifyDataSetChanged();
		}
	}
	
	private String getCounts() {
		Item = db_item.getItemsDetails();
		String c = "";
		for (int i = 0; i < (Item.size() / 8); i++) {
			String count = FormatHelper.toEnglishNumber2(Item.get(i * 8 + 6));
			c += count + ",";
		}
		return c;
	}
	
	private int ConvertToInteger(TextView text) {
		return Integer.valueOf(FormatHelper.toEnglishNumber(text.getText().toString().substring(0, text.getText().toString().length() - 6)));
	}
	
	private void Pay(final String API_KEY, final String AMOUNT) {
		String string_req = "req_fetch";
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		StringRequest strReq = new StringRequest(Request.Method.POST, "https://pay.ir/payment/send", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e("Pay R", response);
				try {
					JSONObject jObj = new JSONObject(response);
					int status = jObj.getInt("status");
					if (status == 1) {
						String Address = "https://pay.ir/payment/gateway/" + jObj.getString("transId");
						ORDER_CODE = jObj.getString("transId");
						ORDER_AMOUNT = AMOUNT;
//                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Address));
						Intent i = new Intent(getApplicationContext(), Web.class);
						i.putExtra(TAGs.ADDRESS, Address);
						startActivityForResult(i, CODE_PAYMENT);
					} else
						Helper.MakeToast(getApplicationContext(), jObj.getString("errorMessage"), TAGs.ERROR);
					hideDialog();
				} catch (JSONException e) {
					e.printStackTrace();
					hideDialog();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("Pay E", error.getMessage());
				if (error.getMessage() != null)
					Helper.MakeToast(getApplicationContext(), error.getMessage(), TAGs.ERROR);
				else
					Helper.MakeToast(getApplicationContext(), "خطایی رخ داده است - اتصال به اینترنت را بررسی نمایید", TAGs.ERROR);
				hideDialog();
				finish();
			}
		}) {
			@Override
			protected java.util.Map<String, String> getParams() {
				java.util.Map<String, String> params = new HashMap<>();
				params.put("api", API_KEY);
				params.put("amount", AMOUNT);
				params.put("factorNumber", "1");
				params.put("redirect", "http://hyper-online.ir/api/callback");
				return params;
			}
		};
		AppController.getInstance().addToRequestQueue(strReq, string_req);
	}
	
	private void Check(final String CODE, final int level) {
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = "https://pay.ir/payment/verify";
			JSONObject params = new JSONObject();
			params.put("api", TAGs.API_KEY);
			params.put("transId", CODE);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					Log.i("Check R", response);
					try {
						JSONObject jObj = new JSONObject(response);
						int status = jObj.getInt("status");
						if (status == 1)
							onPaySuccess();
						else
							onPayCanceled();
					} catch (JSONException e) {
						hideDialog();
						e.printStackTrace();
						finish();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.e("Check E-" + String.valueOf(level), error.toString());
					if (level == 2) {
						if (error.toString().equals("com.android.volley.ServerError"))
							Helper.MakeToast(ShopCard.this, "پرداخت لغو شد", TAGs.ERROR);
						else
							Helper.MakeToast(ShopCard.this, error.toString(), TAGs.ERROR);
						hideDialog();
					} else
						Check(CODE, 2);
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
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_PAYMENT)
			Check(ORDER_CODE, 1);
	}
	
	private void onPaySuccess() {
		new MaterialStyledDialog.Builder(ShopCard.this)
				.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
				.setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت موفقیت آمیز بود. با تشکر از انتخاب شما."))
				.setStyle(Style.HEADER_WITH_TITLE)
				.setHeaderColor(R.color.green)
				.withDarkerOverlay(true)
				.withDialogAnimation(true)
				.setCancelable(false)
				.setPositiveText("باشه")
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						SetOrder(ORDER_CODE, 1);
					}
				})
				.show();
	}
	
	private void onPayCanceled() {
		new MaterialStyledDialog.Builder(ShopCard.this)
				.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت"))
				.setDescription(FontHelper.getSpannedString(getApplicationContext(), "پرداخت با مشکل مواجه شده است. در صورت کسر وجه ، با پشتیبانی تماس حاصل فرمایید."))
				.setStyle(Style.HEADER_WITH_TITLE)
				.withDarkerOverlay(true)
				.withDialogAnimation(true)
				.setCancelable(true)
				.setPositiveText("باشه")
				.show();
	}
	
	private void SetOrder(String CODE, final int payMethod) {
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		showDialog();
		
		try {
			RequestQueue requestQueue = Volley.newRequestQueue(this);
			String URL = getResources().getString(R.string.url_api, HOST) + "orders";
			JSONObject params = new JSONObject();
			String uid = db_user.getUserDetails().get(TAGs.UID);
			params.put("user", uid);
			params.put("code", CODE);
			params.put("stuffs", STUFFS);
			params.put("stuffs_id", STUFFS_ID);
			params.put("stuffs_count", getCounts());
			params.put("hour", ORDER_HOUR);
			params.put("description", DESCRIPTION);
			params.put("method", payMethod);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					Log.i("SetOrder R", response);
					hideDialog();
					try {
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							db_item.deleteItems();
							if (payMethod == 1) {
								Intent intent = new Intent(getApplicationContext(), Activity_Factor.class);
								intent.putExtra("order_code", ORDER_CODE);
								startActivity(intent);
								finish();
							} else {
								new MaterialStyledDialog.Builder(ShopCard.this)
										.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت حضوری"))
										.setDescription(FontHelper.getSpannedString(getApplicationContext(), "با تشکر از انتخاب شما... سبد خرید ثبت شد !!"))
										.setStyle(Style.HEADER_WITH_TITLE)
										.setHeaderColor(R.color.green)
										.withDarkerOverlay(true)
										.withDialogAnimation(true)
										.setCancelable(false)
										.setPositiveText("باشه")
										.onPositive(new MaterialDialog.SingleButtonCallback() {
											@Override
											public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
												Intent intent = new Intent(getApplicationContext(), Activity_Main.class);
												intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
												startActivity(intent);
												startActivity(new Intent(getApplicationContext(), Activity_UserOrders.class));
												finish();
											}
										})
										.show();
							}
						} else {
							Log.e("SetOrder E", jObj.getString(TAGs.ERROR_MSG));
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(ShopCard.this, errorMsg, TAGs.ERROR);
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
					Log.e("SetOrder E", error.toString());
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
	
	private int getTime(int type) {
		Date date = new Date();
		SimpleDateFormat simpleDateFormat;
		if (type == 1) {
			simpleDateFormat = new SimpleDateFormat("HH");
			return Integer.valueOf(simpleDateFormat.format(date));
		} else {
			simpleDateFormat = new SimpleDateFormat("mm");
			return Integer.valueOf(simpleDateFormat.format(date));
		}
	}

//    private class TouchListener implements View.OnTouchListener {
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
//                    break;
//                case MotionEvent.ACTION_CANCEL:
//                case MotionEvent.ACTION_UP:
//                    ((TextView) view).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
//                    break;
//            }
//            return false;
//        }
//    }
	
	@Contract(pure = true)
	private int times(int hour, int minute) {
		// 9 - 11 - 16 - 18 - 19:30
		if (hour >= 9 && hour < 10) send_time = 11;
		if (hour >= 10 && hour < 11)
			if (minute <= 40)
				send_time = 11;
			else
				send_time = 16;
		
		if (hour >= 11 && hour < 15) send_time = 16;
		if (hour >= 15 && hour < 16)
			if (minute <= 40)
				send_time = 16;
			else
				send_time = 18;
		
		if (hour >= 16 && hour < 17) send_time = 18;
		if (hour >= 17 && hour < 18)
			if (minute <= 40)
				send_time = 18;
			else
				send_time = 19;
		
		if (hour >= 18 && hour < 19)
			if (minute <= 50)
				send_time = 19;
			else
				send_time = 9;
		
		if (hour >= 19 && hour <= 23) send_time = 9;
		if (hour >= 0 && hour < 8) send_time = 9;
		if (hour >= 8 && hour < 9)
			if (minute <= 40)
				send_time = 9;
			else
				send_time = 11;
		
		return send_time;
	}
	
	private void sendPrice(int price) {
		if (price >= 30000) {
			tExtend = 0;
			status.setText("ارسال رایگان");
			total_extend.setText(String.valueOf(tExtend) + " تومان");
		} else {
			tExtend = 4000;
			status.setText("خرید های کمتر از 30 هزار تومان با هزینه ارسال می شوند");
			total_extend.setText(String.valueOf(tExtend) + " تومان");
		}
		String p = FormatHelper.toPersianNumber(String.valueOf(db_item.TotalPrice() + tExtend)) + " تومان";
		pay.setText("پرداخت - " + p);
		total_pay.setText(String.valueOf(tPrice + tExtend) + " تومان");
	}
	
	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		setResult(CODE_STATUS, data);
		super.onBackPressed();
	}
	
	private class Adapter_Product extends RecyclerView.Adapter<Adapter_Product.ProductViewHolder> {
		private List<Product> products;
		
		Adapter_Product(List<Product> products) {
			this.products = products;
		}
		
		@Override
		public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cart_product, viewGroup, false);
			return new ProductViewHolder(view);
		}
		
		@Override
		public int getItemCount() {
			return products.size();
		}
		
		@Override
		public void onBindViewHolder(ProductViewHolder viewHolder, int i) {
			viewHolder.product_id.setText(products.get(i).unique_id);
			viewHolder.product_off.setText(String.valueOf(products.get(i).off));
			viewHolder.product_name.setText(products.get(i).name);
			viewHolder.product_description.setText("");
			if (!products.get(i).description.equals("null"))
				viewHolder.product_description.setText(products.get(i).description);
			if (products.get(i).count > 0)
				viewHolder.product_price.setText(String.valueOf(Integer.valueOf(products.get(i).price) / products.get(i).count) + " تومان");
			viewHolder.product_count.setText(String.valueOf(products.get(i).count));
			// temporary use point count var for "original count"
			viewHolder.product_count_original.setText(String.valueOf(products.get(i).point_count));
		}
		
		@Override
		public void onAttachedToRecyclerView(RecyclerView recyclerView) {
			super.onAttachedToRecyclerView(recyclerView);
		}
		
		class ProductViewHolder extends RecyclerView.ViewHolder {
			CardView product;
			TextView product_id;
			TextView product_off;
			TextView product_name;
			TextView product_description;
			TextView product_price;
			TextView product_count;
			TextView product_count_original;
			ImageView product_inc, product_dec;
			
			ProductViewHolder(View itemView) {
				super(itemView);
				product = itemView.findViewById(R.id.product);
				product_id = itemView.findViewById(R.id.product_id);
				product_off = itemView.findViewById(R.id.product_off);
				product_name = itemView.findViewById(R.id.product_name);
				product_description = itemView.findViewById(R.id.product_info);
				product_price = itemView.findViewById(R.id.product_price);
				product_count = itemView.findViewById(R.id.product_count_cart);
				product_count_original = itemView.findViewById(R.id.product_count_original);
				product_dec = itemView.findViewById(R.id.dec);
				product_inc = itemView.findViewById(R.id.inc);
				
				product_dec.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						CODE_STATUS = 1;
						vibrator.vibrate(25);
						if (Integer.valueOf(product_count.getText().toString()) > 1)
							product_count.setText(String.valueOf(Integer.valueOf(product_count.getText().toString()) - 1));
						else {
							product_count.setText("0");
							removeAt(getPosition());
							db_item.deleteItem(product_id.getText().toString());
							check = products.size();
						}
					}
				});
				
				product_inc.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						CODE_STATUS = 1;
						vibrator.vibrate(25);
						int pCount = Integer.valueOf(product_count.getText().toString());
						if (pCount < Integer.valueOf(product_count_original.getText().toString())) {
							product_count.setText(String.valueOf(Integer.valueOf(product_count.getText().toString()) + 1));
						} else
							Helper.MakeToast(getApplicationContext(), "تعداد بیشتر موجود نمی باشد", TAGs.ERROR);
					}
				});

//                product_count.addTextChangedListener(new TextWatcher() {
//                    // save old count
//                    int temp;
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                        if (check == products.size()) {
//                            // off / old count * new count
//                            int new_off;
//                            String pCount;
//                            if (product_count.getText().toString().length() == 1)
//                                pCount = FormatHelper.toEnglishNumber2(product_count.getText().toString());
//                            else
//                                pCount = FormatHelper.toEnglishNumber(product_count.getText().toString());
//
//                            if (Integer.valueOf(pCount) == 0 || temp == 0)
//                                new_off = 0;
//                            else {
//                                new_off = (Integer.valueOf(product_off.getText().toString()) / temp) * Integer.valueOf(pCount);
//                            }
//
//                            product_off.setText(String.valueOf(new_off));
//
//                            db_item.updateItem(
//                                    product_id.getText().toString(),
//                                    pCount,
//                                    String.valueOf(ConvertToInteger(product_price) * Integer.valueOf(pCount)),
//                                    String.valueOf(new_off)
//                            );
//
//                            sendPrice(db_item.TotalPrice());
//                            String price = FormatHelper.toPersianNumber(String.valueOf(db_item.TotalPrice() + tExtend)) + " تومان";
//                            String off = FormatHelper.toPersianNumber(String.valueOf(db_item.TotalOff())) + " تومان";
//                            total_pay.setText(price);
//                            pay.setText("پرداخت - " + price);
//                            total_off.setText(off);
//                            int ttPrice = ConvertToInteger(total_pay) - ConvertToInteger(total_extend) + ConvertToInteger(total_off);
//                            total_price.setText(String.valueOf(ttPrice) + " تومان");
//                            ORDER_AMOUNT = String.valueOf(ttPrice);
//                        } else
//                            check++;
//                    }
//
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        if (product_count.getText().toString().length() == 1)
//                            temp = Integer.valueOf(FormatHelper.toEnglishNumber2(product_count.getText().toString()));
//                        else
//                            temp = Integer.valueOf(FormatHelper.toEnglishNumber(product_count.getText().toString()));
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    }
//                });
			}
			
			void removeAt(int position) {
				try {
					products.remove(position);
					notifyItemRemoved(position);
					notifyItemRangeChanged(position, products.size());
				} catch (Exception e) {
					Log.e("Arash", "Known Error RemoveAt");
				}
			}
		}
	}
}