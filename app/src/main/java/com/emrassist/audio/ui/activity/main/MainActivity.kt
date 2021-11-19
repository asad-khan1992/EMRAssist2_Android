package com.emrassist.audio.ui.activity.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.emrassist.audio.BuildConfig
import com.emrassist.audio.R
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.job_manager.JobManager
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.service.audiouploading.serviceManager.UploadServiceManager
import com.emrassist.audio.service.recording.RecordingService
import com.emrassist.audio.service.recording.RecordingState
import com.emrassist.audio.ui.activity.audio_recorder.AudioRecorderActivity
import com.emrassist.audio.ui.activity.dictation_list.DictationListActivity
import com.emrassist.audio.ui.activity.main.view_model.MainActivityViewModel
import com.emrassist.audio.ui.activity.profile.ProfileActivity
import com.emrassist.audio.ui.base.BaseActivity
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.CrashlyticsUtils
import com.emrassist.audio.utils.DialogLoader
import com.google.gson.Gson
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val TAG: String = MainActivity::class.java.simpleName
    private lateinit var receiver: NetworkReceiver

    //    private lateinit var navController: NavController
    val viewModel: MainActivityViewModel by viewModels()
    var dialogLoader: DialogLoader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CrashlyticsUtils.setUpDefaultUserId()


        setUpData();
        setOnClickListeners()
        JobManager.scheduleJob(this)
        UploadServiceManager.startService(this)
        dialogLoader = DialogLoader(this)

        stopRecordingIfAlreadyRunning()
        saveDataIfNotSavedAlready()
        setupObservers();
        registerNetworkReceiver()
    }

    private fun setupObservers() {
        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
//                    if (it.data.success == 1) {
//                        Utils.logout(this)
//                    } else {
//                    Utils.logout(this)
//                    }
                    sessionExpired(SessionExpired(false))
                    dialogLoader?.dismiss()
                }
                is DataState.ErrorException -> {
                    showErrorAlert(
                        it.exception.localizedMessage
                            ?: "Some thing went wrong. Please try Again later"
                    )
                }
                is DataState.Loading -> {
                }
            }
        })
    }

    private fun showErrorAlert(message: String) {
        dialogLoader?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");

    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterNetworkReceiver()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        stopRecordingIfAlreadyRunning()
    }

    fun stopRecordingIfAlreadyRunning() {
        if (RecordingService.isServiceProcessing(this)) {
            val intent = Intent(this, RecordingService::class.java)
            intent.putExtra(RecordingService.RECORDING_STATE, RecordingState.EXIT)
            startService(intent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemUploadedSuccessfully(item: RecordedItem) {
        Log.d(this.javaClass.simpleName, "onItemUploadedSuccessfully: ${item.fileName}")
        Toast.makeText(this, "Audio Uploaded.", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        showExitAlert()
    }

    private fun setUpData() {
        version_no.setText("Version: ${BuildConfig.VERSION_NAME}")
    }


    private fun setOnClickListeners() {
//        if (SharedPrefsUtils.logFileList.size == 0)
        text_request_log.visibility = View.GONE
//        text_request_log.visibility = View.VISIBLE
        text_request_log.setOnClickListener {
            dialogLoader?.show()
//            CrashlyticsUtils.logData(CrashlyticsUtils.KEY_LOGS, FileManager.readAllFiles(this));
            throw RuntimeException("Crashing Server")
            dialogLoader?.dismiss()
        }
        text_logout.setOnClickListener {
            showLogoutAlert();
        }
        record.setOnClickListener {
            checkPermission()
        }

        dictation_list.setOnClickListener {
            val intent = Intent(this, DictationListActivity::class.java)
            startActivity(intent)
        }

        text_Profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

    }

    private fun checkPermission() {
        val permissions =
            arrayOf<String>(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
//                ,Manifest.permission.READ_PHONE_STATE
            )
        Permissions.check(
            this /*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {
                    openActivity()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String>?
                ) {
                    super.onDenied(context, deniedPermissions)
                    AlertOP.showAlert(this@MainActivity,
                        message = "Audio and Storage Permissions are required for Audio Recording.",
                        pBtnText = "Ok",
                        nBtnText = "Cancel",
                        onPositiveClick = {
                            checkPermission()
                        },
                        onNegativeClick = {
                            finish()
                        }
                    )
                }
            })
    }

    private fun openActivity() {
        val intent = Intent(this, AudioRecorderActivity::class.java)
        startActivity(intent)
    }


    private fun showLogoutAlert() {
        AlertOP.showAlert(
            this,
            title = "",
            message = getString(R.string.logout_confirmation),
            nBtnText = "Cancel",
            onPositiveClick = {
                createLogoutApiRequest()
            },
        )

    }

    private fun showExitAlert() {
        AlertOP.showAlert(
            this,
            message = getString(R.string.exit_confirmation),
            nBtnText = "Cancel",
            onPositiveClick = {
                finishAffinity()
            },
            onNegativeClick = {

            })
    }

    public fun saveDataIfNotSavedAlready() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
        viewModel.updateRecordInDatabase(filesDir)
//        }
    }

    public fun createLogoutApiRequest() {
        dialogLoader?.show();
        viewModel.logout()
    }


    public fun registerNetworkReceiver() {

        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
        receiver = NetworkReceiver()
        registerReceiver(receiver, filter)
    }

    public fun unRegisterNetworkReceiver() {
        if (::receiver.isInitialized)
            unregisterReceiver(receiver)
    }

}