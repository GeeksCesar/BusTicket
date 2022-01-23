package com.smartgeeks.busticket.Menu

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
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
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.api.Service
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.vehicle.SillaOcupada
import com.smartgeeks.busticket.databinding.ActivitySelectSillasBinding
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.presentation.VehicleViewModel
import com.smartgeeks.busticket.sync.SyncServiceRemote
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.Constants
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.Helpers
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil

@AndroidEntryPoint
class SelectSillas : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var listSillasOcupadas: List<SillaOcupada> = ArrayList()
    private val sillasSeleccionadas: MutableList<Int> = ArrayList()
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
    private var state_sync = false
    private var numVoucher = ""

    var listSillas = ""

    //Configuracion Impresora
    private val lisPrintBluetooth = ArrayList<String>()
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
    var bluetoothDevice: BluetoothDevice? = null
    var outputStream: OutputStream? = null
    var outputStreamTitle: OutputStream? = null
    var inputStream: InputStream? = null
    var thread: Thread? = null
    lateinit var readBuffer: ByteArray
    var readBufferPosition = 0

    lateinit var binding: ActivitySelectSillasBinding
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val ticketViewModel: TicketViewModel by viewModels()

    @Volatile
    var stopWorker = false
    lateinit var dialogPrint: Dialog
    lateinit var btnCancelar: Button
    lateinit var lstPrint: ListView
    var formatea = DecimalFormat("###,###.##")
    var preferences: SharedPreferences? = null
    var estadoPrint = false
    var namePrint: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySelectSillasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWidgets()
        fetchData()

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_RUN_REMOTE_SYNC)
        filter.addAction(Constantes.EXTRA_PROGRESS)
        filter.addAction(Constantes.ACTION_FINISH_REMOTE_SYNC)
        val receiver = ResponseReceiver()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            receiver, filter
        )
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

        //Input
        showDataTextView()
        encontrarDispositivoBlue()
        remoteSync()
        getDataPrint()
    }

    /**
     * Request to Service
     */
    private fun fetchData() {
        vehicleViewModel.getOccupiedSeats(id_ruta_disponible, horario).observe(this, { result ->
            when (result) {
                is Resource.Failure -> {
                    progress?.dismiss()
                }
                is Resource.Loading -> {
                    showProgressDialog()
                }
                is Resource.Success -> {

                    val data = result.data

                    when (data.estado) {
                        Constants.SUCCESS_RESPONSE -> {
                            listSillasOcupadas = data.sillas_ocupadas
                            Log.e(TAG, "Se encontraron ${listSillasOcupadas.size} sillas ocupadas.")
                        }
                        Constants.FAILED_RESPONSE -> Log.e(TAG, "Error al traer datos")
                    }
                    fetchVehicleInfo()
                }
            }
        })
    }

    private fun fetchVehicleInfo() {
        vehicleViewModel.getVehicleInfo(id_vehiculo).observe(this, { result ->
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
        })
    }

    private fun getDataPrint() {
        namePrint = RutaPreferences.getInstance(context).namePrint
        estadoPrint = RutaPreferences.getInstance(context).estadoPrint
        Log.d(Service.TAG, "name print: $namePrint")
        Log.d(Service.TAG, "boolen print: $estadoPrint")
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
                if (cant_sillas % 2 == 1 && silla >= cant_sillas - 5) {
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
        for (i in sillasSeleccionadas.indices) {
            if (sillasSeleccionadas[i] == silId) {
                sillasSeleccionadas.removeAt(i)
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
        if (isChecked == true) {
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
        } else if (isChecked == false) {
            removeSillaFromArray(silla_seleccionda)
            buttonView.setTextColor(ContextCompat.getColor(context!!, R.color.md_black_1000))
        }
    }

    /**
     * Consultar sillas ocupadas por horario de ruta
     */

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
            id_vehiculo
        ).observe(this) { result ->
            when (result) {
                is Resource.Failure -> showProgress(false)
                is Resource.Loading -> showProgress(true)
                is Resource.Success -> {
                    val data = result.data
                    if (!data.error) {
                        showProgress(false)
                        numVoucher = data.num_voucher
                        Log.e(TAG, "Num Voucher: $numVoucher")

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
                                alertDialog.dismiss()
                                handlePrintTicket()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
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

    private fun handlePrintTicket() {
        getDataPrint()
        if (estadoPrint) {
            Log.e(Service.TAG, "entro estado: $estadoPrint")
            val pairedDevice = bluetoothAdapter!!.bondedDevices
            if (pairedDevice.size > 0) {
                for (pairedDev in pairedDevice) {
                    if (pairedDev.name == namePrint) {
                        Log.e(Service.TAG, "name impresora_ $namePrint")
                        bluetoothDevice = pairedDev
                        abrirImpresoraBlue()
                        break
                    }
                }
            } else {
                Log.e(Service.TAG, "error devices bluetooh")
            }
        } else {
            showDialogTiquete()
        }
    }

    fun encontrarDispositivoBlue() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                //lblPrinterName.setText("No Bluetooth Adapter found");
            }
            if (bluetoothAdapter?.isEnabled == true) {
                val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBT, 0)
            }
            val pairedDevice = bluetoothAdapter?.bondedDevices
            if ((pairedDevice?.size ?: 0) > 0) {
                for (pairedDev in pairedDevice!!) {
                    lisPrintBluetooth.add(pairedDev.name)
                }
            }
            //lblPrinterName.setText("Bluetookkkkth Printer Attached");
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.i("otro Error", "" + ex.message)
        }
    }

    private fun showProgress(show: Boolean) {
        binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun abrirImpresoraBlue() {
        try {
            //Standard uuid from string //
            val uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            bluetoothSocket = bluetoothDevice!!.createRfcommSocketToServiceRecord(uuidSting)
            bluetoothSocket?.let {
                it.connect()
                outputStream = it.outputStream
                outputStreamTitle = it.outputStream
                inputStream = it.inputStream
            }
            comenzarAEscucharDatos()
            printData()
            goIntentMain()
        } catch (ex: Exception) {
            Log.i("Error P", "" + ex.message)
        }
    }

    fun comenzarAEscucharDatos() {
        try {
            val handler = Handler()
            val delimiter: Byte = 10
            stopWorker = false
            readBufferPosition = 0
            readBuffer = ByteArray(1024)
            thread = Thread {
                while (!Thread.currentThread().isInterrupted && !stopWorker) {
                    try {
                        val byteAvailable = inputStream!!.available()
                        if (byteAvailable > 0) {
                            val packetByte = ByteArray(byteAvailable)
                            inputStream!!.read(packetByte)
                            for (i in 0 until byteAvailable) {
                                val b = packetByte[i]
                                if (b == delimiter) {
                                    val encodedByte = ByteArray(readBufferPosition)
                                    System.arraycopy(
                                        readBuffer, 0,
                                        encodedByte, 0,
                                        encodedByte.size
                                    )
                                    val data = String(encodedByte, StandardCharsets.US_ASCII)
                                    readBufferPosition = 0
                                    handler.post {
                                        //lblPrinterName.setText(data);
                                    }
                                } else {
                                    readBuffer[readBufferPosition++] = b
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        stopWorker = true
                    }
                }
            }
            thread!!.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun printData() {
        val command: ByteArray? = null
        try {
            val split = info_ruta!!.split(",").toTypedArray()
            val arrayOfByte1 = byteArrayOf(27, 33, 0)
            var format = byteArrayOf(27, 33, 0)
            val centrado = byteArrayOf(0x1B, 'a'.toByte(), 0x01)
            val der = byteArrayOf(0x1B, 'a'.toByte(), 0x02)
            val izq = byteArrayOf(0x1B, 'a'.toByte(), 0x00)

            // Espacio superior
            outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            format[2] = (0x21 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            val nom_empre = """
                ${UsuarioPreferences.getInstance(context).nombreEmpresa.toUpperCase()}
                
                """.trimIndent()
            outputStream!!.write(nom_empre.toByteArray(), 0, nom_empre.toByteArray().size)
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            outputStream!!.write(
                "================================".toByteArray(),
                0,
                "================================".toByteArray().size
            )
            outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
            outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
            // Width
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            var str_ruta = """${split[3].trim { it <= ' ' }.toUpperCase()} a
""" + split[4].trim { it <= ' ' }
                .toUpperCase() + "\n\n"
            str_ruta = str_ruta.replace("(", "")
            outputStream!!.write(str_ruta.toByteArray(), 0, str_ruta.toByteArray().size)
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            var str_pago = "Usted pago:\n"
            str_pago = str_pago.replace("(", "")
            outputStream!!.write(str_pago.toByteArray(), 0, str_pago.toByteArray().size)
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            format[2] = (0x21 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            val str_precio = """
                $getPrecioPasaje
                
                """.trimIndent()
            outputStream!!.write(str_precio.toByteArray(), 0, str_precio.toByteArray().size)
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            var str_tipo_pasajero = """
                $nombreUsuario
                
                """.trimIndent()
            str_tipo_pasajero = str_tipo_pasajero.replace("(", "")
            outputStream!!.write(
                str_tipo_pasajero.toByteArray(),
                0,
                str_tipo_pasajero.toByteArray().size
            )
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            outputStream!!.write(
                """--------------------------------
""".toByteArray(), 0, """--------------------------------
""".toByteArray().size
            )
            format[2] = 0x8.toByte()
            outputStream!!.write(format)
            outputStream!!.write(
                printThreeData("Fecha", "Salida", "Asiento", "One").toByteArray(),
                0,
                printThreeData("Fecha", "Salida", "Asiento", "One").toByteArray().size
            )
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(format)
            outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
            format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
            format[2] = (0x10 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(format)
            var fecha_codi = Helpers.getDate()
            val array_fecha = fecha_codi.split("-").toTypedArray()
            fecha_codi = array_fecha[2] + "-" + array_fecha[1] + "-" + array_fecha[0].substring(
                array_fecha[0].length - 2
            )
            val hora_salida_s = split[2].trim { it <= ' ' }.split(":").toTypedArray()
            val hora_salida_str = hora_salida_s[0] + ":" + hora_salida_s[1]
            outputStream!!.write(
                printThreeData(
                    fecha_codi,
                    hora_salida_str,
                    listSillas,
                    "Two"
                ).toByteArray(),
                0,
                printThreeData(fecha_codi, hora_salida_str, listSillas, "Two").toByteArray().size
            )
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(format)
            outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            outputStream!!.write(
                """--------------------------------
""".toByteArray(), 0, """--------------------------------
""".toByteArray().size
            )
            outputStream!!.write(format)
            outputStream!!.write("\n\n".toByteArray(), 0, "\n\n".toByteArray().size)
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            outputStream!!.write(
                """$desc_empresa

""".toByteArray(), 0, """$desc_empresa

""".toByteArray().size
            )
            format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(izq)
            outputStream!!.write(format)
            var str = ""
            str += """
                $numVoucher
                
                """.trimIndent()
            outputStream!!.write(str.toByteArray(), 0, str.toByteArray().size)
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(izq)
            outputStream!!.write(format)
            var str_emision = "Emision: $fecha_codi\n"
            str_emision += """${Helpers.getTime()} ${split[0]} ${
                UsuarioPreferences.getInstance(
                    context
                ).nombre
            }
"""
            outputStream!!.write(str_emision.toByteArray(), 0, str_emision.toByteArray().size)
            format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(centrado)
            outputStream!!.write(format)
            var str_two = ""
            str_two += "www.busticket.cl\n"
            str_two += "Copia Cliente"
            outputStream!!.write(str_two.toByteArray(), 0, str_two.toByteArray().size)
            //no serive desde abajo
            format = byteArrayOf(27, 33, 0)
            outputStream!!.write(format)
            outputStream!!.write("\n\n\n\n".toByteArray(), 0, "\n\n\n\n".toByteArray().size)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showDialogTiquete() {
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
        lstPrint.adapter = object :
            ArrayAdapter<String?>(
                this,
                android.R.layout.simple_list_item_1,
                lisPrintBluetooth.toList()
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
                val name_impresora = parent.getItemAtPosition(position).toString()
                preferences =
                    context!!.getSharedPreferences(RutaPreferences.PREFERENCES_PRINT, MODE_PRIVATE)
                preferences?.edit {
                    putString(RutaPreferences.NAME_PRINT, name_impresora)
                    putBoolean(RutaPreferences.ESTADO_PRINT, true)
                    apply()
                }

                val pairedDevice = bluetoothAdapter!!.bondedDevices
                if (pairedDevice.size > 0) {
                    for (pairedDev in pairedDevice) {
                        if (pairedDev.name == name_impresora) {
                            bluetoothDevice = pairedDev
                            abrirImpresoraBlue()
                            break
                        }
                    }
                }
                dialogPrint.hide()
            }
        btnCancelar.setOnClickListener { dialogPrint.hide() }
        dialogPrint.show()
    }

    /***
     * SQLite - Consulta de datos
     */

    private fun formatPrecio(precio: Int): String {
        var formatPrecio = formatea.format(precio.toLong())
        formatPrecio = formatPrecio.replace(',', '.')
        return formatPrecio
    }

    private fun goIntentMain() {
        try {
            disconnectBT()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        Log.d(Service.TAG, "entro a goIntentMain")
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.BACK, true)
        startActivity(intent)
        finish()
    }

    private fun remoteSync() {
        if (!state_sync) {
            val sync = Intent(context, SyncServiceRemote::class.java)
            sync.action = Constantes.ACTION_RUN_REMOTE_SYNC
            applicationContext.startService(sync)
        }
    }

    // Disconnect Printer //
    fun disconnectBT() {
        try {
            stopWorker = true
            outputStream!!.close()
            inputStream!!.close()
            bluetoothSocket!!.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_RUN_REMOTE_SYNC -> state_sync =
                    intent.getBooleanExtra(Constantes.EXTRA_PROGRESS, false)
            }
        }
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
        fun printThreeData(
            leftText: String,
            middleText: String,
            rightText: String,
            tipo: String
        ): String {
            var leftText = leftText
            val sb = StringBuilder()
            // At most LEFT_TEXT_MAX_LENGTH Chinese characters + two dots are displayed on the left
            if (leftText.length > LEFT_TEXT_MAX_LENGTH) {
                leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + ".."
            }
            val leftTextLength = getBytesLength(leftText)
            val middleTextLength = getBytesLength(middleText)
            val rightTextLength = getBytesLength(rightText)
            sb.append(leftText)
            // Calculate the length of the space between the left text and the middle text
            var marginBetweenLeftAndMiddle = 0
            marginBetweenLeftAndMiddle = if (tipo == "One") {
                LEFT_LENGTH - leftTextLength - middleTextLength / 2
            } else {
                13 - leftTextLength - middleTextLength / 2
            }
            for (i in 0 until marginBetweenLeftAndMiddle) {
                sb.append(" ")
            }
            sb.append(middleText)

            // Calculate the length of the space between the right text and the middle text
            var marginBetweenMiddleAndRight = 0
            marginBetweenMiddleAndRight = if (tipo == "One") {
                RIGHT_LENGTH - middleTextLength / 2 - rightTextLength
            } else {
                13 - middleTextLength / 2 - rightTextLength
            }
            for (i in 0 until marginBetweenMiddleAndRight) {
                sb.append(" ")
            }

            // When printing, I found that the rightmost text is always one character to the right, so a space needs to be deleted
            sb.delete(sb.length - 1, sb.length).append(rightText)
            return sb.toString()
        }

        private fun getBytesLength(msg: String): Int {
            return msg.toByteArray(Charset.forName("GB2312")).size
        }

        fun getScreenWidth(activity: Activity): Int {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            return size.x
        }
    }
}