package com.godinsec.privacy.net;

import com.godinsec.privacy.bean.Upload;
import com.godinsec.privacy.utils.LogUtils;
import com.godinsec.privacy.utils.Validate;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Seeker on 2016/9/13.
 */

public class RetrofitManger extends RetrofitBase{

    private static final String TAG = "RetrofitManger";

    private RetrofitManger() {
        super();
    }

    private static final class Factory{
        private static final RetrofitManger instance = new RetrofitManger();
    }

    public static RetrofitManger getInstance(){
        return Factory.instance;
    }

    /**
     * 上传提交点击打开的应用信息
     * @param upload
     */
    public void postClickAppInfo(Upload upload){

        LogUtils.v(TAG,"enter func postClickAppInfo...");

        Validate.notNull(upload,"upload");

        FactoryInters.PostClickAppInfoServer server = retrofit.create(FactoryInters.PostClickAppInfoServer.class);

        MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
        Gson gson = new Gson();

        String requestJsonStr = gson.toJson(upload);

        LogUtils.d(TAG,"requestJsonStr = "+requestJsonStr);

        RequestBody requestBody = RequestBody.create(jsonType,requestJsonStr);

        Call<ResponseBody> call = server.postClickAppInfo(requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                LogUtils.i(TAG,"result = "+response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LogUtils.e(TAG,t.getMessage());
            }
        });

    }

}
