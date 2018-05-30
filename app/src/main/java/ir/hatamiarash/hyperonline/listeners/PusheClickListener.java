/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class PusheClickListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			switch (intent.getAction()) {
				case "co.ronash.pushe.NOTIF_CLICKED":
					Timber.tag("Pushe").i("Broadcast CLICKED");
					break;
				case "co.ronash.pushe.NOTIF_DISMISSED":
					Timber.tag("Pushe").i("Broadcast DISMISSED");
					break;
				case "co.ronash.pushe.NOTIF_BTN_CLICKED":
					String btnId = intent.getStringExtra("pushe_notif_btn_id");
					Timber.tag("Pushe").i("BroadcastBTN_CLICKED. BtnId = %s", btnId);
					break;
			}
		} catch (NullPointerException ignore) {
		}
	}
}