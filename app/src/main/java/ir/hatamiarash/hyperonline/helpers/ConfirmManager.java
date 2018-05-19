/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfirmManager {
	private static final String PREF_NAME = "HOPhone";
	private static final String KEY_PHONE = "isPhoneConfirm";
	private static final String KEY_INFO = "isInfoConfirm";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	Context _context;
	int PRIVATE_MODE = 0;
	
	public ConfirmManager(Context context) {
		_context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
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
//        return pref.getBoolean(KEY_PHONE, false);
		return true;
	}
	
	public boolean isInfoConfirm() {
		return pref.getBoolean(KEY_INFO, false);
	}
}
