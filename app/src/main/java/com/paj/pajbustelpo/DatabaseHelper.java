package com.paj.pajbustelpo;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pajtelpo.db";
    public static final String TABLE_NAME = "location_table";
    public static final String COL_1 = "id";
    public static final String COL_2 = "latitude";
    public static final String COL_3 = "longitude";
    public static final String COL_4 = "speed";
    public static final String COL_5 = "bearing";
    public static final String COL_6 = "datetime";
    public static final String COL_7 = "accuracy";
    public static final String COL_8 = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 16);
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
//        db.close();
        return res;
    }

    public UserInfo checkUserInfo(String mykad_uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM user_info_table WHERE mykad_uid = '" +mykad_uid+ "' LIMIT 1", null);
        UserInfo userInfo = new UserInfo();
        //if no uuid directly return empty class
        if(res.getCount() == 0) return userInfo;
        while (res.moveToNext()){
            userInfo.setUuid(res.getString(1));
            userInfo.setUsername(res.getString(2));
            userInfo.setMykad_uid(res.getString(3));
            userInfo.setQrcode_uid(res.getString(4));
            userInfo.setActive(res.getString(5));
            userInfo.setExpired(res.getString(6));
        }
//        db.close();
        return userInfo;
    }

    public JSONObject getUnsendLocation() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE status = 0 ORDER BY id ASC LIMIT 15", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONObject();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("id", res.getString(0));
                json.put("latitude", res.getString(1));
                json.put("longitude", res.getString(2));
                json.put("speed", res.getString(3));
                json.put("bearing", res.getString(4));
                json.put("datetime", res.getString(5));
                json.put("accuracy", res.getString(6));
                jsonArray.put(json);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bus", "JTV6100");
            jsonObject.put("data", jsonArray);
//            db.close();
            return jsonObject;
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
//            db.close();
            return new JSONObject();
        }
    }

    public JSONObject getUnsendRidership(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM ridership_table WHERE status = 0 ORDER BY id ASC LIMIT 50", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONObject();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("uuid", res.getString(1));
                json.put("deviceTime", res.getString(2));
                json.put("tap_type", res.getString(3));
                json.put("latitude", res.getString(4));
                json.put("longitude", res.getString(5));
                jsonArray.put(json);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bus", "JTV6100");
            jsonObject.put("data", jsonArray);
//            db.close();
            return jsonObject;
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
//            db.close();
            return new JSONObject();
        }

    }

    public boolean updateLocationStatus(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        db.update(TABLE_NAME, contentValues, " ID = ?", new String[] {id});
//        db.close();
        return true;
    }

    @SuppressLint("DefaultLocale")
    public boolean insertLocationData(double latitude, double longitude, double speed, double bearing, String datetime, double accuracy){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, String.format("%.7f", latitude));
        contentValues.put(COL_3, String.format("%.7f", longitude));
        contentValues.put(COL_4, String.format("%.2f", speed));
        contentValues.put(COL_5, String.format("%.2f", bearing));
        contentValues.put(COL_6, datetime);
        contentValues.put(COL_7, String.format("%.2f", accuracy));
        contentValues.put(COL_8, 0);
        long result = db.insert(TABLE_NAME, null, contentValues);
//        db.close();
        return result != -1;
    }

    @SuppressLint("DefaultLocale")
    public boolean insertRidershipData(String uuid, String deviceTime, String tap_type, double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("deviceTime", deviceTime);
        contentValues.put("tap_type", tap_type);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        long result = db.insert("ridership_table", null, contentValues);
//        db.close();
        return result != -1;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //creating table location
        String qry = "CREATE TABLE location_table"+
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude TEXT, " +
                "longitude TEXT, " +
                "speed TEXT, " +
                "bearing TEXT, " +
                "datetime DATETIME, " +
                "accuracy TEXT, " +
                "status INTERGER)";
        db.execSQL(qry);

        //creating table ridership
        String qry2 = "CREATE TABLE ridership_table" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT, " +
                "devicetime TEXT, " +
                "tap_type TEXT, " +
                "latitude TEXT, " +
                "longitude TEXT, " +
                "status INTERGER)";
        db.execSQL(qry2);

        //creating table user_info
        String qry3 = "CREATE TABLE user_info_table" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT, " +
                "username TEXT, " +
                "mykad_uid TEXT, " +
                "qrcode_uid TEXT, " +
                "active INTERGER, " +
                "expired DATETIME)";
        db.execSQL(qry3);

        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", "c5cdb661-7de4-4028-8f3e-22c20957b8ff");
        contentValues.put("username", "JOHN DOE BIN ABU DEMO");
        contentValues.put("mykad_uid", "0418C582F1");
        contentValues.put("qrcode_uid", (byte[]) null);
        contentValues.put("active", 1);
        contentValues.put("expired", "2025-08-01 00:00:00");
        db.insert("user_info_table", null, contentValues);
//        db.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String qry = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(qry);
        qry = "DROP TABLE IF EXISTS user_info_table";
        db.execSQL(qry);
        qry = "DROP TABLE IF EXISTS ridership_table";
        db.execSQL(qry);
        onCreate(db);
    }
}

