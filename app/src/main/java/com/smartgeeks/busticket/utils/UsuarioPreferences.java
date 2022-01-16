package com.smartgeeks.busticket.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.smartgeeks.busticket.data.auth.User;

public class UsuarioPreferences {

    public static final String SHARED_PREF_NAME = "Usuario";
    public static final String KEY_ROL = "id_rol";
    public static final String KEY_ID_USER = "id_user";
    public static final String KEY_SESSION = "session";
    public static final String KEY_NAME = "name";
    public static final String KEY_RUT = "rut";
    public static final String KEY_ID_EMPRESA = "id_empresa";
    public static final String KEY_NAME_EMPRESA = "name_empresa";
    public static final String KEY_DESC_EMPRESA = "desc_empresa";
    public static final String KEY_ROLE_VENTA = "role_venta"; // Operador o Conductor
    public static final String KEY_ID_TIPO_USUARIO = "id_tipo_usuario";


    private static Context mContext;
    private static UsuarioPreferences mInstance;

    private UsuarioPreferences(Context context) {
        mContext = context;
    }

    public static synchronized UsuarioPreferences getInstance(Context context) {
        if (mInstance == null)
            mInstance = new UsuarioPreferences(context);
        return mInstance;
    }

    public boolean userPreferences(User user) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_ROL, user.getIdRol());
        editor.putInt(KEY_ID_USER, user.getIdUsuario());
        editor.putString(KEY_NAME, user.getNombre());
        editor.putInt(KEY_ID_EMPRESA, user.getIdEmpresa());
        editor.putString(KEY_RUT, user.getRut());
        editor.putString(KEY_NAME_EMPRESA, user.getNombreEmpresa());
        editor.putString(KEY_DESC_EMPRESA, user.getDescEmpresa());

        editor.apply();

        return true;
    }

    public boolean userPreferences(com.smartgeeks.busticket.Objects.User user) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_ROL, user.getIdRol());
        editor.putInt(KEY_ID_USER, user.getIdUsuario());
        editor.putString(KEY_NAME, user.getNombre());
        editor.putInt(KEY_ID_EMPRESA, user.getId_empresa());
        editor.putString(KEY_RUT, user.getRut());
        editor.putString(KEY_NAME_EMPRESA, user.getNombreEmpresa());
        editor.putString(KEY_DESC_EMPRESA, user.getDescEmpresa());

        editor.apply();

        return true;
    }

    public String getSessionUser() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SESSION, "SessionFailed");
    }

    public String getNombre() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NAME, "");
    }

    public int getIdUser() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ID_USER, 0);
    }

    public int getIdEmpresa() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ID_EMPRESA, 0);
    }

    public int getIdTipoUsuario() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_ID_TIPO_USUARIO, 0);
    }

    public String getRut() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_RUT, "");
    }

    public String getNombreEmpresa() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NAME_EMPRESA, "");
    }

    public String getDescEmpresa() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_DESC_EMPRESA, "Su mejor compañía");
    }

    public void setDescEmpresa(String msg) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DESC_EMPRESA, msg);
        editor.apply();
    }

    public void setRoleVenta(String role) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ROLE_VENTA, role);
        editor.apply();
    }

    public void setIdTipoUsuario(int idTipoUsuario) {
        SharedPreferences preferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_ID_TIPO_USUARIO, idTipoUsuario);
        editor.apply();
    }

    public String getRoleVenta() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ROLE_VENTA, "operador");
    }


}

