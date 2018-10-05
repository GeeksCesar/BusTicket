package com.smartgeeks.busticket.Menu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;


public class Inicio extends Fragment {

    View view ;
    RelativeLayout relContenedor;
    Button btnNombreUsuario;

    MainActivity activity ;

    String nameUsuario ;


    public Inicio() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_inicio, container, false);

        btnNombreUsuario = view.findViewById(R.id.btnNameUsuario);
        relContenedor  = view.findViewById(R.id.contentTicket);

        nameUsuario = UsuarioPreferences.getInstance(getActivity()).getNombre();
        btnNombreUsuario.setText(nameUsuario);

        relContenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity() ;
                activity.setFragment(2);
            }
        });

        btnNombreUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity() ;
                activity.setFragment(0);
            }
        });

        return view;
    }

}
