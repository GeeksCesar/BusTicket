package com.smartgeeks.busticket.Menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.Modelo.Ticket
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.databinding.MenuInicioBinding
import com.smartgeeks.busticket.sync.SyncServiceRemote
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.Helpers
import com.smartgeeks.busticket.utils.InternetCheck
import com.smartgeeks.busticket.utils.UsuarioPreferences

class Inicio : Fragment(R.layout.menu_inicio) {

    var tickets_to_sync: Long = 0
    var activity: MainActivity? = null
    var nameUsuario: String? = null
    private var state_sync = false

    private lateinit var binding: MenuInicioBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MenuInicioBinding.bind(view)

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_RUN_REMOTE_SYNC)
        filter.addAction(Constantes.EXTRA_PROGRESS)
        filter.addAction(Constantes.ACTION_FINISH_REMOTE_SYNC)

        val receiver = ResponseReceiver()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            receiver, filter
        )

        initViews()
    }

    private fun initViews() = with(binding) {

        nameUsuario = UsuarioPreferences.getInstance(getActivity()).nombre
        btnNameUsuario.text = nameUsuario
        ivLogo.setOnClickListener {
            activity = getActivity() as MainActivity?
            activity!!.setFragment(2)
        }
        btnNameUsuario.setOnClickListener {
            activity = getActivity() as MainActivity?
            activity!!.setFragment(0)
            //new SaveTicketTest().execute();
        }
        btnSync.setOnClickListener { view ->
            InternetCheck { internet ->
                if (internet) {
                    // Oculto el botón y muestro la barra de progreso
                    progresBar.visibility = View.VISIBLE
                    view.visibility = View.GONE
                    remoteSync()
                } else {
                    DialogAlert.showDialogFailed(
                        requireContext(), "Error",
                        "No hay conexión a internet.", SweetAlertDialog.WARNING_TYPE
                    )
                }
            }.execute()
        }
        binding.tvTicketsToSync.setOnClickListener {
            Log.e(TAG, "onClick: " + Helpers.getCurrentDate())
        }
    }

    private fun saveTicketLocal(precio: Double) {
        Log.e(TAG, "Ticket Guardado Localmente")
        val ticket = Ticket()
        ticket.idRemoto = ""
        ticket.paradaInicio = 53
        ticket.paradaDestino = 55
        ticket.idRutaDisponible = 50
        ticket.idOperador = UsuarioPreferences.getInstance(context).idUser
        ticket.horaSalida = "06:30:00"
        ticket.tipoUsuario = 59
        ticket.fecha = Helpers.getCurrentDate()
        ticket.hora = Helpers.getCurrentTime()
        ticket.cantPasajes = 1
        ticket.totalPagar = precio
        ticket.estado = 0
        ticket.pendiente = Constantes.ESTADO_SYNC
        ticket.save()
        // El estado = 0 y estado_sync = 1, para cuando se inicie la sincronización remota
        // se cambie el estado = 1
    }

    override fun onStart() {
        super.onStart()
        LoadTicket2Sync().execute()
    }

    private fun obtenerRegistrosSincronizar() = with(binding) {

        // Si hay registros por sincronizar, muestro los el boton sincronizar
        if (tickets_to_sync > 0) {
            binding.tvTicketsToSync.text = "$tickets_to_sync Tickets por Sincronizar."
            if (!state_sync) btnSync.visibility = View.VISIBLE else btnSync.visibility =
                View.GONE
        } else {
            binding.tvTicketsToSync.text = "No hay Tickets por sincronizar"
            btnSync.visibility = View.GONE
            progresBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Realizar sincronización remota de datos locales
        remoteSync()
    }

    /**
     * Ejecutar el servicio de Sincronización Remota
     */
    private fun remoteSync() {
        if (!state_sync) {
            val sync = Intent(context, SyncServiceRemote::class.java)
            sync.action = Constantes.ACTION_RUN_REMOTE_SYNC
            getActivity()!!.startService(sync)
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_RUN_REMOTE_SYNC -> {
                    state_sync = intent.getBooleanExtra(Constantes.EXTRA_PROGRESS, false)
                    if (state_sync) {
                        binding.progresBar.visibility = View.VISIBLE
                        tickets_to_sync--
                        obtenerRegistrosSincronizar()
                    } else {
                        Log.e(TAG, "Sincronización Remota Finalizada.")
                        binding.progresBar.visibility = View.GONE
                        LoadTicket2Sync().execute()
                    }
                }
                Constantes.ACTION_FINISH_REMOTE_SYNC -> {
                    Log.e(TAG, "Sincronización Remota Finalizada.")
                    binding.progresBar.visibility = View.GONE
                    LoadTicket2Sync().execute()
                }
            }
        }
    }

    private inner class LoadTicket2Sync : AsyncTask<String?, Void?, String>() {

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            obtenerRegistrosSincronizar()
        }

        override fun doInBackground(vararg params: String?): String {
            // Consultar registros por sincronizar
            val values = arrayOf("1", "" + Constantes.ESTADO_SYNC)
            tickets_to_sync = Ticket.count<Any>(
                Ticket::class.java,
                "pendiente = ? AND estado = ?", values
            )
            Log.e(TAG, "Se encontraron $tickets_to_sync registros por Sincronizar.")
            return "Consultando datos"
        }
    }

    /**
     * Clase de pruebas para guardar Tickets
     */
    private inner class SaveTicketTest : AsyncTask<String?, Void?, String>() {
        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
        }

        override fun doInBackground(vararg params: String?): String {
            val precios = doubleArrayOf(2000.0, 1500.0, 1200.0, 800.0)
            // Consultar registros por sincronizar
            var indice = 0
            for (i in 0..51) {
                if (indice > 3) indice = 0
                saveTicketLocal(precios[indice])
                indice++
            }
            return "Consultando datos"
        }
    }

    companion object {
        val TAG = Inicio::class.java.simpleName
    }
}