package com.emrassist.audio.ui.activity.forgot_password

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.forgot_password.view_model.ForgotPasswordViewModel
import com.emrassist.audio.ui.activity.otp_verification_activity.OtpVerificationActivity
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_forgot_password.*

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {
    private val TAG: String = ForgotPasswordActivity::class.java.simpleName
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var progressDialog: DialogLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setUpObservers()
        setListeners()
    }

    private fun setUpObservers() {
        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        AlertOP.showAlert(this,
                            message = "An email has been send to you with the verification code.",
                            pBtnText = "Ok",
                            onPositiveClick = {
                                openVerificationCodeActivity()
                            });
                    } else {
                        showErrorAlert(it.data.message);
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

    private fun openVerificationCodeActivity() {
        val intent = Intent(this, OtpVerificationActivity::class.java)
        intent.putExtra(OtpVerificationActivity.EMAIL, edit_text_email.text.toString())
        startActivity(intent)
    }

    private fun setListeners() {
        button_forgot_password.setOnClickListener {
            validateFields(edit_text_email.text.toString())
        }
    }

    private fun validateFields(email: String) {
        var errorView: View? = null
        var cancel = false
        val errorEmail = viewModel.isEmailValid(email)
        if (errorEmail != -1) {
            edit_text_email.error = getString(errorEmail)
            errorView = edit_text_email
            cancel = true
        }
        if (cancel) {
            errorView?.requestFocus()
        } else {
            progressDialog = DialogLoader(this)
            progressDialog?.show()
            viewModel.forgotPassword(email)
        }
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

}