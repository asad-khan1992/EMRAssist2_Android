package com.emrassist.audio.utils.audiorecorder.model

import android.media.MediaRecorder

enum class AudioSource {
    MIC, CAMCORDER;

    val source: Int
        get() = when (this) {
            CAMCORDER -> MediaRecorder.AudioSource.CAMCORDER
            else -> MediaRecorder.AudioSource.MIC
        }
}