package com.emrassist.audio.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AlertOP {
    fun showAlert(
        context: Context?,
        title: String = "Alert",
        message: String = "",
        pBtnText: String = "Yes",
        nBtnText: String? = null,
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null,
        cancelable: Boolean = true
    ) {
        var positiveBtnText = pBtnText
        try {
            val builder =
                AlertDialog.Builder(context!!)
            builder.setTitle(title)
            builder.setCancelable(cancelable)
            builder.setMessage(message)
            if (positiveBtnText.isEmpty()) positiveBtnText = "Yes"
            // Set dialog positive button and clickListener
            builder.setPositiveButton(
                positiveBtnText
            ) { dialog, _ ->
                dialog.dismiss()
                onPositiveClick?.invoke()
            }

            // if also have negative button then set it
            if (nBtnText != null && nBtnText.isNotEmpty()) {
                builder.setNegativeButton(
                    nBtnText
                ) { dialog, _ ->
                    dialog.dismiss()
                    onNegativeClick?.invoke()
                }
            }
            val dialog = builder.create()
            dialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

} //main
