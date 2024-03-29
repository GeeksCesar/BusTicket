package com.smartgeeks.busticket.menu;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Modelo.Horario;
import com.smartgeeks.busticket.Modelo.Ruta;
import com.smartgeeks.busticket.Modelo.Vehiculo;
import com.smartgeeks.busticket.Objects.RutaPojo;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.data.api.Service;
import com.smartgeeks.busticket.utils.DialogAlert;
import com.smartgeeks.busticket.utils.Helpers;
import com.smartgeeks.busticket.utils.RutaPreferences;
import com.smartgeeks.busticket.utils.UsuarioPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Archivos donde se imprime el Ticket
 * - PreciosRutaConductor
 * - SelectRutas
 * - SelectSillas
 */

public class Ticket extends Fragment {

    private static String TAG = Ticket.class.getSimpleName();
    View view;
    Context context;
    Spinner spPlaca, spRuta, spHorarios;
    Button btnSiguiente, btnRecordarRuta, btnFinalizarRuta;
    private View mProgressView;
    LinearLayout contentButton;

    private ArrayList<String> listPlacas;
    private ArrayList<String> listRuta;
    private ArrayList<String> listHora;


    private List<Vehiculo> listVehiculos = new ArrayList<>();
    private List<Ruta> listRutas = new ArrayList<>();
    private List<Horario> listHorarios = new ArrayList<>();

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int id_vehiculo, id_ruta, id_horario, id_ruta_disponible;
    String placa, ruta_info, horario, hora;
    boolean getStatusRuta;

    AlertDialog.Builder builder;

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

                new QueryRoutes().execute("" + id_vehiculo);
                placa = listPlacas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spRuta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                id_ruta = listRutas.get(position).getRuta();
                id_ruta_disponible = listRutas.get(position).getRutaDisponible();

                //getHorarioSQLite(id_ruta+"");
                new QueryShedules().execute(id_ruta + "");
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

                builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                builder.setMessage(context.getResources().getString(R.string.dialogMessageFinalizar));

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!DialogAlert.verificaConexion(context)) {
                            DialogAlert.showDialogFailed(context, "Alerta", "Para finalizar ruta, requiere conexión \n a internet", SweetAlertDialog.ERROR_TYPE);
                            dialog.cancel();
                        } else {
                            dialog.cancel();
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

        handleChangeTextSaveRoute(getStatusRuta);
        btnRecordarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = (getStatusRuta) ? getString(R.string.dialog_forgot_route) : getString(R.string.dialog_remember_route);

                final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                builder.setMessage(message);

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         * Save preferences when route is not has been saved on preferences
                         */
                        if (getStatusRuta) {
                            handleChangeTextSaveRoute(false);
                            deleteRoutePreference();
                        } else {
                            handleChangeTextSaveRoute(true);
                            saveRoutePreference();

                            if (UsuarioPreferences.getInstance(context).getRoleVenta().equals("conductor")) {
                                Intent intent = new Intent(context, SelectTarifa.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(context, SelectRutas.class);
                                startActivity(intent);
                            }
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

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Horario: " + hora);
                Log.e(TAG, "Ruta: " + id_ruta_disponible);

                Intent intent;
                if (UsuarioPreferences.getInstance(context).getRoleVenta().equals("conductor")) {
                    Log.e(TAG, "ENTRO CONDUCTOR");
                    intent = new Intent(context, SelectTarifa.class);
                    intent.putExtra(SelectTarifa.ID_RUTA, id_ruta);
                    intent.putExtra(SelectTarifa.ID_VEHICULO, id_vehiculo);
                    intent.putExtra(SelectTarifa.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                    intent.putExtra(SelectTarifa.ID_HORARIO, id_horario);
                    intent.putExtra(SelectTarifa.HORARIO, horario);
                    intent.putExtra(SelectTarifa.INFO, placa + "," + ruta_info + "," + hora);
                } else {
                    // Modo Boleteria
                    intent = new Intent(context, SelectRutas.class);
                    intent.putExtra(SelectRutas.ID_RUTA, id_ruta);
                    intent.putExtra(SelectRutas.ID_VEHICULO, id_vehiculo);
                    intent.putExtra(SelectRutas.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                    intent.putExtra(SelectRutas.ID_HORARIO, id_horario);
                    intent.putExtra(SelectRutas.HORARIO, horario);
                    intent.putExtra(SelectRutas.INFO, placa + "," + ruta_info + "," + hora);
                }

                Log.e(TAG, UsuarioPreferences.getInstance(context).getRoleVenta());
                startActivity(intent);
            }

        });


        return view;
    }

    private void saveRoutePreference() {
        Log.e(TAG, "Horario pref: " + horario);
        Log.e(TAG, "Ruta pref: " + id_ruta_disponible);

        RutaPojo ruta = new RutaPojo();

        ruta.setVehiculo_id(id_vehiculo);
        ruta.setRuta_id(id_ruta);
        ruta.setRuta_disponible_id(id_ruta_disponible);
        ruta.setHorario(horario);
        ruta.setHorario_id(id_horario);
        ruta.setInformacion(placa + "," + ruta_info + "," + hora);
        ruta.setStatus_ruta(true);

        RutaPreferences.getInstance(context).rutaPreferences(ruta);
        getStatusRuta = RutaPreferences.getInstance(context).getEstadoRuta();
    }

    private void deleteRoutePreference() {
        SharedPreferences preferences = context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, MODE_PRIVATE);
        preferences.edit().putBoolean(RutaPreferences.ESTADO, false).apply();
        getStatusRuta = RutaPreferences.getInstance(context).getEstadoRuta();
    }

    private void handleChangeTextSaveRoute(Boolean savedRoute) {
        if (savedRoute) {
            btnRecordarRuta.setText(getString(R.string.forget_route));
        } else {
            btnRecordarRuta.setText(getString(R.string.remember_route));
        }
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
        contentButton = view.findViewById(R.id.contentButton);

        btnSiguiente.setBackgroundResource(R.drawable.bg_button_main);
        btnRecordarRuta.setBackgroundResource(R.drawable.bg_button_main);
        btnFinalizarRuta.setBackgroundResource(R.drawable.bg_button_main);

        contentButton.setVisibility(View.GONE);
        btnSiguiente.setVisibility(View.GONE);

        // Obtain pref from remember Ruta, for show view
        getStatusRuta = RutaPreferences.getInstance(context).getEstadoRuta();

        if (getStatusRuta) {
            if (UsuarioPreferences.getInstance(context).getRoleVenta().equals("conductor")) {
                Intent intent = new Intent(context, SelectTarifa.class);
                startActivity(intent);
//                getActivity().finish();
            } else {
                Intent intent = new Intent(context, SelectRutas.class);
                startActivity(intent);
//                getActivity().finish();
            }
        }

        listPlacas = new ArrayList<String>();
        listRuta = new ArrayList<String>();
        listHora = new ArrayList<String>();

        //getVehiculosSQLite();
        new QueryVehicles().execute();
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
        listHora.clear();
        listRutas = Ruta.find(Ruta.class, "vehiculo = ?", "" + id_vehiculo);

        if (listRutas.size() == 0) {
            DialogAlert.showDialogFailed(context, "¡Atención!", "No se han definido rutas para este Vehiculo", SweetAlertDialog.WARNING_TYPE);
        }

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
            contentButton.setVisibility(View.VISIBLE);
            btnSiguiente.setVisibility(View.VISIBLE);
        }
    }

    private void setFinalizarRuta(final int id_ruta_disponible, final String horario) {

        String URL = Service.SET_LIBERAR_SILLA + id_ruta_disponible + "/" + horario;
        Log.d(Service.TAG, "Url: " + URL);

        stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(Service.TAG, "response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String respuesta = jsonObject.getString("message");

                    if (respuesta.equals("success")) {
                        showProgress(false);
                        btnFinalizarRuta.setEnabled(true);
                        DialogAlert.showDialogFailed(context, "Exito", "Finalizo la ruta con exito", SweetAlertDialog.SUCCESS_TYPE);
                    } else {
                        showProgress(false);
                        btnFinalizarRuta.setEnabled(true);
                        DialogAlert.showDialogFailed(context, "Alerta", "Ha ocurrdio algun problema al finalizar la ruta", SweetAlertDialog.ERROR_TYPE);
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
            }
        });

        requestQueue.add(stringRequest);
    }

    /**
     * Queries
     */
    private class QueryVehicles extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            listPlacas.clear();
            listVehiculos = Vehiculo.listAll(Vehiculo.class);
            for (Vehiculo vehiculo : listVehiculos) {
                listPlacas.add(vehiculo.getPlaca());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //setAdapter
            spPlaca.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_placa, R.id.txtName, listPlacas));

            if (listPlacas.size() == 0) {
                DialogAlert.showDialogFailed(context, "¡Atención!", "No se han definido Vehiculos", SweetAlertDialog.WARNING_TYPE);
            }
        }
    }

    private class QueryRoutes extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            listRuta.clear();
            listHora.clear();
            Log.e(TAG, "Id Vehículo: " + strings[0]);
            listRutas = Ruta.find(Ruta.class, "vehiculo = ?", strings[0]);

            for (Ruta ruta : listRutas) {
                String nameRuta = ruta.getPartida() + " - " + ruta.getDestino();
                listRuta.add(nameRuta);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            spRuta.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_rutas, R.id.txtName, listRuta));

            if (listRutas.size() == 0) {
                DialogAlert.showDialogFailed(context, "¡Atención!", "No se han definido rutas para este Vehiculo", SweetAlertDialog.WARNING_TYPE);
            }
        }
    }

    private class QueryShedules extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            listHora.clear();
            Log.e(TAG, "Id Ruta: " + strings[0]);
            listHorarios = Horario.find(Horario.class, "ruta = ?", new String[]{strings[0]},
                    "hora", "hora", null);

            for (Horario horario : listHorarios) {
                listHora.add(Helpers.formatTwelveHours(horario.getHora()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //setAdapter
            spHorarios.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_horario, R.id.txtName, listHora));

            if (listHorarios.size() > 0) {
                contentButton.setVisibility(View.VISIBLE);
                btnSiguiente.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}