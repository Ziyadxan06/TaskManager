package com.example.taskmanager

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocalHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
