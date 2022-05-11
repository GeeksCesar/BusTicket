package com.smartgeeks.busticket.menu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.databinding.MenuInicioBinding
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.InternetCheck
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.hide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Inicio : Fragment(R.layout.menu_inicio) {

    var activity: MainActivity? = null
    var nameUsuario: String? = null

    private lateinit var binding: MenuInicioBinding
    private val ticketViewModel: TicketViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MenuInicioBinding.bind(view)

        initViews()
        handleShowSyncButton()
    }

    private fun handleShowSyncButton() {
        ticketViewModel.getTickets().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    binding.progresBar.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.btnSync.visibility = View.GONE
                        binding.tvTicketToSync.visibility = View.GONE
                    } else {
                        binding.tvTicketToSync.text = "${result.data.size} Tickets por sincronizar"
                        binding.tvTicketToSync.visibility = View.VISIBLE
                        binding.btnSync.visibility = View.VISIBLE
                    }
                }
                is Resource.Failure -> {
                    binding.progresBar.visibility = View.GONE
                    binding.btnSync.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initViews() = with(binding) {

        if (UsuarioPreferences.getInstance(context).roleVenta == "conductor")
            contentInterUrbanos.hide()

        nameUsuario = UsuarioPreferences.getInstance(getActivity()).nombre
        activity = requireActivity() as MainActivity
        btnNameUsuario.text = nameUsuario

        contentInterStops.setOnClickListener {
            activity?.setFragment(2)
        }

        contentInterUrbanos.setOnClickListener {
            activity?.setFragment(3)
        }

        btnNameUsuario.setOnClickListener {
            activity?.setFragment(0)
        }
        btnSync.setOnClickListener {
            InternetCheck { internet ->
                if (internet) {
                    remoteSync()
                } else {
                    DialogAlert.showDialogFailed(
                        requireContext(), "Error",
                        "No hay conexión a internet.", SweetAlertDialog.WARNING_TYPE
                    )
                }
            }.execute()
        }

    }

    /**
     * Ejecutar el servicio de Sincronización Remota
     */
    private fun remoteSync() {
        ticketViewModel.syncTickets().observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Failure -> {
                    handleShowSyncButton()
                }
                is Resource.Loading -> {
                    // Oculto el botón y muestro la barra de progreso
                    binding.progresBar.visibility = View.VISIBLE
                    binding.btnSync.visibility = View.GONE
                }
                is Resource.Success -> {
                    if (result.data.estado == 1) {
                        Toast.makeText(requireContext(), "Datos sincronizados", Toast.LENGTH_SHORT)
                            .show()
                    }
                    handleShowSyncButton()
                }
            }
        }
    }

    companion object {
        val TAG = Inicio::class.java.simpleName
    }
}