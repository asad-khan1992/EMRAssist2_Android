package com.emrassist.audio.service.audiouploading.serviceManager

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.emrassist.audio.service.audiouploading.QueueService

object UploadServiceManager {
    private val TAG: String = UploadServiceManager::class.java.simpleName
    fun startService(context: Context) {
        Log.d(TAG, "com.app.service: Service Starting")
        val i = Intent(context, QueueService::class.java)
        if (!QueueService.isServiceProcessing(context)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.putExtra("updateData", false)
        } else {
            i.putExtra("updateData", true)
        }
        ContextCompat.startForegroundService(context, i)
    }

    fun stopService(context: Context) {
        val i = Intent(context, QueueService::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.stopService(i)
    }

}