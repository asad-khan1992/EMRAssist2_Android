package com.emrassist.audio.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.forgot_password.ForgotPasswordActivity
import com.emrassist.audio.ui.activity.login.view_model.LoginViewModel
import com.emrassist.audio.ui.activity.main.MainActivity
import com.emrassist.audio.ui.activity.register.RegisterActivity
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.emrassist.audio.utils.KeyboardOp
import com.emrassist.audio.utils.Utils.isSessionExpired
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val TAG: String = LoginActivity::class.java.simpleName
    var progressDialog: DialogLoader? = null
    val viewModel: LoginViewModel by viewModels()
    private var pin: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setUpObservers();
        setListeners()


    }

    private fun setUpObservers() {

        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "setUpObservers: " + Gson().toJson(it.data))
                    if (it.data.success == 1) {
                        SharedPrefsUtils.user = it.data.responseObject
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
                }
            }
        })
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

    private fun openLoginActivity() {
        isSessionExpired = false
        val intent = Intent(
            this@LoginActivity,
            MainActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        this@LoginActivity.finish()

    }

    private fun setListeners() {
        text_forgot_password.setOnClickListener {
            clearFields()
            val intent: Intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        button_login.setOnClickListener {
            KeyboardOp.hideKeyboard(this@LoginActivity)
            edit_text_email.setText(edit_text_email.text.toString().trim())
            validateFields(edit_text_email.text.toString(), edit_text_password.text.toString());
//            progressDialog = DialogLoader(this)
//            progressDialog?.show();
//            viewModel.verifyPin(pin)
        }
        button_register.setOnClickListener {
            clearFields()
            val intent: Intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
//        text_pin_entry.setOnPinEnteredListener {
//            KeyboardOp.hideKeyboard(this@LoginActivity)
//            pin = it.toString()
//            text_pin_entry.setText("")
//            progressDialog = DialogLoader(this)
//            progressDialog?.show();
//            viewModel.verifyPin(pin)
//
//        }
    }

    private fun clearFields() {
        edit_text_email.setText("")
        edit_text_password.setText("")
    }

    private fun validateFields(email: String, password: String) {
        var errorView: View? = null
        var cancel = false
        var errorEmail = viewModel.isEmailValid(email)
        var errorPassword = viewModel.isPasswordValid(password)
        if (errorEmail != -1) {
            edit_text_email.error = getString(errorEmail)
            errorView = edit_text_email
            cancel = true
        } else if (errorPassword != -1) {
            edit_text_password.error = getString(errorPassword)
            errorView = edit_text_password
            cancel = true
        }
        if (cancel) {
            errorView?.requestFocus()
        } else {
            progressDialog = DialogLoader(this)
            progressDialog?.show()
            viewModel.login(email, password)
        }
    }
}