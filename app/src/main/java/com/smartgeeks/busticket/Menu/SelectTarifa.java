package com.smartgeeks.busticket.Menu;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.orm.query.Select;
import com.smartgeeks.busticket.Modelo.Paradero;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.databinding.ActivitySelectTarifaBinding;
import com.smartgeeks.busticket.utils.PrintTicket;
import com.smartgeeks.busticket.utils.RecyclerItemClickListener;
import com.smartgeeks.busticket.utils.RutaPreferences;
import com.smartgeeks.busticket.utils.UsuarioPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SelectTarifa extends AppCompatActivity implements PrintTicket.PrintState {

    Context context;

    public static final String ID_RUTA = "ID";
    public static final String ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE";
    public static final String ID_VEHICULO = "ID_VEHICULO";
    public static final String ID_HORARIO = "ID_HORARIO";
    public static final String HORARIO = "HORARIO";

    public static final String INFO = "INFO";
    private static final String TAG = "SELECTTARIFA";

    Bundle bundle;

    int id_horario, id_vehiculo, id_operador, id_ruta, id_ruta_disponible;
    String horario, info, ruta = "";

    List<TipoUsuario> tarifaLists = new ArrayList<TipoUsuario>();
    RecyclerView.LayoutManager layoutManager;
    AdapterTarifas adapterListTarifas;

    private ActivitySelectTarifaBinding binding;

    private PrintTicket printTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySelectTarifaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = SelectTarifa.this;

        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
        initWidgets();
        setupOnBackButton();

        printTicket = new PrintTicket(SelectTarifa.this, this);
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
        binding.rvTarifas.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        binding.rvTarifas.setLayoutManager(layoutManager);
        binding.rvTarifas.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.rvTarifas.getContext(), LinearLayoutManager.VERTICAL);
        binding.rvTarifas.addItemDecoration(dividerItemDecoration);

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

                TipoUsuario tipoUsuario = tarifaLists.get(position);
                Log.e(TAG, "ID_TIPO_USUARIO: " + tipoUsuario.getId());

                // Listado de Precios para la ruta (Entre paraderos)
                List<TarifaParadero> listPrices = TarifaParadero.find(TarifaParadero.class,
                        "id_ruta = ? and tipo_usuario = ?", new String[]{"" + id_ruta, "" + tipoUsuario.getId_remoto()}, "monto", "monto DESC", null);

                Log.e(TAG, "ListPrices: " + listPrices.size());

                if (listPrices.size() > 1) {
                    Intent intent = new Intent(context, PreciosRutaConductor.class);
                    intent.putExtra(PreciosRutaConductor.ID_RUTA, id_ruta);
                    intent.putExtra(PreciosRutaConductor.ID_TIPO_USUARIO, tipoUsuario.getId_remoto());
                    intent.putExtra(PreciosRutaConductor.NAME_TIPO_USUARIO, tipoUsuario.getNombre());
                    intent.putExtra(PreciosRutaConductor.ID_VEHICULO, id_vehiculo);
                    intent.putExtra(PreciosRutaConductor.ID_RUTA_DISPONIBLE, id_ruta_disponible);
                    intent.putExtra(PreciosRutaConductor.ID_HORARIO, id_horario);
                    intent.putExtra(PreciosRutaConductor.HORARIO, horario);
                    intent.putExtra(PreciosRutaConductor.INFO, info);
                    startActivity(intent);
                } else if (listPrices.size() == 1) {

                    // Destination List
                    List<Paradero> destinationsList = Paradero.find(Paradero.class, "ruta = ?",
                            new String[]{"" + id_ruta}, "remoto", "remoto", null);
                    int departureId = destinationsList.get(0).getIdRemoto();
                    int arrivalId = destinationsList.get((destinationsList.size() - 1)).getIdRemoto();
                    int ticketPrice = listPrices.get(0).getMonto();

                    showDialogPrintTicket(departureId, arrivalId, ticketPrice, tipoUsuario);
                }

            }
        }));


    }

    private void showDialogPrintTicket(final int departureId, final int arrivalId, final int ticketPrice, final TipoUsuario tipoUsuario) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitleText("Vender Ticket")
                .setContentText("Se imprimirá el Ticket con el precio $" + ticketPrice)
                .setConfirmText("Imprimir Ticket")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog swAlert) {
                        swAlert.dismiss();

                        printTicket(
                                departureId,
                                arrivalId,
                                id_ruta_disponible,
                                horario,
                                Integer.parseInt(tipoUsuario.getId_remoto()),
                                Double.parseDouble(String.valueOf(ticketPrice)),
                                id_vehiculo,
                                tipoUsuario.getNombre(),
                                info
                        );

                    }
                })
                .show();

        Button button = sweetAlertDialog.findViewById(R.id.confirm_button);
        button.setTextSize(25);
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlue));

        float density = context.getResources().getDisplayMetrics().density;
        int paddingPixel = (int) (30 * density);
        button.setPadding(paddingPixel, 5, paddingPixel, 5);
    }

    /**
     * This method printTicket.
     */
    private void printTicket(int departureId, int arrivalId, int routeAvailableId, String schedule,
                             int passengerTypeId, Double ticketPrice, int vehicleId, String passengerTypeName,
                             String companyInfo) {
        printTicket.setData(
                departureId,
                arrivalId,
                routeAvailableId,
                schedule,
                passengerTypeId,
                ticketPrice,
                vehicleId,
                passengerTypeName,
                companyInfo
        );
        printTicket.print();
    }


    private void getTarifasLocal() {
        // List<TipoUsuario> tipoUsuarios = TipoUsuario.listAll(TipoUsuario.class);
        List<TipoUsuario> tipoUsuarios = Select.from(TipoUsuario.class).orderBy("nombre").list();

        for (TipoUsuario tipoUsuario : tipoUsuarios) {
            if (Integer.parseInt(tipoUsuario.getId_remoto()) != 0)
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
                try {
                    getDate();
                } catch (Exception e) {

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
        String formato_fecha = String.format("%1$td-%1$tm-%1$tY", fecha);
        String formato_hora = String.format("%1$tH:%1$tM", fecha);

        binding.tvTxtDate.setText(formato_fecha);
        binding.tvTxtHora.setText(formato_hora + " " + sb);
    }

    @Override
    public void isLoading(boolean state) {

    }

    @Override
    public void onFinishPrint() {

    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}