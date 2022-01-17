package com.smartgeeks.busticket.repository.auth

import com.smartgeeks.busticket.data.BasicResponse
import com.smartgeeks.busticket.data.auth.AuthResponse
import com.smartgeeks.busticket.data.auth.CompanyMessage
import com.smartgeeks.busticket.data.auth.LockedDevices
import com.smartgeeks.busticket.data.auth.RequestSessionLogs

interface AuthRepository {
    suspend fun userLogin(email: String, password: String): AuthResponse
    suspend fun setLoginLogs(requestSessionLogs: RequestSessionLogs): BasicResponse
    suspend fun checkLockedDevice(userId: Int, deviceID: String): Boolean
    suspend fun getMessageCompany(companyId: Int): CompanyMessage
    suspend fun setUserStatus(userId: Int, deviceID: String, status: Int): BasicResponse
}