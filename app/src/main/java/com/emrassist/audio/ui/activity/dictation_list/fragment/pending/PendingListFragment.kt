package com.emrassist.audio.ui.activity.dictation_list.fragment.pending

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.emrassist.audio.R
import com.emrassist.audio.aws.AWSManager
import com.emrassist.audio.broadcast.NetworkReceiver
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.service.audiouploading.queueManager.QueueManager
import com.emrassist.audio.service.audiouploading.viewmodel.UploadServiceViewModel
import com.emrassist.audio.ui.activity.audio_recorder.repository.AudioRecorderRepository
import com.emrassist.audio.ui.activity.dictation_list.adapter.DictationListDatabaseAdapter
import com.emrassist.audio.ui.activity.dictation_list.view_model.DictationListViewModel
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.CrashlyticsUtils
import com.emrassist.audio.utils.DialogLoader
import com.emrassist.audio.utils.Utils
import com.emrassist.audio.utils.filemanager.FileManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dictation_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PendingListFragment : Fragment(R.layout.fragment_dictation_list) {
    var adapter: DictationListDatabaseAdapter? = null
    val viewModel: DictationListViewModel by viewModels()

    @Inject
    lateinit var uploadServiceViewModel: UploadServiceViewModel
    var dialogLoader: DialogLoader? = null

    @Inject
    lateinit var audioRecordingRepository: AudioRecorderRepository

    @Inject
    lateinit var awsManager: AWSManager
    fun newInstance(): PendingListFragment {
        return PendingListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogLoader = DialogLoader(requireContext())
        setUpObservers()
        setListeners()

    }

    private fun setUpObservers() {
        viewModel.dataStateRecording.observe(viewLifecycleOwner, Observer {
            swipe_refresh_layout?.isRefreshing = false
            if (it != null && it.isNotEmpty()) {
                recycler_view.visibility = View.VISIBLE
                text_no_records.visibility = View.GONE
                setupAdapter(it)
            } else {
                recycler_view.adapter = null;
                recycler_view.visibility = View.GONE
                text_no_records.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAdapter(list: ArrayList<RecordedItem>) {
        if (adapter == null || recycler_view.adapter == null) {
            adapter = DictationListDatabaseAdapter(requireContext(), list, true, recycler_view)
            recycler_view.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recycler_view.adapter = adapter;
            adapter?.setOnItemClickListener(object :
                DictationListDatabaseAdapter.OnItemClickListener {
                override fun onClick(position: Int, item: RecordedItem) {
                    Utils.openPlayerActivity(item, requireContext())
                }
            })
            adapter?.setOnItemRetryClickListener(object :
                DictationListDatabaseAdapter.OnItemRetryClickListener {
                override fun onClick(position: Int, item: RecordedItem) {
                    uploadItem(position, item)
                }
            })
        } else {
            adapter?.setList(list)
        }

    }

    private fun uploadItem(position: Int, item: RecordedItem) {
        if (NetworkReceiver.isNetworkAvailable(requireContext())) {
            writeLog("Pending list: Upload Button is pressed")
            checkAndUpload(position, item);
        } else {
            AlertOP.showAlert(
                requireContext(),
                message = "Please check you internet connection and try again",
                pBtnText = "Ok"
            )
        }
    }

    private fun checkAndUpload(position: Int, item: RecordedItem) {
        if (item.isFileUploaded) {
            uploadItemToServer(position, item)
        } else {
            uploadItemToS3(position, item)
        }
    }

    private fun uploadItemToServer(position: Int, item: RecordedItem) {
        writeLog("Pending list: Upload file to server ${Gson().toJson(item)}")
        uploadServiceViewModel.uploadFile(item) {
            when (it) {
                is DataState.Success -> {
                    writeLog("Pending list: Upload file to server ${Gson().toJson(item)}")
                    dialogLoader?.dismiss()
                    if (it.data.success == 1) {
                        AlertOP.showAlert(
                            requireContext(),
                            message = "File Uploaded Successfully",
                            pBtnText = "Ok"
                        )
                        writeLog(
                            "Pending list: Upload file to server: File ${Gson().toJson(item)} : Response: ${
                                Gson().toJson(
                                    it.data
                                )
                            }"
                        )

                    } else {
                        writeLog(
                            "Pending list: Upload file to server failed: File ${
                                Gson().toJson(
                                    item
                                )
                            } : Response: ${Gson().toJson(it.data)}"
                        )

//                        AlertOP.showAlert(
//                            requireContext(),
//                            message = it.data.message,
//                            pBtnText = "Ok"
//                        )
                        EventBus.getDefault().post(SessionExpired(true))
                    }
                    item.isUploadedToScribe = it.data.message.contains("File url added successully")
                    item.isPendingResponse = false
                    updateRecordInDatabase(position, item)
                    getListOfPendingAudios()
                }
                is DataState.ErrorException -> {
                    writeLog(
                        "Pending list: Upload file to server Failed with Exception: File ${
                            Gson().toJson(
                                item
                            )
                        } : Exception ${Utils.getStackTrace(it.exception)}"
                    )

                    dialogLoader?.dismiss()
                    item.isPendingResponse = false
                    item.isUploadedToScribe = false
                    updateRecordInDatabase(position, item)
                }
                is DataState.Loading -> {
                    dialogLoader?.show()
                }
            }
        }
    }

    private fun uploadItemToS3(position: Int, item: RecordedItem) {
        dialogLoader?.show()
        awsManager.uploadFile(
            File(item.localPath),
            _onProgressChanged = {

            },
            _onUploadCompleted = {
                writeLog("Pending list: Upload file to s3 Completed File ${Gson().toJson(item)}")
                dialogLoader?.dismiss()
                item.firebaseURL = it
                item.isFileUploaded = true
                updateRecordInDatabase(position, item)
                uploadItemToServer(position, item)
            },
            _onUploadFailed = {
                writeLog(
                    "Pending list: Upload file to s3 Failed with Exception: File ${
                        Gson().toJson(
                            item
                        )
                    } : Exception ${Utils.getStackTrace(it)}"
                )

                dialogLoader?.dismiss()
                item.isPendingResponse = false
                item.isFileUploaded = false
                updateRecordInDatabase(position, item)
                AlertOP.showAlert(requireContext(), message = it.localizedMessage, pBtnText = "Ok")
            })
    }

    private fun updateRecordInDatabase(position: Int, item: RecordedItem) {
        QueueManager.getInstance(audioRecordingRepository).updateAudio(item)
        adapter?.updateItem(position, item)
    }


    private fun setListeners() {
        swipe_refresh_layout?.setOnRefreshListener {
            getListOfPendingAudios()
        }
    }

    override fun onResume() {
        super.onResume()
        swipe_refresh_layout?.post {
            getListOfPendingAudios()
        }
    }

    private fun getListOfPendingAudios() {
        swipe_refresh_layout?.isRefreshing = true
        viewModel.getListOfPendingRecordingsFromDB()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemUploadedSuccessfully(item: RecordedItem) {
        Log.d(this.javaClass.simpleName, "onItemUploadedSuccessfully: ${item.fileName}")
        swipe_refresh_layout?.postDelayed({
            Toast.makeText(requireContext(), "Audio Uploaded.", Toast.LENGTH_SHORT).show()
            getListOfPendingAudios()
        }, 500)
    }

    public fun writeLog(message: String) {
        FileManager.writeLogOnFile(requireContext(), message)
        CrashlyticsUtils.log(message)
    }
}