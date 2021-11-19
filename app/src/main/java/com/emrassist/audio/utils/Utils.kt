package com.emrassist.audio.utils

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.emrassist.audio.App
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.ui.activity.login.LoginActivity
import com.emrassist.audio.ui.activity.player.AudioPlayerActivity
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import dagger.hilt.android.internal.managers.ViewComponentManager.FragmentContextWrapper
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeUnit

object Utils {
    var isSessionExpired = false
    fun getContext(context: Context): Context {
        return if (context is FragmentContextWrapper) {
            context.baseContext
        } else context
    }


    fun getStackTrace(it: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        it.printStackTrace(printWriter)
        return stringWriter.toString()
    }

    fun openPlayerActivity(item: RecordedItem, context: Context) {
        var localPathFound: Boolean = false;
        var url: String = item.firebaseURL
        val intent = Intent(
            context,
            AudioPlayerActivity::class.java
        )
        if (item.localPath.isNotEmpty()) {
            val currentFile: File =
                File(item.localPath.replace("file://", ""))
            if (currentFile.exists()) {
                localPathFound = true;
                url = item.localPath
            }
        }
        if (!localPathFound && !NetworkReceiver.isNetworkAvailable(context)) {
            AlertOP.showAlert(
                context,
                message = "Check your internet connection and try again",
                pBtnText = "Ok"
            )
        }
        intent.putExtra(AudioPlayerActivity.URL, url)
        intent.putExtra(AudioPlayerActivity.IS_LOCAL, localPathFound)
        context.startActivity(intent)
    }

    fun getDurationOfAudio(filePath: String): String {
        try {
            val uri = Uri.parse(filePath)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(App.context, uri)
            val durationStr: String? =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val millSecond = durationStr?.toLong() ?: 0
            return TimeUnit.MILLISECONDS.toSeconds(millSecond).toString()
        } catch (e: Exception) {
        }
        return "0"
    }

    fun logout(context: Context) {
//        viewModel.clearData();
        SharedPrefsUtils.clear()
//        if (filesDir.listFiles() != null && filesDir.listFiles().size > 0)
//            for (item in filesDir.listFiles())
//                item.delete()
        if (context == null) {
            return
        }
        val intent: Intent = Intent(
            context,
            LoginActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        (context as AppCompatActivity).finish()
    }

    fun showSessionExpireAlert(context: Context) {
//        if (context == null) {
//            logout(context)
//            return
//        }
        AlertOP.showAlert(
            context,
            title = "Session Expired",
            message = "Your session has been expired. Please Login Again",
            pBtnText = "Ok",
            cancelable = false,
            onPositiveClick = {
                logout(context)
            })
    }
}