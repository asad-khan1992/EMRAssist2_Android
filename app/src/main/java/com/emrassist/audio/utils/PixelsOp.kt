package com.emrassist.audio.utils

import android.content.Context

object PixelsOp {
    fun convertToDensity(context: Context, pixels: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (pixels * scale + 0.5f).toInt()
    }

    fun dpFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}