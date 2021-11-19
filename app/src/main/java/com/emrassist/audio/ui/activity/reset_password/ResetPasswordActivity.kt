package com.emrassist.audio.ui.activity.reset_password

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.login.LoginActivity
import com.emrassist.audio.ui.activity.reset_password.view_model.ResetPasswordViewModel
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_reset_password.*

@AndroidEntryPoint
class ResetPasswordActivity : AppCompatActivity() {
    private val TAG: String = ResetPasswordActivity::class.java.simpleName
    private val viewModelChange: ResetPasswordViewModel by viewModels()
    private var progressDialog: DialogLoader? = null

    private var email: String = ""
    private var otp: String = ""

    companion object {
        const val EMAIL: String = "email"
        const val OTP: String = "otp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        getDataFromIntent()
        setUpObservers()
        setListeners()
    }

    private fun getDataFromIntent() {
        email = intent?.getStringExtra(EMAIL) ?: ""
        otp = intent?.getStringExtra(OTP) ?: ""
    }

    private fun setUpObservers() {
        viewModelChange.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        AlertOP.showAlert(this,
                            message = "Password Changed Successfully.",
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }

    private fun setListeners() {
        button_reset_password.setOnClickListener {
            validateFields(
                edit_text_password.text.toString(),
                edit_text_confirm_password.text.toString()
            )
        }
    }

    private fun validateFields(password: String, confirmPassword: String) {
        edit_text_confirm_password.error = null
        edit_text_password.error = null
        var errorView: View? = null
        var cancel = false
        val errorPassword = viewModelChange.isPasswordValid(password)
        val errorConfirmPassword = viewModelChange.isConfirmPasswordValid(password, confirmPassword)
        if (errorPassword != -1) {
            edit_text_password.error = getString(errorPassword)
            errorView = edit_text_password
            cancel = true
        } else if (errorConfirmPassword != -1) {
            edit_text_confirm_password.error = getString(errorConfirmPassword)
            errorView = edit_text_confirm_password
            cancel = true
        }
        if (cancel) {
            errorView?.requestFocus()
        } else {
            progressDialog = DialogLoader(this)
            progressDialog?.show()
            viewModelChange.resetPassword(email, otp, edit_text_password.text.toString())
        }
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

}