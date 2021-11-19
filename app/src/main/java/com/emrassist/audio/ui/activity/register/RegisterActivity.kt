package com.emrassist.audio.ui.activity.register

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.login.LoginActivity
import com.emrassist.audio.ui.activity.register.view_model.RegisterViewModel
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.emrassist.audio.utils.KeyboardOp
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_register.*

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private val TAG: String = LoginActivity::class.java.simpleName
    var progressDialog: DialogLoader? = null
    val viewModel: RegisterViewModel by viewModels()
    private var pin: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setUpObservers();
        setListeners()


    }

    private fun setUpObservers() {

        viewModel.dataState.observe(this, Observer {
            when (it) {
                is DataState.Success -> {
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
                }
            }
        })
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

    private fun openLoginActivity() {
        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
//        val intent = Intent(
//            this,
//            MainActivity::class.java
//        )
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(intent)
//        this.finish()
        Handler(Looper.getMainLooper()).postDelayed({
            this.finish()
        }, 200)

    }

    private fun setListeners() {

        button_register.setOnClickListener {
            KeyboardOp.hideKeyboard(this)
            validateFields(
                edit_text_first_name.text.toString(),
                edit_text_last_name.text.toString(),
                edit_text_email.text.toString(),
                edit_text_phone_number.text.toString(),
                edit_text_password.text.toString(),
                edit_text_confirm_password.text.toString()
            )
        }
    }

    private fun validateFields(
        firstName: String,
        lastName: String,
        email: String,
        number: String,
        password: String,
        confirmPassword: String
    ) {
        edit_text_first_name.error = null
        edit_text_last_name.error = null
        edit_text_email.error = null
        edit_text_phone_number.error = null
        edit_text_password.error = null
        edit_text_confirm_password.error = null

        var errorView: View? = null
        var cancel = false
        var errorName = viewModel.isNameValid(firstName)
        var errorLastName = viewModel.isNameValid(lastName)
        var errorNumber = viewModel.isPhoneNumberValid(number)
        var errorEmail = viewModel.isEmailValid(email)
        var errorPassword = viewModel.isPasswordValid(password)
        var errorConfirmPassword = viewModel.isConfirmPasswordValid(password, confirmPassword)
        if (errorName != -1) {
            edit_text_first_name.error = getString(errorName)
            errorView = edit_text_first_name
            cancel = true
        } else if (errorLastName != -1) {
            edit_text_last_name.error = getString(errorLastName)
            errorView = edit_text_last_name
            cancel = true
        } else if (errorEmail != -1) {
            edit_text_email.error = getString(errorEmail)
            errorView = edit_text_email
            cancel = true
        } else if (errorNumber != -1) {
            edit_text_phone_number.error = getString(errorNumber)
            errorView = edit_text_phone_number
            cancel = true
        } else if (errorPassword != -1) {
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
            viewModel.register(
                firstName,
                lastName,
                email,
                ccp.selectedCountryCodeWithPlus + number,
                password
            )
        }
    }
}