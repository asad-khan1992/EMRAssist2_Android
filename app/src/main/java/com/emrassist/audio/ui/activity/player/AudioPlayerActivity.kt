package com.emrassist.audio.ui.activity.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emrassist.audio.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_audio_player.*
import java.io.File

class AudioPlayerActivity : AppCompatActivity() {
    private var name: String = ""
    private var playUrl: String? = null
    private var isLocal: Boolean = false

    private var player: SimpleExoPlayer? = null

    companion object {
        const val URL: String = "url"
        const val IS_LOCAL: String = "is_local"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        getData();
    }

    private fun getData() {
        playUrl = intent.getStringExtra(URL)
        isLocal = intent.getBooleanExtra(IS_LOCAL, false)
        name = File(playUrl).name
        text_audio_name.text = name
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),
            DefaultTrackSelector(), DefaultLoadControl()
        )
        video_view.setPlayer(player)
        player?.setPlayWhenReady(true)
        player?.seekTo(0, 0)
        video_view.setControllerShowTimeoutMs(0)
        val uri = Uri.parse(playUrl)
        val mediaSource = buildMediaSource(uri)
        player?.prepare(mediaSource, true, false)
        player?.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_ENDED -> onBackPressed()
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

            override fun onPlayerError(error: ExoPlaybackException?) {}

            override fun onPositionDiscontinuity(reason: Int) {}

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onSeekProcessed() {}
        })
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return if (isLocal) {
            val playerInfo = Util.getUserAgent(this, "ExoPlayerInfo")
            val dataSourceFactory = DefaultDataSourceFactory(
                this, playerInfo
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

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
//        video_view.setSystemUiVisibility(
//            View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        )
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            player?.release()
            player = null
        }
    }

}