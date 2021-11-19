package com.emrassist.audio.ui.activity.login.requestmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginRequestModel(
    @SerializedName("user_name")
    @Expose
    var email: String,
    @SerializedName("password")
    @Expose
    var password: String
) {}