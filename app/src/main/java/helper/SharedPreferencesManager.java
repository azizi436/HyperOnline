/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
	private static final String PREF_NAME = "HOPhone";
	private static final String KEY_UNREAD_MESSAGE = "unreadMSG";
	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	Context _context;
	int PRIVATE_MODE = 0;
	
	public SharedPreferencesManager(Context context) {
		_context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
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