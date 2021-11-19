package com.emrassist.audio.service.audiouploading.queueManager

import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

class QueueManager(var repository: AudioRecorderRepository) {


    companion object {
        private var instance: QueueManager? = null
        fun getInstance(repository: AudioRecorderRepository): QueueManager {
            if (instance == null) {
                instance = QueueManager(repository)
            }
            return instance!!
        }
    }

    private val operations: ArrayList<RecordedItem> = java.util.ArrayList()

    @Synchronized
    fun loadAllOperations(onRecordRead: ((Int) -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val audios = repository.getRecords()
            val tempAudio: ArrayList<RecordedItem> = ArrayList()
            for (audio in audios) {
//                if (audio.isDeletedLocally) {
//                    tempAudio.add(audio)
//                } else {
                if (!audio.isFileUploaded || !audio.isUploadedToScribe && (!audio.isDeletedLocally && !audio.isPendingResponse)
                /*&&
                    isUpdateAllowed(
                        audio.lastActionPerformedTimeStamp
                    )
                    */
                ) {
                    tempAudio.add(audio)
                }
//                }
            }
            operations.clear();
            operations.addAll(tempAudio)
            onRecordRead?.invoke(operations.size)
        }
    }

    fun isUpdateAllowed(timeStamp: String?): Boolean {
        if (timeStamp.isNullOrEmpty())
            return true
        return try {
            System.currentTimeMillis() - timeStamp.toLong() > 300
        } catch (e: java.lang.Exception) {
            true
        }
    }

    fun getOperation(): RecordedItem? {
        return if (operations.size > 0) {
            operations.get(0)
        } else null
    }

    fun getOperation(position: Int): RecordedItem? {
        return if (operations.size > 0 && position > 0 && position < operations.size) {
            operations.get(position)
        } else null
    }

    fun operationIndex(operation: RecordedItem): Int {
        try {
            for (i in operations.indices) {
                if (operations.get(i).uniqueId == operation.uniqueId) {
                    return i
                }
            }
        } catch (e: Exception) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
        }
        return -1
    }

    fun removeOperation(operation: RecordedItem) {
        val index = operationIndex(operation)
        if (index >= 0) {
            operations.removeAt(index)
        }
    }

    fun getSize(): Int {
        return if (operations.size > 0) {
            operations.size
        } else 0
    }

    fun updateAudio(currentItem: RecordedItem, onRecordUpdated: ((Int) -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            val value = repository.updateRecord(currentItem)
            onRecordUpdated?.invoke(value)
        }
    }

    fun deleteAudio(currentItem: RecordedItem, onRecordDeleted: ((Int) -> Unit)? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            var deleted = repository.deleteRecord(currentItem)
            onRecordDeleted?.invoke(deleted)
        }
    }
}