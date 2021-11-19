package com.emrassist.audio.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "audio_records")
class RecordedItem : Serializable {
    companion object {
        const val COLUMN_NAME: String = "id"
        const val COLUMN_FILE_URL_ID: String = "fileUrlId"
        const val COLUMN_FILE_NAME: String = "fileName"
        const val COLUMN_RECORDED_DATE: String = "recordedDate"
        const val COLUMN_FILE_LOCAL_PATH: String = "localPath"
        const val COLUMN_FILE_URL: String = "fileUrl"
        const val COLUMN_STATUS: String = "status"
        const val COLUMN_IS_FILE_UPLOADED: String = "isFileUploaded"
        const val COLUMN_IS_FILE_UPLOADED_TO_SCRIBE: String = "isUploadedToScribe"
        const val COLUMN_IS_FILE_DELETED_LOCALLY: String = "isDeletedLocally"
        const val COLUMN_IS_PENDING_RESPONSE: String = "isPendingResponse"
        const val COLUMN_TIME_STAMP: String = "timeStamp"
        const val COLUMN_TEST_FIELD: String = "testfield"
        const val COLUMN_DURATION: String = "duration"

    }

    @SerializedName("unique_number")
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_NAME)
    var uniqueId: String = ""

    @SerializedName("tFilesUrl")
    @Expose
    @ColumnInfo(name = COLUMN_FILE_URL_ID)
    var filesUrlID: Long = 0


    @Expose
    @SerializedName("file_name")
    @ColumnInfo(name = COLUMN_FILE_NAME)
    var fileName = ""

    @SerializedName("date")
    @Expose
    @ColumnInfo(name = COLUMN_RECORDED_DATE)
    var recordedDate = ""

    @SerializedName("file_name_url")
    @Expose
    @ColumnInfo(name = COLUMN_FILE_LOCAL_PATH)
    var localPath = ""

    @SerializedName("files_url")
    @Expose
    @ColumnInfo(name = COLUMN_FILE_URL)
    var firebaseURL = ""
        set(data) {
            field = data
            if (field.isNotEmpty()) {
                isFileUploaded = true
                isUploadedToScribe = true
            }
        }

    @SerializedName("status")
    @Expose
    @ColumnInfo(name = COLUMN_STATUS)
    var status = "Processing"

    @Expose
    @ColumnInfo(name = COLUMN_IS_FILE_UPLOADED)
    var isFileUploaded = false

    @Expose
    @ColumnInfo(name = COLUMN_IS_FILE_UPLOADED_TO_SCRIBE)
    var isUploadedToScribe = false

    @Expose
    @ColumnInfo(name = COLUMN_IS_FILE_DELETED_LOCALLY)
    var isDeletedLocally = false

    @Expose
    @ColumnInfo(name = COLUMN_IS_PENDING_RESPONSE)
    var isPendingResponse = false

    @Expose
    @ColumnInfo(name = COLUMN_TIME_STAMP)
    var lastActionPerformedTimeStamp = ""

    @Expose
    @ColumnInfo(name = COLUMN_TEST_FIELD)
    var testfield = ""

    @SerializedName("file_duration")
    @Expose
    @ColumnInfo(name = "duration")
    var duration: String = ""

}

