package com.emrassist.audio.service.audiouploading.repository

import com.emrassist.audio.BuildConfig
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.service.audiouploading.request_model.UploadAudioRequestModel
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class UploadServiceRepository(var apiClient: ApiClient) {
    suspend fun uploadFile(
        id: String,
        version: String,
        device: String,
        phoneNumber: String,
        uploadedUrl: String,
        fileName: String,
        localPath: String,
        duration: String,
        date: String
    ): Flow<DataState<ApiResponse<RecordedItem>>> = flow {
        emit(DataState.Loading)
        val response = apiClient.uploadFile(
            UploadAudioRequestModel(
                id,
                version,
                device,
                phoneNumber,
                uploadedUrl,
                fileName,
                localPath,
                SharedPrefsUtils.user?.id ?: "",
                duration,
                date,
                BuildConfig.VERSION_NAME
            )
        )
        emit(DataState.Success(response))
    }.catch { it ->
        emit(DataState.ErrorException(it))
    }

}