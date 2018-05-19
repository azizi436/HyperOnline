/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.R;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static ir.hatamiarash.hyperonline.helpers.Helper.isAppAvailable;

public class Activity_Social extends AppCompatActivity {
	private static final String CLASS = Activity_Social.class.getSimpleName();
	
	HyperOnline application;
	Analytics analytics;
	
	@BindView(R.id.telegram)
	public ImageView telegram;
	@BindView(R.id.instagram)
	public ImageView instagram;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.social);
		
		ButterKnife.bind(this);
		application = (HyperOnline) getApplication();
		analytics = application.getAnalytics();
		
		telegram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String appName = "org.telegram.messenger";
				boolean isAppInstalled = isAppAvailable(getApplicationContext(), appName);
				if (isAppInstalled) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=online_hyper"));
					startActivity(intent);
				} else {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.me/online_hyper")));
				}
			}
		});
		
		instagram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Uri uri = Uri.parse("http://instagram.com/_u/online.hyper");
				Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
				likeIng.setPackage("com.instagram.android");
				try {
					startActivity(likeIng);
				} catch (ActivityNotFoundException e) {
					Crashlytics.logException(e);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/online.hyper")));
				}
			}
		});
		
		analyticsReport();
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
