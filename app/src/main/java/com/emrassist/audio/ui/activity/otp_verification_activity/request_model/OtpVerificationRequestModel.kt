package com.emrassist.audio.ui.activity.otp_verification_activity.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OtpVerificationRequestModel(
    @SerializedName("email")
    @Expose
    var email: String,
    @SerializedName("verification_code")
    @Expose
    var verification_code: String,
) {}