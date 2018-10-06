package com.smartgeeks.busticket.Menu;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.DialogAlert;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectSillas extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String CANT_PUESTOS = "CANT_PUESTOS";
    public static final String PRECIO_PASAJE = "PRECIO_PASAJE";

    LinearLayout contenedor_bus;
    private  int sillasOcupadas[] = {3, 4, 6, 9, 11, 15, 16, 20};
    private List<Integer> sillasSeleccionadas = new ArrayList<>();
    Bundle bundle;
    int cant_puestos, precio_pasaje;
    Context context;
    DialogAlert dialogAlert = new DialogAlert();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_sillas);

        bundle = getIntent().getExtras();
        context = SelectSillas.this;
        cant_puestos = bundle.getInt(CANT_PUESTOS);
        precio_pasaje = bundle.getInt(PRECIO_PASAJE);

        initWidgets();
        drawChairBus(2,2,10);
    }

    private void initWidgets() {
        contenedor_bus = findViewById(R.id.contenedor_bus);
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

    public void confirmarSilla(View view) {
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
        Log.e("cant: ", ""+cant_puestos);
        Log.e("seleccionadas: ", ""+ sillasSeleccionadas.size());
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
}
