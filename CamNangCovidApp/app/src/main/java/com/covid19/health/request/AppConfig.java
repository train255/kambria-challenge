package com.covid19.health.request;

//import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//import okhttp3.logging.HttpLoggingInterceptor;

public class AppConfig {

    private static String BASE_URL = "https://b30a-2402-800-619d-dac2-00-2.ngrok.io/";

    public static Retrofit getRetrofit() {
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        return new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}