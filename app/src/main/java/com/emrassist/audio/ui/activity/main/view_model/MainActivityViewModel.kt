package com.emrassist.audio.ui.activity.main.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import com.emrassist.audio.ui.activity.main.repository.MainActivityRepository
import com.emrassist.audio.ui.activity.main.request_model.LogoutRequestModel
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private var repository: AudioRecorderRepository,
    private var mainActivityRepository: MainActivityRepository
) :
    ViewModel() {

    private val _datastate: MutableLiveData<DataState<ApiResponse<Any>>> = MutableLiveData()
    val dataState: LiveData<DataState<ApiResponse<Any>>>
        get() = _datastate

    fun clearData() {
        viewModelScope.launch {
            repository.deleteAllRecords()

        }
    }

    fun updateRecordInDatabase(filesDir: File?) {
        val files = filesDir?.listFiles()
        if (files.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            for (file in files) {
                if (file.extension.equals("wav", true)) {
                    val splitName = file.name.split("_");
                    var name = ""
                    if (splitName.size > 0) {
                        for (index in 0 until splitName.size - 1) {
                            name += splitName[index] + "_"
                        }
                        name = name.substring(0, name.length - 1)
                    } else {
                        name = splitName[0]
                    }
                    val list = repository.findRecordByName(name)
                    if (list.isEmpty()) {

                        var recorderItem: RecordedItem = RecordedItem();
                        recorderItem.fileName = name
                        recorderItem.recordedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(
                            Date()
                        )
                        recorderItem.localPath = file.absolutePath
                        recorderItem.uniqueId = System.currentTimeMillis().toString();

                        repository.insertRecord(recorderItem)
                    }


                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            mainActivityRepository.logout(LogoutRequestModel(SharedPrefsUtils.user?.id ?: ""))
                .onEach { data ->
                    _datastate.value = data
                }
                .launchIn(viewModelScope)

        }
    }

}