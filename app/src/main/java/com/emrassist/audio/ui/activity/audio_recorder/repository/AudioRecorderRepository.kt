package com.emrassist.audio.ui.activity.audio_recorder.repository

import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.room.AudioRecordDao

class AudioRecorderRepository(var recorderDao: AudioRecordDao) {
    suspend fun insertRecord(recordedItem: RecordedItem): Long {
        return recorderDao.insert(recordedItem)
    }

    suspend fun getRecords(): List<RecordedItem> {
        return recorderDao.getListOfAudios()
    }

    suspend fun updateRecord(recordedItem: RecordedItem): Int {
        return recorderDao.updateRecord(recordedItem)
    }
    suspend fun deleteRecord(recordedItem: RecordedItem): Int {
        return recorderDao.deleteRecord(recordedItem)
    }

    suspend fun deleteAllRecords() :Int{
        return recorderDao.deleteAllRecords()
    }

    suspend fun findRecordByName(name: String): List<RecordedItem> {
        return recorderDao.findRecordByName(name)
    }
}