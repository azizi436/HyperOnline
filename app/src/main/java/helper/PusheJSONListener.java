/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;

public class PusheJSONListener extends PusheListenerService {
    @Override
    public void onMessageReceived(final JSONObject message, JSONObject content) {
        if (message != null && message.length() > 0) {
            Log.i("Pushe", "Custom json Message: " + message.toString());
            try {
                String s1 = message.getString("key1");
            } catch (JSONException e) {
                Log.e("Pushe", "Exception : ", e);
            }
        }
    }
}