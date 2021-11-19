package com.emrassist.audio.service.audiouploading.request_model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class UploadAudioRequestModel(
    @SerializedName("unique_number") @Expose var id: String,
    @SerializedName("device_name") @Expose var deviceInfo: String,
    @SerializedName("device_type") @Expose var deviceType: String,
    @SerializedName("phone_number") @Expose var phoneNumber: String,
    @SerializedName("files_url") @Expose var uploadedUrl: String,
    @SerializedName("file_name") @Expose var fileName: String,
    @SerializedName("file_name_url") @Expose var localFileUrl: String,
    @SerializedName("user_id") @Expose var userId: String,
    @SerializedName("file_duration") @Expose var duration: String,
    @SerializedName("date") @Expose var date: String,
    @SerializedName("app_version") @Expose var appVersion: String
) {}