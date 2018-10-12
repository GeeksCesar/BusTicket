package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Ticket extends Fragment {

    View view;
    Context context;
    Spinner spPlaca, spRuta, spHorarios;
    Button btnSiguiente;

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

    int id_vehiculo, id_ruta, id_horario;
    String placa, ruta, horario;

    public Ticket() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_ticket, container, false);

        context = getActivity();
        requestQueue = Volley.newRequestQueue(context);

        btnSiguiente = view.findViewById(R.id.btnNext);
        spPlaca = view.findViewById(R.id.spPlaca);
        spRuta = view.findViewById(R.id.spRutas);
        spHorarios = view.findViewById(R.id.spHorarios);

        listPlacas = new ArrayList<String>();
        listRuta = new ArrayList<String>();
        listHorario = new ArrayList<String>();

        getVehiculos();

        // select
        spPlaca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_vehiculo = Integer.parseInt(getIdVehiculo(position));

                getRutas(id_vehiculo);
                placa = listPlacas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spRuta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_ruta = Integer.parseInt(getIdRuta(position));
                Log.d(Service.TAG, "id_ruta: " + id_ruta);
                getHorario(id_ruta);
                ruta = listRuta.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spHorarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_horario = Integer.parseInt(getIdHorario(position));
                Log.d(Service.TAG, "id_horario: " + id_horario);
                btnSiguiente.setBackgroundResource(R.drawable.bg_button_main);
                horario = listHorario.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e(Service.TAG, "id_ruta " + id_ruta);
                Intent intent = new Intent(context, SelectRutas.class);
                intent.putExtra(SelectRutas.ID, id_ruta);
                intent.putExtra(SelectRutas.ID_VEHICULO, id_vehiculo);
                intent.putExtra(SelectRutas.ID_HORARIO, id_horario);
                intent.putExtra(SelectRutas.INFO, placa + "," + ruta + "," + horario);
                startActivity(intent);
            }
        });


        return view;
    }

    private void getHorario(int id_vehiculo) {
        listHorario.clear();

        String URL = Service.GET_HORARIO + id_vehiculo;
        Log.d(Service.TAG, "horarios: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultHorarios = jsonObject.getJSONArray("horarios");
                    Log.d(Service.TAG, "json: " + resultHorarios);
                    if (resultHorarios.length() > 0) {
                        for (int i = 0; i < resultHorarios.length(); i++) {
                            try {
                                JSONObject json = resultHorarios.getJSONObject(i);
                                String horario = json.getString("hora");

                                listHorario.add(horario);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spHorarios.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_horario, R.id.txtName, listHorario));
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        requestQueue.add(stringRequest);

    }

    private void getRutas(int id_vehiculo) {
        listRuta.clear();
        String URL = Service.GET_RUTAS + id_vehiculo;
        Log.d(Service.TAG, "rutas: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultRuta = jsonObject.getJSONArray("rutas");
                    Log.d(Service.TAG, "json: " + resultRuta);
                    if (resultRuta.length() > 0) {
                        for (int i = 0; i < resultRuta.length(); i++) {
                            try {
                                JSONObject json = resultRuta.getJSONObject(i);
                                String nombreIda = json.getString("Ida");
                                String nombreVuelta = json.getString("Vuelta");

                                String nameRuta = nombreIda + " - " + nombreVuelta;

                                listRuta.add(nameRuta);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spRuta.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_rutas, R.id.txtName, listRuta));
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        requestQueue.add(stringRequest);
    }

    private void getVehiculos() {
        listPlacas.clear();

        stringRequest = new StringRequest(Service.GET_VEHICULOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultPlaca = jsonObject.getJSONArray("vehiculos");

                    if (resultPlaca.length() > 0) {
                        for (int i = 0; i < resultPlaca.length(); i++) {
                            try {
                                JSONObject json = resultPlaca.getJSONObject(i);
                                String nombreInstitucion = json.getString("placa");

                                listPlacas.add(nombreInstitucion);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spPlaca.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_placa, R.id.txtName, listPlacas));
                    } else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(stringRequest);

    }

    private String getIdVehiculo(int position) {
        String id_vehiculo = "";
        try {
            JSONObject object = resultPlaca.getJSONObject(position);
            id_vehiculo = object.getString("idVehiculo");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id_vehiculo;
    }


    private String getIdRuta(int position) {
        String idRuta = "";
        try {
            JSONObject object = resultRuta.getJSONObject(position);
            idRuta = object.getString("idRuta");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idRuta;
    }

    private String getIdHorario(int position) {
        String idHorario = "";
        try {
            JSONObject object = resultHorarios.getJSONObject(position);
            idHorario = object.getString("idHorario");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idHorario;
    }

}
