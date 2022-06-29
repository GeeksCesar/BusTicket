package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.BasicResponse
import com.smartgeeks.busticket.data.models.auth.AuthResponse
import com.smartgeeks.busticket.data.models.auth.CompanyMessage
import com.smartgeeks.busticket.data.models.auth.RequestSessionLogs
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
    @POST("api/checkLockedDevice")
    suspend fun checkLockedDevice(
        @Field("userId") userId: Int,
        @Field("deviceId") deviceID: String
    ): Boolean

    @FormUrlEncoded
    @POST("api/setUserStatus/")
    suspend fun setUserStatus(
        @Field("userId") userId: Int,
        @Field("deviceID") deviceID: String,
        @Field("status") status: Int,
    ) : BasicResponse

}