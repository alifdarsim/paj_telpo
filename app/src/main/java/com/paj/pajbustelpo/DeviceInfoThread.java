package com.paj.pajbustelpo;

import static com.paj.pajbustelpo.HttpUtil.getUnsafeOkHttpClient;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceInfoThread implements Runnable {

    DatabaseHelper db;
    MainActivity mainActivity;
    DeviceUtils deviceUtils;
    LoggerHelper logger;

    public DeviceInfoThread(MainActivity mainActivity, DatabaseHelper db){
        this.db = db;
        this.mainActivity = mainActivity;
        this.deviceUtils = new DeviceUtils();
        this.logger = mainActivity.logger;
    }

    public void run(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String sqliteSize = getSqliteSize();
                    int freeSpaceDisk = DiskUtils.freeSpace(true) + DiskUtils.freeSpace(false);
                    String mem  = DeviceUtils.getDeviceInfo(mainActivity, DeviceUtils.DEVICE_USED_MEMORY);
                    String mem2  = DeviceUtils.getDeviceInfo(mainActivity, DeviceUtils.DEVICE_TOTAL_MEMORY);
                    String cpu  = DeviceUtils.getDeviceInfo(mainActivity, DeviceUtils.DEVICE_TOTAL_CPU_USAGE);
                    if (Objects.equals(cpu, "")) cpu = "0";

                    Log.e("Device Info", mem + "," + freeSpaceDisk + "," + cpu + "," + sqliteSize);
                    String finalCpu = cpu;
                    mainActivity.runOnUiThread(()-> {
//                        Toast.makeText(mainActivity, mem + "," + mem2 + "," + finalCpu + "," + sqliteSize, Toast.LENGTH_SHORT).show();
                        try {

                            JSONObject json = new JSONObject();
                            json.put("cpu", finalCpu);
                            json.put("memory",mem);
                            json.put("disk", freeSpaceDisk);
                            json.put("sqlite", sqliteSize);

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("bus", "JTV6100");
                            jsonObject.put("data", json);

//                            Log.e("aaaaa", jsonObject.toString());
                            sendDeviceInfoData(jsonObject.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 300000); //send after 1st 1 minute and every 15minute after that
    }

    private String getSqliteSize(){
        double sqliteSize = db.getSqliteTotalRow();
        return sqliteSize+"";
    }

    public void sendDeviceInfoData(String jsonData){
        logger.writeToLogger("\uD83D\uDFE1 API Send Device Info Data...", "yellow");
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();

        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://dataapi.paj.com.my/api/v1/telpo/deviceinfo")
                .addHeader("api-key", "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a")
                .post(body)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                Log.e("DeviceInfo Response", responseString);
                try{
                    JSONObject obj = new JSONObject(responseString);
                    String status = obj.getString("status");
                    if (status.equals("success")){
                        logger.writeToLogger("\uD83D\uDFE2 API Device Info: SUCCESS insert to server", "green");
                    }
                    else{
                        logger.writeToLogger("\uD83D\uDD34 API Device Info: FAILED insert to server", "green");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.writeToLogger("\uD83D\uDD34 API Device Info: Response Error", "red");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                logger.writeToLogger("API Device Info: FAILED insert to server", "red");
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

}