package com.alsk.onebyone;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.alsk.onebyone.databinding.ActivityMainBinding;
import com.alsk.onebyone.hugejsonservice.models.Feature;
import com.alsk.onebyone.hugejsonservice.rest.HugeJsonApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int TOTAL_ELEMENTS_COUNT = 206560;
    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        playHugeJsonSample();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    public void playHugeJsonSample() {

        HugeJsonApi hugeJsonApi = RestUtils.createService(HugeJsonApi.class, HugeJsonApi.SERVICE_ENDPOINT);
        final int[] counter = {0};
        final Gson gson = new GsonBuilder().create();
        binding.progressbar.setMax(TOTAL_ELEMENTS_COUNT);
        binding.progressbar.setIndeterminate(false);
        binding.progressbar.setProgress(0);
        compositeDisposable.add(hugeJsonApi.get()
                .flatMap(response -> convertObjectsStream(response, gson, Feature.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Feature>() {
                    @Override
                    protected void onStart() {
                        request(1);
                    }
                    @Override
                    public void onNext(Feature feature) {
                        counter[0]++;
                        binding.tvCounter.setText("Read elements counter: " + counter[0]);
                        binding.tvLastElement.setText("Last read element: " + gson.toJson(feature));
                        binding.tvStatus.setText("Used memory: " + getUsedMemoryInMb() + "Mb");
                        binding.progressbar.setProgress(counter[0]);
                        request(1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.tvStatus.setText("Status: something went wrong " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        binding.tvStatus.setText("Status: successfully completed");
                    }
                }));
    }

    private long getUsedMemoryInMb() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        //final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
        return usedMemInMB;
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
}
