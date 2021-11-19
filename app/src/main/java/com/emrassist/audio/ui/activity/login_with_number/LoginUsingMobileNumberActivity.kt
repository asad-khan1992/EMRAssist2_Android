package com.emrassist.audio.ui.activity.login_with_number

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.retrofit.model.ApiResponse
import com.emrassist.audio.ui.activity.login_with_number.view_model.LoginUsingNumberViewModel
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login_using_number.*

/**
 * A login screen that offers login via email/password.
 */
@AndroidEntryPoint
class LoginUsingMobileNumberActivity : AppCompatActivity() {
    private var number: String = ""
    private val TAG: String = LoginUsingMobileNumberActivity::class.java.simpleName
    val viewModel: LoginUsingNumberViewModel by viewModels()

    var progressDialog: DialogLoader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_using_number)

        setObserver()
        setListeners()
    }

    private fun setObserver() {
        viewModel.fieldValidation.observe(this, Observer {
            if (it != -1) {
                AlertOP.showAlert(this, message = getString(it), pBtnText = "Ok")
            } else {
                progressDialog = DialogLoader(this)
                progressDialog?.show()
                viewModel.verifyNumber(number)
            }
        })
        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success<ApiResponse<String>> -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        openLoginActivity(it.data.responseCode)
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

    private fun openLoginActivity(code: String) {
//        Toast.makeText(this, code, Toast.LENGTH_LONG).show()
//        val intent: Intent = Intent(this, OtpVerificationActivity::class.java)
//        intent.putExtra(
//            OtpVerificationActivity.MOBILE_NUMBER,
//            number
//        )
//        intent.putExtra(OtpVerificationActivity.CODE, code)
//        startActivity(
//            intent
//        )
    }

    private fun showErrorAlert(msg: String) {
        AlertOP.showAlert(this, message = msg, pBtnText = "Ok")
    }

    private fun setListeners() {
        btnUseSMS.setOnClickListener {
            number = ccp.selectedCountryCodeWithPlus + etMobileNumber.text.toString()
            viewModel.validateFields(
                ccp.selectedCountryCodeWithPlus,
                etMobileNumber.text.toString()
            )
        }

//        btnLogin.setOnClickListener {
//            startActivity(Intent(this@LoginUsingMobileNumberActivity, MainActivity::class.java))
//        }
    }
}