package com.smartgeeks.busticket.Menu;

import android.annotation.TargetApi;
import android.content.Context;
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

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Api.ApiService;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Login;
import com.smartgeeks.busticket.Modelo.Silla;
import com.smartgeeks.busticket.Modelo.TarifaUsuario;
import com.smartgeeks.busticket.Objcect.Tarifa;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.RecyclerItemClickListener;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectTarifa extends AppCompatActivity {

    TextView  tvFecha, tvHora;
    Context context;
    RecyclerView rvTarifas;

    private static final String TAG = SelectTarifa.class.getSimpleName();

    ApiService apiService;
    Call<TarifaUsuario> call ;
    List<Tarifa> tarifaLists;
    RecyclerView.LayoutManager layoutManager;
    AdapterTarifas adapterListTarifas ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_tarifa);
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread= new Thread(runnable);
        myThread.start();
        initWidgets();
    }

    private void initWidgets() {
        context = SelectTarifa.this;
        apiService = Service.getApiService();
        //bundle = getIntent().getExtras();
        tvFecha = findViewById(R.id.tvTxtDate);
        tvHora = findViewById(R.id.tvTxtHora);
        rvTarifas = findViewById(R.id.rv_tarifas);

        rvTarifas.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        rvTarifas.setLayoutManager(layoutManager);
        rvTarifas.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTarifas.getContext(), LinearLayoutManager.VERTICAL);
        rvTarifas.addItemDecoration(dividerItemDecoration);
        getTarifas();

        rvTarifas.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Tarifa tarifa = tarifaLists.get(position);
                UsuarioPreferences.getInstance(SelectTarifa.this).setIdTipoUsuario(Integer.parseInt(tarifa.getId()));
            }
        }));

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

        tvFecha.setText(formato_fecha);
        tvHora.setText(formato_hora +" "+sb);
    }

    /**
     * Consultar tarifas
     */
    private void getTarifas() {
        call = apiService.allTarifas(10);

        call.enqueue(new Callback<TarifaUsuario>() {
            @Override
            public void onResponse(Call<TarifaUsuario> call, Response<TarifaUsuario> response) {
                tarifaLists = response.body().getTarifas();
                adapterListTarifas = new AdapterTarifas(context,tarifaLists );
                rvTarifas.setAdapter(adapterListTarifas);
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