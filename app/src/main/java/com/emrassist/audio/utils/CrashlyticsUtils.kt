package com.emrassist.audio.utils

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.emrassist.audio.App
import com.emrassist.audio.BuildConfig
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception
import java.lang.RuntimeException

object CrashlyticsUtils {
    val KEY_LOGS: String = "APP_LOGS"

    fun setUpDefaultUserId() {
        FirebaseCrashlytics.getInstance().setUserId(SharedPrefsUtils.phoneNumber.toString())
    }

    fun logException(it: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(it)
    }

    fun logData(key: String, value: String) {
        FirebaseCrashlytics.getInstance().log(value)
        try {
            throw RuntimeException()
        } catch (e: Exception) {
            logException(e)
        }
    }

    fun log(issue: String) {
        FirebaseCrashlytics.getInstance().log(issue)
    }
}