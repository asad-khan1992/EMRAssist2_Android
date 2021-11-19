package com.emrassist.audio.room

import androidx.room.*
import com.emrassist.audio.model.RecordedItem

@Dao
interface AudioRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordedItem: RecordedItem): Long

    @Query("select * from audio_records")
    suspend fun getListOfAudios(): List<RecordedItem>


    //    @Query("UPDATE audio_records SET fileUrl = :url WHERE id = :id")
//    suspend fun updateRecord(id: Long, url: String?): Int
    @Update
    suspend fun updateRecord(recordedItem: RecordedItem): Int

    @Delete
    suspend fun deleteRecord(recordedItem: RecordedItem): Int

    @Query("delete from audio_records")
    suspend fun deleteAllRecords(): Int

    @Query("select * from audio_records where fileName=:name")
    suspend fun findRecordByName(name: String): List<RecordedItem>
}