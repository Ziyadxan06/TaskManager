package com.example.taskmanager

import android.content.Context

object PreferenceHelper {
    private const val PREF_NAME = "lang_pref"
    private const val KEY_LANGUAGE = "app_lang"

    fun saveLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }
}
