package com.smartgeeks.busticket.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.utils.Utilities.formatDate
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.UUID

private const val LEFT_LENGTH = 16
private const val RIGHT_LENGTH = 16
private const val LEFT_TEXT_MAX_LENGTH = 8

private const val TAG: String = "PrintTicket"

class PrintTicket(private val context: Activity, var stateListener: PrintState) {

    interface PrintState {
        fun isLoading(state: Boolean)
        fun onFinishPrint()
    }

    private var printState = false
    private var namePrint: String? = null
    private var formatter = DecimalFormat("###,###.##")
    private lateinit var printDialog: Dialog

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //Configuracion Impresora
    private val lisPrintBluetooth = ArrayList<String>()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothDevice: BluetoothDevice

    private var outputStream: OutputStream? = null
    private var outputStreamTitle: OutputStream? = null
    private var inputStream: InputStream? = null
    private var thread: Thread? = null

    private lateinit var readBuffer: ByteArray
    private var readBufferPosition = 0

    @Volatile
    private var stopWorker = false

    // Ticket Data
    private var idParaderoInicio = 0
    private var idParaderoFin: Int = 0
    private var idRutaDisponible: Int = 0
    private var horario: String = ""
    private var idTipoUsuario: Int = 0
    private var precioSumPasaje: Int = 0
    private var countPasajes = 1
    private var idVehiculo: Int = 0
    private var namePassengerType: String = ""
    private var info: String = ""
    private var sillas: String = ""
    private var showHeader: Boolean = true
    private var isMultipleTicket: Boolean = false
    private var dateTicket: String = ""

    // Preferences
    private var idEmpresa: Int = 0
    private var idOperador: Int = 0
    private var companyName: String = ""
    private var companyDesc: String = ""

    private var numVoucher = ""
    private var paraderoInicio = ""
    private var paraderoDestino = ""
    private var bus = ""
    private var printedTicketHour = ""

    init {
        idOperador = UsuarioPreferences.getInstance(context).idUser
        companyName = UsuarioPreferences.getInstance(context).nombreEmpresa.trim().uppercase()
        companyDesc = UsuarioPreferences.getInstance(context).descEmpresa
        idEmpresa = UsuarioPreferences.getInstance(context).idEmpresa

        encontrarDispositivoBlue()
    }

    fun setData(
        idStartBusStop: Int,
        idEndBusStop: Int,
        idEnabledRoute: Int,
        time: String,
        idPassengerType: Int,
        ticketPrice: Double,
        idVehicle: Int,
        passengerType: String,
        info: String = "",
        ticketQuantity: Int = 1,
        seats: String = "",
        _showHeader: Boolean = true,
        isMultiTicket: Boolean = false,
        travelDate: String = "default",
        numVoucher: String = ""
    ) {
        idParaderoInicio = idStartBusStop
        idParaderoFin = idEndBusStop
        idRutaDisponible = idEnabledRoute
        horario = time
        idTipoUsuario = idPassengerType
        precioSumPasaje = ticketPrice.toInt()
        idVehiculo = idVehicle
        namePassengerType = passengerType
        countPasajes = ticketQuantity
        this.info = info
        this.sillas = seats
        showHeader = _showHeader
        this.isMultipleTicket = isMultiTicket
        this.numVoucher = numVoucher

        if (travelDate == "default") {
            dateTicket = Helpers.getDateTicket()
        } else {
            dateTicket = travelDate.formatDate("yyyy-MM-dd", "dd-M-yy")
        }
        Log.e(
            TAG,
            "Fecha Ticket: $dateTicket - ${this.numVoucher} - multiple $isMultiTicket - Sillas $sillas"
        )

        val ticketInformation: Array<String> = info.split(",").toTypedArray()

        bus = ticketInformation[0]
        if (ticketInformation.size > 3) {
            paraderoInicio = ticketInformation[3]
            paraderoDestino = ticketInformation[4]
        } else {
            paraderoInicio = ticketInformation[1].split("-")[0]
            paraderoDestino = ticketInformation[1].split("-")[1]
        }

        Log.e(TAG, "Inicio: $paraderoInicio - Destino $paraderoDestino - Bus $bus")


        try {
            printedTicketHour = numVoucher.split("-")[2]
            // Regex to add : to each 2 digits on the string -> 082033 -> 08:20:33
            printedTicketHour = printedTicketHour.replace(Regex("(.{2})"), "$1:").removeSuffix(":")
        } catch (e: Exception) {
            e.printStackTrace()
            printedTicketHour = Helpers.getTime()
        }

        Log.e(TAG, "Hora: $printedTicketHour")
    }

