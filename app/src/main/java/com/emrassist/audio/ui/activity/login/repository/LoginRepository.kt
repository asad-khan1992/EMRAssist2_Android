package com.emrassist.audio.ui.activity.login.repository

import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.login.requestmodel.LoginRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class LoginRepository(var apiClient: ApiClient) {

    fun login(loginRequestModel: LoginRequestModel): Flow<DataState<ApiResponse<UserModel>>> =
        flow {
            emit(DataState.Loading)
            val response = apiClient.login(loginRequestModel)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }

}