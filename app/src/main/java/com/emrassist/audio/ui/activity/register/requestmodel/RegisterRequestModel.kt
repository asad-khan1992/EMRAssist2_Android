package com.emrassist.audio.ui.activity.register.requestmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RegisterRequestModel(

    @SerializedName("first_name")
    @Expose
    var firstName: String,
    @SerializedName("last_name")
    @Expose
    var lastName: String,
    @SerializedName("email")
    @Expose
    var email: String,
    @SerializedName("password")
    @Expose
    var password: String,
    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String
) {}