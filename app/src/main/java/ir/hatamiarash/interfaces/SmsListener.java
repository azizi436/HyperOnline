/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.interfaces;

public interface SmsListener {
	void handleSms(String sender, String message);
}