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
    public static final String GET_TIPO_USUARIO = IP + PROYECT + "/web/obtener_tipo_usuario.php";

    // Estados del servicio
    public static final String SUCCESS = "1";
    public static final String FAILED = "2";

    /**
     * Constantes para ProgressIntentService
     */
    public static final String ACTION_RUN_ISERVICE = "com.example.services.action.RUN_INTENT_SERVICE";
    public static final String ACTION_PROGRESS_EXIT = "com.example.services.action.PROGRESS_EXIT";

    public static final String EXTRA_PROGRESS = "com.example.services.extra.PROGRESS";



}
