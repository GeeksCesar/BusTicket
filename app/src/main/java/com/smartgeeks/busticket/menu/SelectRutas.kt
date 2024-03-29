package com.smartgeeks.busticket.menu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import cn.pedant.SweetAlert.SweetAlertDialog
import com.orm.query.Select
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.Modelo.Paradero
import com.smartgeeks.busticket.Modelo.TarifaParadero
import com.smartgeeks.busticket.Modelo.TipoUsuario
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.databinding.ActivitySelectRutasBinding
import com.smartgeeks.busticket.domain.models.PriceByDate
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.InternetCheck
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.util.Locale

@AndroidEntryPoint
class SelectRutas : AppCompatActivity(), PrintTicket.PrintState {
    var bundle: Bundle? = null
    var formatea = DecimalFormat("###,###.##")

    private var listParaderos: ArrayList<String> = ArrayList()
    private var lisUsuarios: ArrayList<String> = ArrayList()
    private val listParaderoFin = ArrayList<String>()

    // Listas para SQLite
    private var paraderosList: List<Paradero> = ArrayList()
    private var tipoUsuariosList: ArrayList<TipoUsuario> = ArrayList()
    var countPasajes = 1
    var precioSumPasaje = 0
    var precioPasaje = 0
    var precioPasajeInicial = 0
    var id_tipo_usuario = 0
    var id_paradero_inicio = 0
    var id_paradero_fin = 0
    var position_tipo_usuario = 0
    var sizeTarifas = 0
    var ruta_inicio: String = ""
    var ruta_fin: String = ""
    var horario: String = ""
    var info: String = ""
    var nombreEmpresa: String = ""
    var desc_empresa: String = ""
    private lateinit var context: Context
    var id_horario = 0
    var id_vehiculo = 0
    var id_operador = 0
    var id_ruta = 0
    var id_ruta_disponible = 0
    var id_empresa = 0
    var nameUsuario: String = ""
    var preferences: SharedPreferences? = null
    var estadoRuta = false
    var estadoPrint = false
    var namePrint: String? = null

    // private lateinit var printTicket: PrintTicketLibrary
    private lateinit var printTicketPrev: PrintTicket

    private val ticketViewModel: TicketViewModel by viewModels()
    private lateinit var binding: ActivitySelectRutasBinding

    private var ticketOneWay: PriceByDate? = null
    private var ticketBack: PriceByDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySelectRutasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWidget()
        setupBackButton()

        // printTicket = PrintTicketLibrary(this@SelectRutas, this)
        printTicketPrev = PrintTicket(this@SelectRutas, this)

