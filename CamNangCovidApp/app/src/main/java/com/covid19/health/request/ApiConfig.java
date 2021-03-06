package com.covid19.health.request;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiConfig {
    @Multipart
    @POST("/uploadAudio")
    Call<ServerResponse> uploadFile(@Part("audio\"; filename=\"record.wav\" ") RequestBody audio_file,
                                    @Part("test_image\"; filename=\"image.png\" ") RequestBody test_image,
                                  @Part("predict_covid") RequestBody predict_covid);

    @Multipart
    @POST("/uploadAudio")
    Call<ServerResponse> sendError(@Part("audio\"; filename=\"record.wav\" ") RequestBody audio_file,
                                    @Part("predict_cough") RequestBody predict_cough);

}
