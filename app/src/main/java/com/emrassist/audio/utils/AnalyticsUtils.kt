package com.emrassist.audio.utils

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.emrassist.audio.App
import com.emrassist.audio.BuildConfig
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsUtils(context: Context) {
    private val mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    fun logEvent(event: String) {
        logEvent(event, null as Bundle?)
    }

    private fun logEvent(event: String, _bundle: Bundle?) {
        var bundle = _bundle
        if (bundle == null) {
            bundle = Bundle()
        }
        bundle.putString("phone_number", SharedPrefsUtils.phoneNumber)
        bundle.putString("time_in_millis", System.currentTimeMillis().toString() + "")
        bundle.putString("build", BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")")
        bundle.putString("device_model", Build.MODEL)
        mFirebaseAnalytics.logEvent(event, bundle)
    }

    fun logEvent(event: String, currentItem: RecordedItem?) {
        logEvent(event, getBundleFromUploadingItem(currentItem))
    }

    fun logEvent(event: String, currentItem: RecordedItem?, ex: Exception) {
        val bundle = getBundleFromUploadingItem(currentItem)
        bundle.putString("error_localized_message", ex.localizedMessage)
        bundle.putString("error_message", ex.message)
        logEvent(event, bundle)
    }

    fun logEvent(event: String, currentItem: RecordedItem?, ex: Throwable) {
        val bundle = getBundleFromUploadingItem(currentItem)
        bundle.putString("error_localized_message", ex.localizedMessage)
        bundle.putString("error_message", ex.message)
        logEvent(event, bundle)
    }

    fun logEvent(event: String, currentItem: RecordedItem?, error: String?) {
        val bundle = getBundleFromUploadingItem(currentItem)
        bundle.putString("error_api", error)
        logEvent(event, bundle)
    }

    private fun getBundleFromUploadingItem(currentItem: RecordedItem?): Bundle {
        val bundle = Bundle()
        if (currentItem != null) {
            bundle.putString("file_name", currentItem.fileName)
            bundle.putString("firebase_url", currentItem.firebaseURL)
            bundle.putString("recorded_date", currentItem.recordedDate)
            bundle.putString("file_status", currentItem.status)
            bundle.putString("file_url", currentItem.filesUrlID.toString() + "")
            bundle.putString("local_path", currentItem.localPath + "")
            bundle.putString("id", currentItem.uniqueId.toString() + "")
        }
        return bundle
    }

    companion object {
        const val eventAWSUploadStarted = "aws_upload_started"
        const val eventAWSUploadSuccess = "aws_success"
        const val eventAWSUploadFailed = "aws_failed"
        const val eventSecondaryServerStarted = "secondary_upload_started"
        const val eventSecondaryServerSuccess = "secondary_success"
        const val eventSecondaryServerFailed = "secondary_failed"
        const val eventPauseAudio = "audio_pause"
        const val eventResumeAudio = "audio_resume"
        const val eventPlayAudio = "audio_play"
        const val eventStopAudio = "audio_stop"
        const val eventSaveAudio = "audio_save"
        const val eventStartAudio = "audio_start"
        const val eventCancelAudio = "audio_cancel"
        const val eventRestartAudio = "audio_restart"
        const val eventCompleteAudio = "audio_complete_button"
        private var INSTANCE: AnalyticsUtils? = null
        fun getInstance(context: Context?): AnalyticsUtils? {
            if (INSTANCE == null) {
                INSTANCE = AnalyticsUtils(context ?: App.context)
            }
            return INSTANCE
        }
    }

}