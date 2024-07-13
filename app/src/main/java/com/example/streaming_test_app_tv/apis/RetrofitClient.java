package com.example.streaming_test_app_tv.apis;


import com.example.streaming_test_app_tv.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient _instance;
    private final API apIs;
    private final FileUploadAPI uploadAPI;

    private RetrofitClient(){
        Retrofit client = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apIs = client.create(API.class);
        uploadAPI = client.create(FileUploadAPI.class);
    }

    public static synchronized RetrofitClient getInstance(){
        if(_instance==null){
            _instance = new RetrofitClient();
        }
        return _instance;
    }
    public API getApIs(){
        return apIs;
    }

    public FileUploadAPI getUploadAPI() {
        return uploadAPI;
    }
}