    @SuppressLint("MissingPermission")
    fun print() {
        try {
            stateListener.isLoading(false)

            getDataPrint()
            if (printState) {
                val pairedDevice = bluetoothAdapter.bondedDevices
                if (pairedDevice.size > 0) {
                    for (pairedDev in pairedDevice) {
                        if (pairedDev.name == namePrint) {
                            bluetoothDevice = pairedDev
                            abrirImpresoraBlue()
                            break
                        } else {
                            Log.e(TAG, "error no existe impresora")
                        }
                    }
                } else {
                    Log.e(TAG, "No hay dispositivos emparejados")
                }
            } else {
                showDialogTiquete()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun showDialogTiquete() {
        printDialog = Dialog(context)
        printDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        printDialog.setContentView(R.layout.dialog_print)
        printDialog.setCanceledOnTouchOutside(false)
        printDialog.setCancelable(false)
        val btnCancel = printDialog.findViewById<Button>(R.id.btnCancelar)
        val lstPrint = printDialog.findViewById<ListView>(R.id.listViewPrint)
        lstPrint.adapter = object :
            ArrayAdapter<String?>(
                context,
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
        // No convertir a lambda
        lstPrint.onItemClickListener = object : AdapterView.OnItemClickListener {
            @SuppressLint("MissingPermission")
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val printerName = parent?.getItemAtPosition(position).toString()
                preferences = context.getSharedPreferences(
                    RutaPreferences.PREFERENCES_PRINT,
                    Context.MODE_PRIVATE
                )
                editor = preferences.edit()
                editor.putString(RutaPreferences.NAME_PRINT, printerName)
                editor.putBoolean(RutaPreferences.ESTADO_PRINT, true)
                editor.apply()
                val pairedDevice = bluetoothAdapter.bondedDevices
                if (pairedDevice.size > 0) {
                    for (pairedDev in pairedDevice) {
                        if (pairedDev.name == printerName) {
                            bluetoothDevice = pairedDev
                            abrirImpresoraBlue()
                            break
                        }
                    }
                }
                printDialog.dismiss()
            }
        }

        btnCancel.setOnClickListener { printDialog.dismiss() }
        try {
            printDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun encontrarDispositivoBlue() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "No tiene Acitivado el bluetooth", Toast.LENGTH_SHORT)
                    .show()
            }
            if (bluetoothAdapter.isEnabled) {
                val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivityForResult(enableBT, 0)
            }
            val pairedDevice = bluetoothAdapter.bondedDevices
            if (pairedDevice.size > 0) {
                for (pairedDev in pairedDevice) {
                    lisPrintBluetooth.add(pairedDev.name)
                    // Log.d(Service.TAG, "se agrego las lista de bluetooth: "+pairedDev.getName());
                }
            } else {
                Log.d(TAG, "no hay lista de bluetooth")
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            Log.i(TAG, "otro Error" + ex.message)
        }
    }

    // ----------------- IMPRESION EN EL VOUCHER -------------------------//
    private fun getDataPrint() {
        namePrint = RutaPreferences.getInstance(context).namePrint
        printState = RutaPreferences.getInstance(context).estadoPrint
    }

    private fun priceFormat(price: Int): String {
        var formattedPrice: String = formatter.format(price.toLong())
        formattedPrice = formattedPrice.replace(',', '.')
        return "$ $formattedPrice"
    }

    @SuppressLint("MissingPermission")
    private fun abrirImpresoraBlue() {
        try {
            Log.e(TAG, "Abriendo impresora $bluetoothDevice   - ${bluetoothDevice.name}")
            //Standard uuid from string //
            val uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting)
            bluetoothSocket.connect()
            outputStream = bluetoothSocket.outputStream
            outputStreamTitle = bluetoothSocket.outputStream
            inputStream = bluetoothSocket.inputStream
            comenzarAEscucharDatos()
            printData()
            finishProcess()
        } catch (ex: java.lang.Exception) {
            Log.i(TAG, "Error P: " + ex.message)
        }
    }

    private fun finishProcess() {
        try {
            disconnectBT()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }

        stateListener.onFinishPrint()
    }

    // Disconnect Printer //
    private fun disconnectBT() {
        try {
            stopWorker = true
            outputStream!!.close()
            inputStream!!.close()
            bluetoothSocket.close()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun comenzarAEscucharDatos() {
        try {

            val delimiter: Byte = 10
            stopWorker = false
            readBufferPosition = 0
            readBuffer = ByteArray(1024)
            thread = Thread {
                Log.d(TAG, "method run")
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
                                } else {
                                    readBuffer[readBufferPosition++] = b
                                }
                            }
                        }
                    } catch (ex: java.lang.Exception) {
                        stopWorker = true
                    }
                }
            }
            thread!!.start()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun printData() {
        Log.d(TAG, "entro a printdata")

        val headerToShow = if (sillas.isNotEmpty()) "Asiento" else "Cantidad"
        val seatsOrQuantity = if (sillas.isNotEmpty()) sillas else countPasajes.toString()

        try {
            if (isMultipleTicket && sillas.isNotEmpty()) {
                precioSumPasaje = precioSumPasaje / countPasajes
                for (seat in sillas.split("-")) {
                    layoutSingleTicket(
                        paraderoInicio,
                        paraderoDestino,
                        bus,
                        headerToShow,
                        seat
                    )
                }
            } else {
                layoutSingleTicket(
                    paraderoInicio,
                    paraderoDestino,
                    bus,
                    headerToShow,
                    seatsOrQuantity
                )
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            Log.e(TAG, "error in printdata")
        }
    }

    private fun layoutSingleTicket(
        startStop: String,
        endStop: String,
        bus: String,
        headerToShow: String,
        seatsOrQuantity: String
    ) {
        val arrayOfByte1 = byteArrayOf(27, 33, 0)
        var format = byteArrayOf(27, 33, 0)
        val centrado = byteArrayOf(0x1B, 'a'.code.toByte(), 0x01)
        // val der = byteArrayOf(0x1B, 'a'.code.toByte(), 0x02)
        val izq = byteArrayOf(0x1B, 'a'.code.toByte(), 0x00)

        // Espacio superior
        outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
        format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
        format[2] = (0x21 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        val nomEmpre = "$companyName \n"
        outputStream!!.write(nomEmpre.toByteArray(), 0, nomEmpre.toByteArray().size)
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

        // Información de la ruta | Inicio - Termino
        if (showHeader) {
            format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
            outputStream!!.write(izq)
            outputStream!!.write(format)
            var route = "Inicio: \n$startStop"
            route += "\nTermino: \n$endStop"
            outputStream!!.write(route.toByteArray(), 0, route.toByteArray().size)
        }

        // Width
        format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        var strPayment = "\n\nUsted pago:\n"
        strPayment = strPayment.replace("(", "")
        outputStream!!.write(strPayment.toByteArray(), 0, strPayment.toByteArray().size)

        format[2] = (0x21 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        val strPrice = "${priceFormat(precioSumPasaje)} \n"
        outputStream!!.write(strPrice.toByteArray(), 0, strPrice.toByteArray().size)

        format[2] = (0x20 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        var strTipoPasajero: String = namePassengerType + "\n"
        strTipoPasajero = strTipoPasajero.replace("(", "")
        outputStream!!.write(
            strTipoPasajero.toByteArray(),
            0,
            strTipoPasajero.toByteArray().size
        )
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        outputStream!!.write(
            ("--------------------------------").toByteArray(), 0,
            ("--------------------------------").toByteArray().size
        )
        format[2] = 0x8.toByte()
        outputStream!!.write(format)
        outputStream!!.write(
            printThreeData(
                "Fecha",
                "Salida",
                headerToShow,
                "One"
            ).toByteArray(),
            0,
            printThreeData("Fecha", "Salida", headerToShow, "One")
                .toByteArray().size
        )
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(format)
        outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
        format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
        format[2] = (0x10 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(format)
        var fechaCodi = Helpers.getDate()
        val arrayFecha = fechaCodi.split("-").toTypedArray()
        fechaCodi = arrayFecha[2] + "-" + arrayFecha[1] + "-" + arrayFecha[0].substring(
            arrayFecha[0].length - 2
        )
        val horaSalidaS: Array<String> =
            horario.trim { it <= ' ' }.split(":").toTypedArray()
        val horaSalidaStr = horaSalidaS[0] + ":" + horaSalidaS[1]
        outputStream!!.write(
            printThreeData(
                dateTicket,
                horaSalidaStr,
                seatsOrQuantity,
                "Two"
            ).toByteArray(),
            0,
            printThreeData(
                dateTicket,
                horaSalidaStr,
                seatsOrQuantity,
                "Two"
            ).toByteArray().size
        )
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(format)
        outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        outputStream!!.write(
            ("--------------------------------").toByteArray(), 0,
            ("--------------------------------").toByteArray().size
        )
        outputStream!!.write(format)
        outputStream!!.write("\n\n".toByteArray(), 0, "\n\n".toByteArray().size)
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        outputStream!!.write(
            (companyDesc + "\n\n").toByteArray(),
            0,
            (companyDesc + "\n\n").toByteArray().size
        )

        /*format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(izq)
        outputStream!!.write(format)
        var str = ""
        str += "$numVoucher \n"
        outputStream!!.write(str.toByteArray(), 0, str.toByteArray().size)*/

        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(izq)
        outputStream!!.write(format)
        var strEmision = "\n$numVoucher"
        strEmision += "\nEmision: $fechaCodi"
        strEmision += "\n${printedTicketHour}"
        strEmision += "\n$bus "
        strEmision += "\n${UsuarioPreferences.getInstance(context).nombre}"
        outputStream!!.write(strEmision.toByteArray(), 0, strEmision.toByteArray().size)

        format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        var strTwo = "\nCopia cliente"
        strTwo += "\nwww.busticket.cl"
        outputStream!!.write(strTwo.toByteArray(), 0, strTwo.toByteArray().size)
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(format)
        outputStream!!.write("\n".toByteArray(), 0, "\n".toByteArray().size)
        outputStream!!.write(centrado)
        outputStream!!.write(format)
        outputStream!!.write(
            ("--------------------------------").toByteArray(), 0,
            ("--------------------------------").toByteArray().size
        )
        format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(izq)
        outputStream!!.write(format)
        val strCoUso = "\n\nCopia Uso Interno"
        outputStream!!.write(strCoUso.toByteArray(), 0, strCoUso.toByteArray().size)
        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(izq)
        outputStream!!.write(format)
        var strInfoTicket = "\nN: $numVoucher"
        strInfoTicket += "\nSalida: $dateTicket $horaSalidaStr"
        strInfoTicket += "\n$headerToShow: $seatsOrQuantity"
        outputStream!!.write(strInfoTicket.toByteArray(), 0, strInfoTicket.toByteArray().size)
        format[2] = (0x8 or arrayOfByte1[2].toInt()).toByte()
        outputStream!!.write(izq)
        outputStream!!.write(format)
        var strOpeTot = ""
        strOpeTot += "\nValor: $strPrice - ${UsuarioPreferences.getInstance(context).nombre}"
        outputStream!!.write(strOpeTot.toByteArray(), 0, strOpeTot.toByteArray().size)

        format = byteArrayOf(27, 33, 0)
        outputStream!!.write(format)
        outputStream!!.write("\n\n\n\n".toByteArray(), 0, "\n\n\n\n".toByteArray().size)
    }

    fun printThreeData(
        _leftText: String,
        middleText: String,
        rightText: String,
        tipo: String
    ): String {
        var leftText = _leftText
        val sb = StringBuilder()
        // At most LEFT_TEXT_MAX_LENGTH Chinese characters + two dots are displayed on the left
        if (leftText.length > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + ".."
        }
        val leftTextLength: Int = getBytesLength(leftText)
        val middleTextLength: Int = getBytesLength(middleText)
        val rightTextLength: Int = getBytesLength(rightText)
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
}