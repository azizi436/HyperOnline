package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.crashlytics.android.Crashlytics;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.preferences.WalletManager;
import ir.hatamiarash.hyperonline.utils.TAGs;
import mehdi.sakout.fancybuttons.FancyButton;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;
import static ir.hatamiarash.hyperonline.helpers.PriceHelper.formatPrice;

public class Activity_Wallet extends AppCompatActivity {
	private static final String CLASS = Activity_Wallet.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	SweetAlertDialog progressDialog;
	ImagePopup imagePopup;
	Vibrator vibrator;
	WalletManager walletManager;
	
	@BindView(R.id.image)
	ImageView walletQRCode;
	@BindView(R.id.progress_bar)
	ProgressBar progressBar;
	@BindView(R.id.title)
	TextView walletTitle;
	@BindView(R.id.code)
	TextView walletCode;
	@BindView(R.id.price)
	TextView walletPrice;
	@BindView(R.id.orderCount)
	TextView walletOrderCount;
	@BindView(R.id.orderPrice)
	TextView walletOrderPrice;
	@BindView(R.id.btnCharge)
	FancyButton btnCharge;
	@BindView(R.id.btnTransfer)
	FancyButton btnTransfer;
	@BindView(R.id.btnTransactions)
	FancyButton btnTransactions;
	@BindView(R.id.wallet_info)
	RelativeLayout walletInfo;
	@BindView(R.id.main)
	RelativeLayout mainLayout;
	
	String wCode;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_wallet);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		mainLayout.setVisibility(View.INVISIBLE);
		
		walletManager = new WalletManager(Activity_Wallet.this);
		imagePopup = new ImagePopup(this);
		imagePopup.setHideCloseIcon(true);
		imagePopup.setImageOnClickClose(true);
		imagePopup.setWindowHeight(800);
		imagePopup.setWindowWidth(800);
		imagePopup.setBackgroundColor(Color.argb(125, 0, 0, 0));
		imagePopup.setFullScreen(true);
		progressBar.setVisibility(View.GONE);
		progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		walletInfo.setVisibility(View.GONE);
		btnCharge.setCustomTextFont("sans.ttf");
		btnTransfer.setCustomTextFont("sans.ttf");
		btnTransactions.setCustomTextFont("sans.ttf");
		
		walletQRCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imagePopup.viewPopup();
			}
		});
		
		btnCharge.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				Intent intent = new Intent(Activity_Wallet.this, Activity_WalletCharge.class);
				intent.putExtra(TAGs.UID, getIntent().getStringExtra(TAGs.UID));
				startActivity(intent);
			}
		});
		
		btnTransfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				Intent intent = new Intent(Activity_Wallet.this, Activity_Transfer.class);
				intent.putExtra(TAGs.CODE, wCode);
				intent.putExtra(TAGs.UID, getIntent().getStringExtra(TAGs.UID));
				startActivity(intent);
			}
		});
		
		btnTransactions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(50);
				Intent intent = new Intent(Activity_Wallet.this, Activity_Transactions.class);
				intent.putExtra(TAGs.UID, getIntent().getStringExtra(TAGs.UID));
				startActivity(intent);
			}
		});
		
		listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				hideDialog();
				try {
					response = fixResponse(response);
					Timber.tag(CLASS).d(response);
					JSONObject object = new JSONObject(response);
					Boolean error = object.getBoolean(TAGs.ERROR);
					if (!error) {
						JSONObject wallet = object.getJSONObject(TAGs.WALLET);
						String title = wallet.getString(TAGs.TITLE);
						String price = wallet.getString(TAGs.PRICE);
						String code = wallet.getString(TAGs.CODE);
						
						wCode = code.substring(3);
						
						walletInfo.setVisibility(View.VISIBLE);
						walletTitle.setText("کیف پول " + title);
						walletPrice.setText(formatPrice(price) + " تومان");
						walletCode.setText("شماره : " + wCode);
						Picasso.with(getApplicationContext())
								.load(getResources().getString(R.string.url_qr, HOST, code))
								.networkPolicy(NetworkPolicy.NO_CACHE)
								.memoryPolicy(MemoryPolicy.NO_CACHE)
								.into(walletQRCode, new com.squareup.picasso.Callback() {
									@Override
									public void onSuccess() {
										progressBar.setVisibility(View.GONE);
										imagePopup.initiatePopup(walletQRCode.getDrawable());
									}
									
									@Override
									public void onError() {
										progressBar.setVisibility(View.GONE);
									}
								});
						
						JSONObject orders = object.getJSONObject("orders");
						String orderCount = orders.getString(TAGs.COUNT);
						String orderPrice = orders.getString(TAGs.PRICE);
						
						walletOrderCount.setText(orderCount + " خرید");
						walletOrderPrice.setText(formatPrice(orderPrice) + " تومان");
						
						if (walletManager.isWalletFirstUse()) {
							LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							assert inflater != null;
							final View customView = inflater.inflate(R.layout.dialog_wallet_policy, null);
							final TextView policies = customView.findViewById(R.id.policies);
							policies.setMovementMethod(new ScrollingMovementMethod());
							policies.setText(getResources().getString(R.string.policy_wallet).replace("\\n", System.getProperty("line.separator")));
							new MaterialStyledDialog.Builder(Activity_Wallet.this)
									.setTitle(FontHelper.getSpannedString(Activity_Wallet.this, "کیف پول"))
									.setDescription(FontHelper.getSpannedString(Activity_Wallet.this, "لطفا قبل از استفاده از کیف پول موارد زیر را خوانده و سپس تایید کنید :"))
									.setStyle(Style.HEADER_WITH_TITLE)
									.setHeaderColor(R.color.green)
									.setCustomView(customView, 5, 5, 5, 5)
									.withDarkerOverlay(true)
									.withDialogAnimation(true)
									.setCancelable(false)
									.setPositiveText("می پذیرم")
									.setNegativeText("نمی پذیرم")
									.onPositive(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											analytics.reportEvent("Wallet - Accept Policies");
											walletManager.setWalletFirstUse(false);
											mainLayout.setVisibility(View.VISIBLE);
										}
									})
									.onNegative(new MaterialDialog.SingleButtonCallback() {
										@Override
										public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
											analytics.reportEvent("Wallet - Decline Policies");
											finish();
										}
									})
									.show();
						} else
							mainLayout.setVisibility(View.VISIBLE);
					} else {
						String errorMsg = object.getString(TAGs.ERROR_MSG);
						Helper.MakeToast(getApplicationContext(), errorMsg, TAGs.ERROR);
						finish();
					}
				} catch (JSONException e) {
					Crashlytics.logException(e);
					hideDialog();
					finish();
				}
			}
		};
		
		errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Crashlytics.logException(error);
				hideDialog();
				finish();
			}
		};
		
		getWallet(getIntent().getStringExtra(TAGs.UID));
		
		analyticsReport();
	}
	
	private void getWallet(final String uid) {
		showDialog();
		String URL = getResources().getString(R.string.url_api, HOST) + "getWallet_byUser/" + uid;
		final String mRequestBody = "";
		
		StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, listener, errorListener) {
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
	}
	
	private void showDialog() {
		if (!progressDialog.isShowing())
			progressDialog.show();
		progressBar.setVisibility(View.VISIBLE);
	}
	
	private void hideDialog() {
		if (progressDialog.isShowing())
			progressDialog.dismiss();
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
