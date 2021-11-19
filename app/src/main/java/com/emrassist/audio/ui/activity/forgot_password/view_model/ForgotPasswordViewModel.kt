package com.emrassist.audio.ui.activity.forgot_password.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.forgot_password.repository.ForgotPasswordRepository
import com.emrassist.audio.ui.activity.forgot_password.request_model.ForgotPasswordRequestModel
import com.emrassist.audio.utils.ValidationOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(val repository: ForgotPasswordRepository) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<Any>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<Any>>>
        get() = _datastate

    fun isEmailValid(email: String): Int {
        return ValidationOP.isEmailValid(email)
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            repository.forgotPassword(ForgotPasswordRequestModel(email))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }
    }
}

