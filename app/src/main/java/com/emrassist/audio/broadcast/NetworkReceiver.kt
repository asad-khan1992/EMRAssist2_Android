package com.emrassist.audio.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.emrassist.audio.service.audiouploading.serviceManager.UploadServiceManager

open class NetworkReceiver() : BroadcastReceiver() {
    private val TAG: String = NetworkReceiver::class.java.simpleName


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "onReceive: Network Received.")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfoWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val isConnectedWifi = activeNetInfoWifi != null && activeNetInfoWifi.isConnected
        if (isConnectedWifi)
            startQueueManager(context)
    }

    open fun startQueueManager(context: Context) {
        UploadServiceManager.startService(context)
    }

    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val mConnectivity =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Skip if no connection
            val info = mConnectivity.activeNetworkInfo ?: return false

            val netType = info.type
            return if (netType == ConnectivityManager.TYPE_WIFI) {
                info.isConnected
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                info.isConnected
            } else {
                false
            }
        }
    }
}
