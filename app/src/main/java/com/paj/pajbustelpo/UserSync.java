package com.paj.pajbustelpo;

import static com.paj.pajbustelpo.HttpUtil.getUnsafeOkHttpClient;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserSync {

    DatabaseHelper db;
    LoggerHelper logger;
    MainActivity mainActivity;

    public UserSync(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.db = mainActivity.db;
        this.logger = mainActivity.logger;
    }

    interface OnSyncSuccess {
        void syncSuccess(boolean isSuccess);
    }

    public void startSync(OnSyncSuccess listener){
        logger.writeToLogger("API Request User Info...", "yellow");
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
                .url("https://dataapi.paj.com.my/api/v1/telpo/userlist")
                .addHeader("api-key", "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a")
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();

                try{
                    JSONObject obj = new JSONObject(responseString);
                    JSONArray data = obj.getJSONArray("data");
                    logger.writeToLogger("API Request User Info SUCCESS: Get '" + data.length() + "' User Info", "green");
                    for (int i = 0; i < data.length(); i++){
                        JSONObject jsonObject=data.getJSONObject(i);
                        String uuid=jsonObject.getString("uuid");
                        String username=jsonObject.getString("username");
                        String mykad_uid=jsonObject.getString("mykad_uid");
                        String qrcode_uid=jsonObject.getString("qrcode_uid");
                        String blacklist=jsonObject.getString("blacklist");
                        String expired=jsonObject.getString("expired");
                        db.insertUserData(uuid, username, mykad_uid, qrcode_uid, Integer.parseInt(blacklist), expired);
                        logger.writeToLogger("Sqlite User insert: " + username, "green");
                    }
                    listener.syncSuccess(true);
                    logger.writeToLogger("Success Sync User Table", "green");
                }catch (Exception e){
                    listener.syncSuccess(false);
                    logger.writeToLogger("JSON Parsing Failed" + e, "red");

                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.syncSuccess(false);
                logger.writeToLogger("API Request: User Information FAILED", "red");
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

}
