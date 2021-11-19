package com.emrassist.audio.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.emrassist.audio.R

class DialogLoader(context: Context) :
    Dialog(context, R.style.CustomDialogNormalStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert_loader_layout)
        setCancelable(false)
    }
}