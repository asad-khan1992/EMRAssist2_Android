package com.emrassist.audio.ui.activity.edit_profile.repository

import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.edit_profile.requestmodel.EditProfileRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class EditProfileRepository(var apiClient: ApiClient) {

    fun updateProfile(model: EditProfileRequestModel): Flow<DataState<ApiResponse<UserModel>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.updateProfile(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}