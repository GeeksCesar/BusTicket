package com.smartgeeks.busticket.Menu;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Horario;
import com.smartgeeks.busticket.Modelo.Silla;
import com.smartgeeks.busticket.Modelo.Vehiculo;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.Helpers;
import com.smartgeeks.busticket.Utils.PrintPicture;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectSillas extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String CANT_PUESTOS = "CANT_PUESTOS";
    public static final String PRECIO_PASAJE = "PRECIO_PASAJE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String ID_RUTA = "ID_RUTA";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_PARADERO_INICIO = "PARADERO_INCIO";
    public static final String ID_PARADERO_FIN = "PARADERO_FINAL";
    public static final String HORARIO = "HORARIO";
    public static final String TIPO_USUARIO = "TIPO_USUARIO";

    private String TAG = "SelectSillas";

    LinearLayout contenedor_bus;
    private List<Silla> listSillasOcupadas = new ArrayList<>();
    private List<Integer> sillasOcupadas = new ArrayList<>();
    private List<Integer> sillasSeleccionadas = new ArrayList<>();
    Bundle bundle;
    int id_tarifa;
    int cant_puestos, precio_pasaje, id_vehiculo, id_horario, id_paradero_incio, id_paradero_final, id_tipo_usuario, id_operador, id_ruta, id_ruta_disponible;
    String horario, info_ruta, nombreEmpresa;
    Context context;
    DialogAlert dialogAlert = new DialogAlert();
    Button btnConfirmarTicket;
    TextView tvVehiculo, tvRuta, tvHora, tvInicio, tvFin;
    ProgressDialog progress;

    private View mProgressView;

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;
    Gson gson = new Gson();

    String listSillas = "";
    private ArrayList<String> lisPrintBluetooth = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    Dialog dialogPrint;
    Button btnCancelar;
    ListView lstPrint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_sillas);

        context = SelectSillas.this;
        requestQueue = Volley.newRequestQueue(context);
        bundle = getIntent().getExtras();

        cant_puestos = bundle.getInt(CANT_PUESTOS);
        precio_pasaje = bundle.getInt(PRECIO_PASAJE);
        id_vehiculo = bundle.getInt(ID_VEHICULO);
        id_horario = bundle.getInt(ID_HORARIO);
        id_ruta = bundle.getInt(ID_RUTA);
        id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
        info_ruta = bundle.getString(SelectRutas.INFO);
        id_paradero_incio = bundle.getInt(ID_PARADERO_INICIO);
        id_paradero_final = bundle.getInt(ID_PARADERO_FIN);
        id_tipo_usuario = bundle.getInt(TIPO_USUARIO);
        horario = bundle.getString(HORARIO);
        id_operador = UsuarioPreferences.getInstance(context).getIdUser();
        nombreEmpresa = UsuarioPreferences.getInstance(context).getNombreEmpresa();

        initWidgets();
        // Obtengo los datos del vehículo
        showProgressDialog();
        getSillasOcupadas(id_ruta_disponible);


        btnConfirmarTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listSillas = "";


                if (sillasSeleccionadas.size() == 0) {
                    dialogAlert.showDialogFailed(context, "Error", "Debe seleccionar puestos", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else if (sillasSeleccionadas.size() < cant_puestos) {
                    dialogAlert.showDialogFailed(context, "Error", "Debe seleccionar " + cant_puestos + " Puestos", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    for (int i = 0; i < sillasSeleccionadas.size(); i++) {
                        int silla = sillasSeleccionadas.get(i);
                        Log.d(Service.TAG, "sillas: " + silla);

                        listSillas = listSillas + silla + "-";
                    }
                    listSillas = listSillas.substring(0, listSillas.length() - 1);

                    btnConfirmarTicket.setEnabled(false);
                    btnConfirmarTicket.setVisibility(View.GONE);

                    showProgress(true);
                    registerTicket(id_paradero_incio, id_paradero_final, id_ruta_disponible, id_operador, id_tipo_usuario, precio_pasaje, listSillas);


                }

            }
        });
    }

    private void initWidgets() {
        contenedor_bus = findViewById(R.id.contenedor_bus);
        btnConfirmarTicket = findViewById(R.id.btnConfirmarTicket);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        tvRuta = findViewById(R.id.tvRuta);
        tvHora = findViewById(R.id.tvHora);
        tvInicio = findViewById(R.id.tvInicio);
        tvFin = findViewById(R.id.tvFin);
        mProgressView = findViewById(R.id.login_progress);

        showDataTextView();

        encontrarDispositivoBlue();
    }

    private void showDataTextView() {
        String[] split = info_ruta.split(",");
        tvVehiculo.setText("Vehículo: " + split[0]);
        tvRuta.setText("Ruta: " + split[1]);
        tvHora.setText("Hora: " + split[2]);
        tvInicio.setText("Inicio: " + split[3]);
        tvFin.setText("Fin: " + split[4]);
    }

    private void drawChairBus(int cant_sillas) {
        int silla = 1;

        int filas = (int) Math.ceil(cant_sillas / 4);
        Log.e(TAG, "Paradero Inicio: "+id_paradero_incio);


        // Parámetros del LinearLayout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 5;
        params.bottomMargin = 5;

        // Parámetros del espacio
        LinearLayout.LayoutParams space_params = new LinearLayout.LayoutParams(50, 0, 1f);

        // Parámetros de la silla
        LinearLayout.LayoutParams silla_params = new LinearLayout.LayoutParams(50,
                70, 1f);
        silla_params.setMargins(4, 8, 4, 8);

        // Dibujo las filas
        for (int i = 1; i <= filas + 1; i++) {

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            //Dibujo las columnas izquierdas
            for (int a = 1; a <= 4; a++) {

                if (silla > cant_sillas) {
                    break;
                }

                if ((cant_sillas % 2) == 1 && silla >= (cant_sillas - 5)) {
                    final ToggleButton puesto = new ToggleButton(this);
                    puesto.setLayoutParams(silla_params);
                    puesto.setPadding(0, 10, 0, 10);
                    puesto.setId(silla);
                    puesto.setBackground(ContextCompat.getDrawable(this, R.drawable.toggle_silla));
                    puesto.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000));
                    puesto.setTextOn("" + silla);
                    puesto.setTextOff("" + silla);
                    puesto.setText("" + silla);
                    puesto.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    puesto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                    // Verificar estado de silla
                    drawSillaOcupada(silla, puesto);
                    silla++;

                    // Agregar Silla al ticket
                    puesto.setOnCheckedChangeListener(this);
                    linearLayout.addView(puesto);

                    if (a == 2) {
                        //Dibujo el espacio de en el bus
                        final ToggleButton extra = new ToggleButton(this);
                        extra.setLayoutParams(silla_params);
                        extra.setPadding(0, 10, 0, 10);
                        extra.setId(silla);
                        extra.setBackground(ContextCompat.getDrawable(this, R.drawable.toggle_silla));
                        extra.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000));
                        extra.setTextOn("" + silla);
                        extra.setTextOff("" + silla);
                        extra.setText("" + silla);
                        extra.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        extra.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                        // Verificar estado de silla
                        drawSillaOcupada(silla, extra);
                        silla++;

                        // Agregar Silla al ticket
                        extra.setOnCheckedChangeListener(this);
                        linearLayout.addView(extra);
                    }

                } else {
                    final ToggleButton puesto = new ToggleButton(this);
                    puesto.setLayoutParams(silla_params);
                    puesto.setPadding(0, 10, 0, 10);
                    puesto.setId(silla);
                    puesto.setBackground(ContextCompat.getDrawable(this, R.drawable.toggle_silla));
                    puesto.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000));
                    puesto.setTextOn("" + silla);
                    puesto.setTextOff("" + silla);
                    puesto.setText("" + silla);
                    puesto.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    puesto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

                    // Verificar estado de silla
                    drawSillaOcupada(silla, puesto);
                    silla++;

                    // Agregar Silla al ticket
                    puesto.setOnCheckedChangeListener(this);
                    linearLayout.addView(puesto);

                    if (a == 2) {
                        //Dibujo el espacio de en el bus
                        View espacio = new View(this);
                        espacio.setLayoutParams(space_params);
                        linearLayout.addView(espacio);
                    }
                }


            }
            contenedor_bus.addView(linearLayout);
        }
    }

    private void showProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setMessage("Cargando bus...");
        progress.setCanceledOnTouchOutside(true);
        progress.setCancelable(false);
        progress.show();
    }

    /**
     * Dibuja las sillas ocupadas
     *
     * @param silla
     * @param puesto
     */
    private void drawSillaOcupada(int silla, ToggleButton puesto) {
        // Verificar si la silla está ocupada
        for (Silla ocupada : listSillasOcupadas) {
            if (ocupada.getNumeroSilla() == silla && ocupada.getDestino() > id_paradero_incio ) {
                puesto.setEnabled(false);
                puesto.setClickable(false);
                puesto.setBackground(ContextCompat.getDrawable(this, R.drawable.silla_ocupada));
            }
        }
    }

    /**
     * Elimina una silla del arreglo, de acuerdo a su posicion
     *
     * @param silId
     */
    private void removeSillaFromArray(int silId) {
        for (int i = 0; i < sillasSeleccionadas.size(); i++) {
            if (sillasSeleccionadas.get(i) == silId) {
                sillasSeleccionadas.remove(i);
            }
        }
    }


    public void goBack(View view) {
        this.finish();
    }

    /**
     * Verifica el estado del toggle button
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int silla_seleccionda = buttonView.getId();
        Log.d(Service.TAG, "silla: " + silla_seleccionda);
        // Guardo o elimino la silla
        if (isChecked == true) {
            sillasSeleccionadas.add(silla_seleccionda);
            if (sillasSeleccionadas.size() > cant_puestos) {
                dialogAlert.showDialogFailed(context, "Error", "Ya has seleccionado los " + cant_puestos + " puestos.", SweetAlertDialog.ERROR_TYPE);
                removeSillaFromArray(silla_seleccionda);
                buttonView.setChecked(false);
                buttonView.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000));
            } else {
                buttonView.setTextColor(ContextCompat.getColor(context, R.color.md_white_1000));
            }
        } else if (isChecked == false) {
            removeSillaFromArray(silla_seleccionda);
            buttonView.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000));
        }
    }


    /**
     * Consultas a base de datos
     */
    private void getVehiculo(int id_vehiculo) {

        String URL = Service.GET_INFO_VEHICULO + id_vehiculo;
        Log.d(Service.TAG, "rutas: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    Log.e(TAG, "" + response);
                    JSONArray jsonArray = jsonObject.getJSONArray("vehiculos");

                    if (jsonArray.length() > 0) {
                        JSONObject json = jsonArray.getJSONObject(0);
                        drawChairBus((json.getInt("can_sillas")));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "" + volleyError);
                progress.dismiss();
            }
        });
        requestQueue.add(stringRequest);

    }

    /**
     * Consultar sillas ocupadas por horario de ruta
     *
     * @param id_ruta_disponible
     */
    private void getSillasOcupadas(int id_ruta_disponible) {
        Log.e(Service.TAG, "id_ruta_disponible: " + id_ruta_disponible);


        String URL = Constantes.GET_SILLAS_OCUPADAS + id_ruta_disponible + "/" +horario;
        Log.i(Service.TAG, "rutas: " + URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                JSONArray sillas = null;

                try {
                    object = new JSONObject(response);
                    // Obtener atributo "estado"
                    String estado = object.getString(Constantes.ESTADO);
                    Log.e(TAG, "Sillas: " + response);

                    switch (estado) {
                        case Constantes.SUCCESS: // EXITO
                            // Obtener array "horarios"
                            sillas = object.getJSONArray(Constantes.SILLAS_OCUPADAS);
                            // Parsear con Gson
                            Silla[] res = gson.fromJson(sillas != null ? sillas.toString() : null, Silla[].class);
                            listSillasOcupadas = Arrays.asList(res);
                            Log.e(TAG, "Se encontraron " + listSillasOcupadas.size() + " sillas ocupadas.");
                            break;
                        case Constantes.FAILED: // FALLIDO
                            Log.e(TAG, "Error al traer datos");
                            break;
                    }

                    getVehiculo(id_vehiculo);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "" + volleyError);
            }
        });
        requestQueue.add(stringRequest);

    }

    private void registerTicket(final int id_paradero_inicio, final int id_paradero_final, final int id_ruta, final int id_operador, final int id_tipo_usuario, final int valor_pagar, final String listSillas) {

        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_ASIENTO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(Service.TAG, "response: " + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String respuesta = jsonObject.getString("message");
                    id_tarifa = jsonObject.getInt("id");
                    Log.w(Service.TAG, "respuesta: " + respuesta);
                    Log.w(Service.TAG, "id_tarifa: " + id_tarifa);

                    if (respuesta.equals("success")) {
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

                                    showProgress(false);
                                    showDialogTiquete();

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                    } else {
                        dialogAlert.showDialogFailed(context, "Error", "Ha ocurrido un error \n al registrar el ticket", SweetAlertDialog.ERROR_TYPE);
                        btnConfirmarTicket.setEnabled(true);
                        btnConfirmarTicket.setVisibility(View.VISIBLE);
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
                Log.e(Service.TAG, "error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_paradero_inicio", String.valueOf(id_paradero_inicio));
                params.put("id_paradero_fin", String.valueOf(id_paradero_final));
                params.put("id_ruta", String.valueOf(id_ruta));
                params.put("id_operador", String.valueOf(id_operador));
                params.put("hora", horario);
                params.put("id_tipo_usuario", String.valueOf(id_tipo_usuario));
                params.put("total_pagar", String.valueOf(valor_pagar));
                params.put("sillas", listSillas);

                return params;
            }
        };
        ;

        requestQueue.add(stringRequest);
    }


    public void encontrarDispositivoBlue() {
        try {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                //lblPrinterName.setText("No Bluetooth Adapter found");
            }
            if (bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 0);
            }
            Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

            if (pairedDevice.size() > 0) {
                for (BluetoothDevice pairedDev : pairedDevice) {

                    lisPrintBluetooth.add(pairedDev.getName());
                    // My Bluetoth printer name is BTP_F09F1A
                    if (pairedDev.getName().equals("BlueTooth Printer")) {
                        bluetoothDevice = pairedDev;
                        //lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                        break;
                    }
                }
            }
            //lblPrinterName.setText("Bluetookkkkth Printer Attached");
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.i("otro Error", "" + ex.getMessage());
        }

    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public void abrirImpresoraBlue() {
        try {
            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            comenzarAEscucharDatos();
            printData();

        } catch (Exception ex) {

            Log.i("Error P", "" + ex.getMessage());

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
                                        final String data = new String(encodedByte, "US-ASCII");
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

    void printData() throws IOException {

        byte[] command = null;

        byte[] format = {27, 33, 0};
        byte[] arrayOfByte1 = {27, 33, 0};

        try {
            String[] split = info_ruta.split(",");

            /*String nombre = ""+nombreEmpresa;
            // Bold
            format[2] = ((byte)(0x8 | arrayOfByte1[2]));
            // Height
            format[2] = ((byte)(0x10 | arrayOfByte1[2]));
            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));

            outputStream.write(format);
            outputStream.write(nombre.getBytes(), 0, nombre.getBytes().length); */

            String msg = nombreEmpresa;
            msg += "\n";
            msg += "\n";
            msg += "\n";
            msg += "Ticket N: " + id_tarifa;
            msg += "\n";
            msg += "Fecha: " + Helpers.getDate();
            msg += "\n";
            msg += "Horario: " + split[2];
            msg += "\n";
            msg += "Operador: " + UsuarioPreferences.getInstance(context).getNombre();
            msg += "\n";
            msg += "Ruta: " + split[1];
            msg += "\n";
            msg += "Inicio: " + split[3];
            msg += "\n";
            msg += "Termino: " + split[4];
            msg += "\n";
            msg += "Vehiculo: " + split[0];
            msg += "\n";
            msg += "Asientos: " + listSillas;
            msg += "\n";
            msg += "Hora: " + Helpers.getTime();
            msg += "\n";
            msg += "\n";
            msg += "Gracias por su preferencia!";

            outputStream.write(msg.getBytes());

            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.img_logo_pdf);
                byte[] data = PrintPicture.POS_PrintBMP(bmp, 384, 0);

                outputStream.write(data);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PrintTools", "the file isn't exists");
            }


            dialogPrint.hide();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void showDialogTiquete() {
        dialogPrint = new Dialog(context);
        dialogPrint.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPrint.setContentView(R.layout.dialog_print);
        dialogPrint.setCanceledOnTouchOutside(false);
        dialogPrint.setCancelable(false);


        dialogPrint.getWindow()
                .setLayout((int) (getScreenWidth(SelectSillas.this) * .9), ViewGroup.LayoutParams.MATCH_PARENT);


        btnCancelar = dialogPrint.findViewById(R.id.btnCancelar);
        lstPrint = dialogPrint.findViewById(R.id.listViewPrint);

        lstPrint.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lisPrintBluetooth) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK);
                return view;
            }
        });

        lstPrint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name_impresora = parent.getItemAtPosition(position).toString();

                Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

                if (pairedDevice.size() > 0) {
                    for (BluetoothDevice pairedDev : pairedDevice) {

                        if (pairedDev.getName().equals(name_impresora)) {
                            bluetoothDevice = pairedDev;
                            //lblPrinterName.setText("Bluetooth Printer Attached: "+pairedDev.getName());
                            abrirImpresoraBlue();
                            break;
                        }

                    }
                }

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

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    void disconnectBT() throws IOException {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /***
     *  SQLite - Consulta de datos
     */

    private void getVehiculoSQLite() {
        List<Vehiculo> vehiculos = Vehiculo.find(Vehiculo.class, "remoto = ?",
                "" + id_vehiculo);
        int cant_sillas = vehiculos.get(0).getNumAsientos();
        drawChairBus(cant_sillas);
    }

}

