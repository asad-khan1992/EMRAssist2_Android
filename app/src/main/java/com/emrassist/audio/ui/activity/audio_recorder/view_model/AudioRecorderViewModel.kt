package com.emrassist.audio.ui.activity.audio_recorder.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioRecorderViewModel
@Inject constructor(var repository: AudioRecorderRepository) :
    ViewModel() {

    private var _recordingInserted: MutableLiveData<Long> = MutableLiveData();
    private var _dataSetOfAudio: MutableLiveData<List<RecordedItem>> = MutableLiveData();
    val isRecordingSaved: LiveData<Long>
        get() = _recordingInserted
    val dataSet: LiveData<List<RecordedItem>>
        get() = _dataSetOfAudio


    public fun insertRecordedItem(recorderItem: RecordedItem) {
        viewModelScope.launch {
            var id: Long = repository.insertRecord(recorderItem)

            _recordingInserted.value = id
        }
    }

    public fun getListOfAudios() {
        viewModelScope.launch {
            var list = repository.getRecords()
            _dataSetOfAudio.value = list
        }
    }
}