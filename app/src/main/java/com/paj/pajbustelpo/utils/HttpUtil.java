package com.paj.pajbustelpo.utils;

import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.paj.pajbustelpo.R;
import com.paj.pajbustelpo.activities.MainActivity;

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
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import okio.Sink;

public class HttpUtil {

    private String url;
    private RequestBody body;
    private Header header;

    private String TAG = "HttpUtil";
    private MainActivity mainActivity;

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
        this.url = "https://iot.paj.com.my/api/v2/"+endpoint;
    }

    public HttpUtil(MainActivity mainActivity, String endpoint, JSONObject jsonObject){
        this.mainActivity = mainActivity;
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        this.body = RequestBody.create(JSON, jsonObject.toString());
        this.url = "https://iot.paj.com.my/api/v2/"+endpoint;
    }

    public HttpUtil(String endpoint, RequestBody paramsBody){
        this.body = paramsBody;
        this.url = "https://iot.paj.com.my/api/v2/"+endpoint;
    }

    public interface OnGetResponse {
        void Response(String response, int responseCode);
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
                listener.Response(responseString, response.code());
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: The http call was fail at url" + url);
            }
        };
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void post(OnGetResponse listener) {
        OkHttpClient okHttpClient = getUnsafeOkHttpClient();

        // Compress the request body using GZIP
        RequestBody compressedBody = compressRequestBody(body);
//        long payloadSize = calculatePayloadSize(body);
//        Log.e(TAG, "post: " + payloadSize);
//        long payloadSize2 = calculatePayloadSize(compressedBody);
//        Log.e(TAG, "post: " + payloadSize2);
        Request request = new Request.Builder()
                .url(url)
                .post(compressedBody)
                .addHeader("Content-Encoding", "gzip") // Set the Content-Encoding header to indicate compression
                .build();

        Callback callback = new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == 200) ChangeConnectedIconColor(R.color.md_green_500);
                else ChangeConnectedIconColor(R.color.md_red_500);
                String responseString = response.body().string();
                listener.Response(responseString, response.code());
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ChangeConnectedIconColor(R.color.md_red_500);
                Log.e(TAG, "onFailure: The http call was fail at url" + url);
            }
        };
         okHttpClient.newCall(request).enqueue(callback);
    }

    private void ChangeConnectedIconColor(int color){
        mainActivity.runOnUiThread(() -> {
            mainActivity.connected_bar.setColorFilter(ContextCompat.getColor(mainActivity, color), PorterDuff.Mode.SRC_IN);
            new Handler().postDelayed(() -> {
                mainActivity.runOnUiThread(() -> mainActivity.connected_bar.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white), PorterDuff.Mode.SRC_IN));
            }, 1500); // Delay in milliseconds (1 second = 1000 milliseconds)
        });
    }

    private RequestBody compressRequestBody(RequestBody requestBody) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return requestBody.contentType();
            }

            @Override
            public long contentLength() throws IOException {
                return -1; // We don't know the compressed length in advance
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                requestBody.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

    private long calculatePayloadSize(RequestBody requestBody) {
        try {
            // Create a sink to write the request body data
            Buffer buffer = new Buffer();
            BufferedSink bufferedSink = Okio.buffer((Sink) buffer);

            // Write the request body to the sink
            requestBody.writeTo(bufferedSink);

            // Close the sink to flush any remaining data
            bufferedSink.close();

            // Return the size of the payload
            return buffer.size();
        } catch (IOException e) {
            // Handle any exceptions that may occur
            e.printStackTrace();
        }

        return 0;
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