/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.listeners;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;
import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.databases.SQLiteHandler;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerItem;
import ir.hatamiarash.hyperonline.databases.SQLiteHandlerSupport;
import ir.hatamiarash.hyperonline.helpers.ConfirmManager;
import ir.hatamiarash.hyperonline.helpers.SessionManager;
import ir.hatamiarash.hyperonline.helpers.SharedPreferencesManager;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import timber.log.Timber;

public class PusheJSONListener extends PusheListenerService {
	SQLiteHandlerSupport db_support;
	SQLiteHandler db_user;
	SQLiteHandlerItem db_item;
	SessionManager session;
	ConfirmManager confirmManager;
	SharedPreferencesManager SPManager;
	
	@Override
	public void onMessageReceived(JSONObject message, JSONObject content) {
		HyperOnline application = HyperOnline.getInstance();
		Analytics analytics = application.getAnalytics();
		Timber.tag("Pushe").d("incoming : %s", message);
		if (message != null && message.length() > 0) {
			db_support = new SQLiteHandlerSupport(getApplicationContext());
			db_user = new SQLiteHandler(getApplicationContext());
			db_item = new SQLiteHandlerItem(getApplicationContext());
			session = new SessionManager(getApplicationContext());
			confirmManager = new ConfirmManager(getApplicationContext());
			SPManager = new SharedPreferencesManager(getApplicationContext());
			try {
				if (message.has("msg")) {
					analytics.reportEvent("Pushe - Normal Message");
					JSONObject msg = message.getJSONObject("msg");
					db_support.AddMessage(
							msg.getString("title"),
							msg.getString("body"),
							msg.getString("date")
					);
					SPManager.setUnreadMessage(true);
				} else if (message.has("logout")) {
					analytics.reportEvent("Pushe - Logout Message");
					JSONObject msg = message.getJSONObject("logout");
					if (msg.getBoolean("out")) {
						if (session.isLoggedIn()) {
							session.setLogin(false);
							confirmManager.setPhoneConfirm(false);
							confirmManager.setInfoConfirm(false);
							db_user.deleteUsers();
							db_item.deleteItems();
							db_support.deleteMessages();
							analytics.reportEvent("User - Logout");
						}
					}
				}
			} catch (JSONException e) {
				Crashlytics.logException(e);
			}
		}
	}
}