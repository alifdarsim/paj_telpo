package com.paj.pajbustelpo.task;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.paj.pajbustelpo.BuildConfig;
import com.paj.pajbustelpo.activities.MainActivity;
import com.paj.pajbustelpo.model.HttpResponse;
import com.paj.pajbustelpo.utils.HttpUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class UpdateTask {

    public static String TAG = "UpdateTask";
    MainActivity mainActivity;

    public UpdateTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void checkUpdate() {
        try {
            updateSoftware();
        } catch (Exception ignored) {
        }
    }

    private void updateSoftware() throws Exception {
        mainActivity.logger.textSpan = "";
        mainActivity.text_log.setText("");
        mainActivity.logger.writeToLogger("\uD83D\uDFE8 Checking for new update.....", "yellow");
//        mainActivity.isLogging = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", BuildConfig.VERSION_CODE);

        HttpUtil httpUtil = new HttpUtil("telpo/update", jsonObject);
        httpUtil.post(response -> mainActivity.runOnUiThread(() -> {

            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<HttpResponse> jsonAdapter = moshi.adapter(HttpResponse.class);
            try {
                HttpResponse post = jsonAdapter.fromJson(response);
                assert post != null;
                if (post.isUpdate()) {
                    new MaterialDialog.Builder(mainActivity)
                            .title("Update Available")
                            .content("Kindly download the newer version and press INSTALL")
                            .positiveText("DOWNLOAD")
                            .negativeText("CANCEL")
                            .onPositive((dialog, which) -> {
                                SpotsDialog.Builder builder = new SpotsDialog.Builder();
                                builder.setContext(mainActivity).setMessage("Updating...").setCancelable(false).build().show();
                                String apk_url = post.getApkUrl();
                                String apk_name = post.getApkName();
                                try {
                                    UpdateProcedure(apk_url, apk_name, builder);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                            .show();
                } else {
                    new MaterialDialog.Builder(mainActivity)
                            .title("No update")
                            .content("This app already using the latest update")
                            .positiveText("OK")
                            .show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }


    private void UpdateProcedure(String base_url, String apk_name, SpotsDialog.Builder builder) throws Exception {


        Log.e(TAG, "checkForUpdates: success");
        Log.e(TAG, "url: " + base_url);

        // Use a networking library to download the new APK from the server
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        String apk_url = base_url + apk_name;
        Log.e(TAG, "apk_url: " + apk_url);
        Call<ResponseBody> call = apiService.downloadApk((apk_url));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                builder.setContext(mainActivity).build().dismiss();
                if (response.isSuccessful()) {
                    File apkFile = new File(mainActivity.getExternalCacheDir(), apk_name);
                    try {
                        FileOutputStream fos = new FileOutputStream(apkFile);
                        fos.write(response.body().bytes());
                        fos.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Install the APK file using a content URI
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri apkUri = FileProvider.getUriForFile(mainActivity, mainActivity.getApplicationContext().getPackageName() + ".provider", apkFile);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mainActivity.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle download failure
                Log.e(TAG, "done10: " + t);
            }
        });
    }

    public interface ApiService {
        @GET
        Call<ResponseBody> downloadApk(@Url String url);
    }

}
