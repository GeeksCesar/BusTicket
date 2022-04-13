package com.smartgeeks.busticket.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smartgeeks.busticket.core.AppPreferences
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.models.auth.RequestSessionLogs
import com.smartgeeks.busticket.repository.auth.AuthRepository
import com.smartgeeks.busticket.utils.InternetChecker.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG: String = AuthViewModel::class.java.simpleName

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun userLogin(email: String, password: String) = liveData(Dispatchers.IO) {

        emit(Resource.Loading())

        try {
            emit(Resource.Success(authRepository.userLogin(email, password)))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }

    }

    fun setLoginLogs(
        userID: Int,
        deviceID: String,
        latLong: String
    ) = liveData(Dispatchers.IO) {

        emit(Resource.Loading())

        try {
            emit(
                Resource.Success(
                    authRepository.setLoginLogs(
                        RequestSessionLogs(userID, deviceID, latLong)
                    )
                )
            )
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }

    }

    fun checkLockedDevice(userID: Int, deviceID: String) = liveData(Dispatchers.IO) {

        emit(Resource.Loading())

        /*if (!isInternetAvailable()) {
            emit(Resource.Success(false))
            return@liveData
        }*/

        try {
            // Save result on preferences
            val result = authRepository.checkLockedDevice(userID, deviceID)
            AppPreferences.isLockedDevice = result

            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun getMessageCompany(companyId: Int) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            emit(Resource.Success(authRepository.getMessageCompany(companyId)))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }

    fun setUserStatus(userID: Int, deviceID: String, status: Int) = viewModelScope.launch {
        try {
            authRepository.setUserStatus(userID, deviceID, status)
        } catch (e: Exception) {
            Log.e(TAG, "setUserStatus: ${e.message}")
        }
    }
}