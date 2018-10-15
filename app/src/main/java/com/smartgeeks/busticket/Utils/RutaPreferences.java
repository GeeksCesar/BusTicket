package com.smartgeeks.busticket.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartgeeks.busticket.Objcect.Ruta;
import com.smartgeeks.busticket.Objcect.User;

public class RutaPreferences {

    public static final String SHARED_PREF_NAME = "Ruta";

    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String ID_RUTA = "ID_RUTA";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String HORARIO = "HORARIO";
    public static final String INFORMACION = "INFORMACION";
    public static final String ESTADO = "ESTADO";

    private static Context mContext ;
    private static RutaPreferences mInstance;

    private RutaPreferences(Context context){
        mContext = context;
    }

    public static synchronized RutaPreferences getInstance(Context context){
        if (mInstance == null)
            mInstance = new RutaPreferences(context);
        return mInstance;
    }

    public boolean rutaPreferences(Ruta ruta){
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(ID_RUTA, ruta.getRuta_id());
        editor.putInt(ID_RUTA_DISPONIBLE, ruta.getRuta_id());
        editor.putInt(ID_VEHICULO, ruta.getVehiculo_id());
        editor.putInt(ID_HORARIO, ruta.getHorario_id());
        editor.putString(HORARIO, ruta.getHorario());
        editor.putString(INFORMACION, ruta.getInformacion());
        editor.putBoolean(ESTADO, ruta.getStatus_ruta());
        editor.apply();

        return true;
    }

    public int getIdVehiculo(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_VEHICULO, 0);
    }

    public int getIdRuta(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_RUTA, 0);
    }
    public int getIdRutaDisponible(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_RUTA_DISPONIBLE, 0);
    }
    public int getIdHorario(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(ID_HORARIO, 0);
    }

    public String getHora(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(HORARIO, "");
    }

    public String getInformacion(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(INFORMACION, "");
    }

    public boolean getEstadoRuta(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ESTADO, false);
    }
}