package com.emrassist.audio.ui.activity.login_with_number.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.login_with_number.repository.LoginUsingNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginUsingNumberViewModel @Inject constructor(var repository: LoginUsingNumberRepository) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<String>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<String>>>
        get() = _datastate

    private val _fieldValidation: MutableLiveData<Int> = MutableLiveData()
    val fieldValidation: LiveData<Int>
        get() = _fieldValidation

    fun verifyNumber(number: String) {
//        viewModelScope.launch {
//            repository.verifyPasswordOtp(number)
//                .onEach { data ->
//                    _datastate.value = data
//                }
//                .launchIn(viewModelScope)
//
//        }

    }

    fun validateFields(selectedCountryCodeWithPlus: String?, number: String) {
        val error: Int = if (selectedCountryCodeWithPlus == null)
            R.string.alert_select_country_code
        else if (number.isEmpty())
            R.string.alert_enter_mobile_number
        else
            -1

        _fieldValidation.value = error


    }
}