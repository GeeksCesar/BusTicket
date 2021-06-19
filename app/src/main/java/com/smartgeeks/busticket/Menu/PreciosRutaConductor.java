package com.smartgeeks.busticket.Menu;

import android.app.Dialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.smartgeeks.busticket.MainActivity;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.utils.Constantes;
import com.smartgeeks.busticket.utils.PrintTicket;
import com.smartgeeks.busticket.utils.RutaPreferences;
import com.smartgeeks.busticket.utils.UsuarioPreferences;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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

    Bundle bundle;

    int precio_sum_pasaje = 0, id_tipo_usuario = 0,
            id_paradero_inicio = 0, id_paradero_fin = 0;
    String horario, info, nombreEmpresa, desc_empresa, ruta = "", getNameTipoPasajero = "";

    Context context;

    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible, id_empresa;

    SharedPreferences preferences;
    boolean estadoRuta;

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

        bundle = getIntent().getExtras();

        estadoRuta = RutaPreferences.getInstance(context).getEstadoRuta();

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
                        printTicket();
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

    public void goBack(View view) {
        this.finish();
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
        this.finish();
    }

}