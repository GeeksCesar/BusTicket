package com.smartgeeks.busticket.menu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.ticket.Ticket
import com.smartgeeks.busticket.data.models.ticket.UpdateTicketPayload
import com.smartgeeks.busticket.databinding.FragmentUpdateTicketBinding
import com.smartgeeks.busticket.domain.models.PriceByDate
import com.smartgeeks.busticket.presentation.InterCitiesViewModel
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.presentation.ui.dialogs.DatePickerDialog
import com.smartgeeks.busticket.presentation.ui.dialogs.DialogSingleChoice
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.Utilities.formatDate
import com.smartgeeks.busticket.utils.hide
import com.smartgeeks.busticket.utils.visible
import dagger.hilt.android.AndroidEntryPoint

private val TAG: String = UpdateTicketFragment::class.java.simpleName

@AndroidEntryPoint
class UpdateTicketFragment : Fragment(R.layout.fragment_update_ticket), PrintTicket.PrintState {

    private lateinit var binding: FragmentUpdateTicketBinding
    private val viewModel: TicketViewModel by viewModels()
    private val interCitiesViewModel: InterCitiesViewModel by viewModels()

    // Data selected
    private var date = ""

    private var dataPriceTicket: PriceByDate? = null
    private var price = 0
    private var quantity = 1

    private var ticket: Ticket? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentUpdateTicketBinding.bind(view)

