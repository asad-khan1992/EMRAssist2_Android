package com.emrassist.audio.utils

import android.graphics.Color

object ColorOP {
    fun getDarkerColor(color: Int): Int {
        val factor = 0.8f
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
            a,
            Math.max((r * factor).toInt(), 0),
            Math.max((g * factor).toInt(), 0),
            Math.max((b * factor).toInt(), 0)
        )
    }
}