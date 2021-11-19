package com.emrassist.audio.di

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtilityOptions
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.emrassist.audio.App
import com.emrassist.audio.aws.AWSManager
import com.emrassist.audio.aws.AWSUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class AWSModule {
    @Singleton
    @Provides
    fun provideTransferUtilityOption(): TransferUtilityOptions {
        val transferUtilityOptions = TransferUtilityOptions()
        transferUtilityOptions.transferThreadPoolSize = 8
        return transferUtilityOptions
    }

    @Singleton
    @Provides
    fun provideCognitoCachingCredentialsProvider(): CognitoCachingCredentialsProvider {
        return CognitoCachingCredentialsProvider(
            App.context, // Context
            "us-east-2:1f8dde53-e9d6-4f04-b3c2-5a166b204b19", // Identity Pool ID
            Regions.US_EAST_2 // Region
        )
//        return CognitoCachingCredentialsProvider(
//            App.context, // Context
//            "us-west-2:738593ad-df9e-40a0-9808-38bf481e3811", // Identity Pool ID
//            Regions.US_WEST_2 // Region
//        )
    }

    @Singleton
    @Provides
    fun provideTransferUtility(
        options: TransferUtilityOptions,
        credentialsProvider: CognitoCachingCredentialsProvider
    ): TransferUtility {
        return TransferUtility.builder()
            .context(App.context)
            .defaultBucket("emrassist")
            .transferUtilityOptions(options)
            .awsConfiguration(provideAWSConfiguration())
            .s3Client(providesAwsS3Client(credentialsProvider))
            .build()
    }

    @Singleton
    @Provides
    fun provideAWSConfiguration(): AWSConfiguration? {
        return AWSMobileClient.getInstance().configuration
    }

    @Singleton
    @Provides
    fun providesAwsS3Client(credentialsProvider: CognitoCachingCredentialsProvider): AmazonS3Client {
//        return AmazonS3Client(credentialsProvider, Region.getRegion(Regions.US_EAST_2))
//        return AmazonS3Client(credentialsProvider, Region.getRegion(Regions.US_WEST_2))
        return AmazonS3Client(credentialsProvider)
    }

    @Singleton
    @Provides
    fun provideAwsUploader(
        transferUtility: TransferUtility,
        awsS3Client: AmazonS3Client
    ): AWSUploader {
        return AWSUploader(transferUtility, awsS3Client)
    }

    @Singleton
    @Provides
    fun provideAWSManager(awsUploader: AWSUploader): AWSManager {
        return AWSManager(awsUploader)
    }
}