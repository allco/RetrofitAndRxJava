package com.alsk.onebyone;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RestUtils {
    public static <T> T createService(final Class<T> clazz, String endpoint) {

        // no read timeout
        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MICROSECONDS).build();

        return new Retrofit.Builder()
                .baseUrl(endpoint)
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
                .create(clazz);
    }
}
