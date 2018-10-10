package com.smartgeeks.busticket.Menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.DialogAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectSillas extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String CANT_PUESTOS = "CANT_PUESTOS";
    public static final String PRECIO_PASAJE = "PRECIO_PASAJE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";

    private String TAG = "SelectSillas";

    LinearLayout contenedor_bus;
    private List<Integer> sillasOcupadas = new ArrayList<>();
    private List<Integer> sillasSeleccionadas = new ArrayList<>();
    Bundle bundle;
    int cant_puestos, precio_pasaje, id_vehiculo, id_horario;
    String info_ruta;
    Context context;
    DialogAlert dialogAlert = new DialogAlert();
    TextView tvVehiculo, tvRuta, tvHora, tvInicio, tvFin;
    ProgressDialog progress;

    //VOLLEY
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    StringRequest stringRequest;

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
        info_ruta = bundle.getString(SelectRutas.INFO);

        initWidgets();
        showProgressDialog();
        // Obtengo los datos del vehículo
        getSillasOcupadas(id_horario);
    }

    private void initWidgets() {
        contenedor_bus = findViewById(R.id.contenedor_bus);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        tvRuta = findViewById(R.id.tvRuta);
        tvHora = findViewById(R.id.tvHora);
        tvInicio = findViewById(R.id.tvInicio);
        tvFin = findViewById(R.id.tvFin);

        showDataTextView();
    }

    private void showDataTextView() {
        String[] split = info_ruta.split(",");
        tvVehiculo.setText("Vehículo: "+split[0]);
        tvRuta.setText("Ruta: "+split[1]);
        tvHora.setText("Hora: "+split[2]);
        tvInicio.setText("Inicio: "+split[3]);
        tvFin.setText("Fin: "+split[4]);
    }

    private void drawChairBus(int columns_izq, int columns_der, int filas){
        int silla = 1;

        // Parámetros del LinearLayout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 5;
        params.bottomMargin = 5;

        // Parámetros del espacio
        LinearLayout.LayoutParams space_params = new LinearLayout.LayoutParams(50,0, 1f );

        // Parámetros de la silla
        LinearLayout.LayoutParams silla_params = new LinearLayout.LayoutParams(50,
                70, 1f);
        silla_params.setMargins(4, 8, 4, 8);

        // Dibujo las filas
        for (int i = 1; i <= filas; i++){

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            //Dibujo las columnas izquierdas
            for (int a = 1; a <= columns_izq; a++){

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
            }

            //Dibujo el espacio de en el bus
            View espacio = new View(this);
            espacio.setLayoutParams(space_params);
            linearLayout.addView(espacio);

            // Dibujo las columnas derechas
            for (int b = 1; b <= columns_der; b++){

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
            }

            contenedor_bus.addView(linearLayout);
        }
    }

    private void showProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setMessage("Cargando bus...");
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
        progress.show();
    }

    /**
     * Dibuja las sillas ocupadas
     * @param silla
     * @param puesto
     */
    private void drawSillaOcupada(int silla, ToggleButton puesto){
        // Verificar si la silla está ocupada
        for (int ocupada : sillasOcupadas) {
            if (ocupada == silla) {
                puesto.setEnabled(false);
                puesto.setClickable(false);
                puesto.setBackground(ContextCompat.getDrawable(this, R.drawable.silla_ocupada));
            }
        }
    }

    /**
     * Elimina una silla del arreglo, de acuerdo a su posicion
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
     * Confirmar silla
     * @param view
     */
    public void confirmarSilla(View view) {
        Toast.makeText(context, "Has seleccionado "+sillasSeleccionadas.size(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Verifica el estado del toggle button
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int silla_seleccionda = buttonView.getId();

        // Guardo o elimino la silla
        if (isChecked == true) {
            sillasSeleccionadas.add(silla_seleccionda);
            if (sillasSeleccionadas.size() > cant_puestos ){
                dialogAlert.showDialogFailed(context, "Error", "Ya has seleccionado los "+cant_puestos+" puestos.", SweetAlertDialog.ERROR_TYPE);
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
        Log.d(Service.TAG, "rutas: "+URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                       jsonObject = new JSONObject(response);
                       Log.e(TAG, ""+response);
                       JSONArray jsonArray = jsonObject.getJSONArray("vehiculos");

                       if (jsonArray.length() > 0) {
                           JSONObject json = jsonArray.getJSONObject(0);
                           drawChairBus(json.getInt("f_izquierda"), json.getInt("f_derecha"),
                                   json.getInt("columnas"));
                       }

                } catch (JSONException e) {
                      e.printStackTrace();
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, ""+volleyError);
                progress.dismiss();
            }
        });
        requestQueue.add(stringRequest);

    }

    /**
     * Consultar sillas ocupadas por horario de ruta
     * @param id_horario
     */
    private void getSillasOcupadas(int id_horario) {

        String URL = Service.SILLAS_OCUPADAS + id_horario;
        Log.d(Service.TAG, "rutas: "+URL);
        stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response);
                    Log.e(TAG, "Sillas: "+response);

                    for (int i = 0; i < jsonArray.length(); i++){
                        sillasOcupadas.add(jsonArray.getJSONObject(i).getInt("silla"));
                    }
                    getVehiculo(id_vehiculo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, ""+volleyError);
            }
        });
        requestQueue.add(stringRequest);

    }

}
