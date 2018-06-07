/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.jetbrains.annotations.NotNull;

import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import timber.log.Timber;

public class SessionManager {
	private static String TAG = SessionManager.class.getSimpleName();
	private static final String PREF_NAME = "AndroidHiveLogin";
	private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
	private SharedPreferences pref;
	private Editor editor;
	private Analytics analytics;
	
	public SessionManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
		
		HyperOnline application = HyperOnline.getInstance();
		analytics = application.getAnalytics();
	}
	
	public void setLogin(boolean isLoggedIn) {
		editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
		// commit changes
		editor.commit();
		Timber.tag(TAG).i("User Login Session Modified");
		analytics.reportEvent("Session - Change Login");
	}
	
	public boolean isLoggedIn() {
		analytics.reportEvent("Session - Check Login");
		return pref.getBoolean(KEY_IS_LOGGEDIN, false);
	}
}