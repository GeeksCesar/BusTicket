package com.smartgeeks.busticket.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.smartgeeks.busticket.core.Resource
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
}