package com.emrassist.audio.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DictationListResponseModel {
    @SerializedName("user_id")
    @Expose
    var userId: String = "";

    @SerializedName("files_urls")
    @Expose
    var data: RecorderFilesData? = null
}

class RecorderFilesData {
    @SerializedName("current_page")
    @Expose
    var currentPage: Int = 0

    @SerializedName("data")
    @Expose
    var listOfAudios: ArrayList<RecordedItem> = ArrayList()

    @SerializedName("last_page")
    @Expose
    var lastPage: Int = 0

    @SerializedName("total")
    @Expose
    var total: Int = 0
}