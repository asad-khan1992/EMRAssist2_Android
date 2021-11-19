package com.emrassist.audio.ui.activity.register.repository

import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.register.requestmodel.RegisterRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class RegisterRepository(var apiClient: ApiClient) {

    fun register(model: RegisterRequestModel): Flow<DataState<ApiResponse<UserModel>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.register(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}