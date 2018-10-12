package com.smartgeeks.busticket.Utils;

public class Constantes {

    // Valores para la columna ESTADO (Tablas SQLite)
    public static final int ESTADO_OK = 0;
    public static final int ESTADO_SYNC = 1;

    //private static final String IP = "http://10.0.3.2";  192.168.0.104
    private static final String IP = "http://192.168.0.104/";
    private static final String PROYECT = "busticket";

    /**
     * URLs del Web Service
     */
    public static final String GET_HORARIOS = IP + PROYECT + "/web/obtener_horarios.php";
    public static final String GET_RUTAS = IP + PROYECT + "/web/obtener_rutas.php";
    public static final String GET_VEHICULOS = IP + PROYECT + "/web/obtener_vehiculos.php";
    public static final String GET_SUBRUTAS = IP + PROYECT + "/web/obtener_subrutas.php";
    public static final String GET_TIPO_USUARIO = IP + PROYECT + "/web/obtener_tipos_usuario.php";

    public static final String INSERT_TICKET = IP + PROYECT + "/web/insertar_ticket.php";

    /**
     * Campos de las repuestas JSON
     */
    public static final String RUTAS = "rutas";
    public static final String HORARIOS = "horarios";
    public static final String VEHICULOS = "vehiculos";
    public static final String SUBRUTAS = "subrutas";
    public static final String TIPO_USUARIO = "tipos_usuario";

    // Estados del servicio
    public static final String SUCCESS = "1";
    public static final String FAILED = "0";
    public static final String ESTADO = "estado";
    public static final String MENSAJE = "mensaje";

    /**
     * Constantes para SyncService (Sincronización)
     */
    public static final String ACTION_RUN_LOCAL_SYNC = "com.smartgeeks.busticket.action.ACTION_RUN_LOCAL_SYNC";
    public static final String ACTION_STOP_LOCAL_SYNC = "com.smartgeeks.busticket.action.ACTION_STOP_LOCAL_SYNC";

    public static final String ACTION_RUN_REMOTE_SYNC = "com.smartgeeks.busticket.action.ACTION_RUN_REMOTE_SYNC";
    public static final String ACTION_STOP_REMOTE_SYNC = "com.smartgeeks.busticket.action.ACTION_STOP_REMOTE_SYNC";

    public static final String EXTRA_PROGRESS = "com.example.services.extra.PROGRESS";

    /**
     * Tipo de cuenta para la sincronización
     */
    public static final String ACCOUNT_TYPE = "com.smartgeeks.busticket.account";


}
