package com.emrassist.audio.broadcast

class CallStateManager(var state:State) {
}
enum class State{
    IDLE,
    RINGING,
    NONE
}