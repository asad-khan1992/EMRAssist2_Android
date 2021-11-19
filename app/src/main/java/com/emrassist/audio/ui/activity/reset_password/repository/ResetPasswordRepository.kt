package com.emrassist.audio.ui.activity.reset_password.repository

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.reset_password.request_model.RequestModelRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ResetPasswordRepository(var apiClient: ApiClient) {
    fun resetPassword(model: RequestModelRequestModel): Flow<DataState<ApiResponse<Any>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.resetPassword(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}