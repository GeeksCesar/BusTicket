package com.smartgeeks.busticket.Menu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.PrintPicture;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectRutas extends AppCompatActivity {

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORA = "HORA";
    public static final String INFO = "INFO";

    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    Button btnSiguiente, btnFinalizar, btnMenos, btnMas;
    Spinner spInicio, spFin, spPasajero;
    CheckBox cbAsiento, cbDePie;
    TextView tvPrecioPasaje, tvCountItem;
    DialogAlert dialogAlert = new DialogAlert();

    private JSONArray resutlParaderos;
    private JSONArray resultUsuarios;
    private ArrayList<String> listParaderos;
    private ArrayList<String> lisUsuarios;
    private ArrayList<String> listParaderoFin = new ArrayList<>();

    //VOLLEY
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int countPasajes = 1, precio_sum_pasaje, precioPasaje, id_usuario, id_paradero_inicio, id_paradero_fin;
    String ruta_inicio, ruta_fin, Horario;

    Context context;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;

    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    private View mProgressView;

    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible;

    StringBuffer stringBuffer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_rutas);

        initWidget();

        spInicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_paradero_inicio = Integer.parseInt(getIdParadero(position));
                Log.d(Service.TAG, "id_paradero_inicio: " + id_paradero_inicio);
                ruta_inicio = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spFin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                id_paradero_fin = Integer.parseInt(getIdParadero(position));
                Log.d(Service.TAG, "id_paradero_fin: " + id_paradero_fin);
                ruta_fin = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPasajero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                id_usuario = Integer.parseInt(getIdUsuario(position));
                Log.d(Service.TAG, "id_usuario: " + id_usuario);
                if (id == 0) {
                    precioPasaje = Integer.parseInt(getPrecio(position, "t_adulto"));
                } else if (id == 1) {
                    precioPasaje = Integer.parseInt(getPrecio(position, "t_estudiante"));
                } else if (id == 2) {
                    precioPasaje = Integer.parseInt(getPrecio(position, "t_frecuente"));
                } else if (id == 3) {
                    precioPasaje = Integer.parseInt(getPrecio(position, "t_normal"));
                }

                formatPrecio(precioPasaje);

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

        bundle = getIntent().getExtras();

        id_ruta = bundle.getInt(ID_RUTA);
        id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
        id_vehiculo = bundle.getInt(ID_VEHICULO);
        id_horario = bundle.getInt(ID_HORARIO);
        Horario = bundle.getString(HORA);
        id_operador = UsuarioPreferences.getInstance(context).getIdUser();


        final String info = bundle.getString(INFO);

        listParaderos = new ArrayList<String>();
        lisUsuarios = new ArrayList<String>();

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ruta_inicio == ruta_fin) {
                    dialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else {
                    if (cbAsiento.isChecked()) {
                        Intent intent = new Intent(context, SelectSillas.class);
                        intent.putExtra(SelectSillas.CANT_PUESTOS, countPasajes);
                        intent.putExtra(SelectSillas.PRECIO_PASAJE, precio_sum_pasaje);
                        intent.putExtra(SelectSillas.ID_VEHICULO, id_vehiculo);
                        intent.putExtra(SelectSillas.ID_RUTA, id_ruta);
                        intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                        intent.putExtra(SelectSillas.ID_HORARIO, id_horario);
                        intent.putExtra(SelectSillas.HORARIO, Horario);
                        intent.putExtra(SelectSillas.ID_PARADERO_INICIO, id_paradero_inicio);
                        intent.putExtra(SelectSillas.ID_PARADERO_FIN, id_paradero_fin);
                        intent.putExtra(SelectSillas.TIPO_USUARIO, id_usuario);
                        intent.putExtra(INFO, info + "," + ruta_inicio + "," + ruta_fin);

                        startActivity(intent);
                    }
                }
            }
        });

        getParaderos(id_ruta);

        validarCheckBox();
        tvCountItem.setText("" + countPasajes);

    }


    private void formatPrecio(int precio) {
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');

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

        Log.i(Service.TAG, "URL: " + Service.GET_PARADEROS + id);

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
                                listParaderoFin.add(paradero);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));
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

    public String getPrecio(int position, String name_usuario) {
        String precio = "";
        try {
            JSONObject object = resutlParaderos.getJSONObject(position);
            precio = object.getString(name_usuario);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return precio;

    }

    public void goBack(View view) {
        this.finish();
    }

    //
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
                    Log.i("nombre Impresora:", "" + pairedDev.getName());
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

        stringBuffer = new StringBuffer();

        byte[] command = null;
        try {


            try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.img_logo_pdf);
                byte[] data = PrintPicture.POS_PrintBMP(bmp, 384, 0);

                byte[] data1 = PrintPicture.POS_PrintBMP(bmp, 384, 0);

                outputStream.write(data);
                outputStream.write(data1);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PrintTools", "the file isn't exists");
            }

            String msg = "Ticket 45";
            msg += "\n";
            msg += "precio: " + precio_sum_pasaje;
            msg += "\n";
            msg += "horario: " + Horario;
            msg += "\n";
            msg += "Ruta: " + ruta_inicio + "," + ruta_fin;
            outputStream.write(msg.getBytes());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerTicket(final int id_paradero_inicio, final int id_paradero_final, final int id_horario, final int id_operador, final int id_tipo_usuario, final int valor_pagar) {

        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_PIE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //    Log.d(Service.TAG, "response: "+response);

                final SweetAlertDialog alertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);
                alertDialog.setTitleText("Exito")
                        .setContentText("Guardo el ticket")
                        .show();

                Button button = alertDialog.findViewById(R.id.confirm_button);
                // button.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                button.setBackgroundResource(R.drawable.bg_button_main);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            alertDialog.dismiss();

                            showProgress(false);
                            btnFinalizar.setVisibility(View.VISIBLE);
                            btnFinalizar.setEnabled(true);

                            encontrarDispositivoBlue();
                            abrirImpresoraBlue();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
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
                params.put("id_horario", String.valueOf(id_horario));
                params.put("id_operador", String.valueOf(id_operador));
                params.put("hora", Horario);
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


}
