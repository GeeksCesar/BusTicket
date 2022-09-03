package com.smartgeeks.busticket.menu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import cn.pedant.SweetAlert.SweetAlertDialog
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.google.android.material.snackbar.Snackbar
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.MyBluetoothPrintersConnections
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.api.Service
import com.smartgeeks.busticket.data.models.vehicle.SillaOcupada
import com.smartgeeks.busticket.databinding.ActivitySelectSillasBinding
import com.smartgeeks.busticket.domain.models.PriceByDate
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.presentation.VehicleViewModel
import com.smartgeeks.busticket.printer.AsyncBluetoothEscPosPrint
import com.smartgeeks.busticket.printer.AsyncEscPosPrinter
import com.smartgeeks.busticket.utils.Constants
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.Utilities.formatDate
import dagger.hilt.android.AndroidEntryPoint
import java.net.SocketTimeoutException
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.ceil

private const val PERMISSION_BLUETOOTH = 1

@AndroidEntryPoint
class SelectSillas : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    PrintTicket.PrintState {

    private var listSillasOcupadas: ArrayList<SillaOcupada> = ArrayList()
    private val sillasSeleccionadas: ArrayList<Int> = ArrayList()
    var bundle: Bundle? = null
    var cant_puestos = 0
    var precio_pasaje = 0
    var id_vehiculo = 0
    var id_horario = 0
    var id_paradero_incio = 0
    var id_paradero_final = 0
    var id_tipo_usuario = 0
    var id_operador = 0
    var id_ruta = 0
    var id_ruta_disponible = 0
    var id_empresa = 0
    var getPrecioPasaje: String = ""
    var horario: String = ""
    var info_ruta: String = ""
    var nombreEmpresa: String = ""
    var nombreUsuario: String = ""
    var desc_empresa: String = ""
    var context: Context? = null
    var progress: ProgressDialog? = null
    private var numVoucher = ""

    var listSillas = ""

    lateinit var binding: ActivitySelectSillasBinding
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val ticketViewModel: TicketViewModel by viewModels()

    lateinit var dialogPrint: Dialog
    lateinit var btnCancelar: Button
    lateinit var lstPrint: ListView
    var formatea = DecimalFormat("###,###.##")
    var preferences: SharedPreferences? = null
    var estadoPrint = false
    var namePrint: String? = null

    private var saleByDate: Boolean = false
    private var ticketOneWay: PriceByDate? = null
    private var ticketDate: String = ""
    private var serviceId: Int = 0

    private lateinit var printTicketPrev: PrintTicket

    private var isFromEditTicket : Boolean = false
    private var chairsEditTicket : String = ""
    private var dateTicketToEdit : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySelectSillasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        printTicketPrev = PrintTicket(this@SelectSillas, this)
        initWidgets()
        getDataPrint()
        fetchData()


