package com.nikdi.recipefyai.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("PermissionPrefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("is_first_run", true)
    }

    fun setFirstRun(isFirstRun: Boolean) {
        sharedPreferences.edit().putBoolean("is_first_run", isFirstRun).apply()
    }
}