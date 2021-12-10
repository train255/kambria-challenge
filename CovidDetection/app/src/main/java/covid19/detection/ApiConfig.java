package covid19.detection;

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
    Call<ServerResponse> uploadFile(@Part("audio\"; filename=\"record.wav\" ") RequestBody file,
                                  @Part("test_type") RequestBody fname);
}
