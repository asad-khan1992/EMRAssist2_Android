package com.emrassist.audio.aws

import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.File

class AWSUploader(
    private var transferUtility: TransferUtility,
    private var s3Client: AmazonS3Client
) {

    @Suppress("PrivatePropertyName")
    private val TAG: String = AWSUploader::class.java.simpleName
    private var metadata: ObjectMetadata = ObjectMetadata()

    init {
        metadata.contentType = "media/vnd.wav"
    }

    fun uploadFile(
        currentFile: File,
        onUploadCompleted: ((String) -> Unit)? = null,
        onProgressChange: ((Int) -> Unit)? = null,
        onUploadFailed: ((java.lang.Exception) -> Unit)? = null
    ) {
        val uploadObserver: TransferObserver =
            transferUtility.upload(currentFile.name, currentFile, metadata)
        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    Log.d(TAG, "com.app.service: checkAndExecuteOperation: s3 uploading completed")
                    //changes bucket name in QueueService to get resource URL
//                    val url: String = s3Client.getResourceUrl("emrassist", currentFile.name)
                    val url: String = "https://emrassist.s3.amazonaws.com/" + currentFile.name
                    onUploadCompleted?.invoke(url)

                } else if (TransferState.FAILED == state) {
                    onUploadFailed?.invoke(java.lang.Exception("Failed to upload Audio. Something went wrong"))
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                Log.d(TAG, "com.app.service: checkAndExecuteOperation: s3 uploading in progress")
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.d(
                    TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                            + " bytesTotal: " + bytesTotal + " " + percentDone + "%"
                )
                onProgressChange?.invoke(percentDone)

            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
                onUploadFailed?.invoke(ex)
            }
        })
    }
}