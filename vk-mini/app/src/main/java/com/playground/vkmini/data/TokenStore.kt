package com.playground.vkmini.data

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("vk_mini", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove("token") else putString("token", value)
            }.apply()
        }

    var demo: Boolean
        get() = prefs.getBoolean("demo", false)
        set(value) {
            prefs.edit().putBoolean("demo", value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
