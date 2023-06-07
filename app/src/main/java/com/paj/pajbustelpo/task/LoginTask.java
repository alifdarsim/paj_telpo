package com.paj.pajbustelpo.task;

import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.activities.SplashActivity;
import com.paj.pajbustelpo.model.HttpResponse;
import com.paj.pajbustelpo.utils.HttpUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class LoginTask {

    public static String TAG = "LoginTask";
    private final String bus;
    private final int version;
    SplashActivity splashActivity;

    public LoginTask(SplashActivity splashActivity, String bus, int version){
        this.splashActivity = splashActivity;
        this.bus = bus;
        this.version = version;
    }

    public interface OnSuccessLogin {
        void onSuccessLogin(boolean isLogged);
    }

    public void login(OnSuccessLogin listener){

        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bus", bus);
            jsonObject.put("version", version);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpUtil httpUtil = new HttpUtil("telpo/login", jsonObject);
        httpUtil.post(response -> splashActivity.runOnUiThread(() -> {

            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<HttpResponse> jsonAdapter = moshi.adapter(HttpResponse.class);

            try {
                HttpResponse post = jsonAdapter.fromJson(response);
                assert post != null;
                if (post.isSuccess()){
                    listener.onSuccessLogin(post.isLogged());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

//            try {
//                HttpResponse post = jsonAdapter.fromJson(response);
//                assert post != null;
//                if (post.isSuccess()){
//                    listener.onSuccessLogin(post.isUpgradable());
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }));
    }

}
