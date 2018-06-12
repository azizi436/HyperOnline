/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.analytics;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.amplitude.api.Amplitude;
import com.bugsnag.android.BeforeNotify;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Error;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.AddToCartEvent;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.PurchaseEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.crashlytics.android.answers.SignUpEvent;
import com.crashlytics.android.answers.StartCheckoutEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.math.BigDecimal;
import java.util.Currency;

import co.ronash.pushe.Pushe;
import ir.hatamiarash.hyperonline.BuildConfig;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.databases.SQLiteHandler;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import ir.hatamiarash.hyperonline.preferences.SessionManager;
import ir.hatamiarash.hyperonline.utils.TAGs;
import timber.log.Timber;

public class ApplicationAnalytics implements Analytics {
	private Tracker mTracker;
	private MixpanelAPI mixpanelAPI;
	private String userId;
	private String userName;
	private String userPhone;
	private SessionManager session;
	private Context mContext;
	
	@Override
	public void init(@NonNull Context context) {
		mContext = context;
		HyperOnline application = HyperOnline.getInstance();
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		mTracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_TRACKER);
		mTracker.enableAdvertisingIdCollection(true);
		
		if (BuildConfig.DEBUG) {
			GoogleAnalytics.getInstance(context).setAppOptOut(true);
			Timber.tag("GoogleAnalytics").w("DEBUG BUILD: ANALYTICS IS DISABLED");
		}
		
		mixpanelAPI = application.getMixpanelAPI();
		session = new SessionManager(context);
		if (session.isLoggedIn()) {
			SQLiteHandler user = new SQLiteHandler(context);
			userId = user.getUserDetails().get(TAGs.UID);
			userName = user.getUserDetails().get(TAGs.NAME);
			userPhone = user.getUserDetails().get(TAGs.PHONE);
		}
	}
	
	@Override
	public void reportScreen(@NonNull String name) {
		mTracker.setScreenName(name);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		Timber.i("Report Screen : %s", name);
	}
	
	@Override
	public void reportAction(@NonNull String category, @NonNull String name) {
		mTracker.send(new HitBuilders.EventBuilder()
				.setCategory(category)
				.setAction(name)
				.build());
		Timber.i("Report Action : %s", name);
	}
	
	@Override
	public void reportEvent(@NonNull String event) {
		Answers.getInstance().logCustom(new CustomEvent(event));
		Amplitude.getInstance().logEvent(event);
		mixpanelAPI.track(event);
		Timber.i("Report Event : %s", event);
	}
	
	@Override
	public void reportCard(@NonNull String id, @NonNull String name, @NonNull String price) {
		Answers.getInstance().logAddToCart(new AddToCartEvent()
				.putItemPrice(BigDecimal.valueOf(Integer.valueOf(price) * 10))
				.putCurrency(Currency.getInstance("IRR"))
				.putItemName(name)
				.putItemId(id));
		Timber.i("Report Card : %s - %s - %s", id, name, price);
		reportEvent("Purchase - Add");
	}
	
	@Override
	public void reportPurchase(@NonNull String id, @NonNull String name, @NonNull String price, boolean status) {
		Answers.getInstance().logPurchase(new PurchaseEvent()
				.putItemPrice(BigDecimal.valueOf(Integer.valueOf(price) * 10))
				.putCurrency(Currency.getInstance("IRR"))
				.putItemName(name)
				.putItemId(id)
				.putSuccess(status));
		Timber.i("Report Purchase : %s - %s - %s - %b", id, name, price, status);
		reportEvent("Purchase - Final");
	}
	
	@Override
	public void reportStartCheckout(int count, int price) {
		Answers.getInstance().logStartCheckout(new StartCheckoutEvent()
				.putTotalPrice(BigDecimal.valueOf(price * 10))
				.putCurrency(Currency.getInstance("IRR"))
				.putItemCount(count));
		Timber.i("Report Checkout : %d - %d", price, count);
		reportEvent("Purchase - Checkout");
	}
	
	@Override
	public void reportSearch(@NonNull String query) {
		Answers.getInstance().logSearch(new SearchEvent()
				.putQuery("mobile analytics"));
		Timber.i("Report Search : %s", query);
		reportEvent("Search");
	}
	
	@Override
	public void reportLogin() {
		Answers.getInstance().logLogin(new LoginEvent()
				.putMethod("Digits")
				.putSuccess(true));
	}
	
	@Override
	public void reportRegister() {
		Answers.getInstance().logSignUp(new SignUpEvent()
				.putMethod("Digits")
				.putSuccess(true));
	}
	
	@Override
	public void reportException(Throwable exception) {
		Bugsnag.beforeNotify(new BeforeNotify() {
			@Override
			public boolean run(Error error) {
				if (session.isLoggedIn())
					error.setUser(userId, userPhone, userName);
				error.addToTab("phone", "api", Build.VERSION.SDK_INT);
				error.addToTab("phone", "pushe", Pushe.getPusheId(mContext));
				return false;
			}
		});
		Bugsnag.notify(exception);
		Crashlytics.logException(exception);
		reportEvent("Exception");
	}
}