package com.emrassist.audio.retrofit

import com.emrassist.audio.retrofit.model.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(var apiClient: ApiClient) {
//    suspend fun verifyPin(pin: String): ApiResponse<String> {
//        return apiClient.verifyPin("getCode", pin)
//    }
}