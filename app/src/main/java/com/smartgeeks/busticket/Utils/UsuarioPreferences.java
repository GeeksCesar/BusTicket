package com.smartgeeks.busticket.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartgeeks.busticket.Objcect.User;

public class UsuarioPreferences {

    public static final String SHARED_PREF_NAME = "Usuario";
    public static final String KEY_ROL = "id_rol";
    public static final String KEY_ID_USER = "id_user";
    public static final String KEY_SESSION = "session";
    public static final String KEY_NAME = "name";
    public static final String KEY_DOCUMENTO = "documento" ;



    private static Context mContext ;
    private static UsuarioPreferences mInstance;

    private UsuarioPreferences(Context context){
        mContext = context;
    }

    public static synchronized UsuarioPreferences getInstance(Context context){
        if (mInstance == null)
            mInstance = new UsuarioPreferences(context);
        return mInstance;
    }

    public boolean userPreferences(User user){
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_ROL, user.getIdRol());
        editor.putInt(KEY_ID_USER, user.getIdUsuario());
        editor.putString(KEY_DOCUMENTO, user.getDocu());
        editor.putString(KEY_NAME, user.getNombre());

        editor.apply();

        return true;
    }

    public String getSessionUser(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SESSION, "SessionFailed");
    }
}
