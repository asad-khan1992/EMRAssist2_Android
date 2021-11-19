package com.emrassist.audio.utils.toolbar

import android.view.View

class MenuItemImgOrStr : MenuItem {
    var resourceId: Int
    var onClickListener: View.OnClickListener
    var title: String?
        private set

    constructor(_resourceId: Int, _onClickListener: View.OnClickListener) {
        resourceId = _resourceId
        title = null
        onClickListener = _onClickListener
    }

    constructor(str: String?, _onClickListener: View.OnClickListener) {
        resourceId = -1
        title = str
        onClickListener = _onClickListener
    }

}