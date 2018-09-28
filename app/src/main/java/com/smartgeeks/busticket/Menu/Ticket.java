package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.smartgeeks.busticket.R;

import org.json.JSONArray;

import java.util.ArrayList;

public class Ticket extends Fragment {

    View view ;
    Context context;
    Spinner spPlaca, spRuta, spHorarios ;

    private JSONArray resultPlaca;
    private JSONArray resultRuta;
    private JSONArray resultHorarios;
    private ArrayList<String> listPlacas;
    private ArrayList<String> listRuta;
    private ArrayList<String> listHorario;

    //VOLLEY
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int id_placa, id_ruta, id_horario;

    public Ticket() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_ticket, container, false);

        spPlaca = view.findViewById(R.id.spPlaca);
        spRuta = view.findViewById(R.id.spRutas);
        spHorarios = view.findViewById(R.id.spHorarios);

        




        return view;
    }

}
