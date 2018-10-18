package com.smartgeeks.busticket.Menu;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Horario;
import com.smartgeeks.busticket.Modelo.Ruta;
import com.smartgeeks.busticket.Modelo.Vehiculo;
import com.smartgeeks.busticket.Objcect.RutaPojo;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.Helpers;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Ticket extends Fragment {

    View view;
    Context context;
    DialogAlert dialogAlert = new DialogAlert();
    Spinner spPlaca, spRuta, spHorarios;
    Button btnSiguiente, btnRecordarRuta, btnFinalizarRuta;
    private View mProgressView;

    private JSONArray resultPlaca;
    private JSONArray resultRuta;
    private JSONArray resultHorarios;
    private ArrayList<String> listPlacas;
    private ArrayList<String> listRuta;
    private ArrayList<String> listHora;
    private ArrayList<String> listHorario = new ArrayList<>();

    private List<Vehiculo> listVehiculos = new ArrayList<>();
    private List<Ruta> listRutas = new ArrayList<>();
    private List<Horario> listHorarios = new ArrayList<>();

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int id_vehiculo, id_ruta, id_horario, id_ruta_disponible;
    String placa, ruta_info, horario, hora;
    boolean getStatusRuta;

    public Ticket() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.menu_ticket, container, false);

        init();

        // select
        spPlaca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_vehiculo = Integer.parseInt(listVehiculos.get(position).getIdRemoto());

                getRutasSQLite(id_vehiculo);
                placa = listPlacas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spRuta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_ruta = Integer.parseInt(listRutas.get(position).getIdRemoto());
                id_ruta_disponible = listRutas.get(position).getRutaDisponible();

                getHorarioSQLite(id_ruta+"");
                ruta_info = listRuta.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spHorarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_horario = Integer.parseInt(listHorarios.get(position).getIdRemoto());
                hora = adapterView.getItemAtPosition(position).toString();
                horario = listHorarios.get(position).getHora();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnFinalizarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                builder.setMessage(context.getResources().getString(R.string.dialogMessageFinalizar));

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!dialogAlert.verificaConexion(context)) {
                            dialogAlert.showDialogFailed(context, "Alerta", "Para finalizar ruta, requiere conexión \n a internet", SweetAlertDialog.ERROR_TYPE);
                        }else {
                            btnFinalizarRuta.setEnabled(false);
                            showProgress(true);
                            setFinalizarRuta(id_ruta_disponible, horario);
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();



            }
        });

        btnRecordarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                builder.setMessage(context.getResources().getString(R.string.dialogMessage));

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RutaPojo ruta = new RutaPojo();

                        ruta.setVehiculo_id(id_vehiculo);
                        ruta.setRuta_id(id_ruta);
                        ruta.setRuta_disponible_id(id_ruta_disponible);
                        ruta.setHorario(horario);
                        ruta.setHorario_id(id_horario);
                        ruta.setInformacion(placa + "," + ruta_info + "," + hora);
                        ruta.setStatus_ruta(true);

                        RutaPreferences.getInstance(context).rutaPreferences(ruta);

                        Intent intent = new Intent(context, SelectRutas.class);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();
            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SelectRutas.class);
                intent.putExtra(SelectRutas.ID_RUTA, id_ruta);
                intent.putExtra(SelectRutas.ID_VEHICULO, id_vehiculo);
                intent.putExtra(SelectRutas.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                intent.putExtra(SelectRutas.ID_HORARIO, id_horario);
                intent.putExtra(SelectRutas.HORA, hora);
                intent.putExtra(SelectRutas.HORARIO, horario);

                intent.putExtra(SelectRutas.INFO, placa + "," + ruta_info + "," + hora);
                startActivity(intent);
            }

        });


        return view;
    }

    private void alertActionServiceSync() {
        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(
                Constantes.ACTION_RUN_LOCAL_SYNC);
        filter.addAction(Constantes.ACTION_FINISH_SYNC);

        // Crear un nuevo ResponseReceiver
        ResponseReceiver receiver = new ResponseReceiver();
        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
    }

    private void init() {
        context = getActivity();
        requestQueue = Volley.newRequestQueue(context);

        btnSiguiente = view.findViewById(R.id.btnNext);
        btnRecordarRuta = view.findViewById(R.id.btnRecordarRuta);
        btnFinalizarRuta = view.findViewById(R.id.btnFinalizarRuta);
        spPlaca = view.findViewById(R.id.spPlaca);
        spRuta = view.findViewById(R.id.spRutas);
        spHorarios = view.findViewById(R.id.spHorarios);
        mProgressView = view.findViewById(R.id.login_progress);

        btnSiguiente.setBackgroundResource(R.drawable.bg_button_main);
        btnRecordarRuta.setBackgroundResource(R.drawable.bg_button_main);
        btnFinalizarRuta.setBackgroundResource(R.drawable.bg_button_main);

        getStatusRuta = RutaPreferences.getInstance(context).getEstadoRuta();
        Log.e(Service.TAG, "estado_ruta: " + getStatusRuta);

        if (getStatusRuta) {
            Intent intent = new Intent(context, SelectRutas.class);
            startActivity(intent);
            getActivity().finish();
        } else {

        }

        listPlacas = new ArrayList<String>();
        listRuta = new ArrayList<String>();
        listHora = new ArrayList<String>();

        getVehiculosSQLite();
    }

    private void getHorario(int id_vehiculo) {
        listHora.clear();

        String URL = Service.GET_HORARIO + id_vehiculo;
        Log.w(Service.TAG, "horarios: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultHorarios = jsonObject.getJSONArray("horarios");
                    Log.v(Service.TAG, "json: " + resultHorarios);
                    if (resultHorarios.length() > 0) {

                        btnSiguiente.setVisibility(View.VISIBLE);
                        btnRecordarRuta.setVisibility(View.VISIBLE);

                        for (int i = 0; i < resultHorarios.length(); i++) {
                            try {
                                JSONObject json = resultHorarios.getJSONObject(i);
                                String hora = json.getString("hora");
                                String horario = json.getString("horario");

                                listHora.add(hora);
                                listHorario.add(horario);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spHorarios.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_horario, R.id.txtName, listHora));
                    } else {
                        btnSiguiente.setVisibility(View.GONE);
                        btnRecordarRuta.setVisibility(View.GONE);
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
        Log.w(Service.TAG, "rutas: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultRuta = jsonObject.getJSONArray("rutas");
                    Log.v(Service.TAG, "json: " + resultRuta);
                    if (resultRuta.length() > 0) {
                        for (int i = 0; i < resultRuta.length(); i++) {
                            try {
                                JSONObject json = resultRuta.getJSONObject(i);
                                String nombreIda = json.getString("Inicio");
                                String nombreVuelta = json.getString("Termina");

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

    private void getVehiculos(int id) {
        listPlacas.clear();

        stringRequest = new StringRequest(Service.GET_VEHICULOS + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultPlaca = jsonObject.getJSONArray("vehiculos");
                    Log.d(Service.TAG, "vehiculos: " + resultPlaca);
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

    /**
     * Consultas en sqlite
     */
    private void getVehiculosSQLite() {
        listPlacas.clear();
        listVehiculos = Vehiculo.listAll(Vehiculo.class);
        for (Vehiculo vehiculo : listVehiculos) {
            listPlacas.add(vehiculo.getPlaca());
        }
        //setAdapter
        spPlaca.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_placa, R.id.txtName, listPlacas));

    }

    private void getRutasSQLite(int id_vehiculo) {
        listRuta.clear();
        listRutas = Ruta.find(Ruta.class, "vehiculo = ?", ""+id_vehiculo);
        for (Ruta ruta : listRutas) {
            String nameRuta = ruta.getPartida() + " - " + ruta.getDestino();
            listRuta.add(nameRuta);
        }

        spRuta.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_rutas, R.id.txtName, listRuta));
    }

    private void getHorarioSQLite(String id_ruta) {
        listHora.clear();
        listHorarios = Horario.find(Horario.class, "ruta = ?", new String[]{id_ruta},
                "hora", "hora", null);

        for (Horario horario : listHorarios) {
            listHora.add(Helpers.formatTwelveHours(horario.getHora()));
        }
        //setAdapter
        spHorarios.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_horario, R.id.txtName, listHora));

        if (listHorarios.size() > 0) {
            btnRecordarRuta.setVisibility(View.VISIBLE);
            btnSiguiente.setVisibility(View.VISIBLE);
        }
    }

    private void setFinalizarRuta(final int id_ruta_disponible, final String horario) {

        stringRequest = new StringRequest(Request.Method.GET, Service.SET_LIBERAR_SILLA+id_ruta_disponible+"/"+horario, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(Service.TAG, "response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String respuesta = jsonObject.getString("message");

                    if (respuesta.equals("success")) {
                        btnFinalizarRuta.setEnabled(true);
                        showProgress(false);
                        dialogAlert.showDialogFailed(context , "Exito" , "Finalizo la ruta con exito", SweetAlertDialog.SUCCESS_TYPE) ;
                    }else {
                        btnFinalizarRuta.setEnabled(true);
                        showProgress(false);
                        dialogAlert.showDialogFailed(context , "Alerta" , "Ha ocurrdio algun problema al finalizar la ruta", SweetAlertDialog.ERROR_TYPE) ;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                btnFinalizarRuta.setEnabled(true);
                showProgress(false);
                dialogAlert.showDialogErrorConexion(context);
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

    private String getIdRutaDisponible(int position) {
        String idRuta = "";
        try {
            JSONObject object = resultRuta.getJSONObject(position);
            idRuta = object.getString("idRutaDisponible");
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

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constantes.ACTION_FINISH_SYNC:
                    //getVehiculosSQLite();
                    break;
            }
        }
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}