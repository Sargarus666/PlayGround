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

    var userId: Long
        get() = prefs.getLong("user_id", 0L)
        set(value) {
            prefs.edit().putLong("user_id", value).apply()
        }

    /** 0 means "does not expire" (VK tokens with the offline scope). */
    var expiresAt: Long
        get() = prefs.getLong("expires_at", 0L)
        set(value) {
            prefs.edit().putLong("expires_at", value).apply()
        }

    var clientId: String
        get() = prefs.getString("client_id", null) ?: VkAuth.DEFAULT_CLIENT_ID
        set(value) {
            prefs.edit().putString("client_id", value.trim()).apply()
        }

    fun saveSession(token: String, userId: Long, expiresIn: Long) {
        val expiresAt = if (expiresIn <= 0L) 0L else nowSeconds() + expiresIn
        prefs.edit()
            .putString("token", token)
            .putLong("user_id", userId)
            .putLong("expires_at", expiresAt)
            .putBoolean("demo", false)
            .apply()
    }

    fun isExpired(): Boolean {
        val expiresAt = expiresAt
        return expiresAt > 0L && nowSeconds() >= expiresAt
    }

    fun clear() {
        val clientId = clientId
        prefs.edit().clear().apply()
        // Keep the user's own client_id across logouts.
        if (clientId != VkAuth.DEFAULT_CLIENT_ID) this.clientId = clientId
    }

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000
}
