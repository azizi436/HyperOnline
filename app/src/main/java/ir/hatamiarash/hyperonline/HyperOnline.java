/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.amplitude.api.Amplitude;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import org.jetbrains.annotations.Contract;

import co.ronash.pushe.Pushe;
import io.fabric.sdk.android.Fabric;
import ir.hatamiarash.hyperonline.analytics.ApplicationAnalytics;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.LruBitmapCache;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class HyperOnline extends Application {
	private static final String CLASS = HyperOnline.class.getSimpleName();
	public static String HOST;
	
	private static HyperOnline mInstance;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	
	private Analytics analytics;
	
	static {
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}
	
	@Contract(pure = true)
	public static synchronized HyperOnline getInstance() {
		if (mInstance == null)
			mInstance = new HyperOnline();
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		configureLibraries();
		setFont();
		HOST = getResources().getString(R.string.url_host);
	}
	
	private RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}
	
	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
		}
		return this.mImageLoader;
	}
	
	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? CLASS : tag);
		getRequestQueue().add(req);
	}
	
	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(CLASS);
		getRequestQueue().add(req);
	}
	
	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
	
	private void setFont() {
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/sans.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);
	}
	
	private void configureLibraries() {
		MultiDex.install(this);
		
		Pushe.initialize(getApplicationContext(), true);
		
		Fabric.with(this, new Crashlytics());
		
		new FlurryAgent.Builder()
				.withLogEnabled(true)
				.build(this, "4WKBBTSJTHP7P8RBTDTH");
		
		Amplitude.getInstance().initialize(this, "37d111e62e3ec73db8327c61d6215006")
				.enableForegroundTracking(this);
	}
	
	@NonNull
	public Analytics getAnalytics() {
		if (analytics == null) {
			analytics = new ApplicationAnalytics();
			analytics.init(this);
		}
		return analytics;
	}
}
