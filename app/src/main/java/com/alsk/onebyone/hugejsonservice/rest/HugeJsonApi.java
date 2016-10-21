package com.alsk.onebyone.hugejsonservice.rest;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import rx.Observable;

// https://raw.githubusercontent.com/zemirco/sf-city-lots-json/master/citylots.json
public interface HugeJsonApi {

    String SERVICE_ENDPOINT = "https://raw.githubusercontent.com";

    @Streaming
    @GET("/zemirco/sf-city-lots-json/master/citylots.json")
    Observable<ResponseBody> get();
}
