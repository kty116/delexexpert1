package com.delex.delexexpert.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface RetrofitService {

    String url = "http://delex.delta-on.com/";

    @FormUrlEncoded
    @POST("expert/login/setDeviceToken")
    Call<Object> getDeviceToken(@Field("userid") String userId, @Field("device_token") String pushToken);
}