        binding.spUsuarios.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                id_tipo_usuario = tipoUsuariosList[position].id_remoto.toInt()
                position_tipo_usuario = position
                nameUsuario = parent.getItemAtPosition(position).toString()
                try {
                    precioPasaje = getPrecioSQLite(
                        id_paradero_inicio,
                        id_paradero_fin,
                        id_tipo_usuario
                    ).toInt()
                    Log.e(TAG, "Precio del pasaje: $precioPasaje")
                    precioPasajeInicial = precioPasaje

                    formatPrecio()
                } catch (e: Exception) {
                    Log.e(TAG, "Error al obtener precio del pasaje")
                    e.message
                }
                if (id_tipo_usuario == 0) {
                    showView(false)
                } else if (sizeTarifas == 0 && tipoUsuariosList[0].id_remoto.toInt() > 0) {
                    Toast.makeText(
                        context,
                        "No se han definido precios para este usuario.",
                        Toast.LENGTH_SHORT
                    ).show()
                    showView(false)
                } else {
                    showView(true)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        binding.spInicio.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                ruta_inicio = parent.getItemAtPosition(position).toString()
                id_paradero_inicio = paraderosList[position].idRemoto
                getParaderosFinSQLite(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spFin.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                l: Long
            ) {
                ruta_fin = parent.getItemAtPosition(position).toString()
                id_paradero_fin = Paradero.find(
                    Paradero::class.java,
                    "ruta = ? AND paradero = ?",
                    arrayOf("" + id_ruta, "" + ruta_fin),
                    "remoto",
                    "remoto",
                    null
                )[0].idRemoto
                try {
                    precioPasaje = getPrecioSQLite(
                        id_paradero_inicio,
                        id_paradero_fin,
                        id_tipo_usuario
                    ).toInt()
                    precioPasajeInicial = precioPasaje
                    formatPrecio()
                } catch (e: Exception) {
                    e.message
                }
                if (sizeTarifas == 0 && tipoUsuariosList[0].id_remoto.toInt() > 0) {
                    Toast.makeText(
                        context,
                        "No se han definido precios para este usuario.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.contenedorCheckbox.visibility = View.GONE
                    binding.contenedorPrecio.visibility = View.GONE
                } else {
                    binding.contenedorCheckbox.visibility = View.VISIBLE
                    binding.contenedorPrecio.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        binding.btnSumar.setOnClickListener {
            countPasajes++
            formatPrecio()
            binding.textCount.text = "" + countPasajes
        }
        binding.btnRestar.setOnClickListener {
            if (countPasajes > 1) {
                countPasajes--
                formatPrecio()
                binding.textCount.text = "" + countPasajes
            }
        }
        binding.btnOlvidarRuta.setOnClickListener {
            preferences =
                context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
            preferences?.edit {
                clear().apply()
            }
            goIntentMain()
        }

        binding.btnFinalizar.setOnClickListener {
            precioSumPasaje = precioPasaje * countPasajes

            if (ruta_inicio == ruta_fin) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Las opciones de paradero deben ser distintas",
                    SweetAlertDialog.NORMAL_TYPE
                )
            } else if (precioSumPasaje == 0) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Verifique el valor de pasaje",
                    SweetAlertDialog.NORMAL_TYPE
                )
            } else {
                binding.btnFinalizar.visibility = View.GONE

                // Guarda los datos en la BD Local
                val fecha = Utilities.getTimeByTimezone("yyyy-MM-dd")
                val hora = Utilities.getTimeByTimezone("HH:mm:ss")
                val numVoucher = Utilities.getVoucherName(id_vehiculo, id_operador, fecha, hora)

                val ticketEntity = TicketEntity(
                    0,
                    id_paradero_inicio,
                    id_paradero_fin,
                    id_ruta_disponible,
                    id_operador,
                    horario,
                    id_tipo_usuario,
                    fecha, hora,
                    1,
                    precioSumPasaje.toDouble(),
                    id_vehiculo,
                    numVoucher
                )
                sendTicket(ticketEntity)

                val ticketInfo = "$info,$ruta_inicio,$ruta_fin"
                /*printTicket.setData(
                    ticketEntity,
                    nameUsuario,
                    ticketInfo
                )
                printTicket.print()*/
                printTicketPrev.setData(
                    id_paradero_inicio,
                    id_paradero_fin,
                    id_ruta_disponible,
                    horario,
                    id_tipo_usuario,
                    precioSumPasaje.toDouble(),
                    id_vehiculo,
                    nameUsuario,
                    info,
                    countPasajes,
                    numVoucher = ticketEntity.voucher
                )
                printTicketPrev.print()
            }
        }

