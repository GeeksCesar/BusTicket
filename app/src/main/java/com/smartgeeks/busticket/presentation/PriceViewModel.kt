package com.smartgeeks.busticket.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.repository.price.PriceRepository
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.Utilities.toString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

private val TAG: String = VehicleViewModel::class.java.simpleName

@HiltViewModel
class PriceViewModel @Inject constructor(
    private val priceRepository: PriceRepository
) : ViewModel() {

    fun getPriceByDate(
        departureId: Int,
        arrivalId: Int,
        passengerType: Int,
        date: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {

            val formattedDate = Utilities.stringToDate(date, "dd/mm/yyyy")?.toString("yyyy-mm-dd") ?: date

            val response =
                priceRepository.getPriceByDate(departureId, arrivalId, passengerType, formattedDate)
            Log.e(TAG, "getPriceByDate: $response")
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(TAG, "getPriceByDate: ${e.message}")
            emit(Resource.Failure(e))
        }
    }
}