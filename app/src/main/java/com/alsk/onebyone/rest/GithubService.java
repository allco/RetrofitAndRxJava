package com.alsk.onebyone.rest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class GithubService {



    public static <T> T createRetrofitService(final Class<T> clazz, String endpoint) {

        return new Retrofit.Builder()
                .baseUrl(endpoint)
                //.addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build().create(clazz);
    }
}
