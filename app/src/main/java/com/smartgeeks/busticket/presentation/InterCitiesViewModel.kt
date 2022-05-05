package com.smartgeeks.busticket.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.repository.intercities.InterCitiesRepository
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.Utilities.toString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

private val TAG: String = InterCitiesViewModel::class.java.simpleName

@HiltViewModel
class InterCitiesViewModel @Inject constructor(
    private val interCitiesRepository: InterCitiesRepository
) : ViewModel() {

    fun getRoutesInterCities(companyId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            val result = interCitiesRepository.getRoutesInterCities(companyId)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getStopBusInterCities(routeId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            val result = interCitiesRepository.getStopBusInterCities(routeId)
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getPriceByDate(
        departureId: Int,
        arrivalId: Int,
        date: String,
        hour: String,
    ) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {

            val formattedDate =
                Utilities.stringToDate(date, "dd/mm/yyyy")?.toString("yyyy-mm-dd") ?: date

            val response =
                interCitiesRepository.getPriceAndHours(departureId, arrivalId, formattedDate, hour)
            Log.e(TAG, "getPriceByDate: $response")
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(TAG, "getPriceByDate: ${e.message}")
            emit(Resource.Failure(e))
        }
    }

    fun getHoursIntercities(
        departureId: Int,
        arrivalId: Int,
        date: String
    ) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {

            val formattedDate =
                Utilities.stringToDate(date, "dd/mm/yyyy")?.toString("yyyy-mm-dd") ?: date

            val response =
                interCitiesRepository.getHoursIntercities(departureId, arrivalId, formattedDate)
            Log.e(TAG, "getHoursIntercities: $response")
            emit(Resource.Success(response))
        } catch (e: Exception) {
            Log.e(TAG, "getHoursIntercities: ${e.message}")
            emit(Resource.Failure(e))
        }
    }
}