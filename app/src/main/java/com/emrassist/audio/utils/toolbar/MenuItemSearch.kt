package com.emrassist.audio.utils.toolbar

import android.text.TextWatcher
import android.view.View

class MenuItemSearch : MenuItem {
    var textWatcher: TextWatcher
        private set
    var onETClickListener: View.OnClickListener
    var onIBClickListener: View.OnClickListener
    var hint: String
        private set
    var text: String? = null
    var resourceId = -1

    constructor(
        hint: String,
        text: String?,
        textWatcher: TextWatcher,
        onETClickListener: View.OnClickListener,
        onIBClickListener: View.OnClickListener
    ) {
        this.hint = hint
        this.text = text
        this.textWatcher = textWatcher
        this.onETClickListener = onETClickListener
        this.onIBClickListener = onIBClickListener
    }

    constructor(
        str: String,
        resourceId: Int,
        textWatcher: TextWatcher,
        onETClickListener: View.OnClickListener,
        onIBClickListener: View.OnClickListener
    ) {
        hint = str
        this.textWatcher = textWatcher
        this.resourceId = resourceId
        this.onETClickListener = onETClickListener
        this.onIBClickListener = onIBClickListener
    }

    fun setTitle(title: String) {
        hint = title
    }

}