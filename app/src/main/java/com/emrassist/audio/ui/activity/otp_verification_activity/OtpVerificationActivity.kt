package com.emrassist.audio.ui.activity.otp_verification_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.otp_verification_activity.view_model.OtpVerificationViewModel
import com.emrassist.audio.ui.activity.reset_password.ResetPasswordActivity
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activit_otp_verification.*

@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {
    val viewModel: OtpVerificationViewModel by viewModels()
    var email: String = ""

    companion object {
        const val EMAIL: String = "email"
        const val CODE: String = "code"
    }

    private val TAG: String = OtpVerificationActivity::class.java.simpleName
//    val viewModel: LoginUsingNumberViewModel by viewModels()

    var progressDialog: DialogLoader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activit_otp_verification)

        getIntentData();
        setObserver()
        setListeners()
        setUpData();
    }

    private fun setUpData() {
        if (email.isEmpty())
            AlertOP.showAlert(
                this,
                message = getString(R.string.please_try_again),
                pBtnText = "Ok",
                onPositiveClick = {
                    onBackPressed()
                })
        else {
            tvMobileNumber.setText(email)
        }
    }

    private fun getIntentData() {

        if (intent != null) {
            email = intent.getStringExtra(EMAIL)!!
        }
    }

    private fun setObserver() {

        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success<ApiResponse<Any>> -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        AlertOP.showAlert(
                            this,
                            message = "An Email has been sent to you containing OTP",
                            pBtnText = "Ok"
                        )
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
                    progressDialog = DialogLoader(this)
                    progressDialog?.show();
                }
            }
        })
        viewModel.verifyOtpDataState.observe(this, Observer {
            when (it) {
                is DataState.Success<ApiResponse<Any>> -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        openLoginActivity()
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
                    progressDialog = DialogLoader(this)
                    progressDialog?.show();
                }
            }
        })
    }

    private fun openLoginActivity() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        intent.putExtra(ResetPasswordActivity.EMAIL, email)
        intent.putExtra(ResetPasswordActivity.OTP, etOTP.text.toString())
        startActivity(intent)
        finish()
    }

    private fun showErrorAlert(msg: String) {
        AlertOP.showAlert(this, message = msg, pBtnText = "Ok")
    }

    private fun setListeners() {
        etOTP.setOnPinEnteredListener {
//            viewModel.validateFields(it.toString())
            viewModel.verifyPasswordOtp(email, it.toString())
        }

        tvResendOTP.setOnClickListener {
            viewModel.forgotPassword(email)
        }
        tvEditMobileNumber.setOnClickListener {
            onBackPressed()
        }
    }
}