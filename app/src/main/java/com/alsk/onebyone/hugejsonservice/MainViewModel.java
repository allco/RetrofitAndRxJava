package com.alsk.onebyone.hugejsonservice;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;

import com.alsk.onebyone.RestUtils;
import com.alsk.onebyone.hugejsonservice.models.Feature;
import com.alsk.onebyone.hugejsonservice.rest.HugeJsonApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

    public static final int TOTAL_ELEMENTS_COUNT = 206560;

    private int counter = 0;

    public static ObservableBoolean initializationInProgress = new ObservableBoolean(false);
    public static ObservableInt progress = new ObservableInt(0);
    public static ObservableField<String> status = new ObservableField<>("Status: initialization");
    public static ObservableField<String> reportCounter = new ObservableField<>("");
    public static ObservableField<String> lastItem = new ObservableField<>("");

    private final Gson gson = new GsonBuilder().create();
    private final HugeJsonApi hugeJsonApi = RestUtils.createService(HugeJsonApi.class, HugeJsonApi.SERVICE_ENDPOINT);
    private final CompositeDisposable subscriptions = new CompositeDisposable();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public MainViewModel reset() {
        counter = 0;
        subscriptions.clear();
        initializationInProgress.set(true);
        subscriptions.add(hugeJsonApi.get()
                .concatMap(response -> convertObjectsStream(response, gson, Feature.class))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(s -> initializationInProgress.set(true))
                .subscribeWith(new DisposableSubscriber<Feature>() {
                    @Override
                    protected void onStart() {
                        request(1);
                    }

                    @Override
                    public void onNext(Feature feature) {
                        counter++;
                        initializationInProgress.set(false);
                        reportCounter.set("Read elements counter: " + counter);
                        lastItem.set("Last read element: " + gson.toJson(feature));
                        status.set("Used memory: " + getUsedMemoryInMb() + "Mb");
                        progress.set(counter);
                        request(1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        status.set("Status: something went wrong " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        status.set("Status: successfully completed");
                    }
                }));
        return this;
    }

    private long getUsedMemoryInMb() {
        final Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
    }

    @NonNull
    private static <TYPE> Flowable<TYPE> convertObjectsStream(final Response<ResponseBody> response, Gson gson, Class<TYPE> clazz) {
        return Flowable.generate(() -> {
                    try {
                        JsonReader reader = gson.newJsonReader(response.body().charStream());
                        reader.beginObject();
                        // looking for a "features" field with actual array of elements
                        while (reader.hasNext()) {
                            // the array begins at json-field "features"
                            if (reader.nextName().equals("features")) {
                                reader.beginArray();
                                return reader;
                            }
                            reader.skipValue();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        RxJavaPlugins.onError(e);
                    }
                    return null;
                }, (jsonReader, typeEmitter) -> {
                    if (jsonReader == null) {
                        typeEmitter.onComplete();
                        return null;
                    }

                    try {
                        if (jsonReader.hasNext()) {
                            TYPE t = gson.fromJson(jsonReader, clazz);
                            typeEmitter.onNext(t);
                        } else {
                            typeEmitter.onComplete();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        typeEmitter.onError(e);
                    }
                    return jsonReader;

                },
                Util::closeQuietly);


    }

    @Override
    protected void onCleared() {
        super.onCleared();
        subscriptions.clear();
    }
}
