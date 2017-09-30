/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;

public class PusheJSONListener extends PusheListenerService {
    SQLiteHandlerSupport db_support;
    
    @Override
    public void onMessageReceived(final JSONObject message, JSONObject content) {
        if (message != null && message.length() > 0) {
            db_support = new SQLiteHandlerSupport(getApplicationContext());
            Log.i("Pushe", "Message: " + message.toString());
            try {
                JSONObject msg = message.getJSONObject("msg");
                db_support.AddMessage(
                        msg.getString("title"),
                        msg.getString("body"),
                        msg.getString("date")
                );
            } catch (JSONException e) {
                Log.e("Pushe", "Exception : ", e);
            }
        }
    }
}