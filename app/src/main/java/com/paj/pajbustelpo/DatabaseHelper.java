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
        super(context, "pajtelpo.db", null, 24);
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM location_table", null);
        return res;
    }

    public User checkUserMyKad(String mykad_uid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM user_info_table WHERE mykad_uid = '" +mykad_uid+ "' LIMIT 1", null);
        User user = new User();
        //if no uuid directly return empty class
        if(res.getCount() == 0) return user;
        while (res.moveToNext()){
            user.setUuid(res.getString(1));
            user.setUsername(res.getString(2));
            user.setMykad_uid(res.getString(3));
            user.setQrcode_uid(res.getString(4));
            user.setBlacklist(res.getString(5));
            user.setExpired(res.getString(6));
        }
        return user;
    }

    public User checkUserQrCode(String qrCode){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM user_info_table WHERE qrcode_uid = '" +qrCode+ "' LIMIT 1", null);
        User user = new User();
        //if no uuid directly return empty class
        if(res.getCount() == 0) return user;
        while (res.moveToNext()){
            user.setUuid(res.getString(1));
            user.setUsername(res.getString(2));
            user.setMykad_uid(res.getString(3));
            user.setQrcode_uid(res.getString(4));
            user.setBlacklist(res.getString(5));
            user.setExpired(res.getString(6));
        }
//        db.close();
        return user;
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
//                json.put("acc", res.getString(6));
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

    public JSONArray getUnsendRidership(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM ridership_table WHERE status = 0 ORDER BY id ASC LIMIT 30", null);
        try{
            JSONArray jsonArray = new JSONArray();
            if(res.getCount() == 0) return new JSONArray();
            while (res.moveToNext()){
                JSONObject json = new JSONObject();
                json.put("id", res.getString(0));
                json.put("cid", res.getString(1));
                json.put("tim", res.getString(2));
                json.put("lat", res.getString(5));
                json.put("lng", res.getString(6));
                json.put("typ", res.getString(3));
                json.put("tap", res.getString(4));
                jsonArray.put(json);
            }
            return jsonArray;
        }
        catch (Exception e){
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

    public double getSqliteSize(){
        SQLiteDatabase db = this.getWritableDatabase();
        return new File(db.getPath()).length();
    }

    public double getSqliteTotalRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) AS totalRow FROM location_table\n" +
                "UNION ALL \n" +
                "SELECT COUNT(*) AS MyCount FROM ridership_table\n" +
                "UNION ALL \n" +
                "SELECT COUNT(*) AS MyCount FROM user_info_table", null);
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
    public boolean isRidershipOut(String uuid, double currentLat, double currentLng) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor res = db.rawQuery("SELECT * FROM ridership_table WHERE uuid = '"+uuid+"' ORDER BY id DESC LIMIT 1", null);
//        if(res.getCount() == 0) return false;
//        String lastTapTime = null;
//        double lastLat = 0, lastLng = 0;
//        int lastTapIn = 0;
//        while (res.moveToNext()){
//            Log.e("tapin", res.getString(4));
//            Log.e("lat", res.getString(5));
//            Log.e("lng", res.getString(6));
//            lastTapIn = Integer.parseInt(res.getString(4));
//            lastTapTime = res.getString(2);
//            lastLat = Double.parseDouble(res.getString(4));
//            lastLng = Double.parseDouble(res.getString(5));
//        }
//
//        if (lastTapIn == 1){
//            Log.e("22","Tapping detect as OUT");
//            return true;
//        }
//        else{
//            Log.e("11","Tapping detect as IN");
//            return false;
//        }

//        if (Helper.SecondAgo(lastTapTime) > 10) {
//            Log.e("11","Tapping detect as IN");
//            return false;
//        }
//
//        //get displacement in meter from previous location
//        double displacement = Helper.getDisplacement(currentLat, currentLng, lastLat, lastLng);
//        if (displacement > -1) {
//            Log.e("22","Tapping detect as OUT");
//            return true;
//        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    public boolean insertRidershipData(String uuid, String time, int tap_type, int tapIn ,double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
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
    public boolean insertUserData(String uuid, String username, String mykad_uid, String qrcode_uid, int active, String expired){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("username", username);
        contentValues.put("mykad_uid", mykad_uid);
        contentValues.put("qrcode_uid", qrcode_uid);
        contentValues.put("blacklist", active);
        contentValues.put("expired", expired);
        long result = db.insert("user_info_table", null, contentValues);
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
                "time TEXT, " +
                "tap_type TEXT, " +
                "tap_in TEXT, " +
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
                "blacklist INTERGER, " +
                "expired DATETIME)";
        db.execSQL(qry3);

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

