package com.playground.vkmini.data

import android.net.Uri

/**
 * VK OAuth (implicit flow) for the in-app WebView login.
 *
 * The app ships with a public client_id so a user can sign in with a VK
 * login/password without creating an app. Advanced users can put their own
 * client_id in the login form.
 */
object VkAuth {

    const val DEFAULT_CLIENT_ID = "2685278"
    const val REDIRECT_URI = "https://oauth.vk.com/blank.html"

    /** Scopes needed by the four screens: feed, messages, audio, profile. */
    const val SCOPE = "friends,photos,audio,video,messages,wall,offline,groups,notifications,stats"

    data class Token(
        val accessToken: String,
        val userId: Long,
        val expiresIn: Long,
    )

    fun authUrl(clientId: String): String {
        val id = clientId.trim().ifBlank { DEFAULT_CLIENT_ID }
        return Uri.parse("https://oauth.vk.com/authorize").buildUpon()
            .appendQueryParameter("client_id", id)
            .appendQueryParameter("display", "mobile")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPE)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("v", VkApi.API_VERSION)
            .build()
            .toString()
    }

    /** True when the URL is the final OAuth redirect (token, error or cancel). */
    fun isRedirect(url: String): Boolean {
        val host = Uri.parse(url).host ?: return false
        if (!host.endsWith("vk.com", ignoreCase = true)) return false
        return url.contains("access_token=") ||
            url.contains("error=") ||
            url.contains("cancel=1")
    }

    fun parse(url: String): Token? {
        val params = params(url)
        val accessToken = params["access_token"] ?: return null
        return Token(
            accessToken = accessToken,
            userId = params["user_id"]?.toLongOrNull() ?: 0L,
            expiresIn = params["expires_in"]?.toLongOrNull() ?: 0L,
        )
    }

    fun error(url: String): String? {
        val params = params(url)
        if (params["cancel"] == "1") return "Вход отменён"
        val code = params["error"] ?: return null
        return params["error_description"] ?: code
    }

    /** OAuth puts the payload into the fragment; fall back to the query string. */
    private fun params(url: String): Map<String, String> {
        val raw = url.substringAfter('#', "").ifBlank { url.substringAfter('?', "") }
        if (raw.isBlank()) return emptyMap()
        return raw.split('&').mapNotNull { pair ->
            val index = pair.indexOf('=')
            if (index <= 0) {
                null
            } else {
                Uri.decode(pair.substring(0, index)) to Uri.decode(pair.substring(index + 1))
            }
        }.toMap()
    }
}
