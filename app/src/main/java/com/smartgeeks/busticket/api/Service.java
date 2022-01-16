package com.smartgeeks.busticket.api;

public class Service {

    public static final String TAG = "Test";

    public static final String URL = "https://mi.appbusticket.com/";

    public static ApiService getApiService() {
        return RetrofitClient.getClient(URL).create(ApiService.class);
    }

    public static final String GET_VEHICULOS = URL + "api/getVehiculos?id=";
    public static final String GET_RUTAS = URL + "api/getRutas?id=";
    public static final String GET_USUARIOS = URL + "api/getTipoUsuarios";
    public static final String GET_INFO_VEHICULO = URL + "Api/getInfoVehiculo?id=";
    public static final String SILLAS_OCUPADAS = URL + "api/getSillasOcupadas/";
    public static final String GET_HORARIO = URL + "api/getHorario?id=";
    public static final String GET_PARADEROS = URL + "api/getParadero?id=";
    public static final String GET_PARADEROS_FIN = URL + "api/getParaderoFinal/";

    // Registra el ticket con seleccion de asiento
    public static final String SET_TICKET_ASIENTO = URL + "api/setTicketAsientoNew";

    // Obtiene el consecutivo del ticket
    public static final String SET_TICKET_PIE = URL + "api/setTicketPie";

    // Obtiene el consecutivo del ticket 66-888888-765652
    public static final String SET_TICKET_PIE_TEST = URL + "api/setTicketPieTest";

    public static final String GET_PRECIO_TIQUETE = URL + "api/getPrecio/";
    public static final String SET_LIBERAR_SILLA= URL + "api/getLiberarSillasDia/";

}
