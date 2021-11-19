package com.emrassist.audio.job_manager

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.util.Log
import com.emrassist.audio.Constants
import com.emrassist.audio.service.audiouploading.serviceManager.UploadServiceManager

class QueueJobService : JobService() {
    private var jobCancelled = false
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        updateOrShowNotification()
        return true
    }

    private fun resetJobService() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, QueueJobService::class.java)
        val queueJob = JobInfo.Builder(Constants.NOTIFY_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic((15 * 60 * 1000).toLong())
            .build()
        val resultCode = scheduler.schedule(queueJob)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "onCreate: Job ReScheduled")
        } else {
            Log.d(TAG, "onCreate: Job ReScheduled Fails")
        }
    }

    private fun updateOrShowNotification() {
        Log.d(TAG, "updateOrShowNotification: jobStarted")
        if (jobCancelled) return
        UploadServiceManager.startService(this)
        jobCancelled = false
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        jobCancelled = true
        Log.d(TAG, "updateOrShowNotification: jobCancelled")
        resetJobService()
        return true
    }

    companion object {
        private const val TAG = "QueueJobService"
    }
}