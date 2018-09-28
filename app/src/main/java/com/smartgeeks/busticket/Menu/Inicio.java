package com.smartgeeks.busticket.Menu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.R;


public class Inicio extends Fragment {

    View view ;
    RelativeLayout relContenedor;

    MainActivity activity ;
    public Inicio() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_inicio, container, false);


        relContenedor  = view.findViewById(R.id.contentTicket);

        relContenedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity = (MainActivity) getActivity() ;
                activity.setFragment(2);
            }
        });

        return view;
    }

}
