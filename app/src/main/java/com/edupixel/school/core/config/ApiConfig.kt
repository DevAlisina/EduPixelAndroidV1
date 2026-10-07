package com.edupixel.school.core.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Centralized API configuration.
 * Default URL is strictly: http://127.0.0.1:7878/
 * Allows custom override in settings for physical device testing without prompting on normal startup.
 */
object ApiConfig {
    const val DEFAULT_API_BASE_URL = "http://127.0.0.1:7878/"
    private const val PREFS_NAME = "edupixel_api_config"
    private const val KEY_BASE_URL = "api_base_url"

    private var prefs: SharedPreferences? = null
    private var cachedBaseUrl: String? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cachedBaseUrl = prefs?.getString(KEY_BASE_URL, DEFAULT_API_BASE_URL) ?: DEFAULT_API_BASE_URL
    }

    var baseUrl: String
        get() = cachedBaseUrl ?: DEFAULT_API_BASE_URL
        set(value) {
            val formatted = if (value.endsWith("/")) value else "$value/"
            cachedBaseUrl = formatted
            prefs?.edit()?.putString(KEY_BASE_URL, formatted)?.apply()
        }

    fun resetToDefault() {
        baseUrl = DEFAULT_API_BASE_URL
    }
}
