package com.paj.pajbustelpo;

import static com.paj.pajbustelpo.HttpUtil.getUnsafeOkHttpClient;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TaskIntervalRequest implements Runnable {

    DatabaseHelper db;
    MainActivity mainActivity;
    LoggerHelper logger;

    public TaskIntervalRequest(MainActivity mainActivity){
        this.db = mainActivity.db;
        this.mainActivity = mainActivity;
        this.logger = mainActivity.logger;
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
        }, 5000, 30000);//put here time 1000 milliseconds=1 second
    }

    private void readDatabase() throws JSONException {
        // try to send the location API
        JSONObject jsonLocation = db.getUnsendLocation();
        if (jsonLocation.length() != 0){
            sendLocationData(jsonLocation.toString());
        }

        // delay 2 second and send Ridership API
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                JSONObject jsonRidership = db.getUnsendRidership();
                if (jsonRidership.length() != 0){
                    sendRidershipData(jsonRidership.toString());
                }
            }
        }, 2000);

    }

    public void sendLocationData(String jsonData){
        logger.writeToLogger("\uD83D\uDFE1 API Request Location Data...", "yellow");
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();

        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://dataapi.paj.com.my/api/v1/telpo/gps")
                .addHeader("api-key", "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a")
                .post(body)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                Log.e("Location Response", responseString);
                try{
                    JSONObject obj = new JSONObject(responseString);
                    JSONArray data = obj.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++){
                        ArrayList<Object> list = ArrayUtil.convert(data);
                        String id = list.get(i).toString();
                        db.updateLocationId(id);
                    }
                    logger.writeToLogger("\uD83D\uDFE2 API Request Location Data: SUCCESS insert to server", "green");
                }catch (Exception e){
                    e.printStackTrace();
                    logger.writeToLogger("\uD83D\uDD34 API Request Location: JSON Parsing Failed", "red");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                logger.writeToLogger("API Request Location Data FAILED", "red");
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void sendRidershipData(String jsonData){
        logger.writeToLogger("\uD83D\uDFE1 API Request Ridership Data...", "yellow");
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();

        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://dataapi.paj.com.my/api/v1/telpo/ridership")
                .addHeader("api-key", "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a")
                .post(body)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                Log.e("Ridership Response", responseString);
                try{
                    JSONObject obj = new JSONObject(responseString);
                    JSONArray data = obj.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++){
                        ArrayList<Object> list = ArrayUtil.convert(data);
                        String id = list.get(i).toString();
                        db.updateRidershipId(id);
                    }
                    logger.writeToLogger("\uD83D\uDFE2API Request Ridership Data SUCCESS", "green");
                }catch (Exception e){
                    e.printStackTrace();
                    logger.writeToLogger("\uD83D\uDD34 API Request Ridership: JSON Parsing Failed", "red");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                logger.writeToLogger("API Request Location Data FAILED", "red");
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

    private String idValue(JSONArray jsonArray, int index) throws JSONException {
        return jsonArray.getJSONObject(index).get("id").toString();
    }

    //just a helper class
    public static class ArrayUtil
    {
        public static ArrayList<Object> convert(JSONArray jArr)
        {
            ArrayList<Object> list = new ArrayList<Object>();
            try {
                for (int i=0, l=jArr.length(); i<l; i++){
                    list.add(jArr.get(i));
                }
            } catch (JSONException e) {}

            return list;
        }

        public JSONArray convert(Collection<Object> list)
        {
            return new JSONArray(list);
        }

    }

}