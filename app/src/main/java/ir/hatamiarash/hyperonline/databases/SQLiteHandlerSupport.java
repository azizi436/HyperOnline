/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ir.hatamiarash.hyperonline.HyperOnline;
import ir.hatamiarash.hyperonline.interfaces.Analytics;
import timber.log.Timber;

public class SQLiteHandlerSupport extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHandlerSupport.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "android_api";
	private static final String TABLE = "support";
	// Setup Table Columns names
	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_DATE = "date";
	
	private Analytics analytics;
	
	public SQLiteHandlerSupport(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		HyperOnline application = HyperOnline.getInstance();
		analytics = application.getAnalytics();
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
		Timber.tag(TAG).i("Database table created - onCreate");
		analytics.reportEvent("Database - Create Table - onCreate");
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
		Timber.tag(TAG).i("Database table created - Manual");
		analytics.reportEvent("Database - Create Table - Manual");
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
		analytics.reportEvent("Database - Add Message");
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
		analytics.reportEvent("Database - Get Message");
		return item;
	}
	
	public void deleteMessage(String date) {
		SQLiteDatabase db = this.getWritableDatabase();
		date = "'" + date + "'";
		db.delete(TABLE, KEY_DATE + "=" + date, null);
		db.close();
		analytics.reportEvent("Database - Remove Message");
	}
	
	public void deleteMessages() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		CreateTable();
		analytics.reportEvent("Database - Remove Messages");
	}
	
	public int getCount() {
		String countQuery = "SELECT  * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		analytics.reportEvent("Database - Get Messages Count");
		return rowCount;
	}
}