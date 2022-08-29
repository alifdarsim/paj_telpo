package com.paj.pajbustelpo;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class TaskIntervalRequest implements Runnable {

    DatabaseHelper db;
    MainActivity mainActivity;

    public TaskIntervalRequest(MainActivity mainActivity, DatabaseHelper db){
        this.db = db;
        this.mainActivity = mainActivity;
    }

    public void run(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    readDatabase();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 20000);//put here time 1000 milliseconds=1 second
    }

    private void readDatabase() throws JSONException {
        JSONObject jsonObject = db.getUnsendLocation();
        Log.e("data", jsonObject.toString());
        JSONArray array = jsonObject.getJSONArray("data");

        JSONObject jsonObject2 = db.getUnsendRidership();

        mainActivity.runOnUiThread(() -> {
            try {
                //logging some data
                mainActivity.logger.writeToLogger("Collect "+ array.length() +
                        " unsent location data: id=["+idValue(array, 0) + "," + idValue(array, 1) + "," + idValue(array, 2) + ",...," + idValue(array, array.length()-1) + "]" , "white");
                //make http request here

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mainActivity.logger.writeToLogger("Calling API - send location...", "yellow");
            mainActivity.logger.writeToLogger("Calling API - send ridership...", "yellow");
            mainActivity.logger.writeToLogger("API request failed - location", "red");
            mainActivity.logger.writeToLogger("API request failed - ridership", "red");
        });
    }

    private String idValue(JSONArray jsonArray, int index) throws JSONException {
        return jsonArray.getJSONObject(index).get("id").toString();
    }
}