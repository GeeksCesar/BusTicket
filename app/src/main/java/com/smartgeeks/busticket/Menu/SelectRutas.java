package com.smartgeeks.busticket.Menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.orm.query.Select;
import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.utils.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SelectRutas extends AppCompatActivity implements PrintTicket.PrintState {

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
    View contenedorCheckBox, contenedorPrecio;
    Button btnSiguiente, btnFinalizar, btnOlvidarRuta, btnMenos, btnMas;
    Spinner spInicio, spFin, spPasajero;
    CheckBox cbAsiento, cbDePie;
    TextView tvPrecioPasaje, tvCountItem;

    private ArrayList<String> listParaderos;
    private ArrayList<String> lisUsuarios;
    private ArrayList<String> listParaderoFin = new ArrayList<>();

    // Listas para SQLite
    private List<Paradero> paraderosList = new ArrayList<>();
    private List<TipoUsuario> tipoUsuariosList = new ArrayList<>();

    int countPasajes = 1, precio_sum_pasaje, precioPasaje, valor_pasaje, id_tipo_usuario, id_paradero_inicio, id_paradero_fin, position_tipo_usuario, sizeTarifas;
    String ruta_inicio, ruta_fin, horario, info, nombreEmpresa, desc_empresa;

    Context context;


    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible, id_empresa;
    String nameUsuario, getPrecioPasaje;

    SharedPreferences preferences;

    boolean estadoRuta, estadoPrint;
    String namePrint;

    private PrintTicket printTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_rutas);

        initWidget();
        printTicket = new PrintTicket(SelectRutas.this, this);

        spPasajero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                id_tipo_usuario = Integer.parseInt(tipoUsuariosList.get(position).getId_remoto());
                position_tipo_usuario = position;

                nameUsuario = parent.getItemAtPosition(position).toString();

                try {
                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, id_tipo_usuario);
                    Log.e(TAG, "Precio del pasaje: " + precioPasaje);
                    valor_pasaje = precioPasaje * countPasajes;

                    formatPrecio(valor_pasaje);
                } catch (Exception e) {
                    Log.e(TAG, "Error al obtener precio del pasaje");
                    e.getMessage();
                }

                if (id_tipo_usuario == 0) {
                    showView(false);
                } else if (sizeTarifas == 0 && Integer.parseInt(tipoUsuariosList.get(0).getId_remoto()) > 0) {
                    Toast.makeText(context, "No se han definido precios para este usuario.", Toast.LENGTH_SHORT).show();
                    showView(false);
                } else {
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
                        new String[]{"" + id_ruta, "" + ruta_fin}, "remoto", "remoto", null).get(0).getIdRemoto();

                try {
                    precioPasaje = (int) getPrecioSQLite(id_paradero_inicio, id_paradero_fin, id_tipo_usuario);
                    valor_pasaje = precioPasaje * countPasajes;
                    formatPrecio(valor_pasaje);
                } catch (Exception e) {
                    e.getMessage();
                }

                if (sizeTarifas == 0 && Integer.parseInt(tipoUsuariosList.get(0).getId_remoto()) > 0) {
                    Toast.makeText(context, "No se han definido precios para este usuario.", Toast.LENGTH_SHORT).show();
                    contenedorCheckBox.setVisibility(View.GONE);
                    contenedorPrecio.setVisibility(View.GONE);
                } else {
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

                String precio = String.valueOf(precio_sum_pasaje);

                if (ruta_inicio.equals(ruta_fin)) {
                    DialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                } else if (precio_sum_pasaje == 0) {
                    DialogAlert.showDialogFailed(context, "Error", "Verifique el valor de pasaje", SweetAlertDialog.NORMAL_TYPE);
                } else {
                    btnFinalizar.setEnabled(false);
                    btnFinalizar.setVisibility(View.GONE);

                    printTicket.setData(
                            id_paradero_inicio,
                            id_paradero_fin,
                            id_ruta_disponible,
                            horario,
                            id_tipo_usuario,
                            precio_sum_pasaje,
                            id_vehiculo,
                            nameUsuario,
                            info
                    );
                    printTicket.print();

                }

            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                precio_sum_pasaje = precioPasaje * countPasajes;
                String precio = String.valueOf(precio_sum_pasaje);

                if (ruta_inicio == ruta_fin) {
                    DialogAlert.showDialogFailed(context, "Error", "Las opciones de paradero deben ser distintas", SweetAlertDialog.NORMAL_TYPE);
                    return;
                } else if (precio_sum_pasaje == 0 || precio == null) {
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

        if (estadoRuta) {
            btnOlvidarRuta.setVisibility(View.VISIBLE);
        } else {
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

        Log.e(TAG, "Horario: " + horario);
        Log.e(TAG, "Ruta: " + id_ruta_disponible);

        id_empresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        listParaderos = new ArrayList<String>();
        lisUsuarios = new ArrayList<String>();

        getParaderosSQLite(id_ruta);

        validarCheckBox();
        tvCountItem.setText("" + countPasajes);

    }

    public void getDataPrint() {
        namePrint = RutaPreferences.getInstance(context).getNamePrint();
        estadoPrint = RutaPreferences.getInstance(context).getEstadoPrint();
    }

    private void showView(boolean show) {
        contenedorCheckBox.setVisibility(show ? View.VISIBLE : View.GONE);
        contenedorPrecio.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void formatPrecio(int precio) {
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');
        tvPrecioPasaje.setText("$ " + formatPrecio);
        getPrecioPasaje = "$ " + formatPrecio;

    }

    private void validarCheckBox() {
        cbAsiento.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                btnSiguiente.setText("Siguiente");

                if (cbAsiento.isChecked()) {
                    cbDePie.setChecked(false);

                    if (sizeTarifas > 0) {
                        btnSiguiente.setVisibility(View.VISIBLE);
                        btnFinalizar.setVisibility(View.GONE);
                        // btnOlvidarRuta.setVisibility(View.GONE);
                    }

                } else {
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

                    if (sizeTarifas > 0) {
                        btnSiguiente.setVisibility(View.GONE);
                        btnFinalizar.setVisibility(View.VISIBLE);
                    }

                    //  btnOlvidarRuta.setVisibility(View.GONE);
                } else {
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

    /**
     * ***********   Consultas SQLite  *************
     */

    private void getParaderosSQLite(int id_ruta) {
        listParaderos.clear();
        listParaderoFin.clear();

        paraderosList = Paradero.find(Paradero.class, "ruta = ?",
                new String[]{"" + id_ruta}, "remoto", "remoto", null);

        if (paraderosList.size() == 0) {
            DialogAlert.showDialogFailed(context, "Atención", "No se han definido paraderos para la ruta ", SweetAlertDialog.WARNING_TYPE);
        }

        for (Paradero paradero : paraderosList) {
            listParaderos.add(paradero.getParadero());
        }

        try {
            listParaderos.remove(paraderosList.size() - 1);
        } catch (Exception e) {
            e.getMessage();
        }

        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));

        getUsuariosSQLite();
    }

    public void getParaderosFinSQLite(int paradero_inicio) {
        listParaderoFin.clear();

        for (int i = (paradero_inicio + 1); i < paraderosList.size(); i++) {
            listParaderoFin.add(paraderosList.get(i).getParadero());
        }

        spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderoFin));
    }

    private void getUsuariosSQLite() {
        lisUsuarios.clear();

        // tipoUsuariosList = TipoUsuario.listAll(TipoUsuario.class, "remoto");
        tipoUsuariosList = Select.from(TipoUsuario.class).orderBy("nombre").list();
        TipoUsuario firstElement = tipoUsuariosList.remove(tipoUsuariosList.size() - 1);
        tipoUsuariosList.add(0, firstElement);

        for (TipoUsuario tipoUsuario : tipoUsuariosList) {
            lisUsuarios.add(tipoUsuario.getNombre());
        }

        //setAdapter
        spPasajero.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_tipo_pasajero, R.id.txtName, lisUsuarios));
    }

    private double getPrecioSQLite(int id_paradero_inicio, int id_paradero_fin, int id_tipo_usuario) {
        List<TarifaParadero> tarifaParaderos = TarifaParadero.find(TarifaParadero.class,
                "parada_inicio = ? AND parada_fin = ? AND tipo_usuario = ?", "" + id_paradero_inicio,
                "" + id_paradero_fin, "" + id_tipo_usuario);

        double precio = tarifaParaderos.get(0).getMonto();
        sizeTarifas = tarifaParaderos.size();

        return precio;
    }


    private void goIntentMain() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.BACK, true);
        startActivity(intent);
        finish();
    }

    @Override
    public void isLoading(boolean state) {
        showProgress(state);
    }

    @Override
    public void onFinishPrint() {

    }
}