package com.emrassist.audio.utils.sharedPrefsUtils

import android.content.Context
import android.content.SharedPreferences
import com.emrassist.audio.App

object SharedPref {
    private val prefs: SharedPreferences by lazy {
        App.context.getSharedPreferences(KeysSharedPrefs.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val edit: SharedPreferences.Editor by lazy { prefs.edit() }

    fun clearCache() {
        edit.clear()
        edit.apply()
    }

    fun read(valueKey: String, valueDefault: String?): String? {
        return prefs.getString(valueKey, valueDefault)
    }

    fun save(valueKey: String, value: String) {
        edit.putString(valueKey, value)
        edit.apply()
    }

    fun read(valueKey: String, valueDefault: Int?): Int {
        return prefs.getInt(valueKey, valueDefault!!)
    }

    fun save(valueKey: String, value: Int) {
        edit.putInt(valueKey, value)
        edit.apply()
    }

    fun read(valueKey: String, valueDefault: Boolean?): Boolean {
        return prefs.getBoolean(valueKey, valueDefault!!)
    }

    fun save(valueKey: String, value: Boolean) {
        edit.putBoolean(valueKey, value)
        edit.apply()
    }

    fun read(valueKey: String, valueDefault: Long?): Long {
        return prefs.getLong(valueKey, valueDefault!!)
    }

    fun save(valueKey: String, value: Long) {
        edit.putLong(valueKey, value)
        edit.apply()
    }

    fun read(valueKey: String, valueDefault: Float?): Float {
        return prefs.getFloat(valueKey, valueDefault!!)
    }

    fun save(valueKey: String, value: Float) {
        edit.putFloat(valueKey, value)
        edit.apply()
    }

    fun clearOldCache() {
        val prefs: SharedPreferences =
            App.context.getSharedPreferences("SCRIBE", Context.MODE_PRIVATE)
        prefs.edit().clear()
    }

}