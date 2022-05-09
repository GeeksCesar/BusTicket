package com.smartgeeks.busticket.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.intercities.RoutesIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import com.smartgeeks.busticket.databinding.FragmentIntercityBinding
import com.smartgeeks.busticket.domain.models.PriceByDate
import com.smartgeeks.busticket.presentation.InterCitiesViewModel
import com.smartgeeks.busticket.presentation.ui.dialogs.DatePickerDialog
import com.smartgeeks.busticket.presentation.ui.dialogs.DialogSingleChoice
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.Utilities.formatCurrency
import com.smartgeeks.busticket.utils.Utilities.formatDate
import com.smartgeeks.busticket.utils.hide
import com.smartgeeks.busticket.utils.visible
import dagger.hilt.android.AndroidEntryPoint

private val TAG: String = IntercityFragment::class.java.simpleName

@AndroidEntryPoint
class IntercityFragment : Fragment(R.layout.fragment_intercity) {

    private lateinit var binding: FragmentIntercityBinding
    private val interCitiesViewModel: InterCitiesViewModel by viewModels()

    var routes: List<RoutesIntercityResponse> = emptyList()
    var stopBusList: List<StopBusResponse> = emptyList()

    // Data selected
    var serviceRoute: RoutesIntercityResponse? = null
    var departureId: Int = 0
    var arrivalId: Int = 0
    private var date = ""

    private var dataPriceTicket: PriceByDate? = null
    private var hourSelected: HoursResponse? = null
    private var price = 0
    private var quantity = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentIntercityBinding.bind(view)

