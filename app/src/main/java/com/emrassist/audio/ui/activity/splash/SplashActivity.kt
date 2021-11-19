package com.emrassist.audio.ui.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.emrassist.audio.R
import com.emrassist.audio.ui.activity.login.LoginActivity
import com.emrassist.audio.ui.activity.main.MainActivity
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.firebase.FirebaseApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Initialize the AWSMobileClient if not initialized
        AWSMobileClient.getInstance().initialize(this)
            .execute()
        FirebaseApp.initializeApp(this)
        SharedPrefsUtils.clearOldSharedPrefs()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent: Intent =
                Intent(
                    this@SplashActivity,
                    (when {
                        SharedPrefsUtils.isAlreadyLoggedIn -> {
                            MainActivity::class.java
                        }
                        else -> {
                            LoginActivity::class.java
                        }
                    })
                )
            startActivity(intent)
            finish()
        }, 2000)
    } //onCreate
} //main
