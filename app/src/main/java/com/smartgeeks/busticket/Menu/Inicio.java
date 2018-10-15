package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Silla;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncService;


public class Inicio extends Fragment {

    Context context;
    View view;
    RelativeLayout relContenedor;
    Button btnNombreUsuario, btnTest, btnSend;

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
        btnTest = view.findViewById(R.id.btnTest);

        btnSend.setVisibility(View.GONE);
        btnTest.setVisibility(View.GONE);

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

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataDummy();
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

    private void saveDataDummy() {
        Ticket ticket = new Ticket();
        ticket.setId_remoto("");
        ticket.setCliente("Johan Mosquera");
        ticket.setHorario_id(1);
        ticket.setSub_partida(1);
        ticket.setSub_destino(2);
        ticket.setTipo_usuario(1);
        ticket.setSillas(4);
        ticket.setFecha("2018-10-10");
        ticket.setHora("23:00:00");
        ticket.setPrecio(12000);
        ticket.setEstado(0);
        ticket.setPendiente_insercion(1);
        ticket.save();

        Silla silla = new Silla();
        silla.setTicket(ticket.getId());
        silla.setNumero_silla(12);
        silla.save();
        Silla silla1 = new Silla();
        silla1.setTicket(ticket.getId());
        silla1.setNumero_silla(2);
        silla1.save();
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
