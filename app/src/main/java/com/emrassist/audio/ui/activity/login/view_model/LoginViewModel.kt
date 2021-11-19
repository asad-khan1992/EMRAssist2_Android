package com.emrassist.audio.ui.activity.login.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.login.repository.LoginRepository
import com.emrassist.audio.ui.activity.login.requestmodel.LoginRequestModel
import com.emrassist.audio.utils.ValidationOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(val repository: LoginRepository) :
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

    fun login(email: String, password: String) {

        viewModelScope.launch {
            repository.login(LoginRequestModel(email, password))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }
    }
}

