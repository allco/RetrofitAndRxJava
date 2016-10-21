package com.alsk.onebyone.rest;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;

public interface GithubApi {

    String SERVICE_ENDPOINT = "https://api.github.com";

    @Streaming
    @GET("/orgs/{login}/repos")
    Observable<ResponseBody> getProjects(@Path("login") String login);
}
