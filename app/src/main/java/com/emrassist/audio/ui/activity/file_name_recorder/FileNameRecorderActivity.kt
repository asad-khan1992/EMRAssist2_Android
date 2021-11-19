package com.emrassist.audio.ui.activity.file_name_recorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.emrassist.audio.R
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.ui.activity.audio_recorder.AudioRecorderActivity
import com.emrassist.audio.utils.AlertOP
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_file_name_recorder.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class FileNameRecorderActivity : AppCompatActivity() {
    private var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_name_recorder)
        setListener();
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun setListener() {
        file_name_button.setOnClickListener {
//view?.findNavController()?.navigate(R.id.)

            getFileNameFromUser();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemUploadedSuccessfully(item: RecordedItem) {
        Log.d(this.javaClass.simpleName, "onItemUploadedSuccessfully: ${item.fileName}")
        Toast.makeText(this, "Audio Uploaded.", Toast.LENGTH_SHORT).show()
    }

    private fun getFileNameFromUser() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_file_name, null)
        builder.setView(view)

        val text: TextView = view.findViewById(R.id.text_confirm)
        val editText: EditText = view.findViewById(R.id.edit_text_file_name);
        val dialog: AlertDialog = builder.create()
        dialog.show()
//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        text.setOnClickListener(View.OnClickListener { //                    dialog.dismiss();
            if (editText.text.toString().isEmpty()) {
                AlertOP.showAlert(
                    this,
                    message = getString(R.string.alert_enter_file_name)
                )
                return@OnClickListener
            } else {
                fileName = editText.text.toString()
                dialog.dismiss()
                checkPermission();
            }
        })
    }

    private fun checkPermission() {
        val permissions =
            arrayOf<String>(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
//                ,Manifest.permission.READ_PHONE_STATE
            )
        Permissions.check(
            this /*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {
                    openActivity()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String>?
                ) {
                    super.onDenied(context, deniedPermissions)
                    AlertOP.showAlert(this@FileNameRecorderActivity,
                        message = "Permission is required for Audio Recording.",
                        pBtnText = "Ok",
                        nBtnText = "Cancel",
                        onPositiveClick = {
                            checkPermission()
                        },
                        onNegativeClick = {
                            finish()
                        }
                    )
                }
            })
    }


    private fun openActivity() {
        val stat = StatFs(Environment.getExternalStorageDirectory().getPath())
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        val megAvailable = bytesAvailable / (1024 * 1024)

        if (megAvailable > 500) {
            var intent: Intent = Intent(this, AudioRecorderActivity::class.java)
            intent.putExtra(AudioRecorderActivity.PARAM_FILE_NAME, fileName);
            startActivity(intent)
        } else {
            AlertOP.showAlert(
                this,
                message = "You have storage less then 500 MB. Please free storage space to continue recording"
            )
        }
//        val bundle = bundleOf("fileName" to fileName)
//        view?.findNavController()
//            ?.navigate(R.id.action_fileNameRecorderFragment3_to_audioRecorderFragment, bundle)
    }

}