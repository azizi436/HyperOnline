/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ConfirmManager {
    private static final String PREF_NAME = "HOPhone";
    private static final String KEY_PHONE = "isPhoneConfirm";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    
    public ConfirmManager(Context context) {
        _context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    public void setPhoneConfirm(boolean isLoggedIn) {
        editor.putBoolean(KEY_PHONE, isLoggedIn);
        editor.commit();
        Log.e("PhoneConfirm", "User phone session modified!");
    }
    
    public boolean isPhoneConfirm() {
        return pref.getBoolean(KEY_PHONE, false);
    }
}
