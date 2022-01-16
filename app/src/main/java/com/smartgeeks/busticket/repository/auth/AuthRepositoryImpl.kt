package com.smartgeeks.busticket.repository.auth

import com.smartgeeks.busticket.api.AuthApi
import com.smartgeeks.busticket.data.auth.AuthResponse
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun userLogin(email: String, password: String): AuthResponse =
        authApi.userLogin(email, password)
}