package com.emrassist.audio.ui.activity.reset_password.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.reset_password.repository.ResetPasswordRepository
import com.emrassist.audio.ui.activity.reset_password.request_model.RequestModelRequestModel
import com.emrassist.audio.utils.ValidationOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(val repository: ResetPasswordRepository) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<Any>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<Any>>>
        get() = _datastate

    fun isEmailValid(email: String): Int {
        return ValidationOP.isEmailValid(email)
    }

    fun resetPassword(email: String, otp: String, password: String) {
        viewModelScope.launch {
            repository.resetPassword(RequestModelRequestModel(email, otp, password))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }
    }

    fun isPasswordValid(password: String): Int {
        return ValidationOP.isPasswordValid(password)
    }

    fun isConfirmPasswordValid(password: String, confirmPassword: String): Int {
        return ValidationOP.isPasswordMatches(password, confirmPassword)
    }
}

