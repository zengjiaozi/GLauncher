package com.godinsec.privacy.net;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Seeker on 2016/9/13.
 */

public interface FactoryInters {

    interface PostClickAppInfoServer{
        @Headers({"Content-Type: application/json","Accept: application/json"})
        @POST("UserApp")
        Call<ResponseBody> postClickAppInfo(@Body RequestBody requestBody);
    }

}
