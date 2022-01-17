package com.smartgeeks.busticket.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.data.auth.RequestSessionLogs
import com.smartgeeks.busticket.repository.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

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

    fun checkLockedDevice(userID: Int, deviceID : String) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            emit(Resource.Success(authRepository.checkLockedDevice(userID, deviceID)))
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

    fun setUserStatus(userID: Int, deviceID: String, status : Int) = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            emit(Resource.Success(authRepository.setUserStatus(
                userID, deviceID, status
            )))
        } catch (e: Exception) {
            emit(Resource.Failure(e))
        }
    }
}