        setupDateSelector()
        handleSearch()
        handleUpdateTicket()
    }

    private fun handleSearch() = with(binding) {
        btnSearchTicket.setOnClickListener {
            if (!validateFields())
                return@setOnClickListener

            observeSearch(etVoucher.text.toString())
        }
    }

    private fun observeSearch(voucher: String) = with(binding) {
        viewModel.searchTicketByVoucher(voucher).observe(viewLifecycleOwner) { result ->

            when (result) {
                is Resource.Failure -> {
                    progressBar.isVisible = false
                }
                is Resource.Loading -> {
                    progressBar.isVisible = true
                }
                is Resource.Success -> {
                    Log.e(TAG, "observeSearch: ${result.data}")
                    progressBar.isVisible = false

                    if (result.data.estado == 1) {
                        ticket = result.data.ticket

                        ticket?.let { ticket ->
                            showTicketInfo(ticket)
                            setDateData(ticket)
                            getHoursInterCities(ticket.fecha.formatDate("yyyy-MM-dd", "dd/MM/yyyy"), false)
                        }
                    } else {
                        Snackbar.make(root, "No se encontró el voucher", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }

        }
    }

    private fun showTicketInfo(ticket: Ticket) {
        val infoTicket = """
            <b>Empresa:</b> ${ticket.empresa}<br/>
            <b>Vehículo:</b> ${ticket.placa}<br/>
            <b>Ruta:</b> ${ticket.nombreRuta}<br/>
            <b>Inicio:</b> ${ticket.nombreParadaInicio}<br/>
            <b>Fin:</b> ${ticket.nombreParadaDestino}<br/>
            <b>Fecha:</b> ${ticket.fecha}<br/>
            <b>Hora:</b> ${ticket.horaSalida}<br/>
            <b>Tipo pasajero:</b> ${ticket.nombreTipoUsuario}<br/>
            <b>Cantidad pasajes</b> ${ticket.cantPasajes}<br/>
            <b>Sillas:</b> ${ticket.sillas}<br/>
        """.trimIndent()
        binding.tvTicketInfo.text =
            HtmlCompat.fromHtml(infoTicket, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setDateData(ticket: Ticket) = with(binding) {
        tvOneWay.text = ticket.fecha.formatDate("yyyy-MM-dd", "dd/MM/yyyy")
        tvHourOneDay.text = ticket.horaSalida
        handleDateChange()
    }

    private fun validateFields(): Boolean = with(binding) {

        if (etVoucher.text.isNotEmpty())
            return true
        else {
            Snackbar.make(
                requireView(),
                "Debes ingresar el numero de voucher",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }
    }

    private fun setupDateSelector() = with(binding) {

        tvOneWay.setOnClickListener {
            DatePickerDialog(tvOneWay.text.toString()) { day, month, year ->
                date = "$day/$month/$year"
                tvOneWay.text = date
                removeDateOneWay.isVisible = true

                getHoursInterCities(date)

            }.show(parentFragmentManager, "datePicker")
        }
    }

    private fun getHoursInterCities(date: String, showDialog : Boolean = true) {

        ticket?.let { ticket ->
            interCitiesViewModel.getHoursIntercities(
                ticket.paradaInicio.toInt(),
                ticket.paradaDestino.toInt(),
                date
            )
                .observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Resource.Failure -> {
                            binding.progressOneWay.hide()
                            Toast.makeText(
                                requireContext(),
                                "${result.exception}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        is Resource.Loading -> {
                            binding.progressOneWay.visible()
                        }
                        is Resource.Success -> {
                            binding.progressOneWay.hide()

                            val data = result.data
                            setupHours(data, showDialog)
                        }
                    }
                }
        }
    }

    private fun setupHours(data: List<HoursResponse>, showDialog: Boolean) = with(binding) {
        val items = data.mapIndexed { index, hours ->
            DialogSingleChoice.SingleItem(
                index,
                hours.horario,
                false
            )
        }
        val dialog = DialogSingleChoice(
            "Horarios ida\n${date.formatDate(outputFormat = "EEE, MMM d")}",
            items = items
        )

        dialog.setOnItemClick {
            val selected = data[it.id]
            Log.e(SelectRutas.TAG, "getPriceTicketOneWay: $selected")
            tvHourOneDay.text = selected.horario
        }

        if (showDialog) dialog.show(parentFragmentManager, "Hours")

        tvHourOneDay.setOnClickListener {
            dialog.show(parentFragmentManager, "Hours")
        }
    }

    private fun handleDateChange() = with(binding) {
        // If tvOneWay or tvHourOneDay change, then enable button
        val watcherListener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnUpdateTicket.isVisible =
                    tvOneWay.text.isNotEmpty() && tvHourOneDay.text.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        }
        tvOneWay.addTextChangedListener(watcherListener)
        tvHourOneDay.addTextChangedListener(watcherListener)
    }

    private fun handleUpdateTicket() = with(binding) {
        btnUpdateTicket.setOnClickListener {
            updateTicket()
        }
    }

    private fun updateTicket() {
        ticket?.let { ticket ->
            val ticketPayload = UpdateTicketPayload(
                numVoucher = ticket.numVoucher,
                horaSalida = binding.tvHourOneDay.text.toString(),
                fecha = binding.tvOneWay.text.toString().formatDate()
            )
            viewModel.updateTicket(ticketPayload).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            requireView(),
                            "${result.exception}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "updateTicket: ${result.data}")
                        if (result.data.estado == 1) {
                            Snackbar.make(
                                requireView(),
                                "Se actualizó el voucher",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.btnUpdateTicket.isVisible = false

                            ticket.horaSalida = binding.tvHourOneDay.text.toString()
                            ticket.fecha = binding.tvOneWay.text.toString().formatDate()

                            showTicketInfo(ticket)
                            // Print ticket
                            printTicket(ticket)
                        } else {
                            Snackbar.make(
                                requireView(),
                                "No se pudo actualizar el voucher",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun printTicket(ticket: Ticket) {
        // vehiculo,ruta,hora,inicio,fin
        val info =
            "${ticket.placa},${ticket.nombreRuta},${ticket.horaSalida},${ticket.nombreParadaInicio},${ticket.nombreParadaDestino}"

        val printerTicketLibrary = PrintTicket(requireActivity(), this)
        printerTicketLibrary.setData(
            idStartBusStop = ticket.paradaInicio.toInt(),
            idEndBusStop = ticket.paradaDestino.toInt(),
            idEnabledRoute = ticket.idRutaDisponible.toInt(),
            time = ticket.horaSalida,
            idPassengerType = ticket.tipoUsuario.toInt(),
            ticketPrice = ticket.totalPagar.toDouble(),
            idVehicle = ticket.idVehiculo.toInt(),
            passengerType = ticket.nombreTipoUsuario,
            info,
            ticketQuantity = ticket.cantPasajes.toInt(),
            _showHeader = false,
            numVoucher = ticket.numVoucher,
            seats = ticket.sillas,
            isMultiTicket = (ticket.idServicio.toInt() > 0),
            travelDate = ticket.fecha,
        )
        printerTicketLibrary.print()
    }

    override fun isLoading(state: Boolean) {
    }

    override fun onFinishPrint() {
        (requireActivity() as MainActivity).setFragment(1)
    }
}