package com.smartgeeks.busticket.Menu;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartgeeks.busticket.Api.ApiService;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.TarifaUsuario;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.Objects.Tarifa;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.RecyclerItemClickListener;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.databinding.ActivitySelectTarifaBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectTarifa extends AppCompatActivity {

    Context context;

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORARIO = "HORARIO";

    public static final String INFO = "INFO";
    private static final String TAG = "SELECTTARIFA";

    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    int id_horario, id_vehiculo , id_operador, id_ruta, id_ruta_disponible, id_empresa;
    String horario, info, nombreEmpresa, desc_empresa, ruta = "";

    ApiService apiService;
    Call<TarifaUsuario> call ;
    List<TipoUsuario> tarifaLists = new ArrayList<TipoUsuario>();
    RecyclerView.LayoutManager layoutManager;
    AdapterTarifas adapterListTarifas ;

    private ActivitySelectTarifaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySelectTarifaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread= new Thread(runnable);
        myThread.start();
        initWidgets();
        setupOnBackButton();
    }

    private void setupOnBackButton() {
        binding.imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initWidgets() {
        context = SelectTarifa.this;
        apiService = Service.getApiService();

        binding.rvTarifas.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        binding.rvTarifas.setLayoutManager(layoutManager);
        binding.rvTarifas.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.rvTarifas.getContext(), LinearLayoutManager.VERTICAL);
        binding.rvTarifas.addItemDecoration(dividerItemDecoration);

//        getTarifas();
        getTarifasLocal();

        bundle = getIntent().getExtras();

        if (bundle != null) {
            id_ruta = bundle.getInt(ID_RUTA);
            id_ruta_disponible = bundle.getInt(ID_RUTA_DISPONIBLE);
            id_vehiculo = bundle.getInt(ID_VEHICULO);
            id_horario = bundle.getInt(ID_HORARIO);
            horario = bundle.getString(HORARIO);
            info = bundle.getString(INFO);
            ruta = bundle.getString(INFO).split(",")[1];
        } else {
            // Cargar las preferencias de la ruta guardada
            id_ruta = RutaPreferences.getInstance(context).getIdRuta();
            id_ruta_disponible = RutaPreferences.getInstance(context).getIdRutaDisponible();
            id_vehiculo = RutaPreferences.getInstance(context).getIdVehiculo();
            id_horario = RutaPreferences.getInstance(context).getIdHorario();
            horario = RutaPreferences.getInstance(context).getHora();
            info = RutaPreferences.getInstance(context).getInformacion();
            ruta = RutaPreferences.getInstance(context).getInformacion().split(",")[1];
        }
        id_operador = UsuarioPreferences.getInstance(context).getIdUser();

        binding.rvTarifas.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {

                Intent intent;
                TipoUsuario tarifa = tarifaLists.get(position);
                Log.e(TAG, "ID_TIPO_USU: "+tarifa.getId());
                intent = new Intent(context, PreciosRutaConductor.class);
                intent.putExtra(PreciosRutaConductor.ID_RUTA, id_ruta);
                intent.putExtra(PreciosRutaConductor.ID_TIPO_USUARIO, tarifa.getId_remoto());
                intent.putExtra(PreciosRutaConductor.NAME_TIPO_USUARIO, tarifa.getNombre());
                intent.putExtra(PreciosRutaConductor.ID_VEHICULO, id_vehiculo);
                intent.putExtra(PreciosRutaConductor.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                intent.putExtra(PreciosRutaConductor.ID_HORARIO, id_horario);
                intent.putExtra(PreciosRutaConductor.HORARIO, horario);
                intent.putExtra(PreciosRutaConductor.INFO, info);
                startActivity(intent);
            }
        }));


    }

    private void getTarifasLocal() {
        List<TipoUsuario> tipoUsuarios = TipoUsuario.listAll(TipoUsuario.class);
        for (TipoUsuario tipoUsuario: tipoUsuarios) {
            if (Integer.parseInt( tipoUsuario.getId_remoto() ) != 0)
                tarifaLists.add(tipoUsuario);
        }

        adapterListTarifas = new AdapterTarifas(context, tarifaLists);
        binding.rvTarifas.setAdapter(adapterListTarifas);
    }

    public void doWork() {
        runOnUiThread(new Runnable() {
            @Override
            @TargetApi(Build.VERSION_CODES.N)
            public void run() {
                try{
                    getDate();
                }catch (Exception e){

                }
            }
        });
    }

    private void getDate() {
        StringBuilder sb = new StringBuilder();
        Calendar fecha = Calendar.getInstance();

        final int ampm = fecha.get(Calendar.AM_PM);
        sb.append(" ");
        if (ampm == Calendar.AM) {
            sb.append("AM");
        } else {
            sb.append("PM");
        }
        String formato_fecha =  String.format("%1$td-%1$tm-%1$tY", fecha);
        String formato_hora =  String.format("%1$tH:%1$tM", fecha);

        binding.tvTxtDate.setText(formato_fecha);
        binding.tvTxtHora.setText(formato_hora +" "+sb);
    }

    /**
     * Consultar tarifas
     */
    private void getTarifas() {
        call = apiService.allTarifas(UsuarioPreferences.getInstance(context).getIdEmpresa());

        call.enqueue(new Callback<TarifaUsuario>() {
            @Override
            public void onResponse(Call<TarifaUsuario> call, Response<TarifaUsuario> response) {
                Log.e(TAG, "onResponse: "+response.body().toString());
//                tarifaLists = response.body().getTarifas();
                adapterListTarifas = new AdapterTarifas(context,tarifaLists );
                binding.rvTarifas.setAdapter(adapterListTarifas);
            }

            @Override
            public void onFailure(Call<TarifaUsuario> call, Throwable t) {

            }
        });

    }

    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

}