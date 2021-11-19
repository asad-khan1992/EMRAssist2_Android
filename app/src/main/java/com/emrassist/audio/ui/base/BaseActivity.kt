package com.emrassist.audio.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.service.audiouploading.QueueService
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.Utils
import com.emrassist.audio.utils.Utils.isSessionExpired
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseActivity : AppCompatActivity() {
    private lateinit var receiver: NetworkReceiver

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
//        try {
        EventBus.getDefault().register(this)
//        } catch (e: Exception) {
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun sessionExpired(expired: SessionExpired) {

        runOnUiThread {
            if (!isSessionExpired) {
                isSessionExpired = true
                if (QueueService.isServiceProcessing(this)) {
                    stopService(Intent(this, QueueService::class.java))
                    QueueService.stopService()
                }
                Log.d(BaseActivity::class.java.simpleName, "sessionExpired: ${expired.showAlert}")
                if (expired.showAlert)
                    Utils.showSessionExpireAlert(this)
                else
                    Utils.logout(this)
            }
        }
    }

}