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

import timber.log.Timber;

public class SQLiteHandlerMain extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHandlerMain.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;             // Database Version
	private static final String DATABASE_NAME = "android_api"; // Database Name
	private static final String TABLE = "main";                // Login table name
	// Setup Table Columns names
	private static final String KEY_SEND_PRICE = "send_price";
	
	public SQLiteHandlerMain(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// create tables on call
	@Override
	public void onCreate(SQLiteDatabase db) {
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE + "("
				+ KEY_SEND_PRICE + " TEXT"
				+ ")";
		db.execSQL(Query);
		Timber.tag(TAG).i("Database table created - onCreate");
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
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE + "("
				+ KEY_SEND_PRICE + " TEXT"
				+ ")";
		db.execSQL(Query);
		Query = "INSERT OR REPLACE INTO " + TABLE + "("
				+ KEY_SEND_PRICE
				+ ") VALUES("
				+ "5000"
				+ ")";
		db.execSQL(Query);
		db.close();
		Timber.tag(TAG).i("Database table created - Manual");
	}
	
	public void addItem(String send_price) {
		SQLiteDatabase db = this.getWritableDatabase();
		send_price = "'" + send_price + "'";
		String query = "UPDATE " + TABLE + " SET " + KEY_SEND_PRICE + "=" + send_price;
		db.execSQL(query);
		db.close();
	}
	
	// get user details from database and send them
	public List<String> getItemsDetails() {
		List<String> item = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			item.add(cursor.getString(cursor.getColumnIndex(KEY_SEND_PRICE)));
		}
		cursor.close();
		db.close();
		return item;
	}
}