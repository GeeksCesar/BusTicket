package com.smartgeeks.busticket.repository.auth

import com.smartgeeks.busticket.data.BasicResponse
import com.smartgeeks.busticket.data.auth.AuthResponse
import com.smartgeeks.busticket.data.auth.CompanyMessage
import com.smartgeeks.busticket.data.auth.LockedDevices
import com.smartgeeks.busticket.data.auth.RequestSessionLogs

interface AuthRepository {
    suspend fun userLogin(email: String, password: String): AuthResponse
    suspend fun setLoginLogs(requestSessionLogs: RequestSessionLogs): BasicResponse
    suspend fun getLockedDevices(userId: Int): LockedDevices
    suspend fun getMessageCompany(companyId: Int): CompanyMessage
}