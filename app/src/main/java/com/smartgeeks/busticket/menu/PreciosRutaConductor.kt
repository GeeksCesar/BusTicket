package com.smartgeeks.busticket.menu

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.Modelo.Paradero
import com.smartgeeks.busticket.Modelo.TarifaParadero
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.databinding.ActivityPrecioRutasConductorBinding
import com.smartgeeks.busticket.menu.AdapterPrecios.ItemClickListener
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreciosRutaConductor : AppCompatActivity(), ItemClickListener, PrintTicket.PrintState {

    var bundle: Bundle? = null
    var precio_sum_pasaje = 0
    var id_tipo_usuario = 0
    var id_paradero_inicio = 0
    var id_paradero_fin = 0
    var horario: String = ""
    var info: String? = null
    var nombreEmpresa: String? = null
    var desc_empresa: String? = null
    var ruta = ""
    var getNameTipoPasajero: String? = ""
    var id_horario = 0
    var id_vehiculo = 0
    var id_operador = 0
    var id_ruta = 0
    var id_ruta_disponible = 0
    var id_empresa = 0
    var preferences: SharedPreferences? = null
    var estadoRuta = false
    private var adapterPrices: AdapterPrecios? = null

    private lateinit var context: Context
    private lateinit var binding: ActivityPrecioRutasConductorBinding
    private var ticketQuantity = 1
    private val ticketViewModel: TicketViewModel by viewModels()

    // Printer settings
    // private lateinit var printTicket: PrintTicketLibrary
    private lateinit var printTicketPrev: PrintTicket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityPrecioRutasConductorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // printTicket = PrintTicketLibrary(this@PreciosRutaConductor, this)
        printTicketPrev = PrintTicket(this@PreciosRutaConductor, this)
        context = this@PreciosRutaConductor

        initWidget()
        setupOnBackButton()
        handleTicketQuantity()

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_RUN_REMOTE_SYNC)
        filter.addAction(Constantes.EXTRA_PROGRESS)
        filter.addAction(Constantes.ACTION_FINISH_REMOTE_SYNC)

        binding.btnOlvidarRuta.setOnClickListener {
            preferences =
                context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
            preferences?.edit {
                clear()
                apply()
            }
            goIntentMain()
        }
    }

    private fun initWidget() {
        bundle = intent.extras
        estadoRuta = RutaPreferences.getInstance(context).estadoRuta

        if (bundle != null) {
            id_ruta = bundle!!.getInt(ID_RUTA)
            id_ruta_disponible = bundle!!.getInt(ID_RUTA_DISPONIBLE)
            id_vehiculo = bundle!!.getInt(ID_VEHICULO)
            id_tipo_usuario = bundle!!.getString(ID_TIPO_USUARIO)!!.toInt()
            getNameTipoPasajero = bundle!!.getString(NAME_TIPO_USUARIO)
            id_horario = bundle!!.getInt(ID_HORARIO)
            horario = bundle!!.getString(HORARIO) ?: ""
            info = bundle!!.getString(INFO)
            ruta = bundle!!.getString(INFO)!!.split(",").toTypedArray()[1]
        } else {
            id_ruta = RutaPreferences.getInstance(context).idRuta
            id_ruta_disponible = RutaPreferences.getInstance(context).idRutaDisponible
            id_vehiculo = RutaPreferences.getInstance(context).idVehiculo
            id_horario = RutaPreferences.getInstance(context).idHorario
            horario = RutaPreferences.getInstance(context).hora
            info = RutaPreferences.getInstance(context).informacion
            ruta = RutaPreferences.getInstance(context).informacion.split(",").toTypedArray()[1]
        }

        id_operador = UsuarioPreferences.getInstance(context).idUser
        nombreEmpresa = UsuarioPreferences.getInstance(context).nombreEmpresa
        desc_empresa = UsuarioPreferences.getInstance(context).descEmpresa
        nombreEmpresa = nombreEmpresa?.trim { it <= ' ' }?.uppercase()

        Log.e(TAG, "Horario: $horario")
        Log.e(TAG, "Ruta: $id_ruta")
        Log.e(TAG, "Tipo usuario: $id_tipo_usuario")
        Log.e(TAG, "Nombre usuario: $getNameTipoPasajero")
        Log.e(TAG, "ID_Ruta: $id_ruta_disponible")
        Log.e(TAG, "Nombre Empresa: $nombreEmpresa")

        // Listado de Precios para la ruta (Entre paraderos)
        val tarifaParaderos = TarifaParadero.find(
            TarifaParadero::class.java,
            "id_ruta = ? and tipo_usuario = ?",
            arrayOf("" + id_ruta, "" + id_tipo_usuario),
            "monto",
            "monto DESC",
            null
        )

        Log.e(TAG, "count-> " + tarifaParaderos.size)
        val tv_ruta = findViewById<TextView>(R.id.tv_ruta)
        tv_ruta.text = ruta
        id_empresa = UsuarioPreferences.getInstance(context).idEmpresa

        // RecyclerView
        adapterPrices = AdapterPrecios(this, tarifaParaderos)
        adapterPrices?.setClickListener(this)
        binding.rvPrecios.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = adapterPrices
        }

        setDataDefault()
    }

    private fun setupOnBackButton() {
        binding.customToolbar.imgBtnBack.setOnClickListener { finish() }
    }

    private fun handleTicketQuantity() = with(binding) {
        btnAdd.setOnClickListener {
            ticketQuantity++
            tvCountTicket.text = ticketQuantity.toString()
        }
        btnRemove.setOnClickListener {
            if (ticketQuantity > 1)
                ticketQuantity--
            tvCountTicket.text = ticketQuantity.toString()
        }

    }

    /**
     * Este método define lo datos por defecto que se necesitan para guardar
     * los datos en la base de datos remota
     */
    private fun setDataDefault() {
        // Datos para mantener la integridad en la BD Remota, porque no acepta 0 como dato
        Log.e(TAG, "Tipo de usuario: $id_tipo_usuario")
        val paraderosList = Paradero.find(
            Paradero::class.java,
            "ruta = ?",
            arrayOf("" + id_ruta),
            "remoto",
            "remoto",
            null
        )
        id_paradero_inicio = paraderosList[0].idRemoto
        id_paradero_fin = paraderosList[paraderosList.size - 1].idRemoto
    }

    private fun showProgress(show: Boolean) {
        binding.progressSave.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onItemClick(view: View, position: Int) {
        precio_sum_pasaje = adapterPrices!!.getItem(position)
        val sweetdialog = SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
        sweetdialog.setTitleText("Vender Ticket")
            .setContentText("Se imprimirá el Ticket con el precio $$precio_sum_pasaje")
            .setConfirmText("Imprimir Ticket")
            .setConfirmClickListener { swAlert ->
                swAlert.dismiss()
                saveTicket()
            }
            .show()
        val button = sweetdialog.findViewById<Button>(R.id.confirm_button)
        button.textSize = 25f
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlue))
        val density = context!!.resources.displayMetrics.density
        val paddingPixel = (30 * density).toInt()
        button.setPadding(paddingPixel, 5, paddingPixel, 5)
    }

    private fun saveTicket() {
        precio_sum_pasaje *= ticketQuantity

        val date = Utilities.getTimeByTimezone("yyyy-MM-dd")
        val hour = Utilities.getTimeByTimezone("HH:mm:ss")
        val numVoucher = Utilities.getVoucherName(id_vehiculo, id_operador, date, hour)
        val ticketEntity = TicketEntity(
            0,
            id_paradero_inicio,
            id_paradero_fin,
            id_ruta_disponible,
            id_operador,
            horario,
            id_tipo_usuario,
            date, hour,
            ticketQuantity,
            precio_sum_pasaje.toDouble(),
            id_vehiculo,
            numVoucher
        )

        printTicket(ticketEntity)
        sendTicket(ticketEntity)
    }

    private fun printTicket(ticketEntity: TicketEntity) {
        printTicketPrev.setData(
            id_paradero_inicio,
            id_paradero_fin,
            id_ruta_disponible,
            horario,
            id_tipo_usuario,
            precio_sum_pasaje.toDouble(),
            id_vehiculo,
            getNameTipoPasajero ?: "",
            info ?: "",
            _showHeader = false
        )
        printTicketPrev.print()
        /*printTicket.setData(
            ticketEntity,
            getNameTipoPasajero!!,
            info!!,
            ticketQuantity
        )
        printTicket.print()*/
    }

    private fun sendTicket(ticketEntity: TicketEntity) {
        ticketViewModel.saveTicket(ticketEntity).observe(this) { result ->
            when (result) {
                is Resource.Failure -> showProgress(false)
                is Resource.Loading -> showProgress(true)
                is Resource.Success -> {
                    showProgress(false)
                    when (result.data) {
                        is Long -> {
                            Toast.makeText(this, "Ticket guardado localmente", Toast.LENGTH_SHORT)
                                .show()
                        }
                        is ResponseSaveTicket -> {
                            if (result.data.estado == 1)
                                Toast.makeText(
                                    this,
                                    "Ticket guardado en el servidor",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }

                    }
                    Log.e(SelectRutas.TAG, "Success ${result.data}")
                }
            }
        }
    }

    private fun goIntentMain() {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.BACK, true)
        startActivity(intent)
        finish()
    }

    override fun isLoading(state: Boolean) {
    }

    override fun onFinishPrint() {
        finish()
    }

    companion object {
        const val ID_RUTA = "ID"
        const val ID_TIPO_USUARIO = "ID_TIPO_USUARIO"
        const val ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE"
        const val ID_VEHICULO = "ID_VEHICULO"
        const val ID_HORARIO = "ID_HORARIO"
        const val HORARIO = "HORARIO"
        const val NAME_TIPO_USUARIO = "NAME_TIPO_USUARIO"
        const val INFO = "INFO"
        val TAG = PreciosRutaConductor::class.java.simpleName
    }
}