package com.paj.pajbustelpo;

import android.database.Cursor;

public class RidershipHelper {

    DatabaseHelper db;
    MainActivity mainActivity;

    public RidershipHelper(MainActivity mainActivity, DatabaseHelper db){
        this.mainActivity = mainActivity;
        this.db = db;
    }

    public String base64toUuid(String base64){
        return base64;
    }

    public void getUserInfo(String uuid){
//        Cursor res = mainActivity.db.getUnsendRidership();
//        if(res.getCount() == 0){
//            return;
//        }
    }


}
