package com.paj.pajbustelpo.task;
import android.util.Log;

import com.paj.pajbustelpo.DatabaseHelper;
import com.paj.pajbustelpo.Helper;
import com.paj.pajbustelpo.User;
import com.paj.pajbustelpo.utils.LoggerHelper;
import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.model.HttpResponse;
import com.paj.pajbustelpo.utils.HttpUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IntervalTask implements Runnable {

    public static String TAG = "IntervalTask";

    DatabaseHelper db;
    MainActivity mainActivity;
    LoggerHelper logger;

    public IntervalTask(MainActivity mainActivity){
        this.db = mainActivity.db;
        this.mainActivity = mainActivity;
        this.logger = mainActivity.logger;
    }

    public void run(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 5000, 10000);//put here time 1000 milliseconds=1 second
    }

    private void sendData() throws JSONException {
        // try to send the location API

        JSONArray currentLocation = new JSONArray();
        currentLocation.put(mainActivity.current_time);
        currentLocation.put(mainActivity.current_Lat);
        currentLocation.put(mainActivity.current_Lng);
        currentLocation.put(mainActivity.current_bearing);
        currentLocation.put(mainActivity.current_speed);

        JSONArray jsonLocation = db.getUnsendLocation();
        JSONArray jsonRidership = db.getUnsendRidership();
        String userLastUpdate = db.getUserLatestUpdate();
        JSONArray jsonActivated = db.getUnsendActivated();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bus", mainActivity.bus_plate);
        jsonObject.put("cur", currentLocation);
        jsonObject.put("loc", jsonLocation);
        jsonObject.put("rid", jsonRidership);
        jsonObject.put("usr", userLastUpdate);
        jsonObject.put("act", jsonActivated);

        sendIntervalTask(jsonObject);
    }

    public void sendIntervalTask(JSONObject jsonBody){

        mainActivity.logger.writeToLogger("\uD83D\uDFE1 Sending Interval data...", "yellow");
        Log.e(TAG, "sendIntervalTask: " + jsonBody);
        HttpUtil httpUtil = new HttpUtil(mainActivity, "telpo/post", jsonBody);
        httpUtil.post((response, responseCode) -> mainActivity.runOnUiThread(() -> {
            if (responseCode != 200){
                mainActivity.logger.writeToLogger("\uD83D\uDFE2 Internal server error.", "red");
                return;
            }
            mainActivity.logger.writeToLogger("\uD83D\uDFE2 Interval success. Response: " + response, "green");
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<HttpResponse> jsonAdapter = moshi.adapter(HttpResponse.class);
            try {
                Log.e(TAG, "getreponse: " + response);
                HttpResponse post = jsonAdapter.fromJson(response);
                assert post != null;

                List<Integer> ids_location = post.l();
                for (int i = 0; i < ids_location.size(); i++) {
                    int id = ids_location.get(i);
                    mainActivity.db.updateLocationId(String.valueOf(id));
                }

                List<Integer> ids_ridership = post.r();
                for (int i = 0; i < ids_ridership.size(); i++) {
                    int id = ids_ridership.get(i);
                    mainActivity.db.updateRidershipId(String.valueOf(id));
                }

                List<User> user_list = post.u();
                for (int i = 0; i < user_list.size(); i++) {
                    String name = user_list.get(i).getName();
                    String citizen = user_list.get(i).getCitizen();
                    String status = user_list.get(i).getStatus();
                    String type = user_list.get(i).getType();
                    String typeid = user_list.get(i).getTypeid();
                    String uid = user_list.get(i).getUid();
                    mainActivity.db.updateUserData(type, typeid, name, status, citizen, uid);
                }

                List<Integer> ids_activated = post.a();
                for (int i = 0; i < ids_activated.size(); i++) {
                    int id = ids_activated.get(i);
                    mainActivity.db.updateActivated(String.valueOf(id));
                }

            } catch (IOException e) {
                e.printStackTrace();
                mainActivity.logger.writeToLogger("\uD83D\uDD34 Sending data failed", "red");
            }

        }));
    }

}