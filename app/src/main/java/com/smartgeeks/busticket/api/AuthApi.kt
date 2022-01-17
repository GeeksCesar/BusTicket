package com.smartgeeks.busticket.api

import com.smartgeeks.busticket.data.BasicResponse
import com.smartgeeks.busticket.data.auth.AuthResponse
import com.smartgeeks.busticket.data.auth.CompanyMessage
import com.smartgeeks.busticket.data.auth.LockedDevices
import com.smartgeeks.busticket.data.auth.RequestSessionLogs
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @GET("apisync/getMsgEmpresa/{companyId}")
    suspend fun getMessageCompany(
        @Path("companyId") companyId: Int
    ) : CompanyMessage

    @FormUrlEncoded
    @POST("api/getLogin")
    suspend fun userLogin(
        @Field("email") email: String,
        @Field("pass") password: String
    ): AuthResponse

    @POST("api/setLoginLogs")
    suspend fun setLoginLogs(
        @Body requestSessionLogs: RequestSessionLogs
    ): BasicResponse

    @FormUrlEncoded
    @POST("api/getLockedDevices")
    suspend fun getLockedDevices(
        @Field("userId") userId: Int
    ): LockedDevices
}