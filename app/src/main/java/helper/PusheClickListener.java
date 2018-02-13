/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PusheClickListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("co.ronash.pushe.NOTIF_CLICKED")) {
			Log.i("Pushe", "Broadcast CLICKED");
		} else if (intent.getAction().equals("co.ronash.pushe.NOTIF_DISMISSED")) {
			Log.i("Pushe", "Broadcast DISMISSED");
		} else if (intent.getAction().equals("co.ronash.pushe.NOTIF_BTN_CLICKED")) {
			String btnId = intent.getStringExtra("pushe_notif_btn_id");
			Log.i("Pushe", "BroadcastBTN_CLICKED. BtnId = " + btnId);
		}
	}
}