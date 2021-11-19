package com.emrassist.audio.ui.activity.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emrassist.audio.R
import com.emrassist.audio.ui.activity.change_password.ChangePasswordActivity
import com.emrassist.audio.ui.activity.edit_profile.EditProfileActivity
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.sharedPrefsUtils.SharedPrefsUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_profile.*

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setUserData()
        setListener()
    }


    private fun setUserData() {
        var user = SharedPrefsUtils.user;
        if (user == null) {
            AlertOP.showAlert(this,
                message = "Some thing went wrong while getting user Detail. Please try again later",
                pBtnText = "Ok",
                onPositiveClick = {
                    onBackPressed()
                }
            )
        } else {
            text_email.text = user.email
            text_name.text = "${user.firstName} ${user.lastName}"
            text_phone_number.text = user.phoneNumber
        }


    }

    private fun setListener() {
        text_basic_detail.setOnClickListener {

            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, 1001)
        }
        text_change_password.setOnClickListener {

            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        setUserData()
    }
}