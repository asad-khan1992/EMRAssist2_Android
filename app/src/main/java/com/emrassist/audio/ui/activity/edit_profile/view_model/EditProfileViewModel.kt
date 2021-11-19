package com.emrassist.audio.ui.activity.edit_profile.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.edit_profile.repository.EditProfileRepository
import com.emrassist.audio.ui.activity.edit_profile.requestmodel.EditProfileRequestModel
import com.emrassist.audio.utils.ValidationOP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(val repository: EditProfileRepository) :
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

    fun updateProfile(
        firstName: String,
        lastName: String,
        userId: String,
        number: String
    ) {

        viewModelScope.launch {
            repository.updateProfile(EditProfileRequestModel(firstName, lastName, userId, number))
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

