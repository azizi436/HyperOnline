/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import helper.Helper;
import ir.hatamiarash.utils.TAGs;

public class Activity_CheckTransaction extends Activity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        Intent intent = getIntent();
        Uri uri = intent.getData();
        try {
            String value = uri.getQueryParameter("id");
            Log.w("URLScheme", value);
            Helper.MakeToast(getApplicationContext(), value, TAGs.SUCCESS, 1);
        } catch (NullPointerException ignored) {
        }
    }
}
