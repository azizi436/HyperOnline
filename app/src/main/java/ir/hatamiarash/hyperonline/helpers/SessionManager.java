/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class SessionManager {
	private static final String PREF_NAME = "AndroidHiveLogin";
	private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
	private static String TAG = SessionManager.class.getSimpleName();
	private SharedPreferences pref;
	private Editor editor;
	
	public SessionManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
	}
	
	public void setLogin(boolean isLoggedIn) {
		editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
		// commit changes
		editor.commit();
		Timber.tag(TAG).i("User login session modified!");
	}
	
	public boolean isLoggedIn() {
		return pref.getBoolean(KEY_IS_LOGGEDIN, false);
	}
}