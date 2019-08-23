package com.smartgeeks.busticket.Menu;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.Helpers;
import com.smartgeeks.busticket.Utils.InternetCheck;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncServiceRemote;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Inicio extends Fragment {

    static final String TAG = Inicio.class.getSimpleName();
    Context context;
    View view;
    RelativeLayout relContenedor;
    Button btnNombreUsuario, btnSync;
    TextView tvTicketToSync;
    ProgressBar progresBar;
    long tickets_to_sync;

    MainActivity activity;

    String nameUsuario;
    private boolean state_sync = false;


    public Inicio() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_inicio, container, false);

        context = getContext();
        btnNombreUsuario = view.findViewById(R.id.btnNameUsuario);
        relContenedor = view.findViewById(R.id.contentTicket);
        btnSync = view.findViewById(R.id.btnSync);
        tvTicketToSync = view.findViewById(R.id.tv_tickets_to_sync);
        progresBar = view.findViewById(R.id.progresBar);

        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(Constantes.ACTION_RUN_REMOTE_SYNC);
        filter.addAction(Constantes.EXTRA_PROGRESS);
        filter.addAction(Constantes.ACTION_FINISH_REMOTE_SYNC);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                receiver, filter);


        nameUsuario = UsuarioPreferences.getInstance(getActivity()).getNombre();
        btnNombreUsuario.setText(nameUsuario);

        relContenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity();
                activity.setFragment(2);

                // Realizar sincronización remota de datos locales
                remoteSync();
            }
        });

        btnNombreUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity();
                activity.setFragment(0);
                //new SaveTicketTest().execute();
            }
        });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new InternetCheck(new InternetCheck.Consumer() {
                    @Override
                    public void accept(Boolean internet) {
                        if (internet) {
                            // Oculto el botón y muestro la barra de progreso
                            progresBar.setVisibility(View.VISIBLE);
                            view.setVisibility(View.GONE);
                            remoteSync();

                        } else {
                            DialogAlert.showDialogFailed(context, "Error",
                                    "No hay conexión a internet.", SweetAlertDialog.WARNING_TYPE);
                        }
                    }
                }).execute();
            }
        });

        return view;
    }

    private void saveTicketLocal(double precio) {
        Log.e(TAG, "Ticket Guardado Localmente");
        Ticket ticket = new Ticket();
        ticket.setIdRemoto("");
        ticket.setParadaInicio(53);
        ticket.setParadaDestino(55);
        ticket.setIdRutaDisponible(50);
        ticket.setIdOperador(UsuarioPreferences.getInstance(context).getIdUser());
        ticket.setHoraSalida("06:30:00");
        ticket.setTipoUsuario(59);
        ticket.setFecha(Helpers.getCurrentDate());
        ticket.setHora(Helpers.getCurrentTime());
        ticket.setCantPasajes(1);
        ticket.setTotalPagar(precio);
        ticket.setEstado(0);
        ticket.setPendiente(Constantes.ESTADO_SYNC);

        ticket.save();
        // El estado = 0 y estado_sync = 1, para cuando se inicie la sincronización remota
        // se cambie el estado = 1
    }

    @Override
    public void onStart() {
        super.onStart();
        new LoadTicket2Sync().execute();
    }

    private void obtenerRegistrosSincronizar() {

        // Si hay registros por sincronizar, muestro los el boton sincronizar
        if (tickets_to_sync > 0){
            tvTicketToSync.setText(tickets_to_sync + " Tickets por Sincronizar.");
            if (!state_sync)
                btnSync.setVisibility(View.VISIBLE);
            else
                btnSync.setVisibility(View.GONE);

        } else {
            tvTicketToSync.setText("No hay Tickets por sincronizar");
            btnSync.setVisibility(View.GONE);
            progresBar.setVisibility(View.GONE);
        }
    }


    /**
     * Ejecutar el servicio de Sincronización Remota
     */
    private void remoteSync() {

        if (!state_sync){
            Intent sync = new Intent(context, SyncServiceRemote.class);
            sync.setAction(Constantes.ACTION_RUN_REMOTE_SYNC);
            getActivity().startService(sync);
        }

    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        Handler handler = new Handler();

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case Constantes.ACTION_RUN_REMOTE_SYNC:

                    state_sync = intent.getBooleanExtra(Constantes.EXTRA_PROGRESS, false);

                    if (state_sync){
                        progresBar.setVisibility(View.VISIBLE);
                        tickets_to_sync--;
                        obtenerRegistrosSincronizar();
                    } else {
                        Log.e(TAG, "Sincronización Remota Finalizada.");
                        progresBar.setVisibility(View.GONE);
                        new LoadTicket2Sync().execute();
                    }


                    break;

                case Constantes.ACTION_FINISH_REMOTE_SYNC:
                    Log.e(TAG, "Sincronización Remota Finalizada.");
                    progresBar.setVisibility(View.GONE);
                    new LoadTicket2Sync().execute();
                    break;
            }
        }
    }


    private class LoadTicket2Sync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            // Consultar registros por sincronizar
            String[] values = {"1", ""+Constantes.ESTADO_SYNC};
            tickets_to_sync = Ticket.count(Ticket.class,
                    "pendiente = ? AND estado = ?", values);

            Log.e(TAG, "Se encontraron " + tickets_to_sync + " registros por Sincronizar.");

            return "Consultando datos";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            obtenerRegistrosSincronizar();
        }

    }

    /**
     * Clase de pruebas para guardar Tickets
     */
    private class SaveTicketTest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            double precios[] = {2000,1500,1200,800};
            // Consultar registros por sincronizar
            int indice = 0;
            for (int i = 0; i < 52; i++){

                if (indice > 3)
                    indice = 0;
                saveTicketLocal(precios[indice]);
                indice++;
            }

            return "Consultando datos";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show();
        }

    }


}
