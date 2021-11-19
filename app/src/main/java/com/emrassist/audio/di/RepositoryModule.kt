package com.emrassist.audio.di

import com.emrassist.audio.retrofit.ApiClient
import com.emrassist.audio.room.AudioRecordDao
import com.emrassist.audio.service.audiouploading.repository.UploadServiceRepository
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import com.emrassist.audio.ui.activity.change_password.repository.ChangePasswordRepository
import com.emrassist.audio.ui.activity.dictation_list.repository.DictationListRepository
import com.emrassist.audio.ui.activity.edit_profile.repository.EditProfileRepository
import com.emrassist.audio.ui.activity.forgot_password.repository.ForgotPasswordRepository
import com.emrassist.audio.ui.activity.login.repository.LoginRepository
import com.emrassist.audio.ui.activity.login_with_number.repository.LoginUsingNumberRepository
import com.emrassist.audio.ui.activity.main.repository.MainActivityRepository
import com.emrassist.audio.ui.activity.register.repository.RegisterRepository
import com.emrassist.audio.ui.activity.reset_password.repository.ResetPasswordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Singleton
    @Provides
    fun provideUserVerificationRepositoryModule(apiClient: ApiClient): LoginRepository {
        return LoginRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideLoginUsingNumberRepositoryModule(apiClient: ApiClient): LoginUsingNumberRepository {
        return LoginUsingNumberRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideAudioRecorderRepository(recorderDao: AudioRecordDao): AudioRecorderRepository {
        return AudioRecorderRepository(recorderDao)
    }

    @Singleton
    @Provides
    fun provideUploadServiceRepository(apiClient: ApiClient): UploadServiceRepository {
        return UploadServiceRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideDictationListRepository(apiClient: ApiClient): DictationListRepository {
        return DictationListRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideForgotPasswordRepository(apiClient: ApiClient): ForgotPasswordRepository {
        return ForgotPasswordRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideResetPasswordRepository(apiClient: ApiClient): ResetPasswordRepository {
        return ResetPasswordRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideRegisterRepository(apiClient: ApiClient): RegisterRepository {
        return RegisterRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideEditProfileRepository(apiClient: ApiClient): EditProfileRepository {
        return EditProfileRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideChangePasswordRepository(apiClient: ApiClient): ChangePasswordRepository {
        return ChangePasswordRepository(apiClient)
    }

    @Singleton
    @Provides
    fun provideMainActivityRepository(apiClient: ApiClient): MainActivityRepository {
        return MainActivityRepository(apiClient)
    }
}