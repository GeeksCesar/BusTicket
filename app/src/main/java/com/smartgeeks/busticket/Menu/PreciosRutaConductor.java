package com.smartgeeks.busticket.Menu;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.sync.SyncServiceRemote;
import com.smartgeeks.busticket.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class PreciosRutaConductor extends AppCompatActivity implements AdapterPrecios.ItemClickListener, PrintTicket.PrintState {

    public static final String ID_RUTA = "ID";
    public static final String ID_TIPO_USUARIO = "ID_TIPO_USUARIO";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORARIO = "HORARIO";
    public static final String NAME_TIPO_USUARIO = "NAME_TIPO_USUARIO";

    public static final String INFO = "INFO";
    public static final String TAG = PreciosRutaConductor.class.getSimpleName();

    private static final int LEFT_LENGTH = 16;
    private static final int RIGHT_LENGTH = 16;
    private static final int LEFT_TEXT_MAX_LENGTH = 8;


    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    //VOLLEY
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int countPasajes = 1, precio_sum_pasaje = 0, id_tipo_usuario = 0,
            id_paradero_inicio = 0, id_paradero_fin = 0;
    String horario, info, nombreEmpresa, desc_empresa, ruta = "", getNameTipoPasajero = "";

    Context context;

    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible, id_empresa;
    private String numVoucher = "";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    boolean estadoRuta, estadoPrint;
    private boolean state_sync = false;
    String namePrint;

    //Configuracion Impresora
    private final ArrayList<String> lisPrintBluetooth = new ArrayList<>();
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
    Button btnCancel;
    ListView lstPrint;
    private AdapterPrecios adapter;
    private ProgressBar progress_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_precio_rutas_conductor);

        initWidget();

        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(Constantes.ACTION_RUN_REMOTE_SYNC);
        filter.addAction(Constantes.EXTRA_PROGRESS);
        filter.addAction(Constantes.ACTION_FINISH_REMOTE_SYNC);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                receiver, filter);

        findViewById(R.id.btn_olvidar_ruta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences = context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().clear().apply();

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
            id_tipo_usuario = Integer.parseInt(bundle.getString(ID_TIPO_USUARIO));
            getNameTipoPasajero = bundle.getString(NAME_TIPO_USUARIO);
            id_horario = bundle.getInt(ID_HORARIO);
            horario = bundle.getString(HORARIO);
            info = bundle.getString(INFO);
            ruta = bundle.getString(INFO).split(",")[1];
        } else {
            id_ruta = RutaPreferences.getInstance(context).getIdRuta();
            id_ruta_disponible = RutaPreferences.getInstance(context).getIdRutaDisponible();
            id_vehiculo = RutaPreferences.getInstance(context).getIdVehiculo();
            id_horario = RutaPreferences.getInstance(context).getIdHorario();
            horario = RutaPreferences.getInstance(context).getHora();
            info = RutaPreferences.getInstance(context).getInformacion();
            ruta = RutaPreferences.getInstance(context).getInformacion().split(",")[1];
        }
        id_operador = UsuarioPreferences.getInstance(context).getIdUser();
        nombreEmpresa = UsuarioPreferences.getInstance(context).getNombreEmpresa();
        desc_empresa = UsuarioPreferences.getInstance(context).getDescEmpresa();
        nombreEmpresa = nombreEmpresa.trim().toUpperCase();

        Log.e(TAG, "Horario: " + horario);
        Log.e(TAG, "Ruta: " + id_ruta);
        Log.e(TAG, "Tipo usuario: " + id_tipo_usuario);
        Log.e(TAG, "Nombre usuario: " + getNameTipoPasajero);
        Log.e(TAG, "ID_Ruta: " + id_ruta_disponible);
        Log.e(TAG, "Nombre Empresa: " + nombreEmpresa);

        // Listado de Precios para la ruta (Entre paraderos)
        List<TarifaParadero> tarifaParaderos = TarifaParadero.find(TarifaParadero.class,
                "id_ruta = ? and tipo_usuario = ?", new String[]{"" + id_ruta, "" + id_tipo_usuario}, "monto", "monto DESC", null);

        Log.e(TAG, "count-> " + tarifaParaderos.size());

        progress_save = findViewById(R.id.progress_save);
        TextView tv_ruta = findViewById(R.id.tv_ruta);
        tv_ruta.setText(ruta);
        id_empresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        // RecyclerView
        RecyclerView rv_precios = findViewById(R.id.rv_precios);
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
                        checkInternetConnection();
                        //printTicket();
                    }
                })
                .show();

        Button button = sweetdialog.findViewById(R.id.confirm_button);
        button.setTextSize(25);
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlue));

        float density = context.getResources().getDisplayMetrics().density;
        int paddingPixel = (int) (30 * density);
        button.setPadding(paddingPixel, 5, paddingPixel, 5);
    }

    private void printTicket() {
        PrintTicket printTicket = new PrintTicket(context, this);
        printTicket.setData(
                id_paradero_inicio,
                id_paradero_fin,
                id_ruta_disponible,
                horario,
                id_tipo_usuario,
                precio_sum_pasaje,
                id_vehiculo,
                getNameTipoPasajero,
                info
        );
        printTicket.print();
    }

    private void checkInternetConnection() {
        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(Boolean internet) {
                if (internet) {
                    // Enviar Ticket al servidor
                    registerTicket();
                    remoteSync(); // Enviar Tickets locales
                } else {
                    // Guardar Ticket en Bd Local para sincronización
                    printOffLine();
                }
            }
        }).execute();
    }

    private void printOffLine() {
        // Guarda los datos en la BD Local
        saveTicketLocal();

        try {
            showProgress(false);
            getDataPrint();

            if (estadoPrint) {

                Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

                if (pairedDevice.size() > 0) {
                    for (BluetoothDevice pairedDev : pairedDevice) {
                        if (pairedDev.getName().equals(namePrint)) {
                            bluetoothDevice = pairedDev;
                            abrirImpresoraBlue();
                            break;
                        } else {
                            Log.e(TAG, "error no existe impresora");
                        }
                    }
                } else {
                    Log.e(TAG, "error no existe impresora");
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
        stringRequest = new StringRequest(Request.Method.POST, Service.SET_TICKET_PIE_TEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "response: " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String respuesta = jsonObject.getString("message");

                            if (respuesta.equals("success")) {

                                showProgress(false);
                                numVoucher = jsonObject.getString("num_voucher");
                                Log.e(TAG, "Num Voucher: " + numVoucher);

                                try {

                                    getDataPrint();

                                    if (estadoPrint) {
                                        Log.e(TAG, "entro estado");
                                        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
                                        Log.e(TAG, "parired: " + pairedDevice.size());

                                        if (pairedDevice.size() > 0) {
                                            for (BluetoothDevice pairedDev : pairedDevice) {
                                                if (pairedDev.getName().equals(namePrint)) {
                                                    bluetoothDevice = pairedDev;
                                                    abrirImpresoraBlue();
                                                    break;
                                                } else {
                                                    Log.e(TAG, "error no existe impresora");
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "error no existe impresora");
                                        }

                                    } else {
                                        showDialogTiquete();
                                    }

                                } catch (Exception ex) {
                                    Log.e(TAG, "onResponse: " + ex.getMessage());
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
                Log.e(TAG, "error: " + volleyError.getMessage());

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
                params.put("id_vehiculo", String.valueOf(id_vehiculo));

                return params;
            }
        };

        requestQueue.add(stringRequest);

    }

    private void saveTicketLocal() {
        String fecha = Helpers.getCurrentDate();
        String hora = Helpers.getCurrentTime();
        numVoucher = id_vehiculo + "" + id_operador + "-" + Helpers.setString2DateVoucher(fecha) + "-" + Helpers.setString2HourVoucher(hora);

        Log.e(TAG, "Ticket Guardado Localmente " + numVoucher);
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


        btnCancel = dialogPrint.findViewById(R.id.btnCancelar);
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
                editor = preferences.edit();

                editor.putString(RutaPreferences.NAME_PRINT, name_impresora);
                editor.putBoolean(RutaPreferences.ESTADO_PRINT, true);
                editor.apply();

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
                dialogPrint.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPrint.dismiss();
            }
        });

        try {
            dialogPrint.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            } else {
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

            printData();
            goIntentTarifas();

        } catch (Exception ex) {
            Log.i(TAG, "Error P: " + ex.getMessage());
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
                    Log.d(TAG, "method run");
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

    public static String printThreeData(String leftText, String middleText, String rightText, String tipo) {
        StringBuilder sb = new StringBuilder();
        // At most LEFT_TEXT_MAX_LENGTH Chinese characters + two dots are displayed on the left
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // Calculate the length of the space between the left text and the middle text
        int marginBetweenLeftAndMiddle = 0;
        if (tipo.equals("One")) {
            marginBetweenLeftAndMiddle = LEFT_LENGTH - leftTextLength - middleTextLength / 2;
        } else {
            marginBetweenLeftAndMiddle = 13 - leftTextLength - middleTextLength / 2;
        }

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(middleText);

        // Calculate the length of the space between the right text and the middle text
        int marginBetweenMiddleAndRight = 0;
        if (tipo.equals("One")) {
            marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength;
        } else {
            marginBetweenMiddleAndRight = 13 - middleTextLength / 2 - rightTextLength;
        }

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }

        // When printing, I found that the rightmost text is always one character to the right, so a space needs to be deleted
        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    void printData() {
        Log.d(TAG, "entro a printdata");
        String[] split = info.split(",");
        byte[] command = null;
        try {
            byte[] arrayOfByte1 = {27, 33, 0};
            byte[] format = {27, 33, 0};
            byte[] centrado = {0x1B, 'a', 0x01};
            byte[] der = {0x1B, 'a', 0x02};
            byte[] izq = {0x1B, 'a', 0x00};

            // Espacio superior
            outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);
            /*try {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.img_logo_pdf);
                byte[] data = PrintPicture.POS_PrintBMP(bmp, 384, 0);
                outputStream.write(data);
                // Espacio inferior
                outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PrintTools", "the file isn't exists");
            }*/
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            format[2] = ((byte) (0x21 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            String nom_empre = UsuarioPreferences.getInstance(context).getNombreEmpresa().toUpperCase() + "\n";
            outputStream.write(nom_empre.getBytes(), 0, nom_empre.getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write(("================================").getBytes(), 0, ("================================").getBytes().length);
            outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);
            outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);
            // Width
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            String str_pago = "Usted pago:\n";
            str_pago = str_pago.replace("(", "");
            outputStream.write(str_pago.getBytes(), 0, str_pago.getBytes().length);
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            format[2] = ((byte) (0x21 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            String str_precio = formatPrecio(precio_sum_pasaje) + "\n";
            outputStream.write(str_precio.getBytes(), 0, str_precio.getBytes().length);
            format[2] = ((byte) (0x20 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            String str_tipo_pasajero = getNameTipoPasajero + "\n";
            str_tipo_pasajero = str_tipo_pasajero.replace("(", "");
            outputStream.write(str_tipo_pasajero.getBytes(), 0, str_tipo_pasajero.getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write(("--------------------------------" + "\n").getBytes(), 0, ("--------------------------------" + "\n").getBytes().length);
            format[2] = (byte) (0x8);
            outputStream.write(format);
            outputStream.write(printThreeData("Fecha", "Salida", "Cantidad", "One").getBytes(), 0, printThreeData("Fecha", "Salida", "Cantidad", "One").getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(format);
            outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);
            format[2] = ((byte) (0x8 | arrayOfByte1[2]));
            format[2] = ((byte) (0x10 | arrayOfByte1[2]));
            outputStream.write(format);
            String fecha_codi = Helpers.getDate();
            String[] array_fecha = fecha_codi.split("-");
            fecha_codi = array_fecha[2] + "-" + array_fecha[1] + "-" + array_fecha[0].substring(array_fecha[0].length() - 2);
            String[] hora_salida_s = horario.trim().split(":");
            String hora_salida_str = hora_salida_s[0] + ":" + hora_salida_s[1];
            outputStream.write(printThreeData(fecha_codi, hora_salida_str, String.valueOf(countPasajes), "Two").getBytes(), 0, printThreeData(fecha_codi, hora_salida_str, String.valueOf(countPasajes), "Two").getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(format);
            outputStream.write(("\n").getBytes(), 0, ("\n").getBytes().length);
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write(("--------------------------------" + "\n").getBytes(), 0, ("--------------------------------" + "\n").getBytes().length);
            outputStream.write(format);
            outputStream.write(("\n\n").getBytes(), 0, ("\n\n").getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(centrado);
            outputStream.write(format);
            outputStream.write((desc_empresa + "\n\n").getBytes(), 0, (desc_empresa + "\n\n").getBytes().length);
            format[2] = ((byte) (0x8 | arrayOfByte1[2]));
            outputStream.write(izq);
            outputStream.write(format);
            String str = "";
            str += numVoucher + "\n";
            outputStream.write(str.getBytes(), 0, str.getBytes().length);
            format = new byte[]{27, 33, 0};
            outputStream.write(izq);
            outputStream.write(format);
            String str_emision = "Emision: " + fecha_codi + "\n";
            str_emision += Helpers.getTime() + " " + split[0] + " " + UsuarioPreferences.getInstance(context).getNombre() + "\n";
            outputStream.write(str_emision.getBytes(), 0, str_emision.getBytes().length);
            format[2] = ((byte) (0x8 | arrayOfByte1[2]));
            outputStream.write(centrado);
            outputStream.write(format);
            String str_two = "";
            str_two += "www.busticket.cl\n";
            str_two += "Copia Cliente";
            outputStream.write(str_two.getBytes(), 0, str_two.getBytes().length);
            //no serive desde abajo
            format = new byte[]{27, 33, 0};
            outputStream.write(format);
            outputStream.write(("\n\n\n\n").getBytes(), 0, ("\n\n\n\n").getBytes().length);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "error in printdata");
        }

    }

    private void goIntentMain() {
        try {
            disconnectBT();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.BACK, true);
        startActivity(intent);
        finish();
    }

    private void goIntentTarifas() {
        try {
            disconnectBT();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*Intent intent = new Intent(context, SelectTarifa.class);
        startActivity(intent);*/
        this.finish();
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

    /**
     * Ejecutar el servicio de Sincronización Remota
     */
    private void remoteSync() {
        if (!state_sync) {
            Intent sync = new Intent(context, SyncServiceRemote.class);
            sync.setAction(Constantes.ACTION_RUN_REMOTE_SYNC);
            getApplicationContext().startService(sync);
        }
    }

    @Override
    public void isLoading(boolean state) {
        showProgress(state);
    }

    @Override
    public void onFinishPrint() {
        this.finish();
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case Constantes.ACTION_RUN_REMOTE_SYNC:
                    state_sync = intent.getBooleanExtra(Constantes.EXTRA_PROGRESS, false);
            }
        }
    }

}