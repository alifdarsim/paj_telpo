package com.paj.pajbustelpo;

//make the system run this script during startup
//no need to create new thread, just use callback at the end, cause we wont run other things while startup script is running

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class HttpUtil {

    // only one client
    private final OkHttpClient httpClient = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        HttpUtil obj = new HttpUtil();
        obj.sendGET();
    }

    public static void get() throws IOException {
        HttpUtil obj = new HttpUtil();
        obj.sendGET();
    }

    private void sendGET() throws IOException {

        Request request = new Request.Builder()
                .url("https://httpbin.org/get")
                .addHeader("custom-key", "mkyong")  // add request headers
                .addHeader("User-Agent", "OkHttp Bot")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    // Get response headers
                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    // Get response body
                    System.out.println(responseBody.string());
                }
            }
        });

    }

}