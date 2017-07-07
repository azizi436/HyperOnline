/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
    private static final String KEY_PRICE_EXTEND = "price_extend";
    private static final String KEY_COUNT = "count";
    private static final String KEY_INFO = "info";
    private static final String KEY_SELLER_ID = "seller_id";
    private static final String KEY_SELLER = "seller";
    private static final String KEY_OFF = "seller_name";
    private static final String KEY_TYPE = "type";
    
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
                + KEY_PRICE_EXTEND + " TEXT,"
                + KEY_INFO + " TEXT,"
                + KEY_SELLER_ID + " TEXT,"
                + KEY_SELLER + " TEXT,"
                + KEY_OFF + " TEXT,"
                + KEY_COUNT + " TEXT,"
                + KEY_TYPE + " TEXT"
                + ")";
        db.execSQL(CREATE_CARD_TABLE);
        Log.d(TAG, "Database table created - onCreate");
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
                + KEY_PRICE_EXTEND + " TEXT,"
                + KEY_INFO + " TEXT,"
                + KEY_SELLER_ID + " TEXT,"
                + KEY_SELLER + " TEXT,"
                + KEY_OFF + " TEXT,"
                + KEY_COUNT + " TEXT,"
                + KEY_TYPE + " TEXT"
                + ")";
        db.execSQL(CREATE_CARD_TABLE);
        db.close();
        Log.d(TAG, "Database table created - Manual");
    }
    
    // add item data to database
    public void addItem(String uid, String name, String price, String info, String seller_id, String seller, String off, String count, String extend, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        name = "'" + name + "'";
        price = "'" + price + "'";
        count = "'" + count + "'";
        uid = "'" + uid + "'";
        info = "'" + info + "'";
        seller_id = "'" + seller_id + "'";
        seller = "'" + seller + "'";
        off = "'" + off + "'";
        extend = "'" + extend + "'";
        type = "'" + type + "'";
        String query = "INSERT OR REPLACE INTO " + TABLE + "("
                + KEY_UID + ", "
                + KEY_NAME + ", "
                + KEY_PRICE + ", "
                + KEY_PRICE_EXTEND + ", "
                + KEY_INFO + ", "
                + KEY_SELLER_ID + ", "
                + KEY_SELLER + ", "
                + KEY_OFF + ", "
                + KEY_COUNT + ", "
                + KEY_TYPE
                + ") VALUES("
                + uid + ", "
                + name + ", "
                + price + ", "
                + extend + ", "
                + info + ", "
                + seller_id + ", "
                + seller + ", "
                + off + ", "
                + count + ", "
                + type
                + ")";
        db.execSQL(query);
        db.close();
        Log.d(TAG, name + " inserted into database");
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
        Log.d(TAG, uid + " updated : " + count + " " + price + " " + off);
    }
    
    // get item's detail from database and send them
    public List<String> getItemDetails() {
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
                item.add(cursor.getString(cursor.getColumnIndex(KEY_PRICE_EXTEND)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_INFO)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_SELLER_ID)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_SELLER)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_OFF)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_COUNT)));
                item.add(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching item from Sqlite: " + item.toString());
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
            Log.d(TAG, "Item Found : " + name);
            cursor.close();
            db.close();
            return true;
        }
        Log.d(TAG, "Item Not Found : " + name);
        cursor.close();
        db.close();
        return false;
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
        Log.d(TAG, "Total Price : " + String.valueOf(total));
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
                total += Integer.parseInt(cursor.getString(8));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Total Off : " + String.valueOf(total));
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
                total += Integer.parseInt(cursor.getString(9));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Total Count : " + String.valueOf(total));
        return total;
    }
    
    // delete one item from database
    public void deleteItem(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        uid = "'" + uid + "'";
        db.delete(TABLE, KEY_UID + "=" + uid, null);
        db.close();
        Log.d(TAG, "Deleted " + uid + " from sqlite");
    }
    
    // delete all items from database
    public void deleteItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
        CreateTable();
        Log.d(TAG, "Deleted all item info from sqlite");
    }
}