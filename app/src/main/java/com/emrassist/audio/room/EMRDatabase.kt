package com.emrassist.audio.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emrassist.audio.model.RecordedItem

@Database(entities = [RecordedItem::class], version = 5)
abstract class EMRDatabase : RoomDatabase() {
    abstract fun audioRecordDAO(): AudioRecordDao

    companion object {
        const val DATABASE_NAME: String = "emr_assists_db"
    }
}