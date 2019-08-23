package com.smartgeeks.busticket.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartgeeks.busticket.Utils.Constantes;


public class SyncServiceLocal extends IntentService {

    private static final String TAG = SyncServiceLocal.class.getSimpleName();
    private Context context;


    public SyncServiceLocal() {
        super(TAG);
        this.context = SyncServiceLocal.this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.e(TAG, "SyncServiceLocal creado...");

            if (Constantes.ACTION_RUN_LOCAL_SYNC.equals(action)) {
                handleLocalSync();
            }

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
        //Intent localIntent = new Intent(Constantes.ACTION_FINISH_LOCAL_SYNC);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.e(TAG, "SyncServiceLocal destruido...");
    }

}
