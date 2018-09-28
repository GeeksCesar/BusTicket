package com.smartgeeks.busticket.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.smartgeeks.busticket.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogAlert {

    public void showDialogFailed(Context context , String title, String message, int type){
        new SweetAlertDialog(context, type)
                .setTitleText(title)
                .setContentText(message)
                .show();
    }


    public void showDialogErrorConexion(Context context){
        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Error")
                .setContentText("No hay conexión de \n internet")
                .setCustomImage(R.mipmap.wifi_error)
                .show();
    }


    public boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        // No sólo wifi, también GPRS
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        // este bucle debería no ser tan ñapa
        for (int i = 0; i < 2; i++) {
            // ¿Tenemos conexión? ponemos a true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }
}
