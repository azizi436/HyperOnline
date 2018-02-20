/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package models;

public class Message {
	public String date;
	public String title;
	public String body;
	
	public Message(String title, String body, String date) {
		this.title = title;
		this.body = body;
		this.date = date;
	}
}