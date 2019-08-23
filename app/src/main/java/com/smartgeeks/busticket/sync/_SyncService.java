package com.smartgeeks.busticket.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartgeeks.busticket.Utils.Constantes;


public class _SyncService extends IntentService {

    private static final String TAG = _SyncService.class.getSimpleName();
    private Context context;


    public _SyncService() {
        super("SyncService");
        this.context = _SyncService.this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.e(TAG, "Servicio creado...");

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
        // Emisión para avisar que se terminó el servicio
        //Intent localIntent = new Intent(Constantes.ACTION_FINISH_SYNC);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.e(TAG, "Servicio destruido...");
    }

}
