package com.smartgeeks.busticket.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.smartgeeks.busticket.utils.Constantes;


public class SyncServiceRemote extends IntentService {

    private static final String TAG = SyncServiceRemote.class.getSimpleName();
    private Context context;


    public SyncServiceRemote() {
        super(TAG);
        this.context = SyncServiceRemote.this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.e(TAG, "...RemoteSyncService creado...");

            if (Constantes.ACTION_RUN_REMOTE_SYNC.equals(action)) {
                handleRemoteSync();
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


    @Override
    public void onDestroy() {
        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constantes.ACTION_FINISH_REMOTE_SYNC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Log.e(TAG, "...RemoteSyncService destruido...");
    }

}
