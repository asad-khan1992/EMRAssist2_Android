package com.emrassist.audio.ui.activity.login_with_number.repository

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.otp_verification_activity.request_model.OtpVerificationRequestModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class LoginUsingNumberRepository(var apiClient: ApiClient) {
    //    suspend fun verifyPasswordOtp(number: OtpVerificationRequestModel): Flow<DataState<ApiResponse<String>>> = flow {
//            emit(DataState.Loading)
//            var response = apiClient.sendSMS("sendSms", number)
//            emit(DataState.Success(response))
//    }.catch { it ->
//        emit(DataState.ErrorException(it))
//    }
    suspend fun verifyPasswordOtp(model: OtpVerificationRequestModel): Flow<DataState<ApiResponse<Any>>> =
        flow {
            emit(DataState.Loading)
            var response = apiClient.verifyPasswordOtp(model)
            emit(DataState.Success(response))
        }.catch { it ->
            emit(DataState.ErrorException(it))
        }
}