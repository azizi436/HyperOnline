/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHandlerSupport extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "android_api";
	private static final String TABLE = "support";
	// Setup Table Columns names
	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_DATE = "date";
	
	public SQLiteHandlerSupport(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// create table on call
	@Override
	public void onCreate(SQLiteDatabase db) {
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE + "("
				+ KEY_TITLE + " TEXT, "
				+ KEY_BODY + " TEXT, "
				+ KEY_DATE + " TEXT "
				+ ")";
		db.execSQL(Query);
	}
	
	// drop and recreate table
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
	}
	
	// create table if onCreate can't do that
	public void CreateTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE + "("
				+ KEY_TITLE + " TEXT, "
				+ KEY_BODY + " TEXT, "
				+ KEY_DATE + " TEXT "
				+ ")";
		db.execSQL(Query);
		db.close();
	}
	
	public void AddMessage(String title, String body, String date) {
		SQLiteDatabase db = this.getWritableDatabase();
		title = "'" + title + "'";
		body = "'" + body + "'";
		date = "'" + date + "'";
		String Query = "INSERT OR REPLACE INTO " + TABLE + "("
				+ KEY_TITLE + ", "
				+ KEY_BODY + ", "
				+ KEY_DATE
				+ ") VALUES("
				+ title + ", "
				+ body + ", "
				+ date
				+ ")";
		db.execSQL(Query);
		db.close();
	}
	
	public List<String> GetMessages() {
		List<String> item = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				item.add(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_BODY)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return item;
	}
	
	public void deleteMessage(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
		date = "'" + date + "'";
		db.delete(TABLE, KEY_DATE + "=" + date, null);
		db.close();
	}
	
	public void deleteMessages() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		CreateTable();
	}
	
	public int getCount() {
		String countQuery = "SELECT  * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		return rowCount;
	}
}