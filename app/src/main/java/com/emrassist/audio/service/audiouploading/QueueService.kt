package com.emrassist.audio.service.audiouploading

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.emrassist.audio.Constants.NOTIFY_ID
import com.emrassist.audio.R
import com.emrassist.audio.aws.AWSManager
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.job_manager.JobManager
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.service.audiouploading.queueManager.QueueManager
import com.emrassist.audio.service.audiouploading.viewmodel.UploadServiceViewModel
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import com.emrassist.audio.ui.activity.main.MainActivity
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AnalyticsUtils
import com.emrassist.audio.utils.CrashlyticsUtils
import com.emrassist.audio.utils.Utils
import com.emrassist.audio.utils.filemanager.FileManager
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import java.io.File
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class QueueService : Service() {

    private var isServerDown: Boolean = false
    var listOfAudios: ArrayList<RecordedItem> = ArrayList<RecordedItem>()

    //    @Inject
    lateinit var queueManager: QueueManager

    //
    @Inject
    lateinit var uploadServiceViewModel: UploadServiceViewModel

    @Inject
    lateinit var awsManager: AWSManager

    @Inject
    lateinit var audioRecordingRepository: AudioRecorderRepository
    private val TAG: String =
        QueueService::class.java.getSimpleName()

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var currentOperation: RecordedItem? = null
    private var channelId = ""
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var serviceHandler: ServiceHandler? =
        null
    private var serviceLooper: Looper? = null

    var isAlreadyUploading = false


    inner class ServiceHandler(looper: Looper) : Handler(
        looper
    ) {
        override fun handleMessage(@NonNull msg: Message) {
            if (!isAlreadyUploading) {
                if (queueManager.getSize() == 0) {
                    queueManager.loadAllOperations {
                        if (it > 0) {
                            startForeground(NOTIFY_ID, notificationBuilder?.build())
                            checkAndExecuteOperation()
                        } else {
                            removeNotification()
                        }
                    }
                } else {
                    currentOperation = null
                    startForeground(NOTIFY_ID, notificationBuilder?.build())
                    checkAndExecuteOperation()
                }
            }
        }
    }

    private fun removeNotification() {
        if (isServiceProcessing(this)) {
            stopService(Intent(this, QueueService::class.java))
        }
        notificationManager?.cancel(NOTIFY_ID)
    }


    private fun checkAndExecuteOperation() {
        if (isServerDown)
            return
        if (isAlreadyUploading) {
            writeLog("Uploading in progress. ${Gson().toJson(currentOperation ?: "")}");
            return
        }
        if (!NetworkReceiver.isNetworkAvailable(this)) {
            updateNotificationWithNetworkError()
            //            startForeground(NOTIFY_ID, notificationBuilder.build());
            isAlreadyUploading = false
            return
        }
        if (currentOperation != null) {
            queueManager.removeOperation(currentOperation!!)
            currentOperation = null;
        }
        val operation = queueManager.getOperation()

        if (operation != null) {
            if (isAlreadyUploading)
                return
            isAlreadyUploading = true
            currentOperation = operation
            writeLog("Item ready to upload. ${Gson().toJson(currentOperation ?: "")}");
            executeOperation(operation)
        } else {
            writeLog("All Items are uploaded. Checking for any New");
            queueManager.loadAllOperations { size ->
                if (size == 0) {
                    writeLog("All Items are uploaded. No new Items Found");
                    isAlreadyUploading = false
                    currentOperation = null
                    removeNotification()
                } else {
                    checkAndExecuteOperation()
                }
            }
        }
    }

    private fun updateNotificationWithNetworkError() {
        writeLog("Internet connection lost. ${Gson().toJson(currentOperation ?: "")}");
        notificationBuilder!!.setContentText("You have pending audios, Check you internet connection.")
            .setSubText("")
            .setProgress(0, 0, false)
        notificationManager!!.notify(NOTIFY_ID, notificationBuilder!!.build())
    }


    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        writeLog("Service Requested. OnStartCommand");
        if (!::queueManager.isInitialized) {
            queueManager = QueueManager.getInstance(audioRecordingRepository)
            queueManager.loadAllOperations {
                if (it > 0) {
                    startServiceHandler(startId, intent)
                } else {
                    removeNotification()
                }
            }
        } else {
            startServiceHandler(startId, intent)
        }
        return START_NOT_STICKY
    }

    private fun startServiceHandler(startId: Int, intent: Intent?) {
        val message: Message? = serviceHandler?.obtainMessage()
        message?.arg1 = startId
        message?.obj =
            intent != null && intent.hasExtra("updateData") && intent.getBooleanExtra(
                "updateData",
                false
            )
        if (message != null) {
            serviceHandler?.sendMessage(message)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        writeLog("Service Killed-Task Removed. Check for pending work ");
        doworkbeforeExit()
        super.onTaskRemoved(rootIntent)
    }

    private fun resetJob() {
        if (!isAlreadyUploading) {
            queueManager.loadAllOperations { size ->
                if (size != 0) {
                    writeLog(
                        "Resetting Job if All Files are uploaded"
                    );
                    JobManager.scheduleJob(this)
                }
            }
        }
    }

    //
    override fun onDestroy() {
//        writeLog("Service Killed-On Destroy. Check for pending work ");
//        doworkbeforeExit(true)
        super.onDestroy()
    }

    private fun doworkbeforeExit(isFromDestroy: Boolean = false): Boolean {
        if (isAlreadyUploading && !NetworkReceiver.isNetworkAvailable(this)) {
            isAlreadyUploading = false
        }

        if (!isAlreadyUploading) {
            writeLog("Working Before Exit. No File in Uploading. Check for Any New Pending File.");
            if (::queueManager.isInitialized) {
//                queueManager = QueueManager.getInstance(audioRecordingRepository)
//            }
                queueManager.loadAllOperations { size ->
                    currentOperation = null
                    writeLog("Work Before Exit. Total New Files are: $size")
                    if (size == 0) {
                        writeLog("Work Before Exit. No File Found.");
//                    if (!isFromDestroy)
                        removeNotification()
                    } else {
                        if (!NetworkReceiver.isNetworkAvailable(this)) {
                            writeLog("Work Before Exit. Files Found. But No Internet Connection.");
                            updateNotificationWithNetworkError()
                            isAlreadyUploading = false
                        } else {
                            writeLog("Work Before Exit. Upload the new files found.");
                            checkAndExecuteOperation()
                        }
                    }
                }
            }
        }
        //        isAlreadyUploading = false;
//        resetJob()
        return false
    }

    @Synchronized
    fun executeOperation(operation: RecordedItem) {
        writeLog("Picked a file And Deciding what to do with this file. ${Gson().toJson(operation ?: "")}");
        notificationBuilder!!.setContentText("Uploading Audio")
            .setSubText("Uploading Audio " + /*(queueManager.operationIndex(operation) + 1).toString() + "/ " + */queueManager.getSize())
        notificationManager!!.notify(NOTIFY_ID, notificationBuilder!!.build())

//        if (operation.isDeletedLocally) {
//            writeLog("Delete File. ${Gson().toJson(operation ?: "")}");
//            if (operation.isUploadedToScribe) {
//                deleteSpecificFile(operation)
//            }
//        } else {
        if (operation.isFileUploaded) {
            writeLog(
                "File is uploaded to S3. Setup to call Api for uploadng. ${
                    Gson().toJson(
                        operation ?: ""
                    )
                }"
            );
            updateNotification(-1, "Uploading Audio")
            uploadFileToServer(operation)
        } else {
            writeLog(
                "File is not uploaded to S3. Setup the file to be uploaded to s3. ${
                    Gson().toJson(
                        operation ?: ""
                    )
                }"
            );
            updateNotification(-3, "Uploading Audio")
            uploadToS3(operation)
        }
