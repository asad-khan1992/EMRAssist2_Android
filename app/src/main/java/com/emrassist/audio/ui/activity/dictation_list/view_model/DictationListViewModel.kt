package com.emrassist.audio.ui.activity.dictation_list.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.DictationListResponseModel
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import com.emrassist.audio.ui.activity.dictation_list.repository.DictationListRepository
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DictationListViewModel @Inject constructor(
    val dictationRepository: DictationListRepository,
    var audioRecorderRepository: AudioRecorderRepository
) :
    ViewModel() {
    private val _datastate: MutableLiveData<DataState<ApiResponse<DictationListResponseModel>>> =
        MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<DictationListResponseModel>>>
        get() = _datastate

    private val _datastateDB: MutableLiveData<ArrayList<RecordedItem>> =
        MutableLiveData()
    val dataStateRecording: LiveData<ArrayList<RecordedItem>>
        get() = _datastateDB

    fun getListOfRecordings(currentPage: Int) {
        viewModelScope.launch {
            dictationRepository.getListOfRecordings(SharedPrefsUtils.user?.id ?: "", currentPage)
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }

    }

    fun getListOfRecordingsFromDB() {
        viewModelScope.launch {
            val list = audioRecorderRepository.getRecords()
            val tempList: ArrayList<RecordedItem> = ArrayList()
            tempList.addAll(list)
            _datastateDB.value = tempList
        }

    }

    fun getListOfPendingRecordingsFromDB() {
        viewModelScope.launch {
            val list = audioRecorderRepository.getRecords()
            val tempList = ArrayList<RecordedItem>()
            for (item in list) {
                if (!item.isUploadedToScribe || !item.isFileUploaded) {
                    tempList.add(item)
                }
            }
            _datastateDB.value = tempList
        }
    }

    fun deleteRecoding(item: RecordedItem) {
        viewModelScope.launch {
            audioRecorderRepository.deleteRecord(item)
            if (File(item.localPath).exists())
                File(item.localPath).delete()
        }
    }
}