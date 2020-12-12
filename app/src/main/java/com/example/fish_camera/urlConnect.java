package com.example.fish_camera;

import android.util.Log;


import java.io.File;
import java.io.IOException;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class urlConnect extends Thread {
    public static final String TAG = "test";
    private boolean lock;
    private String url_method;
    private String connectionResult;
    private File file;
    public urlConnect(String url, File f) {
        lock = true;
        url_method = url;
        file = f;
        connectionResult = "";
    }

    public void run() {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            RequestBody image = RequestBody.create(MediaType.parse("image/jpg"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image",  file.getName(), image)
                        .build();
                Request request = new Request.Builder()
                        .url(url_method)
                        .post(requestBody)
                        .build();

                Response response = okHttpClient.newCall(request).execute();
                String response_str = response.body().string();

                Log.v(TAG,"result string:"+response_str);

                connectionResult = response_str;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock = false;
        }
    }

    public boolean getLockStatus() {
        return lock;
    }

    public String getResult() {
        return connectionResult;
    }
}
