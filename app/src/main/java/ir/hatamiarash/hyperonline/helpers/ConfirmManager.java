/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

public class ConfirmManager {
	private static final String PREF_NAME = "HOPhone";
	private static final String KEY_PHONE = "isPhoneConfirm";
	private static final String KEY_INFO = "isInfoConfirm";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	
	public ConfirmManager(@NotNull Context context) {
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();
	}
	
	public void setPhoneConfirm(boolean isConfirm) {
		editor.putBoolean(KEY_PHONE, isConfirm);
		editor.commit();
	}
	
	public void setInfoConfirm(boolean isConfirm) {
		editor.putBoolean(KEY_INFO, isConfirm);
		editor.commit();
	}
	
	public boolean isPhoneConfirm() {
		return !pref.getBoolean(KEY_PHONE, false);
	}
	
	public boolean isInfoConfirm() {
		return !pref.getBoolean(KEY_INFO, false);
	}
}
