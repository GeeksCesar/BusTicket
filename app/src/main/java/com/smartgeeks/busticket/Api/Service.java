package com.smartgeeks.busticket.Api;

public class Service {

    public static final String TAG = "Test" ;

    public static final String URL = "http://testing.smartgeeks.com.co/busticket/" ;

    public static ApiService getApiService(){
        return RetrofitClient.getClient(URL).create(ApiService.class);
    }


    public static final String GET_VEHICULOS = "http://testing.smartgeeks.com.co/busticket/api/getVehiculos";
    public static final String GET_RUTAS = " http://testing.smartgeeks.com.co/busticket/api/getRutas?id=";
    public static final String GET_USUARIOS = " http://testing.smartgeeks.com.co/busticket/api/getTipoUsuarios";
    public static final String GET_INFO_VEHICULO = "http://testing.smartgeeks.com.co/busticket/api/getInfoVehiculo?id=";
    public static final String SILLAS_OCUPADAS = "http://testing.smartgeeks.com.co/busticket/api/getSillasOcupadas?id=";
    public static final String GET_HORARIO = " http://testing.smartgeeks.com.co/busticket/api/getHorario?id=";
    public static final String GET_PARADEROS = " http://testing.smartgeeks.com.co/busticket/api/getParadero?id=";

}
