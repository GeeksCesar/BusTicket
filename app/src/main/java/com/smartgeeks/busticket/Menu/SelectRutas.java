package com.smartgeeks.busticket.Menu;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.Helpers;
import com.smartgeeks.busticket.Utils.InternetCheck;
import com.smartgeeks.busticket.Utils.PrintPicture;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectRutas extends AppCompatActivity {

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORARIO = "HORARIO";

    public static final String INFO = "INFO";
    public static final String TAG = SelectRutas.class.getSimpleName();

    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    private View mProgressView;
    View contenedorCheckBox, contenedorPrecio ;
    Button btnSiguiente, btnFinalizar, btnOlvidarRuta,  btnMenos, btnMas ;
    Spinner spInicio, spFin, spPasajero ;
    CheckBox cbAsiento, cbDePie ;
    TextView tvPrecioPasaje , tvCountItem;

    DialogAlert dialogAlert = new DialogAlert();

    private JSONArray resutlParaderos;
    private JSONArray resutlParaderosFin;
    private JSONArray resultUsuarios;
    private ArrayList<String> listParaderos;
    private ArrayList<String> lisUsuarios;
    private ArrayList<String> listParaderoFin = new ArrayList<>();

    // Listas para SQLite
    private List<Paradero> paraderosList = new ArrayList<>();
    private List<TipoUsuario> tipoUsuariosList = new ArrayList<>();

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int countPasajes = 1, precio_sum_pasaje, precioPasaje, valor_pasaje, id_tipo_usuario, id_paradero_inicio, id_paradero_fin, position_tipo_usuario, sizeTarifas;
    private String numVoucher = "";
    String ruta_inicio, ruta_fin, horario, info, nombreEmpresa, desc_empresa;

    Context context;



    int id_horario, id_vehiculo , id_operador, id_ruta, id_ruta_disponible, id_empresa;
    String nameUsuario, getPrecioPasaje;

    SharedPreferences preferences ;
    SharedPreferences.Editor editor ;

    boolean estadoRuta , estadoPrint;
    String namePrint ;

    //Configuracion Impresora
    private ArrayList<String> lisPrintBluetooth = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream, outputStreamTitle;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    Dialog dialogPrint;
    Button btnCancelar;
    ListView lstPrint;

    boolean isEstadoRuta ;
    SweetAlertDialog alertDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_rutas);

        initWidget();

        spPasajero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_tipo_usuario = Integer.parseInt(tipoUsuariosList.get(position).getId_remoto());
                position_tipo_usuario = position;

                nameUsuario = parent.getItemAtPosition(position).toString();

                try {
                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, id_tipo_usuario);
                    Log.e(TAG, "Precio del pasaje: " + precioPasaje);
                    valor_pasaje = precioPasaje * countPasajes ;

                    formatPrecio(valor_pasaje);
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener precio del pasaje");
                    e.getMessage();
                }

                if (id_tipo_usuario == 0){
                    showView(false);
                } else if (sizeTarifas == 0 && Integer.parseInt(tipoUsuariosList.get(0).getId_remoto()) > 0){
                    Toast.makeText(context, "No se han definido precios para este usuario.", Toast.LENGTH_SHORT).show();
                    showView(false);
                }else {
                    showView(true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spInicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ruta_inicio = parent.getItemAtPosition(position).toString();
                id_paradero_inicio = paraderosList.get(position).getIdRemoto();

                getParaderosFinSQLite(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                ruta_fin = parent.getItemAtPosition(position).toString();
                id_paradero_fin = Paradero.find(Paradero.class, "ruta = ? AND paradero = ?",
                        new String[]{"" + id_ruta, ""+ruta_fin}, "remoto", "remoto", null).get(0).getIdRemoto();

                try {
                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, id_tipo_usuario);
                    valor_pasaje = precioPasaje * countPasajes ;
                    formatPrecio(valor_pasaje);
                } catch (Exception e) {
                    e.getMessage();
                }

                if (sizeTarifas == 0 && Integer.parseInt(tipoUsuariosList.get(0).getId_remoto()) > 0){
                    Toast.makeText(context, "No se han definido precios para este usuario.", Toast.LENGTH_SHORT).show();
                    contenedorCheckBox.setVisibility(View.GONE);
                    contenedorPrecio.setVisibility(View.GONE);
                }else {
                    contenedorCheckBox.setVisibility(View.VISIBLE);
                    contenedorPrecio.setVisibility(View.VISIBLE);
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

        btnOlvidarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences = context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().clear().apply();

                goIntentMain();
            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                precio_sum_pasaje = precioPasaje * countPasajes;

                String precio = String.valueOf(precio_sum_pasaje) ;

                if (ruta_inicio == ruta_fin) {
                    DialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else if (precio_sum_pasaje == 0 || precio == null){
                    DialogAlert.showDialogFailed(context, "Error", "Verifique el valor de pasaje", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setVisibility(View.GONE);

                    if (DialogAlert.verificaConexion(context)){
                        showProgress(true);
                        registerTicket(id_paradero_inicio, id_paradero_fin, id_ruta_disponible, id_operador, id_tipo_usuario, precio_sum_pasaje,countPasajes);
                    }else {
                        printOffLine();
                    }

                }

            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                precio_sum_pasaje = precioPasaje * countPasajes;
                String precio = String.valueOf(precio_sum_pasaje) ;

                if (ruta_inicio == ruta_fin) {
                    DialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else if (precio_sum_pasaje == 0 || precio == null){
                    DialogAlert.showDialogFailed(context, "Error", "Verifique el valor de pasaje", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    if (cbAsiento.isChecked()) {

                        new InternetCheck(new InternetCheck.Consumer() {
                            @Override
                            public void accept(Boolean internet) {
                                if (internet) {
                                    Log.e(TAG, "Hay conexión a Internet");
                                    //doSomethingOnConnected();
                                    startSelectSillasActivity();
                                } else {
                                    Log.e(TAG, "No hay conexión a Internet");
                                    //doSomethingOnNoInternet();
                                    DialogAlert.showDialogFailed(context, "Error", "Ops.. No hay conexión.", SweetAlertDialog.WARNING_TYPE);
                                }
                            }
                        }).execute();
                    }
                }
            }
        });

    }

    private void startSelectSillasActivity() {
        Intent intent = new Intent(context, SelectSillas.class);
        intent.putExtra(SelectSillas.CANT_PUESTOS, countPasajes);
        intent.putExtra(SelectSillas.PRECIO_PASAJE, precio_sum_pasaje);
        intent.putExtra(SelectSillas.ID_VEHICULO, id_vehiculo);
        intent.putExtra(SelectSillas.ID_RUTA, id_ruta);
        intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, id_ruta_disponible);
        intent.putExtra(SelectSillas.ID_HORARIO, id_horario);
        intent.putExtra(SelectSillas.HORARIO, horario);
        intent.putExtra(SelectSillas.ID_PARADERO_INICIO, id_paradero_inicio);
        intent.putExtra(SelectSillas.ID_PARADERO_FIN, id_paradero_fin);
        intent.putExtra(SelectSillas.TIPO_USUARIO, id_tipo_usuario);
        intent.putExtra(SelectSillas.NAME_TIPO_PASAJERO, nameUsuario);
        intent.putExtra(INFO, info + "," + ruta_inicio + "," + ruta_fin);

        startActivity(intent);
    }

    private void initWidget() {
        context = SelectRutas.this;
        requestQueue = Volley.newRequestQueue(context);

        contenedorCheckBox = findViewById(R.id.contenedorCheckbox);
        contenedorPrecio = findViewById(R.id.contenedorPrecio);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnOlvidarRuta = findViewById(R.id.btnOlvidarRuta);
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

        bundle = getIntent().getExtras();

        estadoRuta = RutaPreferences.getInstance(context).getEstadoRuta();

        getDataPrint();
        btnOlvidarRuta.setVisibility(View.VISIBLE);

       if (estadoRuta){
            btnOlvidarRuta.setVisibility(View.VISIBLE);
        }else {
            btnOlvidarRuta.setVisibility(View.GONE);
        }

        if (bundle != null) {
            id_ruta = bundle.getInt(ID_RUTA);
            id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
            id_vehiculo = bundle.getInt(ID_VEHICULO);
            id_horario = bundle.getInt(ID_HORARIO);
            horario = bundle.getString(HORARIO);
            info = bundle.getString(INFO);
        } else {
            id_ruta = RutaPreferences.getInstance(context).getIdRuta();
            id_ruta_disponible = RutaPreferences.getInstance(context).getIdRutaDisponible();
            id_vehiculo = RutaPreferences.getInstance(context).getIdVehiculo();
            id_horario = RutaPreferences.getInstance(context).getIdHorario();
            horario = RutaPreferences.getInstance(context).getHora();
            info = RutaPreferences.getInstance(context).getInformacion();
        }

        id_operador = UsuarioPreferences.getInstance(context).getIdUser();
        nombreEmpresa = UsuarioPreferences.getInstance(context).getNombreEmpresa();
        desc_empresa = UsuarioPreferences.getInstance(context).getDescEmpresa();
        nombreEmpresa = nombreEmpresa.trim().toUpperCase();

        Log.e(TAG, "Horario: "+horario);
        Log.e(TAG, "Ruta: "+id_ruta_disponible);

        id_empresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        listParaderos = new ArrayList<String>();
        lisUsuarios = new ArrayList<String>();

        encontrarDispositivoBlue();

        getParaderosSQLite(id_ruta);

        validarCheckBox();
        tvCountItem.setText("" + countPasajes);

    }

    private void printOffLine(){
        saveTicketLocal();
        btnFinalizar.setEnabled(true);

        alertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);

        alertDialog.setTitleText("Exito")
                .setContentText("Guardo el ticket")
                .show();

        Button button = alertDialog.findViewById(R.id.confirm_button);
        // button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        button.setBackgroundResource(R.drawable.bg_button_main);
        button.setPadding(5, 5, 5, 5);
        button.setText("Imprimir Ticket");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    alertDialog.dismiss();

                    getDataPrint();

                    if (estadoPrint){

                        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

                        if (pairedDevice.size() > 0) {
                            for (BluetoothDevice pairedDev : pairedDevice) {
                                if (pairedDev.getName().equals(namePrint)) {
                                    bluetoothDevice = pairedDev;
                                    isEstadoRuta = true ;
                                    abrirImpresoraBlue();
                                    //goIntentMain();
                                    break;
                                }else {
                                    Log.e(TAG  , "error no existe impresora");
                                }
                            }
                        }else {
                            Log.e(TAG  , "error no existe impresora");
                        }

                    }else {
                        showDialogTiquete();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    public void getDataPrint(){
        namePrint = RutaPreferences.getInstance(context).getNamePrint() ;
        estadoPrint = RutaPreferences.getInstance(context).getEstadoPrint();
    }

    private void saveTicketLocal() {

        String fecha = Helpers.getCurrentDate();
        String hora = Helpers.getCurrentTime();
        numVoucher = id_vehiculo+""+id_operador+"-"+Helpers.setString2DateVoucher(fecha)+"-"+Helpers.setString2HourVoucher(hora);
        Log.e(TAG, "saveTicketLocal numVoucher: "+numVoucher);

        Ticket ticket = new Ticket();
        ticket.setIdRemoto("");
        ticket.setParadaInicio(id_paradero_inicio);
        ticket.setParadaDestino(id_paradero_fin);
        ticket.setIdRutaDisponible(id_ruta_disponible);
        ticket.setIdOperador(UsuarioPreferences.getInstance(context).getIdUser());
        ticket.setHoraSalida(horario);
        ticket.setTipoUsuario(id_tipo_usuario);
        ticket.setFecha(fecha);
        ticket.setHora(hora);
        ticket.setCantPasajes(countPasajes);
        ticket.setIdVehiculo(id_vehiculo);
        ticket.setTotalPagar(precio_sum_pasaje);
        ticket.setEstado(0);
        ticket.setPendiente(Constantes.ESTADO_SYNC);

        ticket.save();
        // El estado = 0 y estado_sync = 1, para cuando se inicie la sincronización remota
        // se cambie el estado = 1
    }

    private void showView(boolean show) {
        contenedorCheckBox.setVisibility(show ? View.VISIBLE : View.GONE);
        contenedorPrecio.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void formatPrecio(int precio) {
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');
        tvPrecioPasaje.setText("$ " + formatPrecio);
        getPrecioPasaje = "$ " + formatPrecio ;

    }

    private void validarCheckBox() {
        cbAsiento.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                btnSiguiente.setText("Siguiente");

                if (cbAsiento.isChecked()) {
                    cbDePie.setChecked(false);

                    if (sizeTarifas > 0){
                        btnSiguiente.setVisibility(View.VISIBLE);
                        btnFinalizar.setVisibility(View.GONE);
                        // btnOlvidarRuta.setVisibility(View.GONE);
                    }

                }else {
                    btnSiguiente.setVisibility(View.GONE);
                   // btnOlvidarRuta.setVisibility(View.VISIBLE);
                }
            }
        });

        cbDePie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbDePie.isChecked()) {
                    cbAsiento.setChecked(false);

                    if (sizeTarifas > 0){
                        btnSiguiente.setVisibility(View.GONE);
                        btnFinalizar.setVisibility(View.VISIBLE);
                    }

                  //  btnOlvidarRuta.setVisibility(View.GONE);
                }else{
                    btnFinalizar.setVisibility(View.GONE);
                  //  btnOlvidarRuta.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void goBack(View view) {
        listParaderos.clear();
        listParaderoFin.clear();
        lisUsuarios.clear();
        this.finish();
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void registerTicket(final int id_paradero_inicio, final int id_paradero_final, final int id_ruta_disponible, final int id_operador, final int id_tipo_usuario, final int valor_pagar, final int countPasajes) {

        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_PIE_TEST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String respuesta = jsonObject.getString("message");
                    numVoucher = jsonObject.getString("num_voucher");
                    Log.e(TAG, "NumVoucher: "+numVoucher);

                    if (respuesta.equals("success")) {
                        btnFinalizar.setEnabled(true);
                        btnFinalizar.setVisibility(View.VISIBLE);
                        showProgress(false);

                        final SweetAlertDialog alertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);

                        alertDialog.setTitleText("Exito")
                                .setContentText("Guardo el ticket")
                                .show();

                        Button button = alertDialog.findViewById(R.id.confirm_button);
                        // button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        button.setBackgroundResource(R.drawable.bg_button_main);
                        button.setPadding(5, 5, 5, 5);
                        button.setText("Imprimir Ticket");

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    alertDialog.dismiss();

                                    getDataPrint();

                                    if (estadoPrint){
                                        Log.e(TAG, "entro estado") ;
                                        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
                                        Log.e(TAG, "parired: "+pairedDevice.size()) ;

                                        if (pairedDevice.size() > 0) {
                                            for (BluetoothDevice pairedDev : pairedDevice) {
                                                if (pairedDev.getName().equals(namePrint)) {
                                                    bluetoothDevice = pairedDev;
                                                    abrirImpresoraBlue();
                                                    //goIntentMain();
                                                   break;
                                                }else {
                                                    Log.e(TAG  , "error no existe impresora");
                                                    //Toast.makeText(SelectRutas.this, "No se puede imprimir", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }else {
                                            Log.e(TAG  , "error no existe impresora");
                                            //Toast.makeText(SelectRutas.this, "No existe impresora", Toast.LENGTH_SHORT).show();
                                        }

                                    }else {
                                        showDialogTiquete();
                                    }

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });


                    } else {
                        DialogAlert.showDialogFailed(context, "Error", "Ha ocurrido un error \n al registrar el ticket", SweetAlertDialog.ERROR_TYPE);
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
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "error: " + volleyError.getMessage());
                if (volleyError instanceof TimeoutError){
                    DialogAlert.showDialogFailed(context, "Error", "Ha pasado el tiempo Limitado", SweetAlertDialog.WARNING_TYPE);
                    return;
                }else if (volleyError instanceof ServerError){
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. Error en el servidor", SweetAlertDialog.WARNING_TYPE);
                    return;
                }else if (volleyError instanceof NoConnectionError){
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. No hay conexion a internet", SweetAlertDialog.WARNING_TYPE);
                    return;
                }else if (volleyError instanceof NetworkError){
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. Hay error en la red", SweetAlertDialog.WARNING_TYPE);
                    return;
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_paradero_inicio", String.valueOf(id_paradero_inicio));
                params.put("id_paradero_fin", String.valueOf(id_paradero_final));
                params.put("id_ruta", String.valueOf(id_ruta_disponible));
                params.put("id_operador", String.valueOf(id_operador));
                params.put("hora", horario);
                params.put("id_tipo_usuario", String.valueOf(id_tipo_usuario));
                params.put("total_pagar", String.valueOf(valor_pagar));
                params.put("cantidad", String.valueOf(countPasajes));
                params.put("id_empresa", String.valueOf(id_empresa));
                params.put("id_vehiculo", String.valueOf(id_vehiculo));

                return params;
            }
        };

        requestQueue.add(stringRequest);

    }


    /**
     * ***********   Consultas SQLite  *************
     */

    private void getParaderosSQLite(int id_ruta) {
        listParaderos.clear();
        listParaderoFin.clear();

        paraderosList = Paradero.find(Paradero.class, "ruta = ?",
                new String[]{"" + id_ruta}, "remoto", "remoto", null);

        if (paraderosList.size()==0){
            DialogAlert.showDialogFailed(context, "Atención","No se han definido paraderos para la ruta ", SweetAlertDialog.WARNING_TYPE);
        }

        for (Paradero paradero : paraderosList) {
            listParaderos.add(paradero.getParadero());
        }

        try {
            listParaderos.remove(paraderosList.size()-1);
        } catch (Exception e){
            e.getMessage();
        }

        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));

        getUsuariosSQLite();
    }

    public void getParaderosFinSQLite(int paradero_inicio){
        listParaderoFin.clear();

        for (int i = (paradero_inicio+1); i < paraderosList.size(); i++){
            listParaderoFin.add(paraderosList.get(i).getParadero());
        }

        spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderoFin));
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

    private double getPrecioSQLite(int id_paradero_inicio, int id_paradero_fin, int id_tipo_usuario) {
        List<TarifaParadero> tarifaParaderos = TarifaParadero.find(TarifaParadero.class,
                "parada_inicio = ? AND parada_fin = ? AND tipo_usuario = ?", "" + id_paradero_inicio,
                "" + id_paradero_fin, ""+id_tipo_usuario);

        double precio = tarifaParaderos.get(0).getMonto();
        sizeTarifas = tarifaParaderos.size();

        return precio;
    }


    private void showDialogTiquete() {
        dialogPrint = new Dialog(context);
        dialogPrint.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPrint.setContentView(R.layout.dialog_print);
        dialogPrint.setCanceledOnTouchOutside(false);
        dialogPrint.setCancelable(false);


        btnCancelar = dialogPrint.findViewById(R.id.btnCancelar);
        lstPrint = dialogPrint.findViewById(R.id.listViewPrint);

        lstPrint.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lisPrintBluetooth) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }
        });

        lstPrint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name_impresora = parent.getItemAtPosition(position).toString();

                preferences = context.getSharedPreferences(RutaPreferences.PREFERENCES_PRINT, Context.MODE_PRIVATE);
                editor = preferences.edit() ;

                editor.putString(RutaPreferences.NAME_PRINT, name_impresora);
                editor.putBoolean(RutaPreferences.ESTADO_PRINT, true);
                editor.apply() ;

                Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

                if (pairedDevice.size() > 0) {
                    for (BluetoothDevice pairedDev : pairedDevice) {
                        if (pairedDev.getName().equals(name_impresora)) {
                            bluetoothDevice = pairedDev;
                            abrirImpresoraBlue();
                            break;
                        }

                    }
                }
                dialogPrint.hide();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPrint.hide();
            }
        });

        dialogPrint.show();
    }

    public void encontrarDispositivoBlue() {
        try {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "No tiene Acitivado el bluetooth", Toast.LENGTH_SHORT).show();
            }
            if (bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 0);
            }
            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if (pairedDevice.size() > 0) {
                for (BluetoothDevice pairedDev : pairedDevice) {
                    lisPrintBluetooth.add(pairedDev.getName());
                   // Log.d(Service.TAG, "se agrego las lista de bluetooth: "+pairedDev.getName());
                }
            }else {
                Log.d(TAG, "no hay lista de bluetooth");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.i(TAG, "otro Error" + ex.getMessage());
        }

    }

    public void abrirImpresoraBlue() {
        try {
            Log.i(TAG, "Entro a print");
            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            outputStreamTitle = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            comenzarAEscucharDatos();

            if (isEstadoRuta){
                printDataOffLine();
            }else {
                printData();
            }
            goIntentMain();

        } catch (Exception ex) {
            Log.i(TAG, "Error P: " +ex.getMessage());
        }
    }

    void comenzarAEscucharDatos() {
        try {

            final Handler handler = new Handler();
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG , "method run") ;
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int byteAvailable = inputStream.available();
                            if (byteAvailable > 0) {
                                byte[] packetByte = new byte[byteAvailable];
                                inputStream.read(packetByte);

                                for (int i = 0; i < byteAvailable; i++) {
                                    byte b = packetByte[i];
                                    if (b == delimiter) {
                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedByte, 0,
                                                encodedByte.length
                                        );
                                        final String data = new String(encodedByte, StandardCharsets.US_ASCII);
                                        readBufferPosition = 0;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //lblPrinterName.setText(data);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            stopWorker = true;
                        }
                    }

                }
            });

            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    void printData() {
        Log.d(TAG, "entro a printdata") ;

        String[] split = info.split(",");

        byte[] command=null;
        try{

            byte[] arrayOfByte1 = { 27, 33, 0 };
            byte[] format = { 27, 33, 0 };



            byte[] centrado = {0x1B, 'a', 0x01};
            byte[] der = {0x1B, 'a', 0x02};
            byte[] izq = {0x1B, 'a', 0x00};

            // Espacio superior
            outputStream.write(("\n\n").getBytes(),0,("\n\n").getBytes().length);

            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write((nombreEmpresa+ "\n").getBytes(),0,(nombreEmpresa+ "\n").getBytes().length);

            if (!desc_empresa.isEmpty()){
                // Mensaje de la empresa, text small
                format[2] = ((byte)(0x1 | arrayOfByte1[2]));
                outputStream.write(format);
                outputStream.write((desc_empresa+"\n").getBytes(),0,(desc_empresa+"\n").getBytes().length);
                // end - mensaje empresa
            }

            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);

            outputStream.write(izq);

            /**
             * Estructura del voucher
             * D del bus "4",
             * id del vendedor "4",
             * la fecha (ddmmaa) 250121,
             * = 44-250121-163822
             */

            String msg = "";
            msg += "\n";
            msg += "Ticket N:   " + numVoucher;
            msg += "\n";
            msg += "Tarifa:   " + nameUsuario;
            msg += "\n";
            msg += "Fecha:   " + Helpers.getDate();
            msg += "\n";
            outputStream.write(msg.getBytes(), 0, msg.getBytes().length);
            String msg0 = "" ;
            msg0 += "Horario:   " + horario;
            msg0 += "\n";
            msg0 += "Operador:   " + UsuarioPreferences.getInstance(context).getNombre();
            msg0 += "\n";
            outputStream.write(msg0.getBytes(), 0, msg0.getBytes().length);
            String ruta = "" ;
            ruta += "Ruta:  " + split[1] + "\n";
            // Small
            format[2] = ((byte)(0x1 | arrayOfByte1[2]));
            outputStream.write(format);
            outputStream.write(ruta.getBytes(),0,ruta.getBytes().length);
            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);
            String msg1 = "";
            msg1 += "";
            msg1 += "Inicio: " + ruta_inicio;
            msg1 += "\n";
            msg1 += "Termino: " + ruta_fin;
            msg1 += "\n";
            msg1 += "Vehiculo: " + split[0];
            outputStream.write(msg1.getBytes(), 0, msg1.getBytes().length);
            String msg2 = "";
            msg2 += "\n";
            msg2 += "Hora: " + Helpers.getTime();
            msg2 += "\n";
            msg2 += "Cantidad: " + countPasajes;
            msg2 += "\n";
            msg2 += "\n";
            outputStream.write(msg2.getBytes(), 0, msg2.getBytes().length);

            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            String precio = "Precio: "+getPrecioPasaje+"\n";
            outputStream.write(format);
            outputStream.write(precio.getBytes(),0,precio.getBytes().length);
            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);

            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.img_logo_pdf);
                byte[] data = PrintPicture.POS_PrintBMP(bmp, 384, 0);
                outputStream.write(data);

                // Espacio Inferior
                outputStream.write(("\n\n").getBytes(),0,("\n\n").getBytes().length);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Service.TAG, "PrintTools: the file isn't exists");
            }

            outputStream.write(("\n\n\n\n").getBytes(),0,("\n\n\n\n").getBytes().length);

        }catch (Exception ex){
            ex.printStackTrace();
            Log.e(Service.TAG , "error in printdata");
        }


    }

    void printDataOffLine() {
        Log.d(TAG, "entro a printDataOffLine") ;

        String[] split = info.split(",");

        byte[] command=null;
        try{

            byte[] arrayOfByte1 = { 27, 33, 0 };
            byte[] format = { 27, 33, 0 };


            byte[] centrado = {0x1B, 'a', 0x01};
            byte[] der = {0x1B, 'a', 0x02};
            byte[] izq = {0x1B, 'a', 0x00};

            // Espacio Superior
            outputStream.write(("\n\n").getBytes(),0,("\n\n").getBytes().length);

            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write((nombreEmpresa+ "\n").getBytes(),0,(nombreEmpresa+ "\n").getBytes().length);

            if (!desc_empresa.isEmpty()){
                // Mensaje de la empresa, text small
                format[2] = ((byte)(0x1 | arrayOfByte1[2]));
                outputStream.write(format);
                outputStream.write((desc_empresa+"\n").getBytes(),0,(desc_empresa+"\n").getBytes().length);
                // end - mensaje empresa
            }

            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);

            outputStream.write(izq);
            String msg = "";
            msg += "\n";
            msg += "Ticket N:   " + numVoucher;
            msg += "\n";
            msg += "Tarifa:   " + nameUsuario;
            msg += "\n";
            msg += "Fecha:   " + Helpers.getDate();
            msg += "\n";
            outputStream.write(msg.getBytes(), 0, msg.getBytes().length);
            String msg0 = "" ;
            msg0 += "Horario:   " + horario;
            msg0 += "\n";
            msg0 += "Operador:   " + UsuarioPreferences.getInstance(context).getNombre();
            msg0 += "\n";
            outputStream.write(msg0.getBytes(), 0, msg0.getBytes().length);
            String ruta = "" ;
            ruta += "Ruta:  " + split[1] + "\n";
            // Small
            format[2] = ((byte)(0x1 | arrayOfByte1[2]));
            outputStream.write(format);
            outputStream.write(ruta.getBytes(),0,ruta.getBytes().length);
            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);
            String msg1 = "";
            msg1 += "";
            msg1 += "Inicio: " + ruta_inicio;
            msg1 += "\n";
            msg1 += "Termino: " + ruta_fin;
            msg1 += "\n";
            msg1 += "Vehiculo: " + split[0];
            outputStream.write(msg1.getBytes(), 0, msg1.getBytes().length);
            String msg2 = "";
            msg2 += "\n";
            msg2 += "Hora: " + Helpers.getTime();
            msg2 += "\n";
            msg2 += "Cantidad: " + countPasajes;
            msg2 += "\n";
            msg2 += "\n";
            outputStream.write(msg2.getBytes(), 0, msg2.getBytes().length);

            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            String precio = "";
            precio += "Precio: "+getPrecioPasaje+"\n";

            outputStream.write(format);
            outputStream.write(precio.getBytes(),0,precio.getBytes().length);
            format =new byte[]{ 27, 33, 0 };

            outputStream.write(format);

            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.img_logo_pdf);
                byte[] data = PrintPicture.POS_PrintBMP(bmp, 384, 0);
                outputStream.write(data);

                // Espacio inferior
                outputStream.write(("\n\n").getBytes(),0,("\n\n").getBytes().length);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Service.TAG, "PrintTools: the file isn't exists");
            }

            outputStream.write(("\n\n\n\n").getBytes(),0,("\n\n\n\n").getBytes().length);

        }catch (Exception ex){
            ex.printStackTrace();
            Log.e(Service.TAG , "error in printdata");
        }


    }

    private void goIntentMain() {
        try{
            disconnectBT();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.BACK, true);
        startActivity(intent);
        finish();
    }

    // Disconnect Printer //
    void disconnectBT() {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}