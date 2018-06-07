/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import timber.log.Timber;

public class ConfirmManager {
	private static String TAG = ConfirmManager.class.getSimpleName();
	private static final String PREF_NAME = "HOPhone";
	private static final String KEY_PHONE = "isPhoneConfirm";
	private static final String KEY_INFO = "isInfoConfirm";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private Analytics analytics;
	
	public ConfirmManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
		
		HyperOnline application = HyperOnline.getInstance();
		analytics = application.getAnalytics();
	}
	
	public void setPhoneConfirm(boolean isConfirm) {
		editor.putBoolean(KEY_PHONE, isConfirm);
		editor.commit();
		Timber.tag(TAG).i("User Phone Confirmation Modified");
		analytics.reportEvent("Confirmation - Change Phone");
	}
	
	public void setInfoConfirm(boolean isConfirm) {
		editor.putBoolean(KEY_INFO, isConfirm);
		editor.commit();
		Timber.tag(TAG).i("User Info Confirmation Modified");
		analytics.reportEvent("Confirmation - Change Info");
	}
	
	public boolean isPhoneConfirm() {
		analytics.reportEvent("Confirmation - Check Phone");
		return !pref.getBoolean(KEY_PHONE, false);
	}
	
	public boolean isInfoConfirm() {
		analytics.reportEvent("Confirmation - Check Info");
		return !pref.getBoolean(KEY_INFO, false);
	}
}
