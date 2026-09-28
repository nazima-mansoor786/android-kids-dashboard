package com.tabnova.aidashboard.Extra;



import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;


public class RetrofitExtra {

    public static Retrofit getInstance() {

        okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(Consts.BaseUrl)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }


    public static Retrofit getInstanceV2() {

        okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(Consts.BaseUrlV2)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }
}
