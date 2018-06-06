/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ronash.pushe.Pushe;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.databases.SQLiteHandler;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerMain;
import ir.hatamiarash.hyperonline.helpers.ConfirmManager;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.FormatHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.models.Product;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_ShopCard extends AppCompatActivity {
	private static final String CLASS = Activity_ShopCard.class.getSimpleName();
	
	SQLiteHandlerItem db_item;
	SQLiteHandlerMain db_main;
	SQLiteHandler db_user;
	SessionManager session;
	ConfirmManager confirmManager;
	SweetAlertDialog progressDialog;
	Vibrator vibrator;
	List<String> Item;
	List<Product> Products_List;
	Adapter_Product adapter;
	HyperOnline application;
	Analytics analytics;
	
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
	
	int CODE_STATUS = 0;
	int tOff = 0;
	int tPrice = 0;
	int tExtend = 5000;
	int check = 0;
	int send_time;
	String ORDER_AMOUNT = "1000";
	String ORDER_HOUR;
	StringBuilder STUFFS = new StringBuilder();
	StringBuilder STUFFS_ID = new StringBuilder();
	String DESCRIPTION = "";
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_card);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
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
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		pay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!session.isLoggedIn()) {
					Helper.MakeToast(getApplicationContext(), "ابتدا وارد حساب کاربری شوید", TAGs.ERROR);
					startActivity(new Intent(Activity_ShopCard.this, Activity_Login.class));
				} else if (confirmManager.isPhoneConfirm()) {
					new MaterialStyledDialog.Builder(Activity_ShopCard.this)
							.setTitle(FontHelper.getSpannedString(Activity_ShopCard.this, "تایید حساب"))
							.setDescription(FontHelper.getSpannedString(Activity_ShopCard.this, "لطفا شماره تلفن خود را تایید کنید."))
							.setStyle(Style.HEADER_WITH_TITLE)
							.withDarkerOverlay(true)
							.withDialogAnimation(true)
							.setCancelable(true)
							.setPositiveText("باشه")
							.onPositive(new MaterialDialog.SingleButtonCallback() {
								@Override
								public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
									Intent intent = new Intent(Activity_ShopCard.this, Activity_ConfirmPhone.class);
									intent.putExtra(TAGs.PHONE, db_user.getUserDetails().get(TAGs.PHONE));
									startActivity(intent);
									finish();
								}
							})
							.show();
				} else if (confirmManager.isInfoConfirm()) {
					new MaterialStyledDialog.Builder(Activity_ShopCard.this)
							.setTitle(FontHelper.getSpannedString(Activity_ShopCard.this, "تایید حساب"))
							.setDescription(FontHelper.getSpannedString(Activity_ShopCard.this, "متاسفانه اطلاعات حساب شما هنوز تایید نشده است. جهت اطلاعات بیشتر صندوق پیام را بررسی کرده و در صورت هر گونه سوال با ما تماس بگیرید"))
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
						if (send_time == 16 || send_time == 18 || send_time == 20)
							extend = " عصر";
						ORDER_HOUR = time;
						String message;
						if (send_time == 20)
							message = "با توجه به زمان خدمات دهی شرکت ، سفارش شما از ساعت " + time + ":30 الی" + time2 + ":30 " + extend + " برای شما ارسال خواهد شد.";
						else
							message = "با توجه به زمان خدمات دهی شرکت ، سفارش شما از ساعت " + time + " الی " + time2 + extend + " برای شما ارسال خواهد شد.";
						LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						assert inflater != null;
						final View customView = inflater.inflate(R.layout.dialog_pay_method, null);
						final TextView edit_text = customView.findViewById(R.id.edit_text);
						final RadioGroup payMethod = customView.findViewById(R.id.payMethod);
						new MaterialStyledDialog.Builder(Activity_ShopCard.this)
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
										final int payMethod;
										if (rb.getText().toString().equals("آنلاین"))
											payMethod = 1;
										else
											payMethod = 0;
										
										LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										assert inflater != null;
										final View customView = inflater.inflate(R.layout.dialog_pay_way, null);
										final RadioGroup payWay = customView.findViewById(R.id.payWay);
										new MaterialStyledDialog.Builder(Activity_ShopCard.this)
												.setTitle(FontHelper.getSpannedString(getApplicationContext(), "پرداخت هزینه"))
												.setDescription(FontHelper.getSpannedString(getApplicationContext(), "در صورت انتخاب کیف پول ، تمام یا بخشی از مبلغ سبد خرید از کیف پول کسر خواهد شد."))
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
														RadioButton rb = customView.findViewById(payWay.getCheckedRadioButtonId());
														final String payWay;
														if (rb.getText().toString().equals("نقد"))
															payWay = "cash";
														else
															payWay = "wallet";
														
														sendOrder(payMethod, payWay);
													}
												})
												.show();
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
		
		analyticsReport();
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
			
			STUFFS.append(",").append(name);
			STUFFS_ID.append(",").append(uid);
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
	
	@NonNull
	private String getCounts() {
		Item = db_item.getItemsDetails();
		StringBuilder c = new StringBuilder();
		for (int i = 0; i < (Item.size() / 8); i++) {
			String count = FormatHelper.toEnglishNumber(Item.get(i * 8 + 6));
			c.append(count).append(",");
		}
		return c.toString();
	}
	
	private void sendOrder(final int payMethod, final String payWay) {
		showDialog();
		try {
			String count = getCounts();
			analytics.reportStartCheckout(Integer.valueOf(count.substring(0, count.length() - 1)), tPrice + tExtend);
			
			String URL = getResources().getString(R.string.url_api, HOST) + "temp_order";
			JSONObject params = new JSONObject();
			String uid = db_user.getUserDetails().get(TAGs.UID);
			params.put("user", uid);
			params.put(TAGs.CODE, Pushe.getPusheId(getApplicationContext()));
			params.put("stuffs", STUFFS);
			params.put("stuffs_id", STUFFS_ID);
			params.put("stuffs_count", count);
			params.put("hour", ORDER_HOUR);
			params.put("method", payMethod);
			params.put("way", payWay);
			params.put(TAGs.DESCRIPTION, DESCRIPTION);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					hideDialog();
					try {
						response = fixResponse(response);
						JSONObject jObj = new JSONObject(response);
						boolean error = jObj.getBoolean(TAGs.ERROR);
						if (!error) {
							if (payMethod == 1)
								Pay(jObj.getString(TAGs.CODE));
							else {
								new MaterialStyledDialog.Builder(Activity_ShopCard.this)
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
												db_item.deleteItems();
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
							String errorMsg = jObj.getString(TAGs.ERROR_MSG);
							Helper.MakeToast(Activity_ShopCard.this, errorMsg, TAGs.ERROR);
						}
					} catch (JSONException e) {
						Crashlytics.logException(e);
						hideDialog();
						finish();
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Crashlytics.logException(error);
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
				public byte[] getBody() {
					try {
						return mRequestBody.getBytes("utf-8");
					} catch (UnsupportedEncodingException e) {
						Crashlytics.logException(e);
						hideDialog();
						return null;
					}
				}
			};
			stringRequest.setRetryPolicy(new DefaultRetryPolicy(
					0,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
			));
			application.addToRequestQueue(stringRequest);
		} catch (JSONException e) {
			Crashlytics.logException(e);
			hideDialog();
		}
	}
	
	private void Pay(String ID) {
		String Address = getResources().getString(R.string.url_pay, HOST) + ID;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(Address));
		startActivityForResult(i, 200);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 200) {
			Timber.tag(CLASS).d("Pay Gateway Closed");
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
	
	@SuppressLint("SimpleDateFormat")
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
	
	@Contract(pure = true)
	private int times(int hour, int minute) {
		// 9 - 11 - 16 - 18 - 19:30
		// 9 - 11 - 16 - 18 - 20:30
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
				send_time = 20;
		
		if (hour >= 18 && hour < 20)
			if (minute <= 50)
				send_time = 20;
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
			tExtend = Integer.valueOf(db_main.getItemsDetails().get(0));
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
		@NonNull
		public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cart_product, viewGroup, false);
			return new ProductViewHolder(view);
		}
		
		@Override
		public int getItemCount() {
			return products.size();
		}
		
		@Override
		public void onBindViewHolder(@NonNull ProductViewHolder viewHolder, int i) {
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
		public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
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
			}
			
			void removeAt(int position) {
				try {
					products.remove(position);
					notifyItemRemoved(position);
					notifyItemRangeChanged(position, products.size());
				} catch (Exception e) {
					Crashlytics.logException(e);
				}
			}
		}
	}
	
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	
	private void analyticsReport() {
		analytics.reportScreen(CLASS);
		analytics.reportEvent("Open " + CLASS);
	}
}