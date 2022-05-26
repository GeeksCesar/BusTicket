package com.smartgeeks.busticket.repository.auth

import com.smartgeeks.busticket.data.BasicResponse
import com.smartgeeks.busticket.data.api.AuthApi
import com.smartgeeks.busticket.data.models.auth.AuthResponse
import com.smartgeeks.busticket.data.models.auth.CompanyMessage
import com.smartgeeks.busticket.data.models.auth.RequestSessionLogs
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun userLogin(email: String, password: String): AuthResponse =
        authApi.userLogin(email, password)

    override suspend fun setLoginLogs(requestSessionLogs: RequestSessionLogs): BasicResponse =
        authApi.setLoginLogs(requestSessionLogs)

    override suspend fun checkLockedDevice(userId: Int, deviceID: String): Boolean =
        authApi.checkLockedDevice(userId, deviceID)

    override suspend fun getMessageCompany(companyId: Int): CompanyMessage =
        authApi.getMessageCompany(companyId)

    override suspend fun setUserStatus(userId: Int, deviceID: String, status: Int): BasicResponse =
        authApi.setUserStatus(userId, deviceID, status)
}