package com.smartgeeks.busticket.repository.auth

import com.smartgeeks.busticket.data.auth.AuthResponse

interface AuthRepository {
    suspend fun userLogin(email: String, password: String): AuthResponse
}