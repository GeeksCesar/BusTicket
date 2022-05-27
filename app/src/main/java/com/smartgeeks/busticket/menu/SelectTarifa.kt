package com.smartgeeks.busticket.menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.orm.query.Select
import com.smartgeeks.busticket.Modelo.Paradero
import com.smartgeeks.busticket.Modelo.TarifaParadero
import com.smartgeeks.busticket.Modelo.TipoUsuario
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.databinding.ActivitySelectTarifaBinding
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.RecyclerItemClickListener
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectTarifa : AppCompatActivity(), PrintTicket.PrintState {
    var context: Context? = null
    var bundle: Bundle? = null
    var id_horario = 0
    var id_vehiculo = 0
    var id_operador = 0
    var id_ruta = 0
    var id_ruta_disponible = 0
    var horario: String = ""
    var info: String? = null
    var ruta = ""
    var tarifaLists: MutableList<TipoUsuario> = ArrayList()
    var layoutManager: RecyclerView.LayoutManager? = null
    var adapterListTarifas: AdapterTarifas? = null

    // private lateinit var printTicketLibrary: PrintTicketLibrary
    private lateinit var printTicketPrev: PrintTicket

    private lateinit var binding: ActivitySelectTarifaBinding
    private val ticketViewModel: TicketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySelectTarifaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@SelectTarifa
        initWidgets()
        setupOnBackButton()
        // printTicketLibrary = PrintTicketLibrary(this@SelectTarifa, this)
        printTicketPrev = PrintTicket(this@SelectTarifa, this)
    }

    private fun setupOnBackButton() {
        binding.customToolbar.imgBtnBack.setOnClickListener { finish() }
    }

    private fun initWidgets() = with(binding) {

        rvTarifas.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            // val dividerItemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            // addItemDecoration(dividerItemDecoration)
        }

        tvTxtDate.text = Utilities.getDate("dd-MM-yyyy")
        getTarifasLocal()

        bundle = intent.extras
        if (bundle != null) {
            id_ruta = bundle!!.getInt(ID_RUTA)
            id_ruta_disponible = bundle!!.getInt(ID_RUTA_DISPONIBLE)
            id_vehiculo = bundle!!.getInt(ID_VEHICULO)
            id_horario = bundle!!.getInt(ID_HORARIO)
            horario = bundle!!.getString(HORARIO) ?: ""
            info = bundle!!.getString(INFO)
            ruta = bundle!!.getString(INFO)!!.split(",").toTypedArray()[1]
        } else {
            // Cargar las preferencias de la ruta guardada
            id_ruta = RutaPreferences.getInstance(context).idRuta
            id_ruta_disponible = RutaPreferences.getInstance(context).idRutaDisponible
            id_vehiculo = RutaPreferences.getInstance(context).idVehiculo
            id_horario = RutaPreferences.getInstance(context).idHorario
            horario = RutaPreferences.getInstance(context).hora
            info = RutaPreferences.getInstance(context).informacion
            ruta = RutaPreferences.getInstance(context).informacion.split(",").toTypedArray()[1]
        }
        id_operador = UsuarioPreferences.getInstance(context).idUser

        rvTarifas.addOnItemTouchListener(RecyclerItemClickListener(context) { view, position ->
            val tipoUsuario = tarifaLists[position]
            Log.e(TAG, "ID_TIPO_USUARIO: " + tipoUsuario.id)

            // Listado de Precios para la ruta (Entre paraderos)
            val listPrices = TarifaParadero.find(
                TarifaParadero::class.java,
                "id_ruta = ? and tipo_usuario = ?",
                arrayOf("" + id_ruta, "" + tipoUsuario.id_remoto),
                "monto",
                "monto DESC",
                null
            )
            Log.e(TAG, "ListPrices: " + listPrices.size)
            if (listPrices.size > 1) {
                val intent = Intent(context, PreciosRutaConductor::class.java)
                intent.putExtra(PreciosRutaConductor.ID_RUTA, id_ruta)
                intent.putExtra(PreciosRutaConductor.ID_TIPO_USUARIO, tipoUsuario.id_remoto)
                intent.putExtra(PreciosRutaConductor.NAME_TIPO_USUARIO, tipoUsuario.nombre)
                intent.putExtra(PreciosRutaConductor.ID_VEHICULO, id_vehiculo)
                intent.putExtra(PreciosRutaConductor.ID_RUTA_DISPONIBLE, id_ruta_disponible)
                intent.putExtra(PreciosRutaConductor.ID_HORARIO, id_horario)
                intent.putExtra(PreciosRutaConductor.HORARIO, horario)
                intent.putExtra(PreciosRutaConductor.INFO, info)
                startActivity(intent)
            } else if (listPrices.size == 1) {

                // Destination List
                val destinationsList = Paradero.find(
                    Paradero::class.java,
                    "ruta = ?",
                    arrayOf("" + id_ruta),
                    "remoto",
                    "remoto",
                    null
                )
                val departureId = destinationsList[0].idRemoto
                val arrivalId = destinationsList[destinationsList.size - 1].idRemoto
                val ticketPrice = listPrices[0].monto
                showDialogPrintTicket(departureId, arrivalId, ticketPrice, tipoUsuario)
            } else {
                Snackbar.make(
                    binding.root,
                    "No hay precios asignados",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showDialogPrintTicket(
        departureId: Int,
        arrivalId: Int,
        ticketPrice: Int,
        tipoUsuario: TipoUsuario
    ) {
        val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
        sweetAlertDialog.setTitleText("Vender Ticket")
            .setContentText("Se imprimirá el Ticket con el precio $$ticketPrice")
            .setConfirmText("Imprimir Ticket")
            .setConfirmClickListener { swAlert ->
                swAlert.dismiss()
                printTicket(
                    departureId,
                    arrivalId,
                    id_ruta_disponible,
                    horario, tipoUsuario.id_remoto.toInt(), ticketPrice.toString().toDouble(),
                    id_vehiculo,
                    tipoUsuario.nombre,
                    info
                )
            }
            .show()
        val button = sweetAlertDialog.findViewById<Button>(R.id.confirm_button)
        button.textSize = 25f
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlue))
        val density = context!!.resources.displayMetrics.density
        val paddingPixel = (30 * density).toInt()
        button.setPadding(paddingPixel, 5, paddingPixel, 5)
    }

    /**
     * This method printTicket.
     */
    private fun printTicket(
        departureId: Int, arrivalId: Int, routeAvailableId: Int, schedule: String,
        passengerTypeId: Int, ticketPrice: Double, vehicleId: Int, passengerTypeName: String,
        companyInfo: String?
    ) {

        /**
         * This save the ticket in the database Remote or Local
         */
        val date = Utilities.getTimeByTimezone("yyyy-MM-dd")
        val hour = Utilities.getTimeByTimezone("HH:mm:ss")
        val numVoucher = Utilities.getVoucherName(id_vehiculo, id_operador, date, hour)
        val ticketEntity = TicketEntity(
            0,
            departureId,
            arrivalId,
            routeAvailableId,
            id_operador,
            schedule,
            passengerTypeId,
            date, hour,
            1,
            ticketPrice,
            vehicleId,
            numVoucher
        )
        sendTicket(ticketEntity)

        /**
         * This method printTicket.
         */

        /*printTicketLibrary.setData(ticketEntity, passengerTypeName, companyInfo ?: "")
        printTicketLibrary.print()*/
        printTicketPrev.setData(
            departureId,
            arrivalId,
            routeAvailableId,
            schedule,
            passengerTypeId,
            ticketPrice,
            vehicleId,
            passengerTypeName,
            companyInfo ?: "",
            1,
            _showHeader = false,
            numVoucher = ticketEntity.voucher
        )
        printTicketPrev.print()
    }

    private fun sendTicket(ticketEntity: TicketEntity) {
        ticketViewModel.saveTicket(ticketEntity).observe(this) { result ->
            when (result) {
                is Resource.Failure -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> {
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

    // List<TipoUsuario> tipoUsuarios = TipoUsuario.listAll(TipoUsuario.class);
    private fun getTarifasLocal() {
        // List<TipoUsuario> tipoUsuarios = TipoUsuario.listAll(TipoUsuario.class);
        val usersType = Select.from(TipoUsuario::class.java).orderBy("nombre").list()

        for (userType in usersType) {
            if (userType.id_remoto.toInt() != 0) tarifaLists.add(userType)
        }
        adapterListTarifas = AdapterTarifas(context, tarifaLists)
        binding.rvTarifas.adapter = adapterListTarifas
    }

    override fun isLoading(state: Boolean) {}
    override fun onFinishPrint() {}

    companion object {
        const val ID_RUTA = "ID"
        const val ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE"
        const val ID_VEHICULO = "ID_VEHICULO"
        const val ID_HORARIO = "ID_HORARIO"
        const val HORARIO = "HORARIO"
        const val INFO = "INFO"
        private const val TAG = "SELECTTARIFA"
    }
}