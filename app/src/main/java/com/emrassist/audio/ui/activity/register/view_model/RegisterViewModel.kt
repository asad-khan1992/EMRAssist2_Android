package com.emrassist.audio.ui.activity.register.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.register.repository.RegisterRepository
import com.emrassist.audio.ui.activity.register.requestmodel.RegisterRequestModel
import com.emrassist.audio.utils.ValidationOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(val repository: RegisterRepository) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<UserModel>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<UserModel>>>
        get() = _datastate

    fun isEmailValid(email: String): Int {
        return ValidationOP.isEmailValid(email)
    }

    fun isPasswordValid(password: String): Int {
        return ValidationOP.isPasswordValid(password)
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        number: String,
        password: String
    ) {

        viewModelScope.launch {
            repository.register(RegisterRequestModel(firstName, lastName, email, password, number))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }
    }

    fun isNameValid(firstName: String): Int {
        return ValidationOP.isNameValid(firstName)
    }

    fun isPhoneNumberValid(number: String): Int {
        return ValidationOP.isPhoneNumberValid(number)
    }

    fun isConfirmPasswordValid(password: String, confirmPassword: String): Int {
        return ValidationOP.isPasswordMatches(password, confirmPassword)
    }
}

