/*
 * Copyright (c) 2018 - All Rights Reserved - Arash Hatami
 */

package ir.hatamiarash.hyperonline.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHandler.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;             // Database Version
	private static final String DATABASE_NAME = "android_api"; // Database Name
	private static final String TABLE_LOGIN = "login";         // Login table name
	// Login Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_UID = "uid";
	private static final String KEY_NAME = "name";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_ADDRESS = "address";
	private static final String KEY_PHONE = "phone";
	private static final String KEY_COUNTRY = "country";
	private static final String KEY_PROVINCE = "province";
	private static final String KEY_CITY = "city";
	
	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// create tables on call
	@Override
	public void onCreate(SQLiteDatabase db) {
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY, "
				+ KEY_UID + " TEXT, "
				+ KEY_NAME + " TEXT, "
				+ KEY_ADDRESS + " TEXT, "
				+ KEY_PHONE + " TEXT, "
				+ KEY_EMAIL + " TEXT UNIQUE, "
				+ KEY_COUNTRY + " TEXT, "
				+ KEY_PROVINCE + " TEXT,"
				+ KEY_CITY + " TEXT"
				+ ")";
		db.execSQL(Query);
		Log.d(TAG, "Database Table Created - onCreate");
	}
	
	// drop and recreate table
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
		onCreate(db);
	}
	
	// create table if onCreate can't do that
	public void CreateTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
		String Query = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "("
				+ KEY_ID + " INTEGER PRIMARY KEY, "
				+ KEY_UID + " TEXT, "
				+ KEY_NAME + " TEXT, "
				+ KEY_ADDRESS + " TEXT, "
				+ KEY_PHONE + " TEXT, "
				+ KEY_EMAIL + " TEXT UNIQUE, "
				+ KEY_COUNTRY + " TEXT, "
				+ KEY_PROVINCE + " TEXT,"
				+ KEY_CITY + " TEXT"
				+ ")";
		db.execSQL(Query);
		db.close();
		Log.d(TAG, "Database Table Created - Manual");
	}
	
	// add user data to database
	public void addUser(String name, String email, String address, String phone, String uid, String country, String province, String city) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);         // Name
		values.put(KEY_EMAIL, email);       // Email
		values.put(KEY_UID, uid);           // Unique ID
		values.put(KEY_ADDRESS, address);   // Address
		values.put(KEY_PHONE, phone);       // Phone
		values.put(KEY_COUNTRY, country);   // Country
		values.put(KEY_PROVINCE, province); // Province
		values.put(KEY_CITY, city);         // City
		// Inserting Row
		long id = db.insert(TABLE_LOGIN, null, values);
		//db.execSQL(query);
		db.close();
		Log.d(TAG, "New User Inserted Into Database : " + name);
	}
	
	// get user details from database and send them
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<>();
		String Query = "SELECT * FROM " + TABLE_LOGIN;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(Query, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("uid", cursor.getString(1));
			user.put("name", cursor.getString(2));
			user.put("address", cursor.getString(3));
			user.put("phone", cursor.getString(4));
			user.put("email", cursor.getString(5));
			user.put("country", cursor.getString(6));
			user.put("province", cursor.getString(7));
			user.put("city", cursor.getString(8));
		}
		cursor.close();
		db.close();
		Log.d(TAG, "User Fetched From Database : " + user.toString());
		return user;
	}
	
	public void updateUser(String uid, String name, String address) {
		SQLiteDatabase db = this.getWritableDatabase();
		name = "'" + name + "'";
		address = "'" + address + "'";
		uid = "'" + uid + "'";
		String Query = "UPDATE " + TABLE_LOGIN + " SET "
				+ KEY_NAME + "=" + name + ", "
				+ KEY_ADDRESS + "=" + address
				+ " WHERE " + KEY_UID + "=" + uid;
		db.execSQL(Query);
		db.close();
		Log.d(TAG, "User's Table Row Updated");
	}
	
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_LOGIN, null, null);
		db.close();
		Log.d(TAG, "User Deleted !");
	}
}