package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Silla;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Inicio extends Fragment {

    Context context;
    View view;
    RelativeLayout relContenedor;
    Button btnNombreUsuario, btnSend;

    MainActivity activity;

    String nameUsuario;


    public Inicio() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_inicio, container, false);

        context = getContext();
        btnNombreUsuario = view.findViewById(R.id.btnNameUsuario);
        relContenedor = view.findViewById(R.id.contentTicket);
        btnSend = view.findViewById(R.id.btnSend);

        nameUsuario = UsuarioPreferences.getInstance(getActivity()).getNombre();
        btnNombreUsuario.setText(nameUsuario);

        relContenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity();
                activity.setFragment(2);
            }
        });

        btnNombreUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity();
                activity.setFragment(0);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });

        return view;
    }


    /**
     * Ejecutar el servicio de Sincronización Remota
     */
    private void sendData() {
        Intent sync = new Intent(context, SyncService.class);
        sync.setAction(Constantes.ACTION_RUN_REMOTE_SYNC);
        getActivity().startService(sync);
    }

}
