package com.smartgeeks.busticket.data.auth

data class AuthResponse(
    val error: Boolean,
    val message: String,
    val user: User
)