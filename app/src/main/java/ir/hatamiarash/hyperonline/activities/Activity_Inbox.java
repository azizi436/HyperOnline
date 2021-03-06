/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.Context;
import android.os.Bundle;
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

import com.crashlytics.android.Crashlytics;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.adapters.MessageAdapter;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerSupport;
import ir.hatamiarash.hyperonline.helpers.FontHelper;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.interfaces.Refresh;
import ir.hatamiarash.hyperonline.models.Message;
import ir.hatamiarash.hyperonline.preferences.SharedPreferencesManager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Activity_Inbox extends AppCompatActivity implements Refresh {
	private static final String CLASS = Activity_Inbox.class.getSimpleName();
	
	SweetAlertDialog progressDialog;
	SQLiteHandlerSupport db_support;
	SharedPreferencesManager SPManager;
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.list)
	RecyclerView list;
	@BindView(R.id.message)
	TextView message;
	
	List<Message> messageList;
	MessageAdapter messageAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_simple);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		db_support = new SQLiteHandlerSupport(getApplicationContext());
		SPManager = new SharedPreferencesManager(getApplicationContext());
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
			((TextView) v.findViewById(R.id.title_text)).setText(FontHelper.getSpannedString(getApplicationContext(), "صندوق پیام"));
			getSupportActionBar().setCustomView(v, p);
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		} catch (NullPointerException e) {
			Crashlytics.logException(e);
		}
		
		messageList = new ArrayList<>();
		messageAdapter = new MessageAdapter(this, messageList);
		messageAdapter.setHasStableIds(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		list.setLayoutManager(linearLayoutManager);
		list.setItemAnimator(new DefaultItemAnimator());
		list.setAdapter(messageAdapter);
		
		loadMessages();
		
		analyticsReport();
	}
	
	private void loadMessages() {
		showDialog();
		try {
			List<String> List = db_support.GetMessages();
			for (int i = 0; i < (List.size() / 3); i++) {
				String title = List.get(i * 3);
				String body = List.get(i * 3 + 1);
				String date = List.get(i * 3 + 2);
				messageList.add(new Message(
						title,
						body,
						date
				));
				Collections.reverse(messageList);
				messageAdapter.notifyDataSetChanged();
			}
			SPManager.setUnreadMessage(false);
			hideDialog();
			if (messageList.isEmpty()) {
				message.setVisibility(View.VISIBLE);
				message.setText("صندوق پیام خالی است");
			} else {
				message.setVisibility(View.GONE);
			}
		} catch (Exception e) {
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
	public void refresh() {
		messageList.clear();
		messageAdapter.notifyDataSetChanged();
		loadMessages();
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