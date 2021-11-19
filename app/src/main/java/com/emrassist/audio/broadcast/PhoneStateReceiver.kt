package com.emrassist.audio.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import org.greenrobot.eventbus.EventBus

open class PhoneStateReceiver() : BroadcastReceiver() {
    private val TAG: String = PhoneStateReceiver::class.java.simpleName


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "onReceive: Phone State Received.")

        val stateStr = intent?.extras!!.getString(TelephonyManager.EXTRA_STATE)
        var state = State.NONE

        if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
            state = State.IDLE
        } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
            state = State.RINGING
        }
        EventBus.getDefault().post(CallStateManager(state))
    }
}
