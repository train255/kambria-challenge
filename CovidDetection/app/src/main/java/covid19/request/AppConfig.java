package covid19.request;

 import retrofit2.Retrofit;
 import retrofit2.converter.gson.GsonConverterFactory;

public class AppConfig {

    private static String BASE_URL = "https://f658-2402-800-6117-d8e9-00-1.ngrok.io/";

    public static Retrofit getRetrofit() {

        return new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}