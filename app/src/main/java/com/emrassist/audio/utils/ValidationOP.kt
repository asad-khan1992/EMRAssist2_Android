package com.emrassist.audio.utils

import android.util.Patterns
import com.emrassist.audio.R

object ValidationOP {

    fun isFieldEmpty(field: String): Boolean {
        return field.isEmpty()
    }

    public fun isEmailValid(email: String): Int {
        return if (isFieldEmpty(email)) {
            R.string.field_error_email_empty
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            R.string.field_error_email_invalid
        } else {
            -1
        }
    }

    public fun isPasswordValid(password: String): Int {
        return if (isFieldEmpty(password)) {
            R.string.field_error_password_empty
        } else if (password.length < 6) {
            R.string.field_error_password_short
        } else {
            -1
        }
    }

    public fun isPasswordMatches(password: String, confirmPassword: String): Int {
        return if (isFieldEmpty(confirmPassword)) {
            R.string.field_error_confirm_password_empty
        } else if (!password.equals(confirmPassword)) {
            R.string.field_error_pass_not_match
        } else {
            -1
        }
    }


    public fun isOldPasswordSame(password: String, confirmPassword: String): Int {
        return if (isFieldEmpty(confirmPassword)) {
            R.string.field_error_old_password_empty
        } else if (password.equals(confirmPassword)) {
            R.string.field_error_password_old_password_not_match
        } else {
            -1
        }
    }

    fun isNameValid(firstName: String): Int {
        return if (isFieldEmpty(firstName))
            return R.string.field_error_name_empty
        else if (firstName.length < 3)
            return R.string.field_error_name_short
        else
            -1
    }

    fun isPhoneNumberValid(number: String): Int {
        return if (isFieldEmpty(number))
            R.string.field_error_number_empty
        else if (number.length < 6)
            R.string.field_error_number_short
        else
            -1
    }
}