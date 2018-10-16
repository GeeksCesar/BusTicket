package com.smartgeeks.busticket.Menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.Helpers;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectRutas extends AppCompatActivity {

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORA = "HORA";
    public static final String INFO = "INFO";
    public static final String TAG = SelectRutas.class.getSimpleName();

    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    LinearLayout contenedorCheckBox, contenedorPrecio;
    Button btnSiguiente, btnFinalizar, btnMenos, btnMas;
    Spinner spInicio, spFin, spPasajero;
    CheckBox cbAsiento, cbDePie;
    TextView tvPrecioPasaje, tvCountItem;
    DialogAlert dialogAlert = new DialogAlert();

    private JSONArray resutlParaderos;
    private JSONArray resutlParaderosFin;
    private JSONArray resultUsuarios;
    private ArrayList<String> listParaderos;
    private ArrayList<String> lisUsuarios;
    private ArrayList<String> listParaderoFin = new ArrayList<>();

    // Listas para SQLite
    private List<Paradero> paraderoInicioList = new ArrayList<>();
    private List<Paradero> paraderoFinList = new ArrayList<>();
    private List<TipoUsuario> tipoUsuariosList = new ArrayList<>();

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int countPasajes = 1, precio_sum_pasaje, precioPasaje, id_usuario, id_paradero_inicio, id_paradero_fin, position_tipo_usuario, sizeTarifas;

    String ruta_inicio, ruta_fin, hora, info;

    Context context;

    private View mProgressView;

    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible;

    //Prefrences
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_rutas);

        initWidget();
        showView();

        spInicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //id_paradero_inicio = Integer.parseInt(getIdParadero(position)) ;
                id_paradero_inicio = Integer.parseInt(paraderoInicioList.get(position).getIdRemoto());
                ruta_inicio = parent.getItemAtPosition(position).toString();

                //getParaderosFin(id_ruta, id_paradero_inicio);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

                //id_paradero_fin = Integer.parseInt(getIdParadero(position)) ;
                id_paradero_fin = Integer.parseInt(paraderoFinList.get(position).getIdRemoto());
                ruta_fin = parent.getItemAtPosition(position).toString();
                Log.e(TAG, "Paradero inicio: " + id_paradero_inicio);
                Log.e(TAG, "Paradero fin: " + id_paradero_fin);
                //getPrecio(id_paradero_inicio, id_paradero_fin, id_usuario);
                try {

                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, position_tipo_usuario);
                    formatPrecio(precioPasaje);
                    if (sizeTarifas == 0){
                        Toast.makeText(context, "No se han definido precios para estos paraderos", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.getMessage();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPasajero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //id_usuario = Integer.parseInt(getIdUsuario(position)) ;
                id_usuario = Integer.parseInt(tipoUsuariosList.get(position).getId_remoto());
                position_tipo_usuario = position;

                //getPrecio(id_paradero_inicio, id_paradero_fin, id_usuario);
                try {
                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, position);
                    formatPrecio(precioPasaje);
                } catch (Exception e) {
                    e.getMessage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countPasajes++;
                precio_sum_pasaje = precioPasaje * countPasajes;

                formatPrecio(precio_sum_pasaje);
                tvCountItem.setText("" + countPasajes);
            }
        });

        btnMenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (countPasajes > 1) {
                    countPasajes--;
                    precio_sum_pasaje = precioPasaje * countPasajes;

                    formatPrecio(precio_sum_pasaje);
                    tvCountItem.setText("" + countPasajes);
                }

            }
        });


        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                precio_sum_pasaje = precioPasaje * countPasajes;

                //  Log.e(Service.TAG , "id_ruta: "+id_ruta);
                if (ruta_inicio == ruta_fin) {
                    dialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setVisibility(View.GONE);
                    showProgress(true);
                    registerTicket(id_paradero_inicio, id_paradero_fin, id_ruta, id_operador, id_usuario, precio_sum_pasaje);
                }

            }
        });

    }

    private void initWidget() {
        context = SelectRutas.this;
        requestQueue = Volley.newRequestQueue(context);

        contenedorCheckBox = findViewById(R.id.contenedorCheckbox);
        contenedorPrecio = findViewById(R.id.contenedorPrecio);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnMas = findViewById(R.id.btnSumar);
        btnMenos = findViewById(R.id.btnRestar);
        spInicio = findViewById(R.id.spInicio);
        spFin = findViewById(R.id.spFIn);
        spPasajero = findViewById(R.id.spUsuarios);
        cbAsiento = findViewById(R.id.cbAsientos);
        cbDePie = findViewById(R.id.cbPie);
        tvCountItem = findViewById(R.id.textCount);
        tvPrecioPasaje = findViewById(R.id.tvPrecio);
        mProgressView = findViewById(R.id.login_progress);


        btnFinalizar.setVisibility(View.GONE);
        btnSiguiente.setVisibility(View.GONE);
        contenedorCheckBox.setVisibility(View.GONE);
        contenedorPrecio.setVisibility(View.GONE);

        bundle = getIntent().getExtras();


        if (bundle != null) {
            Log.e(Service.TAG, "Entro a Bundle");
            id_ruta = bundle.getInt(ID_RUTA);
            id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
            id_vehiculo = bundle.getInt(ID_VEHICULO);
            id_horario = bundle.getInt(ID_HORARIO);
            hora = bundle.getString(HORA);
            info = bundle.getString(INFO);
            id_operador = UsuarioPreferences.getInstance(context).getIdUser();
        } else {
            id_ruta = RutaPreferences.getInstance(context).getIdRuta();
            id_ruta_disponible = RutaPreferences.getInstance(context).getIdRutaDisponible();
            id_vehiculo = RutaPreferences.getInstance(context).getIdVehiculo();
            id_horario = RutaPreferences.getInstance(context).getIdHorario();
            hora = RutaPreferences.getInstance(context).getHora();
            info = RutaPreferences.getInstance(context).getInformacion();
            id_operador = UsuarioPreferences.getInstance(context).getIdUser();
        }

        listParaderos = new ArrayList<String>();
        lisUsuarios = new ArrayList<String>();

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                precio_sum_pasaje = precioPasaje * countPasajes;

                if (ruta_inicio == ruta_fin) {
                    dialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    if (cbAsiento.isChecked() && DialogAlert.verificaConexion(context)) {
                        Intent intent = new Intent(context, SelectSillas.class);
                        intent.putExtra(SelectSillas.CANT_PUESTOS, countPasajes);
                        intent.putExtra(SelectSillas.PRECIO_PASAJE, precio_sum_pasaje);
                        intent.putExtra(SelectSillas.ID_VEHICULO, id_vehiculo);
                        intent.putExtra(SelectSillas.ID_RUTA, id_ruta);
                        intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                        intent.putExtra(SelectSillas.ID_HORARIO, id_horario);
                        intent.putExtra(SelectSillas.HORARIO, hora);
                        intent.putExtra(SelectSillas.ID_PARADERO_INICIO, id_paradero_inicio);
                        intent.putExtra(SelectSillas.ID_PARADERO_FIN, id_paradero_fin);
                        intent.putExtra(SelectSillas.TIPO_USUARIO, id_usuario);
                        intent.putExtra(INFO, info + "," + ruta_inicio + "," + ruta_fin);

                        startActivity(intent);
                    } else {
                        // Si no hay conexión a internet, guardo el ticket localmente
                        saveTicketLocal();
                    }
                }
            }
        });

        //getParaderos(id_ruta); webservice
        getParaderosSQLite(id_ruta);

        validarCheckBox();
        tvCountItem.setText("" + countPasajes);

    }

    private void saveTicketLocal() {
        Ticket ticket = new Ticket();
        ticket.setIdRemoto("");
        ticket.setParadaInicio(id_paradero_inicio);
        ticket.setParadaDestino(id_paradero_fin);
        ticket.setIdRutaDisponible(id_ruta_disponible);
        ticket.setIdOperador(UsuarioPreferences.getInstance(context).getIdUser());
        ticket.setHoraSalida(hora);
        ticket.setTipoUsuario(3);
        ticket.setFecha(Helpers.getCurrentDate());
        ticket.setHora(Helpers.getCurrentTime());
        ticket.setCantPasajes(countPasajes);
        ticket.setTotalPagar(precio_sum_pasaje);
        ticket.setEstado(0);
        ticket.setPendiente(Constantes.ESTADO_SYNC);

        ticket.save();
        // El estado = 0 y estado_sync = 1, para cuando se inicie la sincronización remota
        // se cambie el estado = 1
    }

    private void showView() {
        contenedorCheckBox.setVisibility(View.VISIBLE);
        contenedorPrecio.setVisibility(View.VISIBLE);
    }


    private void formatPrecio(int precio) {
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');
        if (precioPasaje == 1) {
            precioPasaje = precio;
        }
        tvPrecioPasaje.setText("$ " + formatPrecio);
    }

    private void validarCheckBox() {
        cbAsiento.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbAsiento.isChecked()) {
                    cbDePie.setChecked(false);
                    btnSiguiente.setVisibility(View.VISIBLE);
                    btnFinalizar.setVisibility(View.GONE);
                } else {
                    btnSiguiente.setVisibility(View.GONE);
                }
            }
        });

        cbDePie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbDePie.isChecked()) {
                    cbAsiento.setChecked(false);
                    btnSiguiente.setVisibility(View.GONE);
                    btnFinalizar.setVisibility(View.VISIBLE);
                } else {
                    btnFinalizar.setVisibility(View.GONE);
                }
            }
        });

    }

    private void getParaderos(int id) {
        listParaderos.clear();


        stringRequest = new StringRequest(Service.GET_PARADEROS + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(Service.TAG, "response: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resutlParaderos = jsonObject.getJSONArray("estacion");

                    if (resutlParaderos.length() > 0) {
                        for (int i = 0; i < resutlParaderos.length(); i++) {
                            try {
                                JSONObject json = resutlParaderos.getJSONObject(i);
                                String paradero = json.getString("paradero");

                                listParaderos.add(paradero);
                                //  listParaderoFin.add(paradero);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));
                        // spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderoFin));

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

    private void getParaderosFin(int id_ruta, final int id_paradero) {

        listParaderoFin.clear();

        String Url = Service.GET_PARADEROS_FIN + id_ruta + "/" + id_paradero;
        Log.w(Service.TAG, "Url_Paradero_fin: " + Url);

        stringRequest = new StringRequest(Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(Service.TAG, "response: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resutlParaderosFin = jsonObject.getJSONArray("estacion");
                    Log.e(Service.TAG, "resulParaderoFin:  " + resutlParaderosFin);

                    if (resutlParaderosFin.length() > 0) {
                        for (int i = 0; i < resutlParaderosFin.length(); i++) {
                            try {
                                JSONObject json = resutlParaderosFin.getJSONObject(i);
                                String paradero = json.getString("paradero");

                                listParaderoFin.add(paradero);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderoFin));

                        getUsuarios();

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

    private void getPrecio(int id_inicio, int id_fin, int id_usuario) {
        String URL = Service.GET_PRECIO_TIQUETE + id_inicio + "/" + id_fin + "/" + id_usuario;

        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = null;

                try {
                    jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 0) {
                        precioPasaje = jsonArray.getJSONObject(0).getInt("precio");
                        contenedorCheckBox.setVisibility(View.VISIBLE);
                        contenedorPrecio.setVisibility(View.VISIBLE);

                        formatPrecio(precioPasaje);

                    } else {
                        contenedorCheckBox.setVisibility(View.GONE);
                        contenedorPrecio.setVisibility(View.GONE);
                        Toast.makeText(SelectRutas.this, "No han definido precios", Toast.LENGTH_SHORT).show();
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

    private void getUsuarios() {

        lisUsuarios.clear();

        stringRequest = new StringRequest(Service.GET_USUARIOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultUsuarios = jsonObject.getJSONArray("usuario");

                    if (resultUsuarios.length() > 0) {
                        for (int i = 0; i < resultUsuarios.length(); i++) {
                            try {
                                JSONObject json = resultUsuarios.getJSONObject(i);
                                String nombreInstitucion = json.getString("nombre");

                                lisUsuarios.add(nombreInstitucion);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spPasajero.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_tipo_pasajero, R.id.txtName, lisUsuarios));
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

    private String getIdParadero(int position) {
        String idParadero = "";
        try {
            JSONObject object = resutlParaderos.getJSONObject(position);
            idParadero = object.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idParadero;
    }

    private String getIdParaderoFin(int position) {
        String idParadero = "";
        try {
            JSONObject object = resutlParaderosFin.getJSONObject(position);
            idParadero = object.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idParadero;
    }

    private String getIdUsuario(int position) {
        String id_usuario = "";
        try {
            JSONObject object = resultUsuarios.getJSONObject(position);
            id_usuario = object.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id_usuario;
    }

    public void goBack(View view) {
        this.finish();
    }

    private void registerTicket(final int id_paradero_inicio, final int id_paradero_final, final int id_ruta, final int id_operador, final int id_tipo_usuario, final int valor_pagar) {

        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_PIE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(Service.TAG, "response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String respuesta = jsonObject.getString("message");
                    if (respuesta.equals("success")) {
                        dialogAlert.showDialogFailed(context, "Exito", "Registro el ticket \n exitosamente", SweetAlertDialog.SUCCESS_TYPE);
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setVisibility(View.VISIBLE);
                        showProgress(false);
                    } else {
                        dialogAlert.showDialogFailed(context, "Error", "Ha ocurrido un error \n al registrar el ticket", SweetAlertDialog.SUCCESS_TYPE);
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setVisibility(View.VISIBLE);
                        showProgress(false);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialogAlert.showDialogErrorConexion(context);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_paradero_inicio", String.valueOf(id_paradero_inicio));
                params.put("id_paradero_fin", String.valueOf(id_paradero_final));
                params.put("id_ruta", String.valueOf(id_ruta));
                params.put("id_operador", String.valueOf(id_operador));
                params.put("hora", hora);
                params.put("id_tipo_usuario", String.valueOf(id_tipo_usuario));
                params.put("total_pagar", String.valueOf(valor_pagar));


                return params;
            }
        };
        ;

        requestQueue.add(stringRequest);

    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    /**
     * ***********   Consultas SQLite  *************
     */

    private void getParaderosSQLite(int id_ruta) {
        listParaderos.clear();
        listParaderoFin.clear();

        paraderoInicioList = Paradero.find(Paradero.class, "ruta = ?",
                new String[]{"" + id_ruta}, null, "remoto", null);
        paraderoFinList = Paradero.find(Paradero.class, "ruta = ?",
                new String[]{"" + id_ruta}, null, "remoto", null);

        if (paraderoInicioList.size()==0){
            DialogAlert.showDialogFailed(context, "Atención","No se han definido paraderos para la ruta ", SweetAlertDialog.WARNING_TYPE);
        }

        for (Paradero paradero : paraderoInicioList) {
            listParaderos.add(paradero.getParadero());
            listParaderoFin.add(paradero.getParadero());
        }

        //setAdapter
        try {
            listParaderoFin.remove(0);
            paraderoFinList.remove(0);
        } catch (Exception e) {
            e.getMessage();
        }
        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));
        spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderoFin));

        getUsuariosSQLite();
    }

    private void getUsuariosSQLite() {
        lisUsuarios.clear();

        tipoUsuariosList = TipoUsuario.listAll(TipoUsuario.class, "remoto");

        for (TipoUsuario tipoUsuario : tipoUsuariosList) {
            lisUsuarios.add(tipoUsuario.getNombre());
        }

        //setAdapter
        spPasajero.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_tipo_pasajero, R.id.txtName, lisUsuarios));
    }

    private double getPrecioSQLite(int id_paradero_inicio, int id_paradero_fin, int position) {
        List<TarifaParadero> tarifaParaderos = TarifaParadero.find(TarifaParadero.class,
                "parada_inicio = ? AND parada_fin = ?", "" + id_paradero_inicio,
                "" + id_paradero_fin);

        double precio = 5000;
        Log.e("Size ", "" + tarifaParaderos.size());
        sizeTarifas = tarifaParaderos.size();

        switch (position) {
            case 0:
                precio = tarifaParaderos.get(0).getEstudiante();
                Log.e("Estudiante: ", "" + precio);
                break;
            case 1:
                precio = tarifaParaderos.get(0).getNormal();
                Log.e("Normal: ", "" + precio);
                break;
            case 2:
                precio = tarifaParaderos.get(0).getFrecuente();
                Log.e("Frecuente: ", "" + precio);
                break;
            case 3:
                precio = tarifaParaderos.get(0).getAdulto_mayor();
                Log.e("Adulto: ", "" + precio);
                break;
            case 4:
                precio = tarifaParaderos.get(0).getVale_muni();
                Log.e("ValeMuni: ", "" + precio);
                break;
        }
        return precio;
    }

}