package com.emrassist.audio.ui.activity.change_password

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.emrassist.audio.R
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.change_password.view_model.ChangePasswordViewModel
import com.emrassist.audio.ui.base.BaseActivity
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.DialogLoader
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_change_password.*

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity() {
    private val TAG: String = ChangePasswordActivity::class.java.simpleName
    private val viewModelChange: ChangePasswordViewModel by viewModels()
    private var progressDialog: DialogLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        setUpObservers()
        setListeners()
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
                        sessionExpired(SessionExpired(true))
//                        Utils.logout(this)
//                        showErrorAlert(it.data.message);
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
        this.finish()
    }

    private fun setListeners() {
        button_reset_password.setOnClickListener {
            validateFields(
                edit_text_old_password.text.toString(),
                edit_text_password.text.toString(),
                edit_text_confirm_password.text.toString()
            )
        }
    }

    private fun validateFields(oldPassword: String, password: String, confirmPassword: String) {
        edit_text_confirm_password.error = null
        edit_text_password.error = null
        edit_text_old_password.error = null
        var errorView: View? = null
        var cancel = false
        val errorOldPasswordEmpty = viewModelChange.isPasswordValid(oldPassword)
        val errorPassword = viewModelChange.isPasswordValid(password)
        val errorPasswordOldPassword = viewModelChange.isOldPasswordSame(oldPassword, password)
        val errorConfirmPassword = viewModelChange.isConfirmPasswordValid(password, confirmPassword)
        if (errorOldPasswordEmpty != -1) {
            edit_text_old_password.error = getString(errorOldPasswordEmpty)
            errorView = edit_text_old_password
            cancel = true
        } else if (errorPassword != -1) {
            edit_text_password.error = getString(errorPassword)
            errorView = edit_text_password
            cancel = true
        } else if (errorPasswordOldPassword != -1) {
            edit_text_password.error = getString(errorPasswordOldPassword)
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
            viewModelChange.changePassword(SharedPrefsUtils.user?.id ?: "", oldPassword, password)
        }
    }

    private fun showErrorAlert(message: String) {
        progressDialog?.dismiss()
        AlertOP.showAlert(this, message = message, pBtnText = "Ok");
    }

}