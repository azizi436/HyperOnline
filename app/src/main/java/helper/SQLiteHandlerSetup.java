/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandlerSetup extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandlerSetup.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;             // Database Version
    private static final String DATABASE_NAME = "android_api"; // Database Name
    private static final String TABLE_SETUP = "setup";         // Login table name
    // Setup Table Columns names
    private static final String KEY_WEB = "web";
    private static final String KEY_ID = "id";

    public SQLiteHandlerSetup(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create tables on call
    @Override
    public void onCreate(SQLiteDatabase db) {
        String Query = "CREATE TABLE " + TABLE_SETUP + "("
                + KEY_ID + " INTEGER, "
                + KEY_WEB + " INTEGER "
                + ")";
        db.execSQL(Query);
        Log.d(TAG, "Database table created - onCreate");
    }

    // drop and recreate table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETUP);
        onCreate(db);
    }

    // create table if onCreate can't do that
    public void CreateTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETUP);
        String Query = "CREATE TABLE " + TABLE_SETUP + "("
                + KEY_ID + " INTEGER, "
                + KEY_WEB + " INTEGER "
                + ")";
        db.execSQL(Query);
        Query = "INSERT OR REPLACE INTO " + TABLE_SETUP + "("
                + KEY_ID + ", "
                + KEY_WEB
                + ") VALUES("
                + 0 + ", "
                + -1
                + ")";
        db.execSQL(Query);
        db.close();
        Log.d(TAG, "Database table created - Manual");
    }

    // add user data to database
    public void AddProperty(int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "UPDATE " + TABLE_SETUP + " SET " + KEY_WEB + "=" + value;
        db.execSQL(Query);
        db.close();
        Log.d(TAG, "Row Updated by val " + String.valueOf(value));
    }

    // get user details from database and send them
    public HashMap<String, String> GetProperties() {
        HashMap<String, String> user = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_SETUP;
        Cursor cursor = db.rawQuery(Query, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(KEY_ID, String.valueOf(cursor.getInt(0)));
            user.put(KEY_WEB, String.valueOf(cursor.getInt(1)));
        } else {
            user.put(KEY_WEB, "-1");
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching properties from database: " + user.toString());
        return user;
    }
}