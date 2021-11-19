package com.emrassist.audio

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.multidex.MultiDex
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()
        context = this
        MultiDex.install(this)

        // Initialize the AWSMobileClient if not initialized

        // Initialize the AWSMobileClient if not initialized

        // Initialize the AWSMobileClient if not initialized


        try {
            startService(Intent(applicationContext, TransferService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onTerminate() {
        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isAppInForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isAppInForeground = true
    }

    companion object {
        lateinit var context: Context
        var isAppInForeground = false

    }
}