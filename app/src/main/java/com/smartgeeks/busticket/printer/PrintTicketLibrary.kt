package com.smartgeeks.busticket.printer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.DisplayMetrics
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.MyBluetoothPrintersConnections
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.utils.Constants
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import java.text.DecimalFormat
import java.util.Locale

private const val TAG: String = "PrintTicketLibrary"
private const val PERMISSION_BLUETOOTH = 1

/**
 * This class is use to delegate the work to print Tickets on the activities
 */
class PrintTicketLibrary(private val context: Activity, var stateListener: PrintState) {

    interface PrintState {
        fun isLoading(state: Boolean)
        fun onFinishPrint()
    }

    private var printState = false
    private var namePrint: String? = null
    private var formatter = DecimalFormat("###,###.##")
    private lateinit var printDialog: Dialog

    private lateinit var preferences: SharedPreferences

    // Ticket Data
    private var countPasajes = 1
    private var namePassengerType: String = ""
    private var info: String = ""
    private lateinit var ticket: TicketEntity

    // Preferences
    private var idEmpresa: Int = 0
    private var idOperador: Int = 0
    private var companyName: String = ""
    private var companyDesc: String = ""

    init {
        idOperador = UsuarioPreferences.getInstance(context).idUser
        companyName = UsuarioPreferences.getInstance(context).nombreEmpresa.trim().uppercase()
        companyDesc = UsuarioPreferences.getInstance(context).descEmpresa
        idEmpresa = UsuarioPreferences.getInstance(context).idEmpresa

        setPrinter()
    }

    @SuppressLint("MissingPermission")
    private fun setPrinter() {
        namePrint = RutaPreferences.getInstance(context).namePrint
        printState = RutaPreferences.getInstance(context).estadoPrint

        if (Constants.selectedDevice == null)
            MyBluetoothPrintersConnections().list?.find { it.device.name == namePrint }?.let {
                Constants.selectedDevice = it
                Log.e(TAG, "setPrinter: $it")
            } ?: run {
                showDialogTiquete()
                Log.e(TAG, "setPrinter: Show dialog")
            }
    }

    fun setData(
        ticketEntity: TicketEntity, passengerType: String,
        info: String = "", ticketQuantity: Int = 1
    ) {
        stateListener.isLoading(true)
        ticket = ticketEntity
        namePassengerType = passengerType
        countPasajes = ticketQuantity
        this.info = info
    }

    fun print() {
        printBluetooth()
    }

    @SuppressLint("MissingPermission")
    private fun showDialogTiquete() {
        printDialog = Dialog(context)
        printDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        printDialog.setContentView(R.layout.dialog_print)
        printDialog.setCanceledOnTouchOutside(false)
        printDialog.setCancelable(false)
        val btnCancel = printDialog.findViewById<Button>(R.id.btnCancelar)
        val lstPrint = printDialog.findViewById<ListView>(R.id.listViewPrint)

        val bluetoothPrintersConnections =
            MyBluetoothPrintersConnections().list?.map { it.device.name } ?: listOf()

        lstPrint.adapter = object :
            ArrayAdapter<String?>(
                context,
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

                val printerName = parent?.getItemAtPosition(position).toString()
                preferences = context.getSharedPreferences(
                    RutaPreferences.PREFERENCES_PRINT,
                    Context.MODE_PRIVATE
                )
                preferences.edit {
                    putString(RutaPreferences.NAME_PRINT, printerName)
                    putBoolean(RutaPreferences.ESTADO_PRINT, true)
                    apply()
                }
                Constants.selectedDevice =
                    MyBluetoothPrintersConnections().list?.find { it.device.name == printerName }
                printDialog.dismiss()
            }

        btnCancel.setOnClickListener { printDialog.dismiss() }
        try {
            printDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun priceFormat(price: Int): String {
        var formattedPrice: String = formatter.format(price.toLong())
        formattedPrice = formattedPrice.replace(',', '.')
        return "$ $formattedPrice"
    }

    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    private fun printBluetooth() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.BLUETOOTH),
                PERMISSION_BLUETOOTH
            )
        } else {
            Toast.makeText(context, "${Constants.selectedDevice?.device}", Toast.LENGTH_LONG).show()
            AsyncBluetoothEscPosPrint(context).execute(this.getAsyncEscPosPrinter(Constants.selectedDevice))
        }
    }

    // ----------------- IMPRESION EN EL VOUCHER -------------------------//
    /**
     * Asynchronous printing
     */
    @SuppressLint("SimpleDateFormat")
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?): AsyncEscPosPrinter? {
        val printer = AsyncEscPosPrinter(printerConnection, 203, 48f, 32)

        val logo = PrinterTextParserImg.bitmapToHexadecimalString(
            printer,
            context.resources.getDrawableForDensity(
                R.drawable.imagotipo_busticket,
                DisplayMetrics.DENSITY_MEDIUM
            )
        )

        val companyName =
            UsuarioPreferences.getInstance(context).nombreEmpresa.uppercase(Locale.getDefault())
        val split: Array<String> = info.split(",").toTypedArray()
        val date = Utilities.getDate("dd-MM-yy")

        var stops = ""
        try {
            stops = "<b>${split[3]} a ${split[4]}<b>"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var textToPrint = """
            [L]
            [C]<font size='big'>$companyName</font>
            [C]$stops
            [C]================================
            [L]
            [C]Usted Pagó:
            [C]<font size='wide'>${priceFormat(ticket.totalPagar.toInt())}</font>
            [C]${namePassengerType}
            [L]
            [C]--------------------------------
            [C]Fecha[C]Salida[C]Cantidad
            [C]${date}[C]${ticket.horaSalida.removeSuffix(":00")}[C]$countPasajes
            [C]--------------------------------
            [L]
            [C]${companyDesc}
            [L]
            [L]${ticket.voucher}
            [L]Emisión: $date
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
}