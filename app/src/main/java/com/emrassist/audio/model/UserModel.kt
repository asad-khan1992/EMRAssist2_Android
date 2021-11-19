package com.emrassist.audio.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserModel :Serializable{
    fun updateData(data: UserModel?) {
        if(data!=null){
            firstName = data.firstName
            lastName = data.lastName
            email = data.email
            phoneNumber = data.phoneNumber
        }


    }

    @SerializedName("id")
    @Expose
    var id: String = ""
        get() = field

    @SerializedName("account_type")
    @Expose
    var accountType: String? = null

    @SerializedName("email")
    @Expose
    var email: String = ""

    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String = ""

    @SerializedName("status")
    @Expose
    var status: String = ""

    @SerializedName("first_name")
    @Expose
    var firstName: String = ""

    @SerializedName("last_name")
    @Expose
    var lastName: String = ""

    @SerializedName("password")
    @Expose
    var password: String = ""

    @SerializedName("auth_token")
    @Expose
    var token: String = ""

}