package com.emrassist.audio.service.recording

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.emrassist.audio.Constants
import com.emrassist.audio.R
import com.emrassist.audio.ui.activity.audio_recorder.AudioRecorderActivity
import com.emrassist.audio.utils.AnalyticsUtils
import com.emrassist.audio.utils.audiorecorder.AudioRecorderUtils
import com.emrassist.audio.utils.audiorecorder.model.AudioChannel
import com.emrassist.audio.utils.audiorecorder.model.AudioSampleRate
import com.emrassist.audio.utils.audiorecorder.model.AudioSource
import kotlinx.android.synthetic.main.activity_audio_recorder.*
import omrecorder.AudioChunk
import omrecorder.OmRecorder
import omrecorder.PullTransport
import omrecorder.Recorder
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

class RecordingService : Service(), PullTransport.OnAudioChunkPulledListener {

    private var fileName: String = ""
    private var filePath: String = ""
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private var channelId = ""
    var timer: Timer? = null
    var recorder: Recorder? = null
    var recorderSecondsElapsed = 0

    var source: AudioSource? = null
    var channel: AudioChannel? = null
    var sampleRate: AudioSampleRate? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        channelId = this.getString(R.string.project_id)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Recording Audio"
            notificationChannel.lightColor = Color.BLUE
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
        if (notificationBuilder == null) {
//            val notifyIntent = packageManager.getLaunchIntentForPackage(packageName)
            val notifyIntent = Intent(
                this@RecordingService,
                AudioRecorderActivity::class.java
            )
            val notifyPendingIntent = PendingIntent.getActivity(
                this@RecordingService, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder = NotificationCompat.Builder(this@RecordingService, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("")
                .setContentIntent(notifyPendingIntent)
                .setSound(null)
                .setOngoing(true)
                .setVibrate(null)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setAutoCancel(false)
        }
//        notificationManager?.notify(Constants.RECORDING_NOTIFY_ID, notificationBuilder?.build())

        source = AudioSource.MIC
        channel = AudioChannel.STEREO
        sampleRate = AudioSampleRate.HZ_8000

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var state: RecordingState =
            intent?.getSerializableExtra(RECORDING_STATE) as RecordingState
        if (state == RecordingState.READY) {
            fileName = intent.getStringExtra(RECORDING_FILE_NAME) ?: ""
            filePath = intent.getStringExtra(RECORDING_FILE_PATH) ?: ""
        } else if (state == RecordingState.RESTART) {
            recorderSecondsElapsed = 0;
        } else {
            if (state == RecordingState.STOP || state == RecordingState.PAUSE || state == RecordingState.RESUME || state == RecordingState.START || state == RecordingState.EXIT) {
                toggleRecording(state)
            }
        }
        return START_NOT_STICKY

    }

    private fun toggleRecording(state: RecordingState) {
        AudioRecorderUtils.wait(100, Runnable {
            when (state) {
                RecordingState.STOP -> {
                    stopRecording()
                }
                RecordingState.PAUSE -> {
                    pauseRecording()
                }
                RecordingState.RESTART -> {
                    recorderSecondsElapsed = 0
                    stopTimer()
                }
                RecordingState.EXIT -> {
                    recorderSecondsElapsed = 0
                    stopTimer()
                    stopService()
                }
                else -> {
                    resumeRecording()
                }
            }
        })

    }

    public fun resumeRecording() {
        if (recorder == null) {
            recorder = OmRecorder.wav(
                PullTransport.Default(
                    AudioRecorderUtils.getMic(source!!, channel!!, sampleRate!!),
                    this@RecordingService
                ),
                File(filePath), true
            )
            AnalyticsUtils.getInstance(this@RecordingService)!!
                .logEvent(AnalyticsUtils.eventStartAudio)
        }
        AnalyticsUtils.getInstance(this@RecordingService)!!
            .logEvent(AnalyticsUtils.eventResumeAudio)
        recorder?.resumeRecording()
        startTimer()
    }

    public fun stopRecording() {
        if (recorder != null) {
            recorder?.stopRecording()
            recorder = null
        }
        stopTimer()
    }

    public fun pauseRecording() {
        if (recorder != null) {
            recorder?.pauseRecording()
        }
    }

    public fun sendEvent(event: RecordingState) {
        EventBus.getDefault().post(event)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAudioChunkPulled(audioChunk: AudioChunk?) {
        var event = RecordingState.AUDIO_RECEIVED
        event.value = audioChunk?.maxAmplitude()?.toFloat() ?: 0f
        sendEvent(event)
    }

    fun startTimer() {
        stopTimer()
        timer = Timer()
        updateTimerText()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer()
            }
        }, 1000, 1000)
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    fun updateTimer() {
        recorderSecondsElapsed++
        updateTimerText()

    }

    private fun updateTimerText() {
        var resumeState = RecordingState.RECORDING
        resumeState.value = recorderSecondsElapsed.toFloat()
        sendEvent(resumeState)
        updateNotificationTimer()
    }

    private fun updateNotificationTimer() {
        notificationBuilder?.setContentText(AudioRecorderUtils.formatSeconds(recorderSecondsElapsed))
        notificationManager?.notify(Constants.RECORDING_NOTIFY_ID, notificationBuilder?.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopService()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    public fun stopService() {
        stopTimer()
        stopService(Intent(this, RecordingService::class.java))
        notificationManager?.cancel(Constants.RECORDING_NOTIFY_ID)
    }

    companion object {
        fun isServiceProcessing(context: Context): Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (RecordingService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        public const val RECORDING_STATE: String = "recording_state"
        public const val RECORDING_FILE_PATH: String = "recording_file_path"
        public const val RECORDING_FILE_NAME: String = "recording_file_name"
    }

}