package com.emrassist.audio.ui.activity.reset_password.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestModelRequestModel(
    @SerializedName("email")
    @Expose
    var email: String,
    @SerializedName("verification_code")
    @Expose
    var code: String,
    @SerializedName("password")
    @Expose
    var password: String
) {}