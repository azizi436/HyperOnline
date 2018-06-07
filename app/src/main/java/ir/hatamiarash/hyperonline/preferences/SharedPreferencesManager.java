/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

public class SharedPreferencesManager {
	private static final String PREF_NAME = "HOPhone";
	private static final String KEY_UNREAD_MESSAGE = "unreadMSG";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	public SharedPreferencesManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
	}
	
	public void setUnreadMessage(boolean value) {
		editor.putBoolean(KEY_UNREAD_MESSAGE, value);
		editor.commit();
	}
	
	public boolean isUnreadMessage() {
		return pref.getBoolean(KEY_UNREAD_MESSAGE, false);
	}
}