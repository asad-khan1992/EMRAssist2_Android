package com.emrassist.audio.ui.activity.edit_profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.edit_profile.view_model.EditProfileViewModel
import com.emrassist.audio.ui.activity.login.LoginActivity
import com.emrassist.audio.ui.base.BaseActivity
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.emrassist.audio.utils.KeyboardOp
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_edit_profile.*

@AndroidEntryPoint
class EditProfileActivity : BaseActivity() {
    private val TAG: String = LoginActivity::class.java.simpleName
    var progressDialog: DialogLoader? = null
    val viewModel: EditProfileViewModel by viewModels()
    private var pin: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setData()

        setUpObservers()
        setListeners()


    }

    private fun setData() {
        var user = SharedPrefsUtils.user
        if (user == null) {
            AlertOP.showAlert(this,
                message = "Some thing went wrong while getting user Detail. Please try again later",
                pBtnText = "Ok",
                onPositiveClick = {
                    onBackPressed()
                }
            )
        } else {
            edit_text_first_name.setText(user.firstName)
            edit_text_last_name.setText(user.lastName)
        }
    }

    private fun setUpObservers() {

        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        val user = SharedPrefsUtils.user
                        user?.updateData(it.data.responseObject)
                        SharedPrefsUtils.user = user
                        openLoginActivity()
                    } else {
                        sessionExpired(SessionExpired(true))
                    }
                    progressDialog?.dismiss()
                }
                is DataState.ErrorException -> {
                    showErrorAlert(
                        it.exception.localizedMessage
                            ?: "Some thing went wrong. Please try Again later"
                    )
                }
                is DataState.Loading -> {
                }
            }
        })
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

    private fun openLoginActivity() {
        Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show()
//        val intent = Intent(
//            this,
//            MainActivity::class.java
//        )
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(intent)
//        this.finish()
        Handler(Looper.getMainLooper()).postDelayed({
            setResult(RESULT_OK)
            this.finish()
        }, 200)

    }

    private fun setListeners() {

        button_register.setOnClickListener {
            KeyboardOp.hideKeyboard(this)
            validateFields(
                edit_text_first_name.text.toString(),
                edit_text_last_name.text.toString()
            )
        }
    }

    private fun validateFields(
        firstName: String,
        lastName: String,
    ) {
        edit_text_first_name.error = null
        edit_text_last_name.error = null

        var errorView: View? = null
        var cancel = false
        var errorName = viewModel.isNameValid(firstName)
        var errorLastName = viewModel.isNameValid(lastName)
        if (errorName != -1) {
            edit_text_first_name.error = getString(errorName)
            errorView = edit_text_first_name
            cancel = true
        } else if (errorLastName != -1) {
            edit_text_last_name.error = getString(errorLastName)
            errorView = edit_text_last_name
            cancel = true
        }
        if (cancel) {
            errorView?.requestFocus()
        } else {
            progressDialog = DialogLoader(this)
            progressDialog?.show()
            viewModel.updateProfile(
                firstName,
                lastName,
                SharedPrefsUtils.user?.id ?: "",
                SharedPrefsUtils.user?.phoneNumber ?: ""
            )
        }
    }
}