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

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

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
        super(context, "pajtelpo.db", null, 26);
    }

    public double getSqliteSize(){
        SQLiteDatabase db = this.getWritableDatabase();
        return new File(db.getPath()).length();
    }

    public JSONArray getUnsendLocation() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM location_table WHERE status = 0 ORDER BY id ASC LIMIT 50", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONArray();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("id", res.getString(0));
                json.put("tim", res.getString(5));
                json.put("lat", res.getString(1));
                json.put("lng", res.getString(2));
                json.put("spd", res.getString(3));
                json.put("bea", res.getString(4));
                jsonArray.put(json);
            }
            return jsonArray;
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
//            db.close();
            return new JSONArray();
        }
    }

    public String getUserName(String type, String userid_or_uuid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try{
            if (type.equals("qr")) {
                res = db.rawQuery("SELECT name FROM user_table WHERE type = 'qr' AND typeid = ?", new String[]{userid_or_uuid});
            } else if (type.equals("kmj")) {
                res = db.rawQuery("SELECT name FROM user_table WHERE type = 'kmj' AND uid = ?", new String[]{userid_or_uuid});
            } else {
                return null;  // Invalid type
            }
            if(res.getCount() == 0) {
                return null;
            }
            res.moveToFirst();
            return res.getString(0);
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
            return null;
        }
        finally {
            assert res != null;
            res.close();
            db.close();
        }
    }

    public boolean isUserActive(String type, String userid_or_uuid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try{
            if (type.equals("qr")) {
                res = db.rawQuery("SELECT status FROM user_table WHERE type = 'qr' AND typeid = ?", new String[]{userid_or_uuid});
            } else if (type.equals("kmj")) {
                res = db.rawQuery("SELECT status FROM user_table WHERE type = 'kmj' AND uid = ?", new String[]{userid_or_uuid});
            } else {
                return false;  // Invalid type
            }
            if(res.getCount() == 0) {
                return false;
            }
            res.moveToFirst();
            int status = Integer.parseInt(res.getString(0));
            return status >= 0;
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
            return false;
        }
        finally {
            assert res != null;
            res.close();
            db.close();
        }
    }

    public String getUserLatestUpdate(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT updated_at FROM user_table ORDER BY updated_at DESC LIMIT 1", null);
        try{
            if(res.getCount() == 0) return "2023-01-01 00:00:00";
            res.moveToFirst();
            return res.getString(0);
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
            return "2023-01-01 00:00:00";
        }
        finally {
            res.close();
            db.close();
        }
    }

    public JSONArray getUnsendActivated(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT id, version, activated FROM activated_table WHERE status = 0 ORDER BY id ASC LIMIT 30", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONArray();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("id", res.getString(0));
                json.put("ver", res.getString(1));
                json.put("act", res.getString(2));
                jsonArray.put(json);
            }
            return jsonArray;
        }
        catch (Exception e){
            Log.e("Error", "JSON Parsing error");
            return new JSONArray();
        }
        finally {
            res.close();
            db.close();
        }
    }

    public JSONArray getUnsendRidership(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM ridership_table WHERE status = 0 ORDER BY id ASC LIMIT 30", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONArray();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("id", res.getString(0));
                if (res.getString(2) == null) json.put("uid", -1); //since jsonobject cannot be null, we just put -1, in server will replace -1 to null
                else json.put("uid", res.getString(2));
                if (res.getString(1) == null) json.put("cid", -1); //since jsonobject cannot be null, we just put -1, in server will replace -1 to null
                else json.put("cid", res.getString(1));
                json.put("tim", res.getString(3));
                json.put("lat", res.getString(6));
                json.put("lng", res.getString(7));
                json.put("typ", res.getString(4));
                json.put("tap", res.getString(5));
                jsonArray.put(json);
            }
            return jsonArray;
        }
        catch (Exception e){
            Log.e("Error", e + "");
            Log.e("Error", "JSON Parsing error");
            return new JSONArray();
        }
    }

    public boolean updateLocationId(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        db.update(TABLE_NAME, contentValues, " ID = ?", new String[] {id});
        return true;
    }

    public boolean updateRidershipId(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        db.update("ridership_table", contentValues, " ID = ?", new String[] {id});
        return true;
    }

    public boolean updateActivated(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        db.update("activated_table", contentValues, " ID = ?", new String[] {id});
        return true;
    }


    public double getSqliteTotalRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) AS totalRow FROM location_table\n" +
                "UNION ALL \n" +
                "SELECT COUNT(*) AS MyCount FROM ridership_table\n" +
                "UNION ALL \n" +
                "SELECT COUNT(*) AS MyCount FROM user_table", null);
        int totalRow = 0;
        while (res.moveToNext()){
            int rowNum = Integer.parseInt(res.getString(0));
            totalRow = totalRow + rowNum;
        }
        return totalRow;
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
    public boolean insertRidershipData(String uuid, String user_id, String time, int tap_type, int tapIn ,double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("user_id", user_id);
        contentValues.put("time", time);
        contentValues.put("tap_type", tap_type);
        contentValues.put("tap_in", tapIn);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("status", 0);
        long result = db.insert("ridership_table", null, contentValues);
        return result != -1;
    }

    @SuppressLint("DefaultLocale")
    public boolean insertActivated(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("version", BuildConfig.VERSION_NAME);
        contentValues.put("activated", Helper.getDateTimeString());
        contentValues.put("status", 0);
        long result = db.insert("activated_table", null, contentValues);
        return result != -1;
    }

    @SuppressLint("DefaultLocale")
    public boolean updateUserData(String type, String typeid, String name, String status, String citizen, String uid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("status", status);
        contentValues.put("citizen", citizen);
        contentValues.put("uid", uid);
        contentValues.put("updated_at", Helper.getDateTimeString());
        String[] whereArgs = {type, typeid};

        // Check if the row already exists
        String query = "SELECT COUNT(*) FROM user_table WHERE type = ? AND typeid = ?";
        Cursor cursor = db.rawQuery(query, whereArgs);
        if (cursor.moveToFirst()) {
            int rowCount = cursor.getInt(0);
            cursor.close();
            if (rowCount > 0) {
                // Row exists, perform an update
                db.update("user_table", contentValues, "type = ? AND typeid = ?", whereArgs);
                Log.e("update", name);
            } else {
                // Row doesn't exist, perform an insert
                Log.e("insert", name);
                contentValues.put("type", type);
                contentValues.put("typeid", typeid);
                db.insert("user_table", null, contentValues);
            }
        } else {
            cursor.close();
        }
        return true;
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
                "user_id TEXT, " +
                "time TEXT, " +
                "tap_type TEXT, " +
                "tap_in TEXT, " +
                "latitude TEXT, " +
                "longitude TEXT, " +
                "status INTERGER)";
        db.execSQL(qry2);

        //creating table user
        String qry3 = "CREATE TABLE user_table" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "typeid TEXT, " +
                "name TEXT, " +
                "status TEXT, " +
                "citizen TEXT, " +
                "uid TEXT, " +
                "updated_at DATETIME)";
        db.execSQL(qry3);

        //creating table user
        String qry4 = "CREATE TABLE activated_table" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "version TEXT, " +
                "activated DATETIME, "+
                "status INTERGER)";
        db.execSQL(qry4);

//        db.close();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String qry = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(qry);
        qry = "DROP TABLE IF EXISTS user_table";
        db.execSQL(qry);
        qry = "DROP TABLE IF EXISTS ridership_table";
        db.execSQL(qry);
        qry = "DROP TABLE IF EXISTS activated_table";
        db.execSQL(qry);
        onCreate(db);
    }
}

