package com.smartgeeks.busticket.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.smartgeeks.busticket.Objects.RutaPojo;

public class RutaPreferences {

    public static final String SHARED_PREF_NAME = "Ruta";
    public static final String PREFERENCES_PRINT = "PRINT" ;

    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String ID_RUTA = "ID_RUTA";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String HORARIO = "HORARIO";
    public static final String INFORMACION = "INFORMACION";
    public static final String ESTADO = "ESTADO";

    public static final String NAME_PRINT = "NAME_PRINT";
    public static final String ESTADO_PRINT = "ESTADO_PRINT";

    private static Context mContext;
    private static RutaPreferences mInstance;

    private RutaPreferences(Context context) {
        mContext = context;
    }

    public static synchronized RutaPreferences getInstance(Context context) {
        if (mInstance == null)
            mInstance = new RutaPreferences(context);
        return mInstance;
    }

    public boolean rutaPreferences(RutaPojo ruta) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(ID_RUTA, ruta.getRuta_id());
        editor.putInt(ID_RUTA_DISPONIBLE, ruta.getRuta_disponible_id());
        editor.putInt(ID_VEHICULO, ruta.getVehiculo_id());
        editor.putInt(ID_HORARIO, ruta.getHorario_id());
        editor.putString(HORARIO, ruta.getHorario());
        editor.putString(INFORMACION, ruta.getInformacion());
        editor.putBoolean(ESTADO, ruta.getStatus_ruta());
        editor.apply();

        return true;
    }

    public int getIdVehiculo() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_VEHICULO, 0);
    }

    public int getIdRuta() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_RUTA, 0);
    }

    public int getIdRutaDisponible() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_RUTA_DISPONIBLE, 0);
    }

    public int getIdHorario() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_HORARIO, 0);
    }

    public String getHora() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(HORARIO, "");
    }

    public String getInformacion() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(INFORMACION, "");
    }

    public void setEstadoRuta(boolean estado){
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ESTADO, estado);
        editor.apply();
    }

    public boolean getEstadoRuta() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ESTADO, false);
    }

    public String getNamePrint(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES_PRINT, Context.MODE_PRIVATE);
        return sharedPreferences.getString(NAME_PRINT, "");
    }

    public boolean getEstadoPrint() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES_PRINT, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ESTADO_PRINT, false);
    }
}
