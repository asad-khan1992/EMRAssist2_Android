package com.emrassist.audio.ui.activity.audio_recorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.emrassist.audio.R
import com.emrassist.audio.broadcast.CallStateManager
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.broadcast.State
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.room.AudioRecordDao
import com.emrassist.audio.service.audiouploading.serviceManager.UploadServiceManager
import com.emrassist.audio.service.recording.RecordingService
import com.emrassist.audio.service.recording.RecordingState
import com.emrassist.audio.ui.activity.audio_recorder.view_model.AudioRecorderViewModel
import com.emrassist.audio.utils.*
import com.emrassist.audio.utils.audiorecorder.AudioRecorderUtils
import com.emrassist.audio.utils.audiorecorder.AudioRecorderUtils.getDarkerColor
import com.emrassist.audio.utils.audiorecorder.VisualizerHandler
import com.emrassist.audio.utils.audiorecorder.model.AudioChannel
import com.emrassist.audio.utils.audiorecorder.model.AudioSampleRate
import com.emrassist.audio.utils.audiorecorder.model.AudioSource
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_audio_recorder.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import omrecorder.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class AudioRecorderActivity : AppCompatActivity(), PullTransport.OnAudioChunkPulledListener,
    OnCompletionListener {

    private var isAudioFocusLost: Boolean = false
    private lateinit var handler: Handler
    private lateinit var focusRequest: AudioFocusRequest

    //    private lateinit var phoneStateReceiver: PhoneStateReceiver
    private lateinit var audioManager: AudioManager
    val viewModel: AudioRecorderViewModel by viewModels()

    companion object {
        const val PARAM_FILE_NAME: String = "file_name"
    }

    @Inject
    lateinit var audioRecorderDao: AudioRecordDao

    var fileName: String = ""
    var filePath: String = ""
    var source: AudioSource? = null
    var channel: AudioChannel? = null
    var sampleRate: AudioSampleRate? = null
    var color = 0
    var autoStart = false
    var keepDisplayOn = false
    var player: SimpleExoPlayer? = null

    //    MediaPlayer player;
    //kmphasis
    //kmphasis
    var recorder: Recorder? = null
    var visualizerHandler: VisualizerHandler? = null
    var timer: Timer? = null
    var recorderSecondsElapsed = 0
    var playerSecondsElapsed = 0
    var isRecording = false
    var hasRecorded = false
    var visualizerView: GLAudioVisualizationView? = null
    var isPlayed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recorder)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        getDataFromIntent()
        initDefaultData()


        setDataToViews()
        setListeners()

        setUpObservers()
        setUpAudioManager()
        setUpReceiver()
        getFileNameFromUser()
        EventBus.getDefault().register(this)
    }

    @SuppressLint("SetTextI18n")
    private fun getFileNameFromUser() {

        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_file_name, null)
        builder.setView(view)

        val text: TextView = view.findViewById(R.id.text_confirm)
        val textCancel: TextView = view.findViewById(R.id.text_cancel)
        val editText: EditText = view.findViewById(R.id.edit_text_file_name);
        val dialog: androidx.appcompat.app.AlertDialog = builder.create()
        dialog.show()
