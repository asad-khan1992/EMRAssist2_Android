package com.emrassist.audio.ui.activity.forgot_password.repository

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.forgot_password.request_model.ForgotPasswordRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ForgotPasswordRepository(var apiClient: ApiClient) {
    fun forgotPassword(model: ForgotPasswordRequestModel): Flow<DataState<ApiResponse<Any>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.forgotPassword(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}