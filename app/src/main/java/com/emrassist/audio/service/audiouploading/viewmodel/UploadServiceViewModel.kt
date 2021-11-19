package com.emrassist.audio.service.audiouploading.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.service.audiouploading.repository.UploadServiceRepository
import com.emrassist.audio.utils.Utils
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class UploadServiceViewModel @Inject constructor(val repository: UploadServiceRepository) :
    ViewModel() {
//    private val _uploadDataState: MutableLiveData<DataState<ApiResponse<RecordedItem>>> =
//        MutableLiveData()
//    val uploadDataState: LiveData<DataState<ApiResponse<RecordedItem>>>
//        get() = _uploadDataState
//
//    private val _deleteDataState: MutableLiveData<DataState<ApiResponse<String>>> =
//        MutableLiveData()
//    val deleteDataState: LiveData<DataState<ApiResponse<String>>>
//        get() = _deleteDataState

    fun uploadFile(
        currentItem: RecordedItem,
        onItemStateStateChane: ((DataState<ApiResponse<RecordedItem>>) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (currentItem.duration.isNullOrEmpty()) {
                currentItem.duration = Utils.getDurationOfAudio(currentItem.localPath)

            }
            repository.uploadFile(
                currentItem.uniqueId.toString(),
                Build.MODEL,
                "android",
                SharedPrefsUtils.phoneNumber.toString(),
                currentItem.firebaseURL,
                currentItem.fileName,
                currentItem.localPath,
                currentItem.duration,
                currentItem.recordedDate
            )
                .onEach { data ->
                    onItemStateStateChane?.invoke(data)
                }
                .launchIn(viewModelScope)

        }

    }
}