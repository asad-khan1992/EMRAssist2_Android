package com.emrassist.audio.utils.sharedPrefsUtils

import com.emrassist.audio.model.UserModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object SharedPrefsUtils {
    fun clear() {
        SharedPref.clearCache()
    }

    fun clearOldSharedPrefs() {
        SharedPref.clearOldCache()
    }

    var user: UserModel?
        set(value) {
            if (value == null)
                SharedPref.save(KeysSharedPrefs.KEY_USER, "")
            else {
                SharedPref.save(KeysSharedPrefs.KEY_USER, Gson().toJson(value))
            }
        }
        get() {
            val data = SharedPref.read(KeysSharedPrefs.KEY_USER, "")
            if (data.isNullOrEmpty())
                return null
            else
                return Gson().fromJson(data, UserModel::class.java)
        }
    var logFileList: ArrayList<String>
        get() {
            val data = SharedPref.read(KeysSharedPrefs.KEY_LOG_LIST, "")
            return if (data.isNullOrEmpty()) {
                ArrayList()
            } else {
                Gson().fromJson(
                    data,
                    object : TypeToken<ArrayList<String>>() {}.type
                )
            }
        }
        set(value) {
            val logs: String = Gson().toJson(value)
            SharedPref.save(KeysSharedPrefs.KEY_LOG_LIST, logs)
        }

    var logFileNumber: Int
        get() = SharedPref.read(KeysSharedPrefs.KEY_LOG, 1.toInt()) ?: 1
        set(value) {
            SharedPref.save(KeysSharedPrefs.KEY_LOG, value)
        }

    public var verfication: String?
        set(value) {
            SharedPref.save(KeysSharedPrefs.KEY_VERIFICATION_CODE, value ?: "")
        }
        get() {
            return SharedPref.read(KeysSharedPrefs.KEY_VERIFICATION_CODE, "")
        }

    public val phoneNumber: String?
        get() {
            return user?.phoneNumber
        }
    val isAlreadyLoggedIn: Boolean
        get() {
            return user != null
        }
    val isAlreadyVerified: Boolean
        get() {
            return !verfication.isNullOrEmpty()
        }
}