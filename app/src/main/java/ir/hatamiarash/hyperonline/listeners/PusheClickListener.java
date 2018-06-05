/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import timber.log.Timber;

public class PusheClickListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		HyperOnline application = HyperOnline.getInstance();
		Analytics analytics = application.getAnalytics();
		try {
			switch (intent.getAction()) {
				case "co.ronash.pushe.NOTIF_CLICKED":
					Timber.tag("Pushe").i("Notification Click");
					analytics.reportEvent("Pushe - Notification Click");
					break;
				case "co.ronash.pushe.NOTIF_DISMISSED":
					Timber.tag("Pushe").i("Notification Dismiss");
					analytics.reportEvent("Pushe - Notification Dismiss");
					break;
				case "co.ronash.pushe.NOTIF_BTN_CLICKED":
					String btnId = intent.getStringExtra("pushe_notif_btn_id");
					Timber.tag("Pushe").i("Button Click. ID = %s", btnId);
					analytics.reportEvent("Pushe - Button Click");
					break;
			}
		} catch (NullPointerException ignore) {
		}
	}
}