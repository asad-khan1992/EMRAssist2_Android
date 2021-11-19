package com.emrassist.audio.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.room.AudioRecordDao
import com.emrassist.audio.room.EMRDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RoomModule {


    @Singleton
    @Provides
    fun provideBlogDb(@ApplicationContext context: Context): EMRDatabase {
        return Room.databaseBuilder(context, EMRDatabase::class.java, EMRDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }


    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE audio_records ADD COLUMN ${RecordedItem.COLUMN_TIME_STAMP} TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE audio_records ADD COLUMN ${RecordedItem.COLUMN_TEST_FIELD} TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE audio_records ADD COLUMN ${RecordedItem.COLUMN_DURATION} TEXT NOT NULL DEFAULT ''"
            )
        }
    }
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }

    @Singleton
    @Provides
    fun provideBlogDao(eMRDatabase: EMRDatabase): AudioRecordDao {
        return eMRDatabase.audioRecordDAO()
    }
}