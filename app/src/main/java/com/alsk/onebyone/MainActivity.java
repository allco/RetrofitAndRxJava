package com.alsk.onebyone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.alsk.onebyone.models.GithubProject;
import com.alsk.onebyone.rest.GithubApi;
import com.alsk.onebyone.rest.GithubService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static okhttp3.internal.Util.closeQuietly;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playSample();
    }

    public static void playSample() {

        GithubApi githubApi = GithubService.createRetrofitService(GithubApi.class, GithubApi.SERVICE_ENDPOINT);

        convert(githubApi.getProjects("linkedin"), new Gson(), GithubProject.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GithubProject>() {
                    @Override
                    public void onCompleted() {
                        Log.e(TAG, "onCompleted() called with: " + "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(GithubProject githubProject) {
                        Log.e(TAG, githubProject.name);
                        request(1);
                    }
                });
    }

    static public <TYPE> Observable<TYPE> convert(Observable<ResponseBody> sourceObservable, Gson gson, Class<TYPE> clazz) {
        return sourceObservable
                .flatMap(responseBody -> Observable.create((Observable.OnSubscribe<TYPE>) subscriber -> {
                    JsonReader reader = null;
                    try {
                        Type type = TypeToken.get(clazz).getType();
                        reader = gson.newJsonReader(responseBody.charStream());
                        reader.beginArray();
                        while (reader.hasNext()) {
                            if (subscriber.isUnsubscribed()) {
                                subscriber.onCompleted();
                                return;
                            }
                            TYPE t = gson.fromJson(reader, type);
                            subscriber.onNext(t);
                        }
                        reader.endArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    } finally {
                        subscriber.onCompleted();
                        closeQuietly(reader);
                    }
                }))
                .onBackpressureBuffer();
    }
}
