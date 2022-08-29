package com.paj.pajbustelpo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {

    public static void hideSystemUI(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public static String epochToDate(long epoch) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        return sdf.format(new Date(epoch));
    }

    public static Date stringToDate(String str) {
        Log.e("date", str.toString());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getTimeNow(){
        SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss]", Locale.getDefault());
        return sdf.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateTimeString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        //dasdasd
        return dateFormat.format(date);
    }

    public static double msToKmh(double ms) {
        return ms*3.6;
    }

}
