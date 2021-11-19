package com.emrassist.audio.retrofit.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ApiResponse<T> : Serializable {
    @SerializedName("status")
    @Expose
    var success: Int = 0
        get() {
            return field;
        }

    @SerializedName("message")
    @Expose
    var message: String = ""
        get() {
            return field;
        }

    @SerializedName("data")
    @Expose
    var responseObject: T? = null
        get() {
            return field;
        }

    @SerializedName("code")
    @Expose
    var responseCode: String = ""
        get() {
            return field;
        }
}