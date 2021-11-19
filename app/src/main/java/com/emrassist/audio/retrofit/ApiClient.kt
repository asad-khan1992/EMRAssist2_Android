package com.emrassist.audio.retrofit

import com.emrassist.audio.model.DictationListResponseModel
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.model.UserModel
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.service.audiouploading.request_model.UploadAudioRequestModel
import com.emrassist.audio.ui.activity.change_password.request_model.ChangePasswordRequestModel
import com.emrassist.audio.ui.activity.edit_profile.requestmodel.EditProfileRequestModel
import com.emrassist.audio.ui.activity.forgot_password.request_model.ForgotPasswordRequestModel
import com.emrassist.audio.ui.activity.login.requestmodel.LoginRequestModel
import com.emrassist.audio.ui.activity.main.request_model.LogoutRequestModel
import com.emrassist.audio.ui.activity.otp_verification_activity.request_model.OtpVerificationRequestModel
import com.emrassist.audio.ui.activity.register.requestmodel.RegisterRequestModel
import com.emrassist.audio.ui.activity.reset_password.request_model.RequestModelRequestModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiClient {
    @POST("login")
    suspend fun login(@Body loginRequestModel: LoginRequestModel): ApiResponse<UserModel>

    @POST("forgot_password")
    suspend fun forgotPassword(@Body email: ForgotPasswordRequestModel): ApiResponse<Any>

    @POST("verify_password_otp")
    suspend fun verifyPasswordOtp(@Body model: OtpVerificationRequestModel): ApiResponse<Any>

    @POST("reset_update_password_app")
    suspend fun resetPassword(@Body model: RequestModelRequestModel): ApiResponse<Any>

    @POST("register")
    suspend fun register(@Body model: RegisterRequestModel): ApiResponse<UserModel>

    @GET("audio_file_list")
    suspend fun getListOfAudios(
        @Query("user_id") id: String,
        @Query("page") currentPage: Int,
    ): ApiResponse<DictationListResponseModel>

    @POST("add_file_url")
    suspend fun uploadFile(
        @Body model: UploadAudioRequestModel
    ): ApiResponse<RecordedItem>


    @POST("update_profile")
    suspend fun updateProfile(@Body model: EditProfileRequestModel): ApiResponse<UserModel>

    @POST("change_password")
    suspend fun changePassword(@Body model: ChangePasswordRequestModel): ApiResponse<Any>

    @POST("logout")
    suspend fun logout(@Body model: LogoutRequestModel): ApiResponse<Any>
}