//        }
    }

    override fun onCreate() {
        super.onCreate()
        writeLog("Service Created.");
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        channelId = this.getString(R.string.project_id)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Uploading Audio"
            notificationChannel.lightColor = Color.BLUE
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
        if (notificationBuilder == null) {
            val notifyIntent = Intent(
                this@QueueService,
                MainActivity::class.java
            )
            val notifyPendingIntent = PendingIntent.getActivity(
                this@QueueService, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder = NotificationCompat.Builder(this@QueueService, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Checking for pending audios.")
                .setContentIntent(notifyPendingIntent)
                .setSound(null)
                .setOngoing(true)
                .setVibrate(null)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setAutoCancel(false)
        }
        startForeground(NOTIFY_ID, notificationBuilder?.build())
        val thread = HandlerThread(
            "ServiceStartArguments",
            Process.THREAD_PRIORITY_BACKGROUND
        )
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper!!)
    }


    private fun updateNotification(progress: Int, downloaded: String?) {
//        //updating current notification.
        if (!isServiceProcessing(this)) {
            removeNotification()
            return
        }
        if (progress < 0) {
            notificationBuilder!!.setProgress(0, 0, false)
            notificationBuilder!!.setContentText(downloaded)
        } else {
            notificationBuilder!!.setProgress(100, progress, false)
            notificationBuilder!!.setContentText("Uploaded: $progress %")
        }
        notificationManager!!.notify(NOTIFY_ID, notificationBuilder!!.build())
    }

    @Synchronized
    private fun uploadFileToServer(currentItem: RecordedItem) {
        var isSessionExpired = false;
        AnalyticsUtils.getInstance(this)!!
            .logEvent(AnalyticsUtils.eventSecondaryServerStarted, currentItem)
        writeLog("Uploading file to server. ${Gson().toJson(currentItem ?: "")}")
        uploadServiceViewModel.uploadFile(currentItem) {
            when (it) {
                is DataState.Success -> {
                    if (it.data.success == 1) {
                        currentItem.isUploadedToScribe = true
                        updateNotification(-4, "Upload Successful")
                        writeLog(
                            "File uploaded to Server. File : ${Gson().toJson(currentItem ?: "")}, Response: ${
                                Gson().toJson(
                                    it.data
                                )
                            }"
                        )

                        AnalyticsUtils.getInstance(this@QueueService)!!
                            .logEvent(AnalyticsUtils.eventSecondaryServerSuccess, currentItem)
                        EventBus.getDefault().post(currentItem)
                    } else {
                        writeLog(
                            "File is not uploaded to Server. File : ${
                                Gson().toJson(
                                    currentItem ?: ""
                                )
                            }, Response: ${Gson().toJson(it.data)}"
                        )
                        isSessionExpired = true
//                        AnalyticsUtils.getInstance(this@QueueService)!!.logEvent(
//                            AnalyticsUtils.eventSecondaryServerFailed,
//                            currentItem,
//                            Gson().toJson(it.data)
//                        )
//                        updateNotification(-4, "Upload failed")
//                        currentItem.isUploadedToScribe = false
                    }
                    isAlreadyUploading = false
                    currentItem.isPendingResponse = false
                    updateRecordInDatabase(currentItem)
                    if (isSessionExpired) {
                        EventBus.getDefault().post(SessionExpired(true))
                        return@uploadFile
                    } else
                        handler.postDelayed(::checkAndExecuteOperation, 200)
                }
                is DataState.ErrorException -> {

                    writeLog(
                        "Error in uploading File to Server. File : ${
                            Gson().toJson(
                                currentItem ?: ""
                            )
                        }, Exception: ${Utils.getStackTrace(it.exception)}"
                    )
                    AnalyticsUtils.getInstance(this@QueueService)?.logEvent(
                        AnalyticsUtils.eventSecondaryServerFailed,
                        currentItem,
                        it.exception
                    )
                    CrashlyticsUtils.logException(it.exception)
                    updateNotification(-4, "Upload failed")
                    isAlreadyUploading = false
                    currentItem.isPendingResponse = false
                    currentItem.isUploadedToScribe = false
                    currentItem.lastActionPerformedTimeStamp = System.currentTimeMillis().toString()
                    updateRecordInDatabase(currentItem)
                    if (it.exception is SocketTimeoutException || it.exception is SocketException) {
                        updateNotification(
                            -5,
                            "Server is down at the moment. Will retry in 5 minutes."
                        )
                        setUpServerErrorHandler()
                        return@uploadFile
                    } else {
                        if (it.exception is JSONException || it.exception is JsonSyntaxException || it.exception is JsonParseException) {
                            currentItem.isUploadedToScribe = true
                            updateNotification(-4, "Upload Successful")
                            AnalyticsUtils.getInstance(this@QueueService)!!
                                .logEvent(AnalyticsUtils.eventSecondaryServerSuccess, currentItem)
                            EventBus.getDefault().post(currentItem)
                            isAlreadyUploading = false
                            currentItem.isPendingResponse = false
                            updateRecordInDatabase(currentItem)
                        }
                        handler.postDelayed(::checkAndExecuteOperation, 200)
                    }
                }
            }
        }
    }

    private fun setUpServerErrorHandler() {
        isServerDown = true
        handler.postDelayed({
            isServerDown = false
            checkAndExecuteOperation()
        }, 300000)
    }


