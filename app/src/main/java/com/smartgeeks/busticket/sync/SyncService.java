package com.smartgeeks.busticket.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.smartgeeks.busticket.Utils.Constantes;


public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getSimpleName();


    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            switch (action) {
                case Constantes.ACTION_RUN_LOCAL_SYNC:
                    handleLocalSync();
                    break;
                case Constantes.ACTION_RUN_REMOTE_SYNC:
                    handleRemoteSync();
                    break;
            }

        }
    }

    /**
     * Maneja la acción de ejecución de la sincronización Remota
     */
    private void handleRemoteSync() {
        try {
            OpsTicket.realizarSincronizacionRemota(getApplicationContext());
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Maneja la acción de ejecución de la sincronización Local
     */
    private void handleLocalSync() {
        try {
            // Construyo la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Sincronizando datos")
                    .setContentText("Procesando...");

            startForeground(1, builder.build());

            // Sincronizo los datos
            OpsRuta.realizarSincronizacionLocal(getApplicationContext());
            OpsHorario.realizarSincronizacionLocal(getApplicationContext());
            OpsVehiculo.realizarSincronizacionLocal(getApplicationContext());
            OpsSubrutas.realizarSincronizacionLocal(getApplicationContext());
            OpsTipoUsuario.realizarSincronizacionLocal(getApplicationContext());

            // Quito la notificacion de primer plano
            stopForeground(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
      //  Toast.makeText(this, "Servicio destruido...", Toast.LENGTH_SHORT).show();

        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constantes.ACTION_STOP_LOCAL_SYNC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
