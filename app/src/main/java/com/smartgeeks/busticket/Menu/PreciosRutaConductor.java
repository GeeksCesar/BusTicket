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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

public class PreciosRutaConductor extends AppCompatActivity implements AdapterPrecios.ItemClickListener {

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORARIO = "HORARIO";

    public static final String INFO = "INFO";
    public static final String TAG = PreciosRutaConductor.class.getSimpleName();


    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int countPasajes = 1, precio_sum_pasaje = 0, id_tipo_usuario = 0,
            id_paradero_inicio = 0, id_paradero_fin = 0;
    String horario, info, nombreEmpresa, desc_empresa, ruta = "";

    Context context;

    int id_horario, id_vehiculo , id_operador, id_ruta, id_ruta_disponible, id_empresa;
    int countConsecutivo = 0;

    SharedPreferences preferences ;
    SharedPreferences.Editor editor ;

    boolean estadoRuta , estadoPrint;
    String namePrint;

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
    private RecyclerView rv_precios;
    private AdapterPrecios adapter;
    private TextView tv_ruta;
    private ProgressBar progress_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_precio_rutas_conductor);

        initWidget();

        findViewById(R.id.btn_olvidar_ruta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences = context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().clear().commit();

                goIntentMain();
            }
        });
    }

    private void initWidget() {
        context = PreciosRutaConductor.this;
        requestQueue = Volley.newRequestQueue(context);

        bundle = getIntent().getExtras();

        estadoRuta = RutaPreferences.getInstance(context).getEstadoRuta();

        getDataPrint();

        if (bundle != null) {
            id_ruta = bundle.getInt(ID_RUTA);
            id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
            id_vehiculo = bundle.getInt(ID_VEHICULO);
            id_horario = bundle.getInt(ID_HORARIO);
            horario = bundle.getString(HORARIO);
            info = bundle.getString(INFO);
            ruta = bundle.getString(INFO).split(",")[1];
            id_operador = UsuarioPreferences.getInstance(context).getIdUser();
            nombreEmpresa = UsuarioPreferences.getInstance(context).getNombreEmpresa();
            desc_empresa = UsuarioPreferences.getInstance(context).getDescEmpresa();
        } else {
            id_ruta = RutaPreferences.getInstance(context).getIdRuta();
            id_ruta_disponible = RutaPreferences.getInstance(context).getIdRutaDisponible();
            id_vehiculo = RutaPreferences.getInstance(context).getIdVehiculo();
            id_horario = RutaPreferences.getInstance(context).getIdHorario();
            horario = RutaPreferences.getInstance(context).getHora();
            info = RutaPreferences.getInstance(context).getInformacion();
            ruta = RutaPreferences.getInstance(context).getInformacion().split(",")[1];
            id_operador = UsuarioPreferences.getInstance(context).getIdUser();
            nombreEmpresa = UsuarioPreferences.getInstance(context).getNombreEmpresa();
            desc_empresa = UsuarioPreferences.getInstance(context).getDescEmpresa();
        }
        nombreEmpresa = nombreEmpresa.trim().toUpperCase();

        Log.e(TAG, "Horario: "+horario);
        Log.e(TAG, "Ruta: "+id_ruta_disponible);
        Log.e(TAG, "Nombre Empresa: "+nombreEmpresa);

        // Listado de Precios para la ruta (Entre paraderos)
        List<TarifaParadero> tarifaParaderos = TarifaParadero.find(TarifaParadero.class,
                "id_ruta = ?", new String[]{"" + id_ruta}, "monto", "monto DESC", null);

        Log.e(TAG, "" + tarifaParaderos.size());

        progress_save = findViewById(R.id.progress_save);
        tv_ruta = findViewById(R.id.tv_ruta);
        tv_ruta.setText(ruta);
        id_empresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        // RecyclerView
        rv_precios = findViewById(R.id.rv_precios);
        rv_precios.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AdapterPrecios(this, tarifaParaderos);
        adapter.setClickListener(this);
        rv_precios.setAdapter(adapter);

        encontrarDispositivoBlue();

        setDataDefault();

    }

    /**
     * Este método define lo datos por defecto que se necesitan para guardar
     * los datos en la base de datos remota
     */
    private void setDataDefault() {
        // Datos para mantener la integridad en la BD Remota, porque no acepta 0 como dato
        id_tipo_usuario = Integer.parseInt(
                TipoUsuario.listAll(TipoUsuario.class, "remoto").get(1).getId_remoto());
        Log.e(TAG, "Tipo de usuario: " + id_tipo_usuario);
        List<Paradero> paraderosList = Paradero.find(Paradero.class, "ruta = ?",
                new String[]{"" + id_ruta}, "remoto", "remoto", null);
        id_paradero_inicio = paraderosList.get(0).getIdRemoto();
        id_paradero_fin = paraderosList.get((paraderosList.size() - 1)).getIdRemoto();
    }

    private void showProgress(boolean show) {
        progress_save.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(View view, int position) {

        precio_sum_pasaje = adapter.getItem(position);
        SweetAlertDialog sweetdialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);

        sweetdialog.setTitleText("Vender Ticket")
                .setContentText("Se imprimirá el Ticket con el precio $" + precio_sum_pasaje)
                .setConfirmText("Imprimir Ticket")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog swAlert) {

                        showProgress(true);
                        swAlert.dismiss();
                        checkInternetConection();
                    }
                })
                .show();

        Button button = sweetdialog.findViewById(R.id.confirm_button);
        button.setTextSize(25);
        button.setBackgroundColor(ContextCompat.getColor(this,R.color.colorBlue));

        float density = context.getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(30 * density);
        button.setPadding(paddingPixel, 5, paddingPixel, 5);
    }

    private void checkInternetConection() {
        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(Boolean internet) {
                if (internet) {
                    // Enviar Ticket al servidor
                    Log.e("TAG", "Hay conexión a Internet");
                    //doSomethingOnConnected
                    registerTicket();

                } else {
                    // Guardar Ticket en Bd Local para sincronización
                    Log.e("TAG", "No hay conexión a Internet");
                    //doSomethingOnNoInternet
                    printOffLine();
                }
            }
        }).execute();
    }

    private void printOffLine(){
        // Guarda los datos en la BD Local
        saveTicketLocal();

        try {
            showProgress(false);
            getDataPrint();

            if (estadoPrint == true) {

                Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

                if (pairedDevice.size() > 0) {
                    for (BluetoothDevice pairedDev : pairedDevice) {
                        if (pairedDev.getName().equals(namePrint)) {
                            bluetoothDevice = pairedDev;
                            abrirImpresoraBlue();
                            break;
                        } else {
                            Log.e(Service.TAG, "error no existe impresora");
                        }
                    }
                } else {
                    Log.e(Service.TAG, "error no existe impresora");
                }

            } else {
                showDialogTiquete();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Envía el Ticket al servidor e imprime el boleto
     */
    private void registerTicket() {
        Log.e(TAG, "Enviando Ticket al servidor");
        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_PIE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(Service.TAG, "response: " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String respuesta = jsonObject.getString("message");

                            if (respuesta.equals("success")) {

                                showProgress(false);
                                countConsecutivo = jsonObject.getInt("count");
                                Log.e(TAG, "Consecutivo: " + countConsecutivo);

                                try {

                                    getDataPrint();

                                    if (estadoPrint == true) {
                                        Log.e(Service.TAG, "entro estado");
                                        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
                                        Log.e(Service.TAG, "parired: " + pairedDevice.size());

                                        if (pairedDevice.size() > 0) {
                                            for (BluetoothDevice pairedDev : pairedDevice) {
                                                if (pairedDev.getName().equals(namePrint)) {
                                                    bluetoothDevice = pairedDev;
                                                    abrirImpresoraBlue();
                                                    break;
                                                } else {
                                                    Log.e(Service.TAG, "error no existe impresora");
                                                }
                                            }
                                        } else {
                                            Log.e(Service.TAG, "error no existe impresora");
                                        }

                                    } else {
                                        showDialogTiquete();
                                    }

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                            } else {
                                DialogAlert.showDialogFailed(context, "Error", "Ha ocurrido un error \n al registrar el ticket", SweetAlertDialog.ERROR_TYPE);
                                showProgress(false);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showProgress(false);
                DialogAlert.showDialogFailed(context, "Error", "Ha ocurrido un error \n al registrar el ticket",
                        SweetAlertDialog.ERROR_TYPE);
                Log.e(Service.TAG, "error: " + volleyError.getMessage());

                if (volleyError instanceof TimeoutError) {
                    DialogAlert.showDialogFailed(context, "Error", "Ha pasado el tiempo Limitado", SweetAlertDialog.WARNING_TYPE);
                    return;
                } else if (volleyError instanceof ServerError) {
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. Error en el servidor", SweetAlertDialog.WARNING_TYPE);
                    return;
                } else if (volleyError instanceof NoConnectionError) {
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. No hay conexion a internet", SweetAlertDialog.WARNING_TYPE);
                    return;
                } else if (volleyError instanceof NetworkError) {
                    DialogAlert.showDialogFailed(context, "Error", "Ops.. Hay error en la red", SweetAlertDialog.WARNING_TYPE);
                    return;
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("id_paradero_inicio", String.valueOf(id_paradero_inicio));
                params.put("id_paradero_fin", String.valueOf(id_paradero_fin));
                params.put("id_ruta", String.valueOf(id_ruta_disponible));
                params.put("id_operador", String.valueOf(id_operador));
                params.put("hora", horario);
                params.put("id_tipo_usuario", String.valueOf(id_tipo_usuario));
                params.put("total_pagar", String.valueOf(precio_sum_pasaje));
                params.put("cantidad", String.valueOf(countPasajes));
                params.put("id_empresa", String.valueOf(id_empresa));

                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void saveTicketLocal() {
        Log.e(TAG, "Ticket Guardado Localmente");
        Ticket ticket = new Ticket();
        ticket.setIdRemoto("");
        ticket.setParadaInicio(id_paradero_inicio);
        ticket.setParadaDestino(id_paradero_fin);
        ticket.setIdRutaDisponible(id_ruta_disponible);
        ticket.setIdOperador(UsuarioPreferences.getInstance(context).getIdUser());
        ticket.setHoraSalida(horario);
        ticket.setTipoUsuario(id_tipo_usuario);
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

    // ----------------- IMPRESION EN EL VOUCHER -------------------------//

    public void getDataPrint() {
        namePrint = RutaPreferences.getInstance(context).getNamePrint();
        estadoPrint = RutaPreferences.getInstance(context).getEstadoPrint();
    }

    private String formatPrecio(int precio) {
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');
        return "$ " + formatPrecio;

    }

    public void goBack(View view) {
        this.finish();
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
                editor.commit() ;

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
                Log.d(Service.TAG, "no hay lista de bluetooth");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.i(Service.TAG, "otro Error" + ex.getMessage());
        }

    }

    public void abrirImpresoraBlue() {
        try {
            Log.i(Service.TAG, "Entro a print");
            //Standard uuid from string //
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            outputStreamTitle = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            comenzarAEscucharDatos();

            printData();

        } catch (Exception ex) {
            Log.i(Service.TAG, "Error P: " +ex.getMessage());
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
                    Log.d(Service.TAG , "method run") ;
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
        Log.d(Service.TAG, "entro a printdata") ;

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
            String msg = "";
            msg += "\n";
            if (countConsecutivo > 0)
                msg += "Ticket N:   " + countConsecutivo + "\n";


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
            precio += "Precio: " + formatPrecio(precio_sum_pasaje) + "\n";
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

        goIntentMain();
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