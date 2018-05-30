package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.utils.TAGs;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.helpers.PriceHelper.formatPrice;

public class Activity_TransferConfirm extends AppCompatActivity {
	private static final String CLASS = Activity_TransferConfirm.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.tPrice)
	TextView tPrice;
	@BindView(R.id.cWallet)
	TextView cWallet;
	@BindView(R.id.tUser)
	TextView tUser;
	@BindView(R.id.cPrice)
	TextView cPrice;
	@BindView(R.id.aPrice)
	TextView aPrice;
	@BindView(R.id.message)
	TextView message;
	@BindView(R.id.confirm)
	CardView confirm;
	
	String transferCode;
	String transferUser;
	String transferPrice;
	String currentPrice;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_transfer_confirm);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
		progressDialog.setCancelable(false);
		progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getApplicationContext(), R.color.accent));
		progressDialog.setTitleText(getResources().getString(R.string.wait));
		
		setSupportActionBar(toolbar);
		try {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_action_bar_title, null);
			ActionBar.LayoutParams p = new ActionBar.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.END);
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "تایید انتقال"));
			getSupportActionBar().setCustomView(v, p);
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
		}
		
		transferCode = getIntent().getStringExtra(TAGs.CODE);
		transferUser = getIntent().getStringExtra(TAGs.NAME);
		transferPrice = getIntent().getStringExtra("tPrice");
		currentPrice = getIntent().getStringExtra(TAGs.PRICE);
		
		message.setVisibility(View.GONE);
		tPrice.setText(formatPrice(transferPrice) + " تومان");
		cPrice.setText(formatPrice(currentPrice) + " تومان");
		int afterPrice = Integer.valueOf(currentPrice) - Integer.valueOf(transferPrice);
		aPrice.setText(formatPrice(String.valueOf(afterPrice)) + " تومان");
		cWallet.setText("اصلی");
		tUser.setText(transferUser);
		
		if (afterPrice < 0) {
			message.setVisibility(View.VISIBLE);
			confirm.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray3));
		}
		
		analyticsReport();
	}
	
	private void showDialog() {
		if (!progressDialog.isShowing())
			progressDialog.show();
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