//    private fun deleteSpecificFile(operation: RecordedItem) {
//        writeLog("Setting up to Detele File. ${Gson().toJson(operation ?: "")}");
//        Log.d(TAG, "com.app.service: checkAndExecuteOperation: deleting")
//        updateNotification(-2, "Deleting Audio")
//        uploadServiceViewModel.deleteAudio(operation.uniqueId.toString())
//        CoroutineScope(Dispatchers.Main).launch {
//            uploadServiceViewModel.deleteDataState.observeForever {
//                when (it) {
//                    is DataState.Success -> {
//                        writeLog("File Deleted. ${Gson().toJson(operation ?: "")}");
//                        deleteRecordFromDatabase(operation)
//                        queueManager.loadAllOperations()
//                        updateNotification(-2, "Audio Deleted")
//                        checkAndExecuteOperation()
//                    }
//                    is DataState.ErrorException -> {
//                        writeLog(
//                            "Error in Deleting File. File ${Gson().toJson(operation ?: "")}: Exception ${
//                                Utils.getStackTrace(
//                                    it.exception
//                                )
//                            }"
//                        );
//                        CrashlyticsUtils.logException(it.exception)
//                        updateNotification(-2, "Could not delete Audio. please Try Again Later")
//                        checkAndExecuteOperation()
//                    }
//                }
//            }
//        }
//    }

    private fun deleteRecordFromDatabase(operation: RecordedItem) {
        queueManager.deleteAudio(operation) {
            Log.d(TAG, "deleteRecordFromDatabase: $it")
        }
    }

    @Synchronized
    fun uploadToS3(currentItem: RecordedItem) {
        AnalyticsUtils.getInstance(this)!!
            .logEvent(AnalyticsUtils.eventAWSUploadStarted, currentItem)
        writeLog("Uploading to S3. ${Gson().toJson(currentItem ?: "")}");

        Log.d(TAG, "com.app.service: checkAndExecuteOperation: uploading to s3 started")
        if (!File(currentItem.localPath).exists()) {
            writeLog(
                "Uploading to S3 But File not File not found in local Storage. So Deleting File. ${
                    Gson().toJson(
                        currentItem ?: ""
                    )
                }"
            );
            isAlreadyUploading = false
            deleteRecordFromDatabase(currentItem)
            handler.postDelayed(::checkAndExecuteOperation, 200)
            return
        }
        awsManager.uploadFile(File(currentItem.localPath),
            _onProgressChanged = {
                updateNotification(it, "Uploading Audio")
            },
            _onUploadFailed = {
                writeLog(
                    "Uploading to S3 Filed. File. ${Gson().toJson(currentItem ?: "")}  Exception: ${
                        Utils.getStackTrace(
                            it
                        )
                    }"
                );
                AnalyticsUtils.getInstance(this@QueueService)!!
                    .logEvent(AnalyticsUtils.eventAWSUploadFailed, currentItem, it)
                CrashlyticsUtils.logException(it)

                currentItem.isPendingResponse = false
                currentItem.isFileUploaded = false
                isAlreadyUploading = false
                updateRecordInDatabase(currentItem)
                handler.postDelayed(::checkAndExecuteOperation, 200)
            },
            _onUploadCompleted = {
                AnalyticsUtils.getInstance(this@QueueService)!!
                    .logEvent(AnalyticsUtils.eventAWSUploadSuccess, currentItem)
                writeLog("Uploading to S3 Completed. ${Gson().toJson(currentItem ?: "")}");
                currentItem.firebaseURL = it
                currentItem.isFileUploaded = true
                updateRecordInDatabase(currentItem)
                uploadFileToServer(currentItem)
            })
    }

    private fun updateRecordInDatabase(currentItem: RecordedItem) {
        queueManager.updateAudio(currentItem)
    }

    companion object {
        private var notificationManager: NotificationManager? = null
        fun isServiceProcessing(context: Context): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (QueueService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun stopService() {
            notificationManager?.cancel(NOTIFY_ID)
        }
    }

    private fun writeLog(s: String) {
        Log.d(TAG, "com.app.service: ${s}")
        FileManager.writeLogOnFile(this, s)
        CrashlyticsUtils.log(s)

    }

}