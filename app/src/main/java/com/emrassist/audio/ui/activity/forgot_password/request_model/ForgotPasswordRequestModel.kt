package com.emrassist.audio.ui.activity.forgot_password.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ForgotPasswordRequestModel(
    @SerializedName("email")
    @Expose
    var email: String,
) {}