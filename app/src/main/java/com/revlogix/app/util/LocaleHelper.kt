package com.revlogix.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Handles the in-app language switch (English, Afrikaans, isiZulu).
 * The chosen code is kept in SharedPreferences so it is known before the UI is created;
 * MainActivity wraps its context with wrap() and recreates itself when the language changes.
 */
object LocaleHelper {
    const val PREFS = "revlogix_prefs"
    private const val KEY_LANGUAGE = "language"

    val supported = listOf("en", "af", "zu")

    fun savedLanguage(context: Context): String {
        val code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
        return if (code in supported) code else "en"
    }

    fun saveLanguage(context: Context, code: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, code).apply()
    }

    /** Returns a context whose resources use the saved language. */
    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(savedLanguage(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