//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        text.setOnClickListener(View.OnClickListener { //                    dialog.dismiss();
            if (editText.text.toString().isEmpty()) {
                AlertOP.showAlert(
                    this,
                    message = getString(R.string.alert_enter_file_name)
                )
                return@OnClickListener
            } else {
                fileName = editText.text.toString()
//                fileName = fileName.replace(" ", "-")
                getDataFromIntent()
                setRecordingServiceState(RecordingState.READY, fileName, filePath)
                text_filename?.setText("$fileName.wav")
                dialog.dismiss()
            }
        })
        textCancel.setOnClickListener(View.OnClickListener { //                    dialog.dismiss();
            finish()
        })
    }


    private fun setRecordingServiceState(
        event: RecordingState,
        fileName: String = "",
        filePath: String = ""
    ) {
        val intent = Intent(this, RecordingService::class.java)
        intent.putExtra(RecordingService.RECORDING_STATE, event)
        if (!filePath.isEmpty()) {
            intent.putExtra(RecordingService.RECORDING_FILE_PATH, filePath)
        }
        if (!fileName.isEmpty()) {
            intent.putExtra(RecordingService.RECORDING_FILE_NAME, fileName)
        }
        if (!RecordingService.isServiceProcessing(this)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startService(intent)
//        ContextCompat.startForegroundService(this, intent)
    }

    private fun setUpReceiver() {
//        val filter = IntentFilter()
//        filter.addAction("android.intent.action.PHONE_STATE")
//        phoneStateReceiver = PhoneStateReceiver()
//        registerReceiver(phoneStateReceiver, filter)
    }


    private fun setUpAudioManager() {
        handler = Handler(Looper.getMainLooper())
        // initializing variables for audio focus and playback management
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(
            afChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
//                if (isRecording || isPlaying) {
//                    onPauseRecording()
//                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                isAudioFocusLost = true
                if (isRecording || isPlaying) {
//                    Toast.makeText(this,"Audio focus Gone", Toast.LENGTH_SHORT).show()
                    onPauseRecording()
                }
                // Pause playback
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
//                Toast.makeText(this,"Audio focus Gain", Toast.LENGTH_SHORT).show()
                isAudioFocusLost = false

            }
        }
    }        // player = new MediaPlayer();

    private fun setUpObservers() {
        viewModel.isRecordingSaved.observe(this, androidx.lifecycle.Observer {
            if (it > 0) {
                AnalyticsUtils.getInstance(this)?.logEvent(AnalyticsUtils.eventSaveAudio)
                if (!NetworkReceiver.isNetworkAvailable(this)) {
                    Toast.makeText(
                        this,
                        "Internet not available. Audio will be uploaded when internet is connected.",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                        this,
                        "Uploading Audio...",
                        Toast.LENGTH_SHORT
                    ).show();
                }
                UploadServiceManager.startService(this)
                Handler(Looper.getMainLooper()).postDelayed({
                    setResult(RESULT_OK)
                    finish()
                }, 1000)
            } else {
                Toast.makeText(
                    this,
                    "Error in saving file locally. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        })
        viewModel.dataSet.observe(this, androidx.lifecycle.Observer {
        })
    }

    private fun setListeners() {
        text_restart.setOnClickListener { view ->
            if (isCallInProgress()) {
                AlertOP.showAlert(this, message = "Cannot Reset Recording.", pBtnText = "Ok")
                return@setOnClickListener
            }
            val dialog = AlertDialog.Builder(this@AudioRecorderActivity)
                .setTitle("Alert")
                .setMessage("Your recording is not saved. Would you like to Redo it?")
                .setPositiveButton(
                    "Cancel"
                ) { dialog, which -> dialog.dismiss() }
                .setNegativeButton(
                    "Yes"
                ) { dialog, which ->
                    dialog.dismiss()
                    AnalyticsUtils.getInstance(this@AudioRecorderActivity)!!
                        .logEvent(AnalyticsUtils.eventRestartAudio)
                    resetRecording(view)
                }.create()
            dialog.show()
        }
        text_cancel.setOnClickListener { view ->
            onBackPressed()
        }
        text_complete.setOnClickListener {
            writeLog("Recording Complete")

            text_complete.isClickable = false
            AnalyticsUtils.getInstance(this@AudioRecorderActivity)!!
                .logEvent(AnalyticsUtils.eventCompleteAudio)
            selectAudio()
        }
        text_play.setOnClickListener { view -> togglePlaying(view) }
        start_recording_button.setOnClickListener { view ->
            toggleRecording(view)
        }

        isPlayed = false
    }

    private fun setDataToViews() {
        text_filename?.setText("$fileName.wav")
        relative_layout_content!!.setBackgroundColor(getDarkerColor(color))
        relative_layout_content!!.addView(visualizerView, 0)
        text_restart!!.visibility = View.INVISIBLE
        text_play!!.visibility = View.INVISIBLE
        text_complete!!.visibility = View.INVISIBLE
        if (AudioRecorderUtils.isBrightColor(color)) {
            text_status!!.setTextColor(Color.BLACK)
            text_timer!!.setTextColor(Color.BLACK)
            text_restart!!.setTextColor(Color.BLACK)
            start_recording_button!!.setColorFilter(Color.BLACK)
            text_play!!.setTextColor(Color.BLACK)
            text_cancel!!.setTextColor(Color.BLACK)
            text_complete!!.setTextColor(Color.BLACK)
        }
    }

    private fun initDefaultData() {
        source = AudioSource.MIC
        channel = AudioChannel.STEREO
        sampleRate = AudioSampleRate.HZ_8000
        color = Color.GRAY
        autoStart = false
        keepDisplayOn = true
        visualizerView = GLAudioVisualizationView.Builder(this)
            .setLayersCount(1)
            .setWavesCount(6)
            .setWavesHeight(R.dimen.aar_wave_height)
            .setWavesFooterHeight(R.dimen.aar_footer_height)
            .setBubblesPerLayer(20)
            .setBubblesSize(R.dimen.aar_bubble_size)
            .setBubblesRandomizeSize(true)
            .setBackgroundColor(getDarkerColor(color))
            .setLayerColors(intArrayOf(color))
            .build()
    }

    private fun getDataFromIntent() {
//        fileName = intent.getStringExtra(PARAM_FILE_NAME) ?: "file"
        filePath = "$filesDir/" + fileName.replace(
            "[^A-Za-z0-9_-]",
            ""
        ) + "_" + System.currentTimeMillis() + ".wav"
    }

    fun isConnected(): Boolean {
        return false;
    }

    fun onCancelPress(v: View?) {
        writeLog("Recording Canceled")
        stopRecording()
        deleteRecording()
        finish()
    }

    fun onComplete(v: View) {
        selectAudio()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (autoStart && !isRecording) {
            toggleRecording(null)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            visualizerView!!.onResume()
        } catch (e: Exception) {
        }
    }

    private fun onPauseRecording() {
        pauseRecording()
        stopRecording()

    }

    override fun onPause() {
        try {
            visualizerView!!.onPause()
        } catch (e: Exception) {
        }
        super.onPause()
    }

    override fun onDestroy() {
//        unregisterReceiver(phoneStateReceiver)
        stopRecording()
        stopSevice()
        setResult(RESULT_CANCELED)
        try {
            visualizerView!!.release()
        } catch (e: Exception) {
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun stopSevice() {
        val i = Intent(this, RecordingService::class.java)
        stopService(i)
    }

    override fun onAudioChunkPulled(audioChunk: AudioChunk) {
        val amplitude = if (isRecording) audioChunk.maxAmplitude() else 0f
        visualizerHandler?.onDataReceived(amplitude.toFloat())
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        stopPlaying()
    }

    fun selectAudio() {
        val progressDialog = DialogLoader(this)
        progressDialog.show()

        stopRecording()
        val recorderItem: RecordedItem = RecordedItem();
        recorderItem.fileName = fileName;
        recorderItem.recordedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())
        recorderItem.localPath = File(filePath).absolutePath
        recorderItem.uniqueId = System.currentTimeMillis().toString();

        recorderItem.duration = Utils.getDurationOfAudio(filePath)

        viewModel.insertRecordedItem(recorderItem)

        progressDialog.dismiss()
    }

    fun toggleRecording(v: View?) {
        if (isCallInProgress()) {
            AlertOP.showAlert(this, message = "Cannot Start Recording.", pBtnText = "Ok")
            return
        }
        stopPlaying()
        AudioRecorderUtils.wait(100, Runnable {
            if (isRecording) {
                writeLog("Recording Stopped")

                stopRecording()
                AnalyticsUtils.getInstance(this@AudioRecorderActivity)!!
                    .logEvent(AnalyticsUtils.eventPauseAudio)
            } else {
                if (!hasRecorded) {
                    hasRecorded = true;
                }
                writeLog("Recording Stopped")
                resumeRecording()
            }
        })
    }

    fun togglePlaying(v: View?) {
        if (isCallInProgress()) {
            AlertOP.showAlert(this, message = "Cannot Play Recording.", pBtnText = "Ok")
            return
        }
        stopRecording()
        AudioRecorderUtils.wait(100, Runnable {
            if (isPlaying) {
                writeLog("Audio Playing Stop")

                stopPlaying()
                AnalyticsUtils.getInstance(this@AudioRecorderActivity)!!
                    .logEvent(AnalyticsUtils.eventStopAudio)
            } else {
                writeLog("Audio Playing Started")

                AnalyticsUtils.getInstance(this@AudioRecorderActivity)!!
                    .logEvent(AnalyticsUtils.eventPlayAudio)
                startPlaying()
            }
        })
    }

    private fun isCallInProgress(): Boolean {
        return isAudioFocusLost ||
                audioManager.mode == AudioManager.RINGER_MODE_NORMAL ||
                audioManager.mode == AudioManager.RINGER_MODE_VIBRATE ||
                audioManager.mode == AudioManager.MODE_IN_CALL ||
                audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    fun resetRecording(v: View?) {
        writeLog("Restarting Recording")

        start_recording_button!!.setImageResource(R.drawable.aar_ic_rec)
        text_timer!!.text = "00:00:00"
        recorderSecondsElapsed = 0
        playerSecondsElapsed = 0
        isPlayed = false
        stopRecording()
        stopPlaying()
        resetRecordingEvent()
        deleteRecording();
        visualizerHandler = VisualizerHandler()
        visualizerView!!.linkTo(visualizerHandler!!)
        visualizerView!!.release()
        if (visualizerHandler != null) {
            visualizerHandler?.stop()
        }
        text_status!!.visibility = View.INVISIBLE
        text_restart!!.visibility = View.INVISIBLE
        text_play!!.visibility = View.INVISIBLE
        text_complete!!.visibility = View.INVISIBLE
        toggleRecording(v)
    }

    private fun resetRecordingEvent() {
        setRecordingServiceState(RecordingState.RESTART)
    }

    private fun deleteRecording() {
        if (File(filePath).exists()) {
            File(filePath).delete()
        }
    }

    fun restartRecording(v: View?) {
        if (isRecording) {
            stopRecording()
        } else if (isPlaying) {
            stopPlaying()
        } else {
            visualizerHandler = VisualizerHandler()
            visualizerView!!.linkTo(visualizerHandler!!)
            visualizerView!!.release()
            if (visualizerHandler != null) {
                visualizerHandler?.stop()
            }
        }
        text_status!!.visibility = View.INVISIBLE
        text_restart!!.visibility = View.INVISIBLE
        text_play!!.visibility = View.INVISIBLE
        text_complete!!.visibility = View.INVISIBLE
        start_recording_button!!.setImageResource(R.drawable.aar_ic_rec)
        text_timer!!.text = "00:00:00"
        recorderSecondsElapsed = 0
        playerSecondsElapsed = 0
    }

    fun resumeRecording() {
        start_recording_button!!.visibility = View.VISIBLE
        isRecording = true
        text_status!!.setText(R.string.aar_recording)
        text_status!!.visibility = View.VISIBLE
        text_restart!!.visibility = View.INVISIBLE
        text_play!!.visibility = View.INVISIBLE
        text_complete!!.visibility = View.INVISIBLE
        start_recording_button!!.setImageResource(R.drawable.aar_ic_pause)
        text_play!!.text = "Play"
        visualizerHandler = VisualizerHandler()
        visualizerView!!.linkTo(visualizerHandler!!)
        setRecordingServiceState(RecordingState.RESUME)
//        startTimer()
    }

    //
    fun pauseRecording() {
        isRecording = false
        text_status!!.setText(R.string.aar_paused)
        text_status!!.visibility = View.VISIBLE
        text_restart!!.visibility = View.VISIBLE
        text_play!!.visibility = View.VISIBLE
        text_complete!!.visibility = View.VISIBLE
        start_recording_button!!.setImageResource(R.drawable.aar_ic_rec)
        text_play!!.text = "Play"
        visualizerView!!.release()
        if (visualizerHandler != null) {
            visualizerHandler?.stop()
        }
        setRecordingServiceState(RecordingState.PAUSE)
//        stopTimer()
    }


    fun stopRecording() {
        isRecording = false
        text_status!!.setText(R.string.aar_paused)
        text_status!!.visibility = View.VISIBLE
        text_restart!!.visibility = View.VISIBLE
        text_play!!.visibility = View.VISIBLE
        text_complete!!.visibility = View.VISIBLE
        start_recording_button!!.setImageResource(R.drawable.aar_ic_rec)
        text_play!!.text = "Play"
        visualizerView!!.release()
        if (visualizerHandler != null) {
            visualizerHandler?.stop()
        }
//        stopTimer()
//        recorderSecondsElapsed = 0;
        setRecordingServiceState(RecordingState.STOP)
    }

    fun startPlaying() {
        try {
            isPlayed = true
            stopRecording()
//            player = new MediaPlayer();
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                DefaultTrackSelector(), DefaultLoadControl()
            )


            //kmphasis

            //kmphasis
            val file = File(filePath)
            Log.e("11/01 filepath ", filePath!!)
            Log.e("11/01 file.path ", file.absolutePath)

            if (file.exists()) {
                Log.e("11/01/2020 file exists ", "exists")
            } else {
                Log.e("file not exists ", "not exists")
            }
            player!!.playWhenReady = true
            player!!.seekTo(0, 0)
            val uri = Uri.parse("file://" + file.absolutePath)
            Log.e(
                "11/01 uri", uri.toString()
                        + "getAbsolutePath " + file.absolutePath
            )
            val mediaSource = buildMediaSource(uri)
            player!!.prepare(mediaSource, true, false)
//            recorderSecondsElapsed = player!!.duration.toInt()
            text_timer!!.text = "00:00:00"
            text_status!!.setText(R.string.aar_playing)
            text_status!!.visibility = View.VISIBLE
            text_play!!.text = "Stop"

            playerSecondsElapsed = 0
            startTimer()
            player!!.addListener(object : Player.EventListener {
                override fun onTimelineChanged(
                    timeline: Timeline,
                    @Nullable manifest: Any?,
                    reason: Int
                ) {
                }

                override fun onTracksChanged(
                    trackGroups: TrackGroupArray,
                    trackSelections: TrackSelectionArray
                ) {
                }

                override fun onLoadingChanged(isLoading: Boolean) {}
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                        }
                        Player.STATE_ENDED -> stopPlaying()
                        Player.STATE_IDLE -> {
                        }
                        Player.STATE_READY -> {
                        }
                        else -> {
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {}
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
                override fun onPlayerError(error: ExoPlaybackException) {}
                override fun onPositionDiscontinuity(reason: Int) {}
                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
                override fun onSeekProcessed() {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun buildMediaSource(uri: Uri): ExtractorMediaSource? {
        return if (uri.toString().startsWith("file://")) {
            val playerInfo = Util.getUserAgent(
                this@AudioRecorderActivity,
                "ExoPlayerInfo"
            )
            val dataSourceFactory = DefaultDataSourceFactory(
                this@AudioRecorderActivity, playerInfo
            )
            ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(DefaultExtractorsFactory())
                .createMediaSource(uri)
        } else {
            ExtractorMediaSource.Factory(
                DefaultHttpDataSourceFactory("exoplayer-codelab")
            ).createMediaSource(uri)
        }
    }

    fun stopPlaying() {
        text_status!!.text = ""
        text_status!!.visibility = View.INVISIBLE
        text_play!!.text = "Play"
        visualizerView!!.release()
        if (visualizerHandler != null) {
            visualizerHandler?.stop()
        }
        if (player != null) {
            try {
                player!!.stop()
                //                player.reset();
                player!!.playWhenReady = true
                player!!.seekTo(0, 0)
            } catch (e: Exception) {
            }
        }
        stopTimer()
    }

    //            return player != null && player.isPlaying() && !isRecording;
    val isPlaying: Boolean
        get() = try {
            //            return player != null && player.isPlaying() && !isRecording;
            player != null && player!!.playbackState == Player.STATE_READY && player!!.playWhenReady && !isRecording
        } catch (e: Exception) {
            false
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
        runOnUiThread(Runnable {
            if (isRecording) {
                recorderSecondsElapsed++
            } else if (isPlaying) {
                playerSecondsElapsed++
                if (playerSecondsElapsed > recorderSecondsElapsed) {
                    recorderSecondsElapsed = playerSecondsElapsed
                }
            }
            updateTimerText()
        })
    }

    private fun updateTimerText() {
        if (isRecording) {
            text_timer.setText(AudioRecorderUtils.formatSeconds(recorderSecondsElapsed))
        } else if (isPlaying) {
            text_timer.setText(AudioRecorderUtils.formatSeconds(playerSecondsElapsed))
        }
    }

    override fun onBackPressed() {
        if (hasRecorded) {
            stopRecording()
            stopPlaying()
        }
        val dialog = AlertDialog.Builder(this@AudioRecorderActivity)
            .setTitle("Alert")
            .setMessage("Your recording is not saved. Would you like to cancel it?")
            .setPositiveButton(
                "No"
            ) { dialog, _ -> dialog.dismiss() }
            .setNegativeButton(
                "Yes"
            ) { dialog, _ ->
                dialog.dismiss()
                onCancelPress(null)
            }.create()
        dialog.show()
    }

    fun writeLog(message: String) {
        CrashlyticsUtils.log(message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallStateArrived(data: CallStateManager) {
        if (data.state == State.RINGING) {
            if (isRecording || isPlaying) {
                onPauseRecording()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAudioRecordingStateChange(event: RecordingState) {
        when (event) {
            RecordingState.RECORDING -> {
                updateRecordingState(event.value)
            }
            RecordingState.AUDIO_RECEIVED -> {
                updateVisualizerView(event.value)
            }
            else -> {
                updateViews(event)
            }
        }
    }

    private fun updateViews(event: RecordingState) {
        CoroutineScope(Dispatchers.Main).launch {
        }
    }

    private fun updateRecordingState(event: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            recorderSecondsElapsed = event.toInt()
            text_timer.text = AudioRecorderUtils.formatSeconds(recorderSecondsElapsed)

        }
    }

    private fun updateVisualizerView(amplitude: Float) {
        CoroutineScope(Dispatchers.Main).launch {
            visualizerHandler?.onDataReceived(amplitude)
        }
    }
}