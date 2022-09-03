package com.smartgeeks.busticket.menu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.snackbar.Snackbar
import com.smartgeeks.busticket.MainActivity
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.ticket.RefundTicketPayload
import com.smartgeeks.busticket.data.models.ticket.Ticket
import com.smartgeeks.busticket.data.models.ticket.UpdateTicketPayload
import com.smartgeeks.busticket.databinding.FragmentUpdateTicketBinding
import com.smartgeeks.busticket.domain.models.PriceByDate
import com.smartgeeks.busticket.presentation.InterCitiesViewModel
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.presentation.ui.dialogs.DatePickerDialog
import com.smartgeeks.busticket.presentation.ui.dialogs.DialogSingleChoice
import com.smartgeeks.busticket.utils.PrintTicket
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.Utilities.formatCurrency
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
    private var ticketDateRegistered: String = ""

    private var seatsToModify = mutableListOf<String>()
    private var priceTicketIndividual = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentUpdateTicketBinding.bind(view)

        setupDateSelector()
        handleSearch()
        handleSelectChair()
        handleRefundTicket()
    }

    private fun handleSearch() = with(binding) {

        // Observe when is writing in search field and put - each 6 characters
        etVoucher.addTextChangedListener(object : TextWatcher {
            var lengthText = 0
            var second = 0

            override fun afterTextChanged(s: Editable?) {
                second = lengthText
                lengthText = s?.length ?: 0
                if ((s?.length == 6 || s?.length == 13) && lengthText > second) {
                    s.append("-")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSearchTicket.setOnClickListener {

            if (!validateFields())
                return@setOnClickListener

            (activity as MainActivity).hideKeyboard()

            val voucher = etVoucher.text.toString().trim()
            observeSearch(voucher)
            etVoucher.setText(voucher)
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
                        btnSelectChair.isVisible = true
                        btnRefundTicket.isVisible = true
                        selectorOneWay.isVisible = true
                        tvTitleModifySeats.isVisible = true

                        ticket?.let { ticket ->
                            ticketDateRegistered = ticket.fecha
                            priceTicketIndividual =
                                (ticket.totalPagar.toDouble() / ticket.cantPasajes.toInt()).toInt()
                            showTicketInfo(ticket)
                            setDateData(ticket)
                            getHoursInterCities(
                                ticket.fecha.formatDate("yyyy-MM-dd", "dd/MM/yyyy"),
                                false
                            )
                            setSeatsToModify(ticket.sillas)
                        }
                    } else {
                        Snackbar.make(root, result.data.mensaje, Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }

        }
    }

    private fun setSeatsToModify(seats: String) {
        val seatsArray = seats.split(",")
        // Clear view
        binding.containerSelectSeats.removeAllViews()
        seatsToModify.clear()

        // Create a list of CheckBoxes and put them in the LinearLayout containerSelectSeats
        for (seat in seatsArray) {
            val checkBox = CheckBox(context)
            checkBox.text = seat

            // Set wrap_content to the width and height of the CheckBox
            checkBox.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add margin horizontally to the checkbox on dp
            val marginValue = Utilities.dpToPx(binding.root.context, 10)
            checkBox.setPadding(marginValue, 0, marginValue, 0)

            // Add event listener to the CheckBox
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Save the seat in the list of seats to modify
                    seatsToModify.add(seat)
                } else {
                    seatsToModify.remove(seat)
                }
            }

            binding.containerSelectSeats.addView(checkBox)
        }

        // Set center_horizontal gravity to the LinearLayout containerSelectSeats
        binding.containerSelectSeats.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun handleRefundTicket() {

        binding.btnRefundTicket.setOnClickListener {

            if (!validateFieldsToRefund())
                return@setOnClickListener

            // Calculate the total price to refund
            val totalPriceToRefund = priceTicketIndividual * seatsToModify.size
            val priceRetentionFormatted = ((totalPriceToRefund * 0.15).toInt()).formatCurrency()
            val priceToRefundFormatted = ((totalPriceToRefund * 0.85).toInt()).formatCurrency()

            SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Está seguro de que desea reembolsar el ticket?")
                .setContentText("El ticket será reembolsado y no se podrá volver a utilizar. " +
                    "\n\nSe retendra el 15% del valor del ticket" +
                    " equivalente a $priceRetentionFormatted" +
                    "\n\nDevolución de $priceToRefundFormatted")
                .setConfirmText("Confirmar")
                .setCancelText("Volver")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    refundTicket(seatsToModify)
                }
                .show()
        }
    }

    private fun validateFieldsToRefund() : Boolean {
        if (seatsToModify.isEmpty()) {
            Snackbar.make(binding.root, "Seleccione las sillas a devolver", Snackbar.LENGTH_LONG)
                .show()
            return false
        }
        if (priceTicketIndividual == 0) {
            Snackbar.make(binding.root, "No se puede devolver el boleto", Snackbar.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun refundTicket(seatsToRefund: MutableList<String>) = with(binding) {

        ticket?.let {

            val seatsToUpdate = it.sillas.split(",").toMutableList()

            // Remove the seats to refund from the list of seats to modify
            seatsToRefund.forEach { seat ->
                seatsToUpdate.remove(seat)
            }

            val refundTicketPayload = RefundTicketPayload(
                id = it.id.toLong(),
                numVoucher = it.numVoucher,
                seatsToRefund = seatsToRefund.joinToString(","),
                seatsToMaintain = seatsToUpdate.joinToString(","),
            )

            viewModel.refundTicket(refundTicketPayload).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Failure -> {
                        progressBar.isVisible = false
                    }
                    is Resource.Loading -> {
                        progressBar.isVisible = true
                    }
                    is Resource.Success -> {
                        progressBar.isVisible = false
                        Snackbar.make(root, result.data.mensaje, Snackbar.LENGTH_LONG)
                            .show()

                        ticket = null
                        btnSelectChair.isVisible = false
                        btnRefundTicket.isVisible = false

                        // Set Fragment Inicio
                        (activity as MainActivity).setFragment(1)
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
            <b>Asiento:</b> ${ticket.sillas}<br/>
        """.trimIndent()
        binding.tvTicketInfo.text =
            HtmlCompat.fromHtml(infoTicket, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setDateData(ticket: Ticket) = with(binding) {
        tvOneWay.text = ticket.fecha.formatDate("yyyy-MM-dd", "dd/MM/yyyy")
        tvHourOneDay.text = ticket.horaSalida
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
                ticket?.fecha = date.formatDate()

                getHoursInterCities(date)

            }.show(parentFragmentManager, "datePicker")
        }
    }

    private fun getHoursInterCities(date: String, showDialog: Boolean = true) {

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
            ticket?.horaSalida = selected.horario
        }

        if (showDialog) dialog.show(parentFragmentManager, "Hours")

        tvHourOneDay.setOnClickListener {
            dialog.show(parentFragmentManager, "Hours")
        }
    }

    private fun updateTicket() {
        ticket?.let { ticket ->
            val ticketPayload = UpdateTicketPayload(
                numVoucher = ticket.numVoucher,
                horaSalida = binding.tvHourOneDay.text.toString(),
                fecha = binding.tvOneWay.text.toString().formatDate(),
                sillas = ticket.sillas,
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
                            binding.btnSelectChair.isVisible = false

                            //ticket.horaSalida = binding.tvHourOneDay.text.toString()
                            //ticket.fecha = binding.tvOneWay.text.toString().formatDate()

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

    private fun handleSelectChair() {
        binding.btnSelectChair.setOnClickListener {

            // Open Select Chair Activity and wait for result
            // Caller
            ticket?.let { ticket ->

                val intent = Intent(requireContext(), SelectSillas::class.java)

                Log.e(TAG, "$ticket")

                intent.putExtra(SelectSillas.CANT_PUESTOS, ticket.cantPasajes.toInt())
                intent.putExtra(SelectSillas.PRECIO_PASAJE, priceTicketIndividual)
                intent.putExtra(SelectSillas.ID_VEHICULO, ticket.idVehiculo.toInt())
                intent.putExtra(SelectSillas.ID_RUTA, ticket.idRutaDisponible.toInt())
                intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, ticket.idRutaDisponible.toInt())
                intent.putExtra(SelectSillas.ID_HORARIO, 0)
                intent.putExtra(SelectSillas.HORARIO, ticket.horaSalida)
                intent.putExtra(SelectSillas.ID_PARADERO_INICIO, ticket.paradaInicio)
                intent.putExtra(SelectSillas.ID_PARADERO_FIN, ticket.paradaDestino)
                intent.putExtra(SelectSillas.TIPO_USUARIO, ticket.tipoUsuario)
                intent.putExtra(SelectSillas.NAME_TIPO_PASAJERO, ticket.nombreTipoUsuario)
                intent.putExtra(
                    SelectRutas.INFO,
                    "${ticket.placa},${ticket.nombreRuta},${ticket.horaSalida},${ticket.nombreParadaInicio},${ticket.nombreParadaDestino}"
                )
                intent.putExtra(SelectSillas.SERVICE_ID, ticket.idServicio.toInt())

                val ticketOneWay = PriceByDate(
                    horario = ticket.horaSalida,
                    tarifa = ticket.totalPagar,
                    fecha = ticket.fecha,
                )
                intent.putExtra(SelectSillas.SALE_BY_DATE, true)
                intent.putExtra(SelectSillas.TICKET_ONE_WAY, ticketOneWay)
                intent.putExtra(SelectSillas.IS_EDIT_TICKET, true)
                intent.putExtra(SelectSillas.CHAIRS_EDIT_TICKET, ticket.sillas)
                intent.putExtra(SelectSillas.DATE_TICKET_REGISTERED, ticketDateRegistered)

                getResult.launch(intent)

            }

        }
    }

    // Receiver
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                var sillas = it.data?.getStringExtra("listSillas")

                if (sillas != null) {
                    sillas = sillas.replace("-", ",").trim()
                    ticket?.sillas = sillas
                    ticket?.let { data -> showTicketInfo(data) }
                    //binding.btnUpdateTicket.isVisible = true
                    Log.e(TAG, "Result: $sillas")

                    updateTicket()
                    // Clear view
                    binding.containerSelectSeats.removeAllViews()
                    seatsToModify.clear()
                }
            }
        }

    override fun isLoading(state: Boolean) {
    }

    override fun onFinishPrint() {
        (requireActivity() as MainActivity).setFragment(1)
    }
}