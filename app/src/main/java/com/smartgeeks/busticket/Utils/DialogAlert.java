package com.smartgeeks.busticket.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.smartgeeks.busticket.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DialogAlert {

    public void showDialogFailed(Context context , String title, String message, int type){
        SweetAlertDialog alertDialog = new SweetAlertDialog(context, type);
                alertDialog.setTitleText(title)
                .setContentText(message)
                .show();

        Button button =  alertDialog.findViewById(R.id.confirm_button);
        button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
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
