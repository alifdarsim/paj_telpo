package com.paj.pajbustelpo.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class HttpUtil {

    private String url;
    private RequestBody body;
    private Header header;

    private String TAG = "HttpUtil";

    //overriding the server certificate
    public OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .writeTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpUtil(String endpoint){
        this.url = "https://iot.paj.com.my/api/v1/"+endpoint;
    }

    public HttpUtil(String endpoint, JSONObject jsonObject){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        this.body = RequestBody.create(JSON, jsonObject.toString());
        this.url = "https://iot.paj.com.my/api/v1/"+endpoint;
    }

    public HttpUtil(String endpoint, RequestBody paramsBody){
        this.body = paramsBody;
        this.url = "https://iot.paj.com.my/api/v1/"+endpoint;
    }

    public interface OnGetResponse {
        void Response(String response);
    }

    public void getResponse(OnGetResponse listener) {
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                listener.Response(responseString);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: The http call was fail at url" + url);
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void post(OnGetResponse listener) {
//        Log.e(TAG, "Try to call post");
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();
                listener.Response(responseString);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: The http call was fail at url" + url);
//                listener.Failed("asd");
            }
        };
         okHttpClient.newCall(request).enqueue(callback);
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }
}