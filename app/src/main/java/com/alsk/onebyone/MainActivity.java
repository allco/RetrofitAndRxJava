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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int TOTAL_ELEMENTS_COUNT = 206560;
    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Observable<Response<ResponseBody>> responseObservable;

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
        responseObservable = hugeJsonApi.get();
        final int[] counter = {0};
        final Gson gson = new GsonBuilder().create();
        binding.progressbar.setMax(TOTAL_ELEMENTS_COUNT);
        binding.progressbar.setIndeterminate(false);
        binding.progressbar.setProgress(0);
        compositeDisposable.add(responseObservable
                .flatMap(new Function<Response<ResponseBody>, Observable<Feature>>(){
                    @Override
                    public Observable<Feature> apply(@io.reactivex.annotations.NonNull Response<ResponseBody> response) throws Exception {
                        return convertObjectsStream(response, gson, Feature.class);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Feature>() {

                    @Override
                    public void onNext(Feature feature) {
                        counter[0]++;
                        binding.tvCounter.setText("Read elements counter: " + counter[0]);
                        binding.tvLastElement.setText("Last read element: " + gson.toJson(feature));
                        binding.tvStatus.setText("Used memory: " + getUsedMemoryInMb() + "Mb");
                        binding.progressbar.setProgress(counter[0]);
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
    private static <TYPE> Observable<TYPE> convertObjectsStream(final Response<ResponseBody> response, Gson gson, Class<TYPE> clazz) {
        return Observable.generate(() -> {
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

                }

        );


    }
}
