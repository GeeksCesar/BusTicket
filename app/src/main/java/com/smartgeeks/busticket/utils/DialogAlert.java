package com.smartgeeks.busticket.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.smartgeeks.busticket.R;

public class DialogAlert {

    public static void showDialogFailed(Context context, String title, String message, int type) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(context, type);
        alertDialog.setTitleText(title)
                .setContentText(message)
                .show();

        Button button = alertDialog.findViewById(R.id.confirm_button);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_main));
    }


    public void showDialogErrorConexion(Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("Error")
                .setContentText("No hay conexión de \n internet")
                .setCustomImage(R.mipmap.wifi_error)
                .show();
    }


    public static boolean verificaConexion(Context ctx) {
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
