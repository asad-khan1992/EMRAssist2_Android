package com.emrassist.audio.ui.activity.change_password.repository

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.change_password.request_model.ChangePasswordRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ChangePasswordRepository(var apiClient: ApiClient) {
    fun changePassword(model: ChangePasswordRequestModel): Flow<DataState<ApiResponse<Any>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.changePassword(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}