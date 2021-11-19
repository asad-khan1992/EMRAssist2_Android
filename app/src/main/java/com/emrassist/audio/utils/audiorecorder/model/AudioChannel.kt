package com.emrassist.audio.utils.audiorecorder.model

import android.media.AudioFormat

enum class AudioChannel {
    STEREO, MONO;

    val channel: Int
        get() = when (this) {
            MONO -> AudioFormat.CHANNEL_IN_MONO
            else -> AudioFormat.CHANNEL_IN_STEREO
        }
}