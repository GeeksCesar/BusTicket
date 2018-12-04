package com.smartgeeks.busticket.Utils;

public class Constantes {

    // Valores para la columna ESTADO (Tablas SQLite)
    public static final int ESTADO_OK = 0;
    public static final int ESTADO_SYNC = 1;

    //private static final String IP = "http://10.0.3.2";  192.168.0.104
    private static final String IP = "http://test.appbusticket.com/";
    private static final String PROYECT = "apisync/";

    /**
     * URLs del Web Service
     */
    public static final String GET_TIPOS_USUARIO = IP + PROYECT + "getTiposUsuario/";
    public static final String GET_TARIFAS_PARADERO = IP + PROYECT + "getTarifasParadero/";
    /**
     * Los siguientes se consultan por id de empresa
     */
    public static final String GET_VEHICULOS = IP + PROYECT + "getVehiculos/";
    public static final String GET_RUTAS = IP + PROYECT + "getRutas/";
    public static final String GET_PARADEROS = IP + PROYECT + "getParaderos/";
    public static final String GET_HORARIOS = IP + PROYECT + "getHorarios/";
    public static final String GET_SILLAS_OCUPADAS = IP + PROYECT + "getSillasOcupadas/";

    public static final String INSERT_TICKET = IP + PROYECT + "saveTicket/";

    public static final String GET_SUBRUTAS = "horarios";

    /**
     * Campos de las repuestas JSON
     */
    public static final String TIPOS_USUARIO = "tipos_usuario";
    public static final String TARIFAS_PARADERO = "tarifas_paradero";
    public static final String VEHICULOS = "vehiculos";
    public static final String RUTAS = "rutas";
    public static final String PARADEROS = "paraderos";
    public static final String HORARIOS = "horarios";
    public static final String SILLAS_OCUPADAS = "sillas_ocupadas";

    public static final String SUBRUTAS = "horarios";


    // Estados del servicio
    public static final String SUCCESS = "1";
    public static final String FAILED = "0";
    public static final String ESTADO = "estado";
    public static final String MENSAJE = "mensaje";

    /**
     * Constantes para SyncService (Sincronización)
     */
    public static final String ACTION_RUN_LOCAL_SYNC = "com.smartgeeks.busticket.action.ACTION_RUN_LOCAL_SYNC";
    public static final String ACTION_FINISH_SYNC = "com.smartgeeks.busticket.action.ACTION_FINISH_SYNC";
    public static final String ACTION_RUN_REMOTE_SYNC = "com.smartgeeks.busticket.action.ACTION_RUN_REMOTE_SYNC";


    public static final String EXTRA_PROGRESS = "com.example.services.extra.PROGRESS";

    /**
     * Tipo de cuenta para la sincronización
     */
    public static final String ACCOUNT_TYPE = "com.smartgeeks.busticket.account";


}
