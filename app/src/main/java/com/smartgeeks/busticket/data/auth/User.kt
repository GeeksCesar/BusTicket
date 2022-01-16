package com.smartgeeks.busticket.data.auth

data class User(
    val descEmpresa: String,
    val idEmpresa: Int,
    val idRol: Int,
    val idUsuario: Int,
    val nombre: String,
    val nombreEmpresa: String,
    val rut: String = ""
)