        initViews()
        observeRoutes()
        setupDateSelector()
        setupButtons()
    }

    private fun initViews() = with(binding) {

        spRoute.apply {
            adapter = ArrayAdapter(
                requireContext(),
                R.layout.custom_spinner_rutas,
                R.id.txtName,
                listOf("Seleccione una ruta")
            )
        }

        spBus.apply {
            adapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_placa,
                    R.id.txtName,
                    listOf("Bus")
                )
        }

        spDeparture.apply {
            adapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_inicio,
                    R.id.txtName,
                    listOf("Seleccione paradero Inicio")
                )
        }

        spArrival.apply {
            adapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_fin,
                    R.id.txtName,
                    listOf("Seleccione paradero Destino")
                )
        }

    }

    private fun setupButtons() = with(binding) {
        btnRestar.setOnClickListener {

            if (quantity == 1)
                return@setOnClickListener

            quantity--
            setupPrice()
            textCount.text = quantity.toString()
        }

        btnSumar.setOnClickListener {
            quantity++
            setupPrice()
            textCount.text = quantity.toString()
        }

        removeDateOneWay.setOnClickListener {
            date = ""
            dataPriceTicket = null
            tvOneWay.text = getString(R.string.one_way)
            tvHourOneDay.text = ""
            it.hide()
            btnNext.hide()

            price = 0
            setupPrice()
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

    private fun getHoursInterCities(date: String) {
        interCitiesViewModel.getHoursIntercities(departureId, arrivalId, date)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Failure -> {
                        binding.progressOneWay.hide()
                        Toast.makeText(requireContext(), "${result.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Resource.Loading -> {
                        binding.progressOneWay.visible()
                    }
                    is Resource.Success -> {
                        binding.progressOneWay.hide()

                        val data = result.data
                        setupHours(data)
                    }
                }
            }
    }

    private fun getPriceByHour(date: String, hour : String) {
        interCitiesViewModel.getPriceByDate(departureId, arrivalId, date, hour)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Failure -> {
                        binding.progressOneWay.hide()
                        Toast.makeText(requireContext(), "${result.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Resource.Loading -> {
                        binding.progressOneWay.visible()
                    }
                    is Resource.Success -> {
                        binding.progressOneWay.hide()

                        val data = result.data
                        setupHoursAndPrice(data)
                    }
                }
            }
    }

    private fun setupHours(data: List<HoursResponse>) = with(binding) {
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

            hourSelected = selected
            getPriceByHour(date, selected.horario)
        }
        dialog.show(parentFragmentManager, "Hours")

        tvHourOneDay.setOnClickListener {
            dialog.show(parentFragmentManager, "Hours")
        }
    }

    private fun setupHoursAndPrice(data: List<PriceByDate>) = with(binding) {
        val items = data.mapIndexed { index, priceByDate ->
            DialogSingleChoice.SingleItem(
                index,
                "${priceByDate.pasajero} \t\t-\t\t ${priceByDate.tarifa.formatCurrency()}",
                false
            )
        }
        val dialog = DialogSingleChoice(
            "Horario ${hourSelected?.horario} \n${date.formatDate(outputFormat = "EEE, MMM d")}",
            items = items
        )

        dialog.setOnItemClick {
            val selected = data[it.id]
            Log.e(SelectRutas.TAG, "getPriceTicketOneWay: $selected")
            tvHourOneDay.text = selected.horario

            dataPriceTicket = selected
            price = selected.tarifa.toInt()
            setupPrice()

        }
        dialog.show(parentFragmentManager, "Price")

        /*tvHourOneDay.setOnClickListener {
            dialog.show(parentFragmentManager, "Price")
        }*/

        if (data.isNotEmpty()) {
            btnNext.visible()
            removeDateOneWay.visible()
        } else {
            resetData()
            btnNext.hide()
        }
    }

    private fun setupPrice() = with(binding) {
        val totalPrice = price * quantity
        tvPrecio.text = totalPrice.toString().formatCurrency()

        handleConfirmButton()
    }

    private fun handleConfirmButton() = with(binding) {
        btnNext.setOnClickListener {
            if (serviceRoute == null || price == 0)
                return@setOnClickListener
            startSelectSillasActivity()
        }

    }

    private fun observeRoutes() = with(binding) {

        interCitiesViewModel.getRoutesInterCities(UsuarioPreferences.getInstance(requireContext()).idEmpresa)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Resource.Failure -> {
                        Toast.makeText(requireContext(), "${result.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Resource.Loading -> Unit
                    is Resource.Success -> {

                        if (result.data.isEmpty()) {
                            SweetAlertDialog(context).setTitleText("No hay rutas")
                                .setContentText("No se han definido rutas").show()
                        } else {
                            routes = result.data
                            val dataSpinner = routes.map { "${it.inicio} - ${it.termino}" }

                            spRoute.adapter = ArrayAdapter(
                                requireContext(),
                                R.layout.custom_spinner_rutas,
                                R.id.txtName,
                                dataSpinner
                            )

                            handleListenerSpRouter()
                        }
                    }
                }
            }
    }

    private fun observeStopBus(routeId: Int) {
        interCitiesViewModel.getStopBusInterCities(routeId).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "${result.exception}", Toast.LENGTH_SHORT)
                        .show()
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val data = result.data
                    stopBusList = data

                    if (data.isNotEmpty()) {
                        setupDataBusStopSpinners(data)
                    }
                }
            }
        }
    }

    private fun setupDataBusStopSpinners(data: List<StopBusResponse>) = with(binding) {

        val departureList = data.filter { it.tipo == "I" }.map { it.paradero }

        spDeparture.apply {
            adapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_inicio,
                    R.id.txtName,
                    departureList
                )
        }

        handleListenerDepartureSpinner(data)
    }

    private fun setupBus() = with(binding) {
        serviceRoute?.let {
            spBus.apply {
                adapter =
                    ArrayAdapter(
                        requireContext(),
                        R.layout.custom_spinner_placa,
                        R.id.txtName,
                        listOf(it.vehiculo)
                    )
            }
        }
    }

    private fun handleListenerSpRouter() = with(binding) {
        spRoute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                serviceRoute = routes[position]
                observeStopBus(routes[position].ruta)
                setupBus()
                resetData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun resetData() {
        dataPriceTicket = null
        price = 0
        binding.tvHourOneDay.text = ""
        setupPrice()
    }

    private fun handleListenerDepartureSpinner(data: List<StopBusResponse>) = with(binding) {
        spDeparture.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                departureId = data.find { it.paradero == spDeparture.selectedItem }?.idParadero ?: 0
                Log.e(TAG, "onItemSelected: ${spDeparture.selectedItem} $departureId")

                setupDataArrivalSpinner(data)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun setupDataArrivalSpinner(data: List<StopBusResponse>) = with(binding) {
        val arrivalList = data.filter { it.tipo == "D" && it.idParadero != departureId }.map { it.paradero }

        spArrival.apply {
            adapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.custom_spinner_fin,
                    R.id.txtName,
                    arrivalList
                )
        }

        spArrival.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                arrivalId = data.find { it.paradero == spArrival.selectedItem }?.idParadero ?: 0
                Log.e(TAG, "onItemSelected: ${spArrival.selectedItem} $arrivalId")

                // setDefaultPrice(data)
                if (date.isNotEmpty())
                    getHoursInterCities(date)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setDefaultPrice(data: List<StopBusResponse>) {
        /**
         * Get default date
         */
        if (data.isNotEmpty()) {
            val date = Utilities.getDate("dd/MM/yyyy")
            getHoursInterCities(date)
            binding.tvOneWay.text = date
        }
    }

    private fun startSelectSillasActivity() = with(binding) {

        Log.e(TAG, "startSelectSillasActivity: $serviceRoute")
        Log.e(TAG, "startSelectSillasActivity: $dataPriceTicket")

        val totalPrice = price * quantity
        val info = serviceRoute?.vehiculo + "," + spRoute.selectedItem + "," + dataPriceTicket?.horario

        val intent = Intent(context, SelectSillas::class.java)
        intent.putExtra(SelectSillas.CANT_PUESTOS, quantity)
        intent.putExtra(SelectSillas.PRECIO_PASAJE, totalPrice)
        intent.putExtra(SelectSillas.ID_VEHICULO, serviceRoute?.IdVehiculo)
        intent.putExtra(SelectSillas.ID_RUTA, serviceRoute?.ruta)
        intent.putExtra(SelectSillas.ID_RUTA_DISPONIBLE, serviceRoute?.ruta)
        intent.putExtra(SelectSillas.ID_HORARIO, 0)
        intent.putExtra(SelectSillas.HORARIO, dataPriceTicket?.horario)
        intent.putExtra(SelectSillas.ID_PARADERO_INICIO, departureId)
        intent.putExtra(SelectSillas.ID_PARADERO_FIN, arrivalId)
        intent.putExtra(SelectSillas.TIPO_USUARIO, dataPriceTicket?.idTipoPasajero)
        intent.putExtra(SelectSillas.NAME_TIPO_PASAJERO, dataPriceTicket?.idTipoPasajero)
        intent.putExtra(
            SelectRutas.INFO,
            "$info,${spDeparture.selectedItem},${spArrival.selectedItem}"
        )

        val saleByDate = dataPriceTicket != null
        intent.putExtra(SelectSillas.SALE_BY_DATE, saleByDate)
        intent.putExtra(SelectSillas.TICKET_ONE_WAY, dataPriceTicket)
        intent.putExtra(SelectSillas.SERVICE_ID, serviceRoute?.id)

        startActivity(intent)
    }
}