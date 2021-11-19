package com.emrassist.audio.job_manager

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.emrassist.audio.Constants

object JobManager {
    private val TAG: String = JobManager.javaClass.simpleName

    public fun scheduleJob(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName: ComponentName = ComponentName(
            context,
            QueueJobService::class.java
        )
        val queueJob = JobInfo.Builder(Constants.NOTIFY_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic((15 * 60 * 1000).toLong())
            .build()

        val resultCode = scheduler.schedule(queueJob)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "onCreate: Job Scheduled")
        } else {
            Log.d(
                TAG,
                "onCreate: Job Scheduled Fails"
            )
        }
    }
}