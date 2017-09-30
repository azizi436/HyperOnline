/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;
import ir.hatamiarash.hyperonline.Activity_Main;

public class PusheJSONListener extends PusheListenerService {
    @Override
    public void onMessageReceived(final JSONObject message, JSONObject content) {
        if (message != null && message.length() > 0) {
            Log.i("Pushe", "Message: " + message.toString());
            try {
                JSONObject msg = message.getJSONObject("msg");
                Activity_Main.addMessage(
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