/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;

public class PusheJSONListener extends PusheListenerService {
	SQLiteHandlerSupport db_support;
	SQLiteHandler db_user;
	SQLiteHandlerItem db_item;
	SessionManager session;
	ConfirmManager confirmManager;
	SharedPreferencesManager SPManager;
	
	@Override
	public void onMessageReceived(final JSONObject message, JSONObject content) {
		if (message != null && message.length() > 0) {
			db_support = new SQLiteHandlerSupport(getApplicationContext());
			db_user = new SQLiteHandler(getApplicationContext());
			db_item = new SQLiteHandlerItem(getApplicationContext());
			session = new SessionManager(getApplicationContext());
			confirmManager = new ConfirmManager(getApplicationContext());
			SPManager = new SharedPreferencesManager(getApplicationContext());
			Log.i("Pushe", "Message: " + message.toString());
			try {
				if (message.has("msg")) {
					JSONObject msg = message.getJSONObject("msg");
					db_support.AddMessage(
							msg.getString("title"),
							msg.getString("body"),
							msg.getString("date")
					);
					SPManager.setUnreadMessage(true);
				} else if (message.has("logout")) {
					JSONObject msg = message.getJSONObject("logout");
					if (msg.getBoolean("out")) {
						if (session.isLoggedIn()) {
							session.setLogin(false);
							confirmManager.setPhoneConfirm(false);
							confirmManager.setInfoConfirm(false);
							db_user.deleteUsers();
							db_item.deleteItems();
							db_support.deleteMessages();
						}
					}
				}
			} catch (JSONException e) {
				Log.e("Pushe", "Exception : ", e);
			}
		}
	}
}