        binding.btnConfirmarTicket.setOnClickListener(View.OnClickListener {
            listSillas = ""
            if (sillasSeleccionadas.size == 0) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Debe seleccionar puestos",
                    SweetAlertDialog.NORMAL_TYPE
                )
                return@OnClickListener
            } else if (sillasSeleccionadas.size < cant_puestos) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Debe seleccionar $cant_puestos Puestos",
                    SweetAlertDialog.NORMAL_TYPE
                )
                return@OnClickListener
            } else {

                for (i in sillasSeleccionadas.indices) {
                    val silla = sillasSeleccionadas[i]
                    Log.d(Service.TAG, "sillas: $silla")
                    listSillas = "$listSillas$silla-"
                }
                listSillas = listSillas.substring(0, listSillas.length - 1)

                /*printBluetooth()
                return@OnClickListener*/

                // Check if is come edit ticket, for only select chair
                if (isFromEditTicket) {
                    // Show native dialog to confirm
                    SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE).apply {
                        titleText = "Confirmar"
                        contentText = "¿Deseas escoger el asiento ${listSillas}?"
                        confirmText = "Confirmar"
                        cancelText = "Cancelar"
                        setConfirmClickListener {
                            // Return to previous activity with result list of chair
                            val intent = Intent()
                            intent.putExtra("listSillas", listSillas)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                        setCancelClickListener {
                            it.dismissWithAnimation()
                        }
                    }.show()
                    return@OnClickListener
                }

                binding.btnConfirmarTicket.isEnabled = false
                binding.btnConfirmarTicket.visibility = View.GONE
                showProgress(true)
                registerTicket(
                    id_paradero_incio,
                    id_paradero_final,
                    id_ruta_disponible,
                    id_operador,
                    id_tipo_usuario,
                    precio_pasaje,
                    listSillas
                )
            }
        })
    }

    private fun initWidgets() {
        context = this@SelectSillas
        bundle = intent.extras
        cant_puestos = bundle!!.getInt(CANT_PUESTOS)
        precio_pasaje = bundle!!.getInt(PRECIO_PASAJE)
        id_vehiculo = bundle!!.getInt(ID_VEHICULO)
        id_horario = bundle!!.getInt(ID_HORARIO)
        id_ruta = bundle!!.getInt(ID_RUTA)
        id_ruta_disponible = bundle!!.getInt(ID_RUTA_DISPONIBLE)
        info_ruta = bundle!!.getString(SelectRutas.INFO, "")
        id_paradero_incio = bundle!!.getInt(ID_PARADERO_INICIO)
        id_paradero_final = bundle!!.getInt(ID_PARADERO_FIN)
        id_tipo_usuario = bundle!!.getInt(TIPO_USUARIO)
        horario = bundle!!.getString(HORARIO, "")
        nombreUsuario = bundle!!.getString(NAME_TIPO_PASAJERO, "")
        id_operador = UsuarioPreferences.getInstance(context).idUser
        nombreEmpresa = UsuarioPreferences.getInstance(context).nombreEmpresa
        desc_empresa = UsuarioPreferences.getInstance(context).descEmpresa
        id_empresa = UsuarioPreferences.getInstance(context).idEmpresa
        nombreEmpresa = nombreEmpresa.trim { it <= ' ' }.uppercase(Locale.getDefault())
        getPrecioPasaje = "$ " + formatPrecio(precio_pasaje)

        saleByDate = bundle!!.getBoolean(SALE_BY_DATE)
        ticketOneWay = bundle!!.getSerializable(TICKET_ONE_WAY) as PriceByDate?
        serviceId = bundle!!.getInt(SERVICE_ID, 0)
        isFromEditTicket = bundle!!.getBoolean(IS_EDIT_TICKET, false)
        chairsEditTicket = bundle!!.getString(CHAIRS_EDIT_TICKET, "")
        dateTicketToEdit = bundle!!.getString(DATE_TICKET_REGISTERED, "")
        Log.e(TAG, "serviceId: $serviceId")

        //Input
        showDataTextView()
        ticketDate = ticketOneWay?.fecha ?: Utilities.getDate("yyyy-MM-dd")

        setChairsToEdit()
    }

    /**
     * Request to Service
     */
    private fun fetchData() {
        vehicleViewModel.getOccupiedSeats(id_ruta_disponible, horario, ticketDate, serviceId, id_vehiculo)
            .observe(this) { result ->
                when (result) {
                    is Resource.Failure -> {

                        Snackbar.make(
                            binding.root,
                            "Se ha producido un error al cargar los datos",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "fetchData: ${result.exception}")

                        when (result.exception) {
                            is SocketTimeoutException -> {
                                fetchData()
                            }
                        }

                        progress?.dismiss()
                    }
                    is Resource.Loading -> showProgressDialog()
                    is Resource.Success -> {
                        val data = result.data

                        when (data.estado) {
                            Constants.SUCCESS_RESPONSE -> {
                                listSillasOcupadas = data.sillas_ocupadas as ArrayList<SillaOcupada>
                                Log.e(
                                    TAG,
                                    "Se encontraron ${listSillasOcupadas.size} sillas ocupadas."
                                )

                                // Remove chairs already selected
                                removeOccupiedChairs()

                            }
                            Constants.FAILED_RESPONSE -> Log.e(TAG, "Error al traer datos")
                        }
                        fetchVehicleInfo()
                        checkBluetoothStatus()
                    }
                }
            }
    }

    private fun removeOccupiedChairs() {
        /*// Check if the date of the ticket to update is the same of the date of the ticket to edit
        Log.e(TAG, "dateTicketToEdit: $ticketDate == $dateTicketToEdit")
        if (ticketDate != dateTicketToEdit)
            return*/
        // Make copy of list of chairs to edit
        val sillasSeleccionadasCopy = sillasSeleccionadas.toMutableList()
        val listSillasOcupadasCopy = listSillasOcupadas.toMutableList()
        for (selected in sillasSeleccionadasCopy) {
            for (ocupada in listSillasOcupadasCopy) {
                if (selected == ocupada.numeroSilla && dateTicketToEdit == ticketDate) {
                    listSillasOcupadas.remove(ocupada)
                } else if (selected == ocupada.numeroSilla && dateTicketToEdit != ticketDate) {
                    sillasSeleccionadas.remove(selected)
                }
            }
        }
    }

    private fun fetchVehicleInfo() {
        vehicleViewModel.getVehicleInfo(id_vehiculo, saleByDate).observe(this) { result ->
            when (result) {
                is Resource.Failure -> {
                    progress?.dismiss()
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    Log.e(TAG, "fetchData: ${result.data}")
                    progress?.dismiss()
                    with(result.data) {
                        if (vehiculos.isNotEmpty())
                            drawChairBus(vehiculos[0].can_sillas)
                    }
                }
            }
        }
    }

    private fun getDataPrint() {
        namePrint = RutaPreferences.getInstance(context).namePrint
        estadoPrint = RutaPreferences.getInstance(context).estadoPrint

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.e(TAG, "getDataPrint: Request Permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    PERMISSION_BLUETOOTH
                )
            }
            return
        } else {
            Log.e(TAG, "getDataPrint: Permission Granted")
            Constants.selectedDevice =
                MyBluetoothPrintersConnections().list?.find { it.device.name == namePrint }
        }
        Log.e(
            TAG,
            "Printer name: $namePrint - ${Constants.selectedDevice?.device?.name}  ${Constants.selectedDevice?.isConnected}"
        )
        Log.d(TAG, "boolen print: $estadoPrint")
    }

    private fun setChairsToEdit() {
        chairsEditTicket.let {
            if (it.isNotEmpty()) {
                val chairs = it.split(",")
                for (chair in chairs) {
                    sillasSeleccionadas.add(chair.toInt())
                }
                Log.e(TAG, "Sillas seleccionadas: $sillasSeleccionadas")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDataTextView() = with(binding) {
        val split = info_ruta.split(",").toTypedArray()
        tvVehiculo.text = "Vehículo: " + split[0]
        tvRuta.text = "Ruta: " + split[1]
        tvHora.text = "Hora: " + split[2]
        tvInicio.text = "Inicio: " + split[3]
        tvFin.text = "Fin: " + split[4]
    }

    private fun drawChairBus(cant_sillas: Int) {
        var silla = 1
        val filas = ceil((cant_sillas / 4).toDouble()).toInt()
        Log.e(TAG, "Paradero Inicio: $id_paradero_incio")

        // Parámetros del LinearLayout
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 5
        params.bottomMargin = 5

        // Parámetros del espacio
        val space_params = LinearLayout.LayoutParams(50, 0, 1f)

        // Parámetros de la silla
        val silla_params = LinearLayout.LayoutParams(
            50,
            70, 1f
        )
        silla_params.setMargins(4, 8, 4, 8)

        // Dibujo las filas
        for (i in 1..filas + 1) {
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = params
            linearLayout.orientation = LinearLayout.HORIZONTAL

            //Dibujo las columnas izquierdas
            for (a in 1..4) {
                if (silla > cant_sillas) {
                    break
                }
                if (cant_sillas % 2 == 1 && silla >= cant_sillas) {
                    val puesto = ToggleButton(this)
                    puesto.layoutParams = silla_params
                    puesto.setPadding(0, 10, 0, 10)
                    puesto.id = silla
                    puesto.background = ContextCompat.getDrawable(this, R.drawable.toggle_silla)
                    puesto.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000))
                    puesto.textOn = "" + silla
                    puesto.textOff = "" + silla
                    puesto.text = "" + silla
                    puesto.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    puesto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)

                    // Verificar estado de silla
                    drawSillaOcupada(silla, puesto)
                    silla++

                    // Agregar Silla al ticket
                    puesto.setOnCheckedChangeListener(this)
                    linearLayout.addView(puesto)
                    if (a == 2) {
                        //Dibujo el espacio de en el bus
                        val extra = ToggleButton(this)
                        extra.layoutParams = silla_params
                        extra.setPadding(0, 10, 0, 10)
                        extra.id = silla
                        extra.background = ContextCompat.getDrawable(this, R.drawable.toggle_silla)
                        extra.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000))
                        extra.textOn = "" + silla
                        extra.textOff = "" + silla
                        extra.text = "" + silla
                        extra.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        extra.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)

                        // Verificar estado de silla
                        drawSillaOcupada(silla, extra)
                        silla++

                        // Agregar Silla al ticket
                        extra.setOnCheckedChangeListener(this)
                        linearLayout.addView(extra)
                    }
                } else {
                    val puesto = ToggleButton(this)
                    puesto.layoutParams = silla_params
                    puesto.setPadding(0, 10, 0, 10)
                    puesto.id = silla
                    puesto.background = ContextCompat.getDrawable(this, R.drawable.toggle_silla)
                    puesto.setTextColor(ContextCompat.getColor(this, R.color.md_black_1000))
                    puesto.textOn = "" + silla
                    puesto.textOff = "" + silla
                    puesto.text = "" + silla
                    puesto.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    puesto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)

                    // Verificar estado de silla
                    drawSillaOcupada(silla, puesto)
                    silla++

                    // Agregar Silla al ticket
                    puesto.setOnCheckedChangeListener(this)
                    linearLayout.addView(puesto)
                    if (a == 2) {
                        //Dibujo el espacio de en el bus
                        val espacio = View(this)
                        espacio.layoutParams = space_params
                        linearLayout.addView(espacio)
                    }
                }
            }
            binding.contenedorBus.addView(linearLayout)
        }
    }

    private fun showProgressDialog() {
        progress = ProgressDialog(this)
        progress!!.setMessage("Cargando bus...")
        progress!!.setCanceledOnTouchOutside(true)
        progress!!.setCancelable(false)
        progress!!.show()
    }

    /**
     * Dibuja las sillas ocupadas
     *
     * @param silla
     * @param puesto
     */
    private fun drawSillaOcupada(silla: Int, puesto: ToggleButton) {

        // Draw selected seats
        for (seat in sillasSeleccionadas) {
            if (seat == silla) {
                puesto.isChecked = true
                puesto.setTextColor(ContextCompat.getColor(context!!, R.color.md_white_1000))
            }
        }

        // Verificar si la silla está ocupada
        for (ocupada in listSillasOcupadas) {
            if (ocupada.numeroSilla == silla && ocupada.destino > id_paradero_incio) {
                puesto.isEnabled = false
                puesto.isClickable = false
                puesto.background = ContextCompat.getDrawable(this, R.drawable.silla_ocupada)
            }
        }
    }

    /**
     * Elimina una silla del arreglo, de acuerdo a su posicion
     *
     * @param silId
     */
    private fun removeSillaFromArray(silId: Int) {
        val copySelectedSeats = sillasSeleccionadas.toMutableList()
        for (seat in copySelectedSeats) {
            if (seat == silId) {
                sillasSeleccionadas.remove(seat)
            }
        }
    }

    fun goBack(view: View?) {
        finish()
    }

    /**
     * Verifica el estado del toggle button
     *
     * @param buttonView
     * @param isChecked
     */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val silla_seleccionda = buttonView.id
        // Guardo o elimino la silla
        if (isChecked) {
            sillasSeleccionadas.add(silla_seleccionda)
            if (sillasSeleccionadas.size > cant_puestos) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Ya has seleccionado los $cant_puestos puestos.",
                    SweetAlertDialog.ERROR_TYPE
                )
                removeSillaFromArray(silla_seleccionda)
                buttonView.isChecked = false
                buttonView.setTextColor(ContextCompat.getColor(context!!, R.color.md_black_1000))
            } else {
                buttonView.setTextColor(ContextCompat.getColor(context!!, R.color.md_white_1000))
            }
        } else if (!isChecked) {
            removeSillaFromArray(silla_seleccionda)
            buttonView.setTextColor(ContextCompat.getColor(context!!, R.color.md_black_1000))
        }
    }

    private fun registerTicket(
        id_paradero_inicio: Int,
        id_paradero_final: Int,
        id_ruta: Int,
        id_operador: Int,
        id_tipo_usuario: Int,
        valor_pagar: Int,
        listSillas: String
    ) {

        ticketViewModel.saveSeatTicket(
            id_paradero_inicio,
            id_paradero_final,
            id_ruta,
            id_operador,
            horario,
            id_tipo_usuario,
            valor_pagar.toDouble(),
            listSillas,
            id_empresa,
            id_vehiculo,
            ticketOneWay?.fecha ?: "",
            idService = serviceId
        ).observe(this) { result ->
            when (result) {
                is Resource.Failure -> {

                    Log.e(TAG, "registerTicket: ${result.exception}")
                    showProgress(false)

                    when (result.exception) {
                        is SocketTimeoutException -> {
                            //showDialogPrintTicket(id_paradero_inicio)
                            Snackbar.make(
                                binding.contenedorBus,
                                "Error de conexión, intente nuevamente",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Snackbar.make(
                                binding.root,
                                "Se ha producido un error registrando el ticket",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                is Resource.Loading -> showProgress(true)
                is Resource.Success -> {
                    val data = result.data
                    if (!data.error) {
                        showProgress(false)
                        numVoucher = data.num_voucher
                        Log.e(TAG, "Num Voucher: $numVoucher")
                        Snackbar.make(
                            binding.root,
                            "Ticket registrado con éxito",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        showDialogPrintTicket(id_paradero_inicio)
                    } else {
                        DialogAlert.showDialogFailed(
                            context,
                            "OCUPADO",
                            "${data.message} ${data.silla}\n Seleccione otro",
                            SweetAlertDialog.ERROR_TYPE
                        )
                        binding.btnConfirmarTicket.isEnabled = true
                        binding.btnConfirmarTicket.visibility = View.VISIBLE
                        showProgress(false)
                    }
                }
            }
        }
    }

    private fun showDialogPrintTicket(id_paradero_inicio: Int) {
        val alertDialog = SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
        alertDialog.setTitleText("Exito")
            .setContentText("Guardo el ticket")
            .show()
        val button = alertDialog.findViewById<Button>(R.id.confirm_button)

        button.setBackgroundResource(R.drawable.bg_button_main)
        button.setPadding(15, 5, 15, 5)
        button.text = "Imprimir Ticket"

        /**
         * Print Ticket
         */
        button.setOnClickListener {
            try {
                Log.e(TAG, "registerTicket: $info_ruta")
                printTicketPrev.setData(
                    id_paradero_inicio,
                    id_paradero_final,
                    id_ruta_disponible,
                    horario,
                    id_tipo_usuario,
                    precio_pasaje.toDouble(),
                    id_vehiculo,
                    nombreUsuario,
                    info_ruta,
                    ticketQuantity = cant_puestos,
                    seats = listSillas,
                    isMultiTicket = saleByDate,
                    travelDate = ticketDate,
                    numVoucher = numVoucher
                )
                printTicketPrev.print()
                alertDialog.dismiss()
                // printBluetooth()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothStatus() {
        if (!Utilities.isBluetoothEnabled(this)) {
            val alertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            alertDialog.titleText = "Bluetooth"
            alertDialog.contentText = "Habilita el Bluetooth"
            alertDialog.confirmText = "Aceptar"
            alertDialog.setConfirmClickListener {
                alertDialog.dismissWithAnimation()

                // Turn on Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, PERMISSION_BLUETOOTH)
            }
            alertDialog.show()
        } else {
            if (Constants.selectedDevice == null) {
                showDialogTicketLibrary()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showDialogTicketLibrary() {
        dialogPrint = Dialog(context!!)
        dialogPrint.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPrint.setContentView(R.layout.dialog_print)
        dialogPrint.setCanceledOnTouchOutside(false)
        dialogPrint.setCancelable(false)
        dialogPrint.window?.setLayout(
            (getScreenWidth(this@SelectSillas) * .9).toInt(),
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        btnCancelar = dialogPrint.findViewById(R.id.btnCancelar)
        lstPrint = dialogPrint.findViewById(R.id.listViewPrint)

        val bluetoothPrintersConnections =
            MyBluetoothPrintersConnections().list?.map { it.device.name } ?: listOf()

        lstPrint.adapter = object :
            ArrayAdapter<String?>(
                this,
                android.R.layout.simple_list_item_1,
                bluetoothPrintersConnections.toList()
            ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text = view.findViewById<TextView>(android.R.id.text1)
                text.setTextColor(Color.BLACK)
                return view
            }
        }
        lstPrint.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val deviceName = parent.getItemAtPosition(position).toString()
                // Connect to the device
                Constants.selectedDevice =
                    MyBluetoothPrintersConnections().list?.find { it.device.name == deviceName }

                preferences =
                    context!!.getSharedPreferences(RutaPreferences.PREFERENCES_PRINT, MODE_PRIVATE)
                preferences?.edit {
                    putString(RutaPreferences.NAME_PRINT, deviceName)
                    apply()
                }

                Log.e(TAG, "SelectPrinter: $deviceName - ${Constants.selectedDevice}")
                dialogPrint.hide()
            }
        btnCancelar.setOnClickListener { dialogPrint.hide() }
        dialogPrint.show()
    }

    private fun showProgress(show: Boolean) {
        binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    /***
     * SQLite - Consulta de datos
     */

    private fun formatPrecio(precio: Int): String {
        var formatPrecio = formatea.format(precio.toLong())
        formatPrecio = formatPrecio.replace(',', '.')
        return formatPrecio
    }

    fun getScreenWidth(activity: Activity): Int {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return size.x
    }

    companion object {
        const val CANT_PUESTOS = "CANT_PUESTOS"
        const val PRECIO_PASAJE = "PRECIO_PASAJE"
        const val ID_VEHICULO = "ID_VEHICULO"
        const val ID_HORARIO = "ID_HORARIO"
        const val ID_RUTA = "ID_RUTA"
        const val ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE"
        const val ID_PARADERO_INICIO = "PARADERO_INCIO"
        const val ID_PARADERO_FIN = "PARADERO_FINAL"
        const val HORARIO = "HORARIO"
        const val TIPO_USUARIO = "TIPO_USUARIO"
        const val NAME_TIPO_PASAJERO = "NAME_USUARIO"
        const val RUTA_LOGO_TICKET = "https://mi.appbusticket.com/public/common/img/"
        private const val LEFT_LENGTH = 16
        private const val RIGHT_LENGTH = 16
        private const val LEFT_TEXT_MAX_LENGTH = 8
        private val TAG = SelectSillas::class.java.simpleName

        const val SALE_BY_DATE = "sale_by_date"
        const val TICKET_ONE_WAY = "ticket_one_way_data"
        const val TICKET_BACK = "ticket_back_data"
        const val SERVICE_ID = "service_id"
        const val IS_EDIT_TICKET = "edit_ticket"
        const val CHAIRS_EDIT_TICKET = "chairs_edit_ticket"
        const val DATE_TICKET_REGISTERED = "date_ticket_to_edit"
    }

/*==============================================================================================
======================================BLUETOOTH PART============================================
==============================================================================================*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_BLUETOOTH -> printBluetooth()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_BLUETOOTH && resultCode == RESULT_OK) {
            // showDialogTicketLibrary()
        }
    }

    private fun printBluetooth() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else {
            AsyncBluetoothEscPosPrint(this).execute(this.getAsyncEscPosPrinter(Constants.selectedDevice))
        }
    }

    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?): AsyncEscPosPrinter? {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)
        val split = info_ruta.split(",")
        val image = PrinterTextParserImg.bitmapToHexadecimalString(
            printer, this.applicationContext.resources.getDrawableForDensity(
                R.drawable.imagotipo_busticket,
                DisplayMetrics.DENSITY_MEDIUM
            )
        )
        val logo = PrinterTextParserImg.bitmapToHexadecimalString(
            printer,
            this.applicationContext.resources.getDrawableForDensity(
                R.drawable.imagotipo_busticket,
                DisplayMetrics.DENSITY_MEDIUM
            )
        )
        val stopOne = split[3].trim { it <= ' ' }.uppercase(Locale.getDefault())
        val stopTwo = split[4].trim { it <= ' ' }.uppercase(Locale.getDefault())
        val companyName =
            UsuarioPreferences.getInstance(context).nombreEmpresa.uppercase(Locale.getDefault())

        val dateToPrint =
            ticketDate.formatDate(inputFormat = "yyyy-MM-dd", outputFormat = "dd-MM-yy")

        var textToPrint = """
            [C]<font size='big'>$companyName</font>
            [L]
            [C]<b>${stopOne} a ${stopTwo}<b>
            [L]
            [C]Usted Pagó:
            [L]
            [C]<font size='wide'>$getPrecioPasaje</font>
            [C]${nombreUsuario}
            [L]
            [C]--------------------------------
            [C]Fecha[C]Salida[C]Asiento
            [C]${dateToPrint}[C]${split[2]}[C]$listSillas
            [C]--------------------------------
            [L]
            [C]${desc_empresa}
            [L]
            [L]$numVoucher
            [L]Emisión: ${Utilities.getDate("dd-MM-yy")}
            [L]${Utilities.getTime()} ${split[0]} ${UsuarioPreferences.getInstance(context).nombre}
            [L]
            [C]www.busticket.cl
            [C]Copia Cliente
            """

        // Transform text to print ticket correctly
        textToPrint = textToPrint.lines().joinToString(transform = String::trim, separator = "\n")
        Log.e(TAG, textToPrint)

        return printer.setTextToPrint(textToPrint)
    }

    override fun isLoading(state: Boolean) {
    }

    override fun onFinishPrint() {
        finish()
    }
}