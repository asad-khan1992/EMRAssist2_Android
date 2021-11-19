package com.emrassist.audio.ui.activity.main.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LogoutRequestModel(
    @SerializedName("user_id")
    @Expose
    var id: String
) {}