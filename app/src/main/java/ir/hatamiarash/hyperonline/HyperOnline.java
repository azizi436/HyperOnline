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
import com.bugsnag.android.Bugsnag;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.jetbrains.annotations.Contract;

import co.ronash.pushe.Pushe;
import io.fabric.sdk.android.Fabric;
import ir.hatamiarash.hyperonline.analytics.ApplicationAnalytics;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.libraries.LruBitmapCache;
import ir.hatamiarash.hyperonline.logs.CrashlyticsTree;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class HyperOnline extends Application {
	private static final String CLASS = HyperOnline.class.getSimpleName();
	public static String HOST;
	
	private static HyperOnline mInstance;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private MixpanelAPI mixpanelAPI;
	
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
		configureLibraries_Analytics();
		configureLibraries_Log();
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
	
	public <T> void addToRequestQueue(String tag, Request<T> req) {
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
	
	private void configureLibraries_Analytics() {
		MultiDex.install(this);
		
		Pushe.initialize(getApplicationContext(), true);
		
		Fabric.with(this, new Crashlytics());
		
		new FlurryAgent.Builder()
				.withLogEnabled(true)
				.build(this, BuildConfig.FLURRY_API);
		
		Amplitude.getInstance().initialize(this, BuildConfig.AMPLITUSE_API)
				.enableForegroundTracking(this);
		
		Bugsnag.init(this);
		Bugsnag.setAutoCaptureSessions(true);
		Bugsnag.setAppVersion(BuildConfig.VERSION_NAME);
		
		mixpanelAPI = MixpanelAPI.getInstance(this, BuildConfig.MIXPANEL_API);
	}
	
	private void configureLibraries_Log() {
		FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
				.showThreadInfo(true)
				.methodCount(3)
				.methodOffset(5)
				.tag("ARASH_LOG")
				.build();
		
		Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
			@Override
			public boolean isLoggable(int priority, String tag) {
				return BuildConfig.DEBUG;
			}
		});
		
		if (BuildConfig.DEBUG)
			Timber.plant(new Timber.DebugTree() {
				@Override
				protected void log(int priority, String tag, @NonNull String message, Throwable t) {
					Logger.log(priority, tag, message, t);
				}
			});
		else
			Timber.plant(new CrashlyticsTree());
	}
	
	@NonNull
	public Analytics getAnalytics() {
		if (analytics == null) {
			analytics = new ApplicationAnalytics();
			analytics.init(this);
		}
		return analytics;
	}
	
	public MixpanelAPI getMixpanelAPI() {
		return mixpanelAPI;
	}
}
