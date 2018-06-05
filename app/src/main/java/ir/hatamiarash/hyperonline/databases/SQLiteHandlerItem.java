/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SQLiteHandlerItem extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHandlerItem.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;             // Database Version
	private static final String DATABASE_NAME = "android_api"; // Database Name
	private static final String TABLE = "card";           // Login table name
	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_UID = "uid";
	private static final String KEY_NAME = "name";
	private static final String KEY_PRICE = "price";
	private static final String KEY_COUNT = "count";
	private static final String KEY_INFO = "info";
	private static final String KEY_OFF = "off";
	private static final String KEY_COUNT_ORIGINAL = "ocount";
	
	public SQLiteHandlerItem(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// create table on call
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CARD_TABLE = "CREATE TABLE " + TABLE + "("
				+ KEY_ID + " INTEGER PRIMARY KEY,"
				+ KEY_UID + " TEXT,"
				+ KEY_NAME + " TEXT,"
				+ KEY_PRICE + " TEXT,"
				+ KEY_INFO + " TEXT,"
				+ KEY_OFF + " TEXT,"
				+ KEY_COUNT + " TEXT,"
				+ KEY_COUNT_ORIGINAL + " TEXT"
				+ ")";
		db.execSQL(CREATE_CARD_TABLE);
		Timber.tag(TAG).i("Database table created - onCreate");
	}
	
	// drop and recreate table
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
	}
	
	// create table manually
	public void CreateTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		String CREATE_CARD_TABLE = "CREATE TABLE " + TABLE + "("
				+ KEY_ID + " INTEGER PRIMARY KEY,"
				+ KEY_UID + " TEXT,"
				+ KEY_NAME + " TEXT,"
				+ KEY_PRICE + " TEXT,"
				+ KEY_INFO + " TEXT,"
				+ KEY_OFF + " TEXT,"
				+ KEY_COUNT + " TEXT,"
				+ KEY_COUNT_ORIGINAL + " TEXT"
				+ ")";
		db.execSQL(CREATE_CARD_TABLE);
		db.close();
		Timber.tag(TAG).i("Database table created - Manual");
	}
	
	// add item data to database
	public void addItem(String uid, String name, String price, String info, String off, String count, String o_count) {
		SQLiteDatabase db = this.getWritableDatabase();
		name = "'" + name + "'";
		price = "'" + price + "'";
		count = "'" + count + "'";
		o_count = "'" + o_count + "'";
		uid = "'" + uid + "'";
		info = "'" + info + "'";
		off = "'" + off + "'";
		String query = "INSERT OR REPLACE INTO " + TABLE + "("
				+ KEY_UID + ", "
				+ KEY_NAME + ", "
				+ KEY_PRICE + ", "
				+ KEY_INFO + ", "
				+ KEY_OFF + ", "
				+ KEY_COUNT + ", "
				+ KEY_COUNT_ORIGINAL
				+ ") VALUES("
				+ uid + ", "
				+ name + ", "
				+ price + ", "
				+ info + ", "
				+ off + ", "
				+ count + ", "
				+ o_count
				+ ")";
		db.execSQL(query);
		db.close();
		Timber.tag(TAG).d("%s inserted into database", name);
	}
	
	// update item's details
	public void updateItem(String uid, String count, String price, String off) {
		SQLiteDatabase db = this.getWritableDatabase();
		uid = "'" + uid + "'";
		count = "'" + count + "'";
		price = "'" + price + "'";
		off = "'" + off + "'";
		String query = "UPDATE " + TABLE + " SET "
				+ KEY_COUNT + "=" + count + ", "
				+ KEY_PRICE + "=" + price + ", "
				+ KEY_OFF + "=" + off
				+ " WHERE " + KEY_UID + "=" + uid;
		db.execSQL(query);
		db.close();
		Timber.tag(TAG).d("%s updated : %s %s %s", uid, count, price, off);
	}
	
	public void updateCount(String uid, String count, String price, String off) {
		SQLiteDatabase db = this.getWritableDatabase();
		uid = "'" + uid + "'";
		count = "'" + count + "'";
		price = "'" + price + "'";
		off = "'" + off + "'";
		String query = "UPDATE " + TABLE + " SET "
				+ KEY_COUNT + "=" + count + ", "
				+ KEY_PRICE + "=" + price + ", "
				+ KEY_OFF + "=" + off
				+ " WHERE " + KEY_UID + "=" + uid;
		db.execSQL(query);
		db.close();
		Timber.tag(TAG).d("%s updated : %s %s %s", uid, count, price, off);
	}
	
	// get item's detail from database and send them
	public List<String> getItemsDetails() {
		List<String> item = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				item.add(cursor.getString(cursor.getColumnIndex(KEY_ID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_UID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_PRICE)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_INFO)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_OFF)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT_ORIGINAL)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return item;
	}
	
	public List<String> getItemDetails(String uid) {
		uid = "'" + uid + "'";
		List<String> item = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + KEY_UID + "=" + uid;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				item.add(cursor.getString(cursor.getColumnIndex(KEY_ID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_UID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_PRICE)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_INFO)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_OFF)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT_ORIGINAL)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		Timber.tag(TAG).d("Fetching item from Sqlite : %s" , item.toString());
		return item;
	}
	
	// get item's detail from database and send them
	public Boolean isExists(String name) {
		name = "'" + name + "'";
		String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + KEY_NAME + "=" + name;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			cursor.close();
			db.close();
			return true;
		}
		cursor.close();
		db.close();
		return false;
	}
	
	public Boolean isExistsID(String uid) {
		uid = "'" + uid + "'";
		String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + KEY_UID + "=" + uid;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			cursor.close();
			db.close();
			return true;
		}
		cursor.close();
		db.close();
		return false;
	}
	
	public int getCount(String uid) {
		uid = "'" + uid + "'";
		String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + KEY_UID + "=" + uid;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		int count = 0;
		if (cursor.getCount() > 0)
			count = cursor.getInt(cursor.getColumnIndex(KEY_COUNT));
		cursor.close();
		db.close();
		return count;
	}
	
	// calculate total price of items from database
	public int TotalPrice() {
		int total = 0;
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				total += Integer.parseInt(cursor.getString(3));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return total;
	}
	
	public int TotalOff() {
		int total = 0;
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				total += Integer.parseInt(cursor.getString(5));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return total;
	}
	
	// count all items from database
	public int getRowCount() {
		String countQuery = "SELECT  * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int rowCount = cursor.getCount();
		db.close();
		cursor.close();
		return rowCount;
	}
	
	public int getItemCount() {
		int total = 0;
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				total += Integer.parseInt(cursor.getString(6));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		return total;
	}
	
	// delete one item from database
	public void deleteItem(String uid) {
		SQLiteDatabase db = this.getWritableDatabase();
		uid = "'" + uid + "'";
		db.delete(TABLE, KEY_UID + "=" + uid, null);
		db.close();
	}
	
	// delete all items from database
	public void deleteItems() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		CreateTable();
	}
	
	// delete all items from database then return for analytics reports
	public List<String> deleteItems2(){
		List<String> item = new ArrayList<>();
		String selectQuery = "SELECT * FROM " + TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				item.add(cursor.getString(cursor.getColumnIndex(KEY_ID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_UID)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_PRICE)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_INFO)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_OFF)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT)));
				item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT_ORIGINAL)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		db.close();
		
		db = this.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		CreateTable();
		return item;
	}
}