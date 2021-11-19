package com.emrassist.audio.ui.activity.change_password.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ChangePasswordRequestModel(
    @SerializedName("user_id")
    @Expose
    var id: String,
    @SerializedName("old_password")
    @Expose
    var oldPassword: String,
    @SerializedName("password")
    @Expose
    var password: String
) {}