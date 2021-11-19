package com.emrassist.audio.service.recording

enum class RecordingState(var value: Float) {
    READY(0f),
    START(1f),
    STOP(2f),
    RESUME(3f),
    PAUSE(4f),
    RECORDING(5f),
    RESTART(6f),
    EXIT(7f),
    AUDIO_RECEIVED(8f);

}
