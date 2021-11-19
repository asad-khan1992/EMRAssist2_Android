package com.emrassist.audio.ui.activity.otp_verification_activity.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.forgot_password.repository.ForgotPasswordRepository
import com.emrassist.audio.ui.activity.forgot_password.request_model.ForgotPasswordRequestModel
import com.emrassist.audio.ui.activity.login_with_number.repository.LoginUsingNumberRepository
import com.emrassist.audio.ui.activity.otp_verification_activity.request_model.OtpVerificationRequestModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpVerificationViewModel @Inject constructor(
    var repository: LoginUsingNumberRepository,
    var repositoryForgotPasswordRepository: ForgotPasswordRepository
) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<Any>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<Any>>>
        get() = _datastate

    private val _verifyOtpDataState: MutableLiveData<DataState<ApiResponse<Any>>> =
        MutableLiveData()
    val verifyOtpDataState: LiveData<DataState<ApiResponse<Any>>>
        get() = _verifyOtpDataState

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            repositoryForgotPasswordRepository.forgotPassword(ForgotPasswordRequestModel(email))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }

    }

    fun validateFields(otp: String): Int {
        return if (otp.isEmpty())
            R.string.alert_enter_otp
        else -1
    }

    fun verifyPasswordOtp(email: String, otp: String) {
        viewModelScope.launch {
            repository.verifyPasswordOtp(OtpVerificationRequestModel(email, otp)).onEach { data ->
                _verifyOtpDataState.value = data
            }
                .launchIn(viewModelScope)
        }
    }

}