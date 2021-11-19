package com.emrassist.audio.ui.activity.dictation_list.repository

import com.emrassist.audio.model.DictationListResponseModel
import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class DictationListRepository(var apiClient: ApiClient) {
    suspend fun getListOfRecordings(number: String, currentPage: Int): Flow<DataState<ApiResponse<DictationListResponseModel>>> = flow {
            emit(DataState.Loading)
            var response = apiClient.getListOfAudios(number,currentPage)
            emit(DataState.Success(response))
    }.catch { it ->
        emit(DataState.ErrorException(it))
    }
}