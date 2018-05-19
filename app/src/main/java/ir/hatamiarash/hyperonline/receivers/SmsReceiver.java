/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.Map;

import ir.hatamiarash.hyperonline.interfaces.SmsListener;

public class SmsReceiver extends BroadcastReceiver {
	private SmsListener handler;
	
	public SmsReceiver() {
	}
	
	/* Constructor. Handler is the activity  *
	 * which will show the messages to user. */
	public SmsReceiver(SmsListener handler) {
		this.handler = handler;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			/* Retrieve the sms message chunks from the intent */
			SmsMessage[] rawSmsChunks;
			try {
				rawSmsChunks = Telephony.Sms.Intents.getMessagesFromIntent(intent);
			} catch (NullPointerException e) {
				Crashlytics.logException(e);
				return;
			}
			/* Gather all sms chunks for each sender separately */
			Map<String, StringBuilder> sendersMap = new HashMap<>();
			for (SmsMessage rawSmsChunk : rawSmsChunks) {
				if (rawSmsChunk != null) {
					String sender = rawSmsChunk.getDisplayOriginatingAddress();
					String smsChunk = rawSmsChunk.getDisplayMessageBody();
					StringBuilder smsBuilder;
					if (!sendersMap.containsKey(sender)) {
						/* For each new sender create a separate StringBuilder */
						smsBuilder = new StringBuilder();
						sendersMap.put(sender, smsBuilder);
					} else {
						/* Sender already in map. Retrieve the StringBuilder */
						smsBuilder = sendersMap.get(sender);
					}
					/* Add the sms chunk to the string builder */
					smsBuilder.append(smsChunk);
				}
			}
			/* Loop over every sms thread and concatenate the sms chunks to one piece */
			for (Map.Entry<String, StringBuilder> smsThread : sendersMap.entrySet()) {
				try {
					String sender = smsThread.getKey();
					StringBuilder smsBuilder = smsThread.getValue();
					String message = smsBuilder.toString();
					handler.handleSms(sender, message);
				} catch (Exception e) {
					Crashlytics.logException(e);
				}
			}
		}
	}
}