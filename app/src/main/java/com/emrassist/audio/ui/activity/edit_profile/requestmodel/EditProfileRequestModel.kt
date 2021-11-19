package com.emrassist.audio.ui.activity.edit_profile.requestmodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EditProfileRequestModel(

    @SerializedName("first_name")
    @Expose
    var firstName: String,
    @SerializedName("last_name")
    @Expose
    var lastName: String,
    @SerializedName("user_id")
    @Expose
    var userId: String,
    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String
) {}