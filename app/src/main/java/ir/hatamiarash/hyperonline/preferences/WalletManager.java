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

public class WalletManager {
	private static String TAG = ConfirmManager.class.getSimpleName();
	private static final String PREF_NAME = "HOWallet";
	private static final String KEY_FIRST = "isFirstUser";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private Analytics analytics;
	
	public WalletManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
		
		HyperOnline application = HyperOnline.getInstance();
		analytics = application.getAnalytics();
	}
	
	public void setWalletFirstUse(boolean isConfirm) {
		editor.putBoolean(KEY_FIRST, isConfirm);
		editor.commit();
		Timber.tag(TAG).i("Wallet FirstUse Modified");
		analytics.reportEvent("Wallet - Change FirstUse");
	}
	
	public boolean isWalletFirstUse() {
		analytics.reportEvent("Wallet - Check FirstUse");
		return pref.getBoolean(KEY_FIRST, true);
	}
}