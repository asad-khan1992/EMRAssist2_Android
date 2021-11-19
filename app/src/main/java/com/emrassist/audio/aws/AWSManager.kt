package com.emrassist.audio.aws

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class AWSManager(var awsUploader: AWSUploader) {
    public fun uploadFile(
        file: File,
        _onUploadCompleted: ((String) -> Unit)? = null,
        _onProgressChanged: ((Int) -> Unit)? = null,
        _onUploadFailed: ((java.lang.Exception) -> Unit)? = null
    ) {
        awsUploader.uploadFile(currentFile = file,
            onProgressChange = {
                _onProgressChanged?.invoke(it)
            },
            onUploadCompleted = {
                _onUploadCompleted?.invoke(it)

            }, onUploadFailed = {
                _onUploadFailed?.invoke(it)

            }
        )
    }

//
//    private var _onUploadCompeted = MutableLiveData<String>()
//    val onUploadCompeted: LiveData<String>
//        get() = _onUploadCompeted
//
//    private var _onProgressChanged: MutableLiveData<Int> = MutableLiveData()
//    val onProgressChanged: LiveData<Int>
//        get() = _onProgressChanged
//
//    private var _onUploadFailed: MutableLiveData<String> = MutableLiveData()
//    val onUploadFailed: LiveData<String>
//        get() = _onUploadFailed
//
//    public fun uploadFile(file: File) {
//        awsUploader.uploadFile(currentFile = file,
//            onProgressChange = {
//                _onProgressChanged.value = it
//            },
//            onUploadCompleted = {
//                _onUploadCompeted.value = it
//
//            }, onUploadFailed = {
//                _onUploadFailed.value = it
//
//            }
//        )
//    }
}