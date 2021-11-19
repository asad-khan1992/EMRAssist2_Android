package com.emrassist.audio.ui.activity.main.repository

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.main.request_model.LogoutRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class MainActivityRepository(var apiClient: ApiClient) {
    fun logout(model: LogoutRequestModel): Flow<DataState<ApiResponse<Any>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.logout(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}