package com.smartgeeks.busticket.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.InternetCheck;


public class SyncService extends IntentService {

    private static final String TAG = SyncService.class.getSimpleName();
    private Context context;


    public SyncService() {
        super("SyncService");
        this.context = SyncService.this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            switch (action) {
                case Constantes.ACTION_RUN_LOCAL_SYNC:
                    handleLocalSync();
                    new InternetCheck(new InternetCheck.Consumer() {
                        @Override
                        public void accept(Boolean internet) {
                            Toast.makeText(context, "Sincronización finalizada", Toast.LENGTH_SHORT).show();
                        }
                    }).execute();

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
            // Sincronizo los datos
            OpsTipoUsuario.realizarSincronizacionLocal(getApplicationContext());
            OpsTarifaParadero.realizarSincronizacionLocal(getApplicationContext());
            OpsVehiculo.realizarSincronizacionLocal(getApplicationContext());
            OpsRuta.realizarSincronizacionLocal(getApplicationContext());
            OpsParaderos.realizarSincronizacionLocal(getApplicationContext());
            OpsHorario.realizarSincronizacionLocal(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "Servicio destruido...");
        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constantes.ACTION_FINISH_SYNC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
