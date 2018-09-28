package com.smartgeeks.busticket.Api;

import com.smartgeeks.busticket.Modelo.Signin;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("api/getLogin")
    Call<Signin> userLogin(@Field("email") String email , @Field("pass") String password);
}
