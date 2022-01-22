package com.smartgeeks.busticket.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

private val TAG: String = VehicleViewModel::class.java.simpleName

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    fun getOccupiedSeats(availableRouteId: Int, hour: String) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            emit(Resource.Success(vehicleRepository.getOccupiedSeats(availableRouteId, hour)))
        } catch (e: Exception) {
            Log.e(TAG, "getOccupiedSeats: ${e.message}")
            emit(Resource.Failure(e))
        }
    }

    fun getVehicleInfo(vehicleId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            emit(Resource.Success(vehicleRepository.getVehicleInfo(vehicleId)))
        } catch (e: Exception) {
            Log.e(TAG, "getVehicleInfo: ${e.message}")
            emit(Resource.Failure(e))
        }
    }
}