        binding.btnSiguiente.setOnClickListener(View.OnClickListener {
            precioSumPasaje = precioPasaje * countPasajes

            if (ruta_inicio === ruta_fin) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Las opciones de paradero deben ser distintas",
                    SweetAlertDialog.NORMAL_TYPE
                )
                return@OnClickListener
            } else if (precioSumPasaje == 0) {
                DialogAlert.showDialogFailed(
                    context,
                    "Error",
                    "Verifique el valor de pasaje",
                    SweetAlertDialog.NORMAL_TYPE
                )
                return@OnClickListener
            } else {
                if (binding.cbAsientos.isChecked) {
                    InternetCheck { internet ->
                        if (internet) {
                            Log.e(TAG, "Hay conexión a Internet")
                            startSelectSillasActivity()
                        } else {
                            Log.e(TAG, "No hay conexión a Internet")
                            DialogAlert.showDialogFailed(
                                context,
                                "Error",
                                "Ops.. No hay conexión.",
                                SweetAlertDialog.WARNING_TYPE
                            )
                        }
                    }.execute()
                }
            }
        })
    }

    private fun setupBackButton() {
        binding.commonToolbar.imgBtnBack.setOnClickListener {
            goBack()
        }
    }

    private fun sendTicket(ticketEntity: TicketEntity) {
        ticketViewModel.saveTicket(ticketEntity).observe(this) { result ->
            when (result) {
                is Resource.Failure -> Log.e(TAG, "Failure: ${result.exception.message}")
                is Resource.Loading -> Log.e(TAG, "Loading")
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
                }
            }
        }
    }

    private fun startSelectSillasActivity() {

        val routeId = ticketOneWay?.let {
            it.idRuta
        } ?: id_ruta_disponible

        val intent = Intent(context, SelectSillas::class.java)
        intent.putExtra(SelectSillas.CANT_PUESTOS, countPasajes)
        intent.putExtra(SelectSillas.PRECIO_PASAJE, precioSumPasaje)
        intent.putExtra(SelectSillas.ID_VEHICULO, id_vehiculo)
        intent.putExtra(SelectSillas.ID_RUTA, id_ruta)
        intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, routeId)
        intent.putExtra(SelectSillas.ID_HORARIO, id_horario)
        intent.putExtra(SelectSillas.HORARIO, horario)
        intent.putExtra(SelectSillas.ID_PARADERO_INICIO, id_paradero_inicio)
        intent.putExtra(SelectSillas.ID_PARADERO_FIN, id_paradero_fin)
        intent.putExtra(SelectSillas.TIPO_USUARIO, id_tipo_usuario)
        intent.putExtra(SelectSillas.NAME_TIPO_PASAJERO, nameUsuario)
        intent.putExtra(INFO, "$info,$ruta_inicio,$ruta_fin")

        val saleByDate = ticketOneWay != null
        intent.putExtra(SelectSillas.SALE_BY_DATE, saleByDate)
        intent.putExtra(SelectSillas.TICKET_ONE_WAY, ticketOneWay)
        intent.putExtra(SelectSillas.TICKET_BACK, ticketBack)
        resultLauncher.launch(intent)
    }

    private fun initWidget() = with(binding) {
        context = this@SelectRutas

        btnFinalizar.visibility = View.GONE
        btnSiguiente.visibility = View.GONE
        bundle = intent.extras
        estadoRuta = RutaPreferences.getInstance(context).estadoRuta
        dataPrint
        /*btnOlvidarRuta.visibility = View.VISIBLE
        if (estadoRuta) {
            btnOlvidarRuta.visibility = View.VISIBLE
        } else {
            btnOlvidarRuta.visibility = View.GONE
        }*/
        if (bundle != null) {
            id_ruta = bundle!!.getInt(ID_RUTA)
            id_ruta_disponible = bundle!!.getInt(ID_RUTA_DISPONIBLE)
            id_vehiculo = bundle!!.getInt(ID_VEHICULO)
            id_horario = bundle!!.getInt(ID_HORARIO)
            horario = bundle!!.getString(HORARIO, "")
            info = bundle!!.getString(INFO, "")
        } else {
            id_ruta = RutaPreferences.getInstance(context).idRuta
            id_ruta_disponible = RutaPreferences.getInstance(context).idRutaDisponible
            id_vehiculo = RutaPreferences.getInstance(context).idVehiculo
            id_horario = RutaPreferences.getInstance(context).idHorario
            horario = RutaPreferences.getInstance(context).hora
            info = RutaPreferences.getInstance(context).informacion
        }
        id_operador = UsuarioPreferences.getInstance(context).idUser
        nombreEmpresa = UsuarioPreferences.getInstance(context).nombreEmpresa
        desc_empresa = UsuarioPreferences.getInstance(context).descEmpresa
        nombreEmpresa = nombreEmpresa.trim { it <= ' ' }.uppercase(Locale.getDefault())
        Log.e(TAG, "Horario: $horario")
        Log.e(TAG, "Ruta: $id_ruta_disponible")
        id_empresa = UsuarioPreferences.getInstance(context).idEmpresa
        listParaderos = ArrayList()
        lisUsuarios = ArrayList()
        getParaderosSQLite(id_ruta)
        validarCheckBox()
        textCount.setText("" + countPasajes)

    }

    val dataPrint: Unit
        get() {
            namePrint = RutaPreferences.getInstance(context).namePrint
            estadoPrint = RutaPreferences.getInstance(context).estadoPrint
        }

    private fun showView(show: Boolean) = with(binding) {
        contenedorCheckbox.visibility =
            if (show) View.VISIBLE else View.GONE
        contenedorPrecio.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun formatPrecio() {
        precioSumPasaje = precioPasaje * countPasajes
        var formatPrecio = formatea.format(precioSumPasaje)
        formatPrecio = formatPrecio.replace(',', '.')
        binding.tvPrecio.text = "$ $formatPrecio"
    }

    private fun validarCheckBox() = with(binding) {
        cbAsientos.setOnCheckedChangeListener { _, isChecked ->
            btnSiguiente.text = "Siguiente"

            if (isChecked) {
                cbPie.isChecked = false

                if (sizeTarifas > 0) {
                    btnSiguiente.visibility = View.VISIBLE
                    btnFinalizar.visibility = View.GONE
                }
            } else {
                btnSiguiente.visibility = View.GONE
            }
        }
        cbPie.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbAsientos.isChecked = false
                if (sizeTarifas > 0) {
                    btnSiguiente.visibility = View.GONE
                    btnFinalizar.visibility = View.VISIBLE
                }
            } else {
                btnFinalizar.visibility = View.GONE
            }
        }
    }

    fun goBack() {
        listParaderos.clear()
        listParaderoFin.clear()
        lisUsuarios.clear()
        finish()
    }

    private fun showProgress(show: Boolean) {
        binding.mProgressView.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * ***********   Consultas SQLite  *************
     */
    private fun getParaderosSQLite(id_ruta: Int) {
        listParaderos.clear()
        listParaderoFin.clear()
        paraderosList = Paradero.find(
            Paradero::class.java,
            "ruta = ?",
            arrayOf("" + id_ruta),
            "remoto",
            "remoto",
            null
        )
        if (paraderosList.size == 0) {
            DialogAlert.showDialogFailed(
                context,
                "Atención",
                "No se han definido paraderos para la ruta ",
                SweetAlertDialog.WARNING_TYPE
            )
        }
        for (paradero in paraderosList) {
            listParaderos.add(paradero.paradero)
        }
        try {
            listParaderos.removeAt(paraderosList.size - 1)
        } catch (e: Exception) {
            e.message
        }
        binding.spInicio.adapter =
            ArrayAdapter(
                this,
                R.layout.custom_spinner_inicio,
                R.id.txtName,
                listParaderos
            )
        getUsuariosSQLite()
    }

    fun getParaderosFinSQLite(paradero_inicio: Int) {
        listParaderoFin.clear()
        for (i in paradero_inicio + 1 until paraderosList.size) {
            listParaderoFin.add(paraderosList[i].paradero)
        }
        binding.spFin.adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_fin,
            R.id.txtName,
            listParaderoFin
        )
    }
    // tipoUsuariosList = TipoUsuario.listAll(TipoUsuario.class, "remoto");

    //setAdapter
    private fun getUsuariosSQLite() {
        lisUsuarios.clear()

        // tipoUsuariosList = TipoUsuario.listAll(TipoUsuario.class, "remoto");
        tipoUsuariosList = Select.from(TipoUsuario::class.java).orderBy("nombre")
            .list() as ArrayList<TipoUsuario>
        val firstElement: TipoUsuario = tipoUsuariosList.removeAt(tipoUsuariosList.size - 1)
        tipoUsuariosList.add(0, firstElement)
        for (tipoUsuario in tipoUsuariosList) {
            lisUsuarios.add(tipoUsuario.nombre)
        }

        //setAdapter
        binding.spUsuarios.adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_tipo_pasajero,
            R.id.txtName,
            lisUsuarios
        )
    }

    private fun getPrecioSQLite(
        id_paradero_inicio: Int,
        id_paradero_fin: Int,
        id_tipo_usuario: Int
    ): Double {
        val tarifaParaderos = TarifaParadero.find(
            TarifaParadero::class.java,
            "parada_inicio = ? AND parada_fin = ? AND tipo_usuario = ?", "" + id_paradero_inicio,
            "" + id_paradero_fin, "" + id_tipo_usuario
        )
        val precio = tarifaParaderos[0].monto.toDouble()
        sizeTarifas = tarifaParaderos.size
        return precio
    }

    private fun goIntentMain() {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MainActivity.BACK, true)
        startActivity(intent)
        finish()
    }

    override fun isLoading(state: Boolean) {
        showProgress(state)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                // There are no request codes  -> val data: Intent? = result.data
                countPasajes = 1
                binding.textCount.text = "" + countPasajes
                formatPrecio()
            }
        }

    override fun onFinishPrint() {}

    companion object {
        const val ID_RUTA = "ID"
        const val ID_RUTA_DISPONIBLE = "ID_RUTA_DISPONIBLE"
        const val ID_VEHICULO = "ID_VEHICULO"
        const val ID_HORARIO = "ID_HORARIO"
        const val HORARIO = "HORARIO"
        const val INFO = "INFO"
        val TAG = SelectRutas::class.java.simpleName
    }
}