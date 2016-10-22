package com.alsk.onebyone;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.alsk.onebyone.hugejsonservice.models.Feature;
import com.alsk.onebyone.hugejsonservice.rest.HugeJsonApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import static okhttp3.internal.Util.closeQuietly;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playHugeJsonSample();
    }

    public void playHugeJsonSample() {

        HugeJsonApi hugeJsonApi = RestUtils.createService(HugeJsonApi.class, HugeJsonApi.SERVICE_ENDPOINT);

        final int[] counter = {0};
        Gson gson = new GsonBuilder().create();
        Handler handler = new Handler(Looper.getMainLooper());

        hugeJsonApi.get()
                .flatMap(responseBody -> convertObjectsStream(responseBody, gson, Feature.class))
                .subscribeOn(Schedulers.io())
                .subscribe(feature -> handler.post(()->{Log.i(TAG, gson.toJson(feature)); counter[0]++;}),
                           e  -> Log.e(TAG, "something went wrong", e),
                           () -> handler.post(()-> Log.i(TAG, "onCompleted() called. Fetched elements:" + counter[0])));
    }

    @NonNull
    private static <TYPE> Observable<TYPE> convertObjectsStream(ResponseBody responseBody, Gson gson, Class<TYPE> clazz) {
        return Observable.create(subscriber -> {
            JsonReader reader = null;
            try {
                // parse json here semi-manually
                subscriber.onStart();
                Type type = TypeToken.get(clazz).getType();
                reader = gson.newJsonReader(responseBody.charStream());
                reader.beginObject();
                while (reader.hasNext()) {
                    // the array begins at json-field "features"
                    if (!reader.nextName().equals("features")) {
                        reader.skipValue();
                        continue;
                    }
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
                }
                reader.endObject();
            } catch (Exception e) {
                e.printStackTrace();
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
                closeQuietly(reader);
            }
        });
    }
}
