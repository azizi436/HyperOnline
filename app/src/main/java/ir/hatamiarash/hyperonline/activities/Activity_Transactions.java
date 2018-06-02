package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.jetbrains.annotations.Contract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.adapters.TransactionAdapter;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.helpers.Helper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.models.Transaction;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.HyperOnline.HOST;
import static ir.hatamiarash.hyperonline.helpers.FormatHelper.fixResponse;

public class Activity_Transactions extends AppCompatActivity {
	private static final String CLASS = Activity_Transactions.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	SweetAlertDialog progressDialog;
	Response.Listener<String> listener;
	Response.ErrorListener errorListener;
	
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.list)
	RecyclerView list;
	@BindView(R.id.message)
	TextView message;
	
	List<Transaction> transactionList;
	TransactionAdapter transactionAdapter;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_list_simple);
		
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
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "تراکنش های کیف پول"));
			getSupportActionBar().setCustomView(v, p);
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
		}
		
		transactionList = new ArrayList<>();
		transactionAdapter = new TransactionAdapter(this, transactionList);
		transactionAdapter.setHasStableIds(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		list.setItemAnimator(new DefaultItemAnimator());
		list.setAdapter(transactionAdapter);
		
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
						JSONArray transactions = object.getJSONArray(TAGs.LIST);
						for (int i = 0; i < transactions.length(); i++) {
							JSONObject transaction = transactions.getJSONObject(i);
							String date = transaction.getString(TAGs.CREATE_DATE);
							String card = transaction.getString(TAGs.CARD);
							String price = transaction.getString(TAGs.PRICE);
							String description = transaction.getString(TAGs.DESCRIPTION);
							
							transactionList.add(new Transaction(date, price, card, description));
						}
						if (transactionList.isEmpty()) {
							message.setVisibility(View.VISIBLE);
							message.setText("تراکنشی ثبت نشده است");
						} else {
							message.setVisibility(View.GONE);
						}
						transactionAdapter.notifyDataSetChanged();
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
		
		getTransactions(getIntent().getStringExtra(TAGs.UID));
		
		analyticsReport();
	}
	
	private void getTransactions(String id) {
		showDialog();
		try {
			String URL = getResources().getString(R.string.url_api, HOST) + "transactions";
			JSONObject params = new JSONObject();
			params.put(TAGs.UID, id);
			final String mRequestBody = params.toString();
			
			StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, listener, errorListener) {
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
			// add retry policy to prevent send request twice
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
