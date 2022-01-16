package com.smartgeeks.busticket.api;

import com.smartgeeks.busticket.Modelo.Signin;
import com.smartgeeks.busticket.Modelo.TarifaUsuario;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("api/getLogin")
    Call<Signin> userLogin(@Field("email") String email, @Field("pass") String password);

    @GET("api/getTipoTarifas")
    Call<TarifaUsuario> allTarifas(@Query("id") int id);

}
