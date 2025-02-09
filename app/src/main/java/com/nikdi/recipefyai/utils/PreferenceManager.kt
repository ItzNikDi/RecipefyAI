package com.nikdi.recipefyai.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        return prefs.getBoolean("isFirstRun", true)
    }

    fun setFirstRun(isFirstRun: Boolean) {
        prefs.edit().putBoolean("isFirstRun", isFirstRun).apply()
    }
}