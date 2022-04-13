package com.smartgeeks.busticket.data.models.auth

data class AuthResponse(
    val error: Boolean,
    val message: String,
    val user: User
)