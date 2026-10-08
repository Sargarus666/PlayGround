package com.mmocal.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class SteamMedia(
    val iconUrl: String?,
    val shots: List<String>
)

private fun steamNorm(s: String): String =
    s.lowercase().filter { it.isLetterOrDigit() }

// Строгое сходство названий: короткое должно покрывать 80%+ длинного,
// иначе "Icarus Online" притянет чужой "Guns of Icarus Online".
private fun namesClose(a: String, b: String): Boolean {
    val x = steamNorm(a)
    val y = steamNorm(b)
    if (x.length < 5 || y.length < 5) return false
    if (x == y) return true
    val (short, long) = if (x.length <= y.length) x to y else y to x
    return short in long && short.length.toDouble() / long.length >= 0.8
}

private fun steamGet(url: String): String? = runCatching {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 10000
    conn.readTimeout = 15000
    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
    val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    conn.disconnect()
    text
}.getOrNull()

object SteamSource {
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("steam_cache", android.content.Context.MODE_PRIVATE)
    }

    private fun readCache(title: String): SteamMedia? {
        val o = runCatching {
            JSONObject(prefs?.getString("map", "{}") ?: "{}").optJSONObject(steamNorm(title))
        }.getOrNull() ?: return null
        val arr = o.optJSONArray("shots")
        return SteamMedia(
            o.optString("icon").takeIf { it.isNotBlank() },
            if (arr == null) emptyList() else (0 until arr.length()).map { arr.getString(it) }
        )
    }

    private fun writeCache(title: String, media: SteamMedia) {
        val root = runCatching {
            JSONObject(prefs?.getString("map", "{}") ?: "{}")
        }.getOrDefault(JSONObject())
        root.put(
            steamNorm(title),
            JSONObject().apply {
                put("icon", media.iconUrl ?: "")
                put("shots", org.json.JSONArray(media.shots))
            }
        )
        prefs?.edit()?.putString("map", root.toString())?.apply()
    }

    suspend fun findMedia(title: String): SteamMedia? = withContext(Dispatchers.IO) {
        readCache(title)?.let { return@withContext it }
        val q = URLEncoder.encode(title, "UTF-8")
        val search = steamGet(
            "https://store.steampowered.com/api/storesearch/?term=$q&l=english&cc=US"
        ) ?: return@withContext null
        val items = runCatching {
            JSONObject(search).getJSONArray("items")
        }.getOrNull() ?: return@withContext null
        if (items.length() == 0) return@withContext null
        val first = items.getJSONObject(0)
        val name = first.optString("name")
        if (!namesClose(title, name)) return@withContext null
        val appId = first.optInt("id", 0)
        if (appId == 0) return@withContext null
        val detail = steamGet(
            "https://store.steampowered.com/api/appdetails?appids=$appId&filters=name,header_image,screenshots"
        ) ?: return@withContext null
        val data = runCatching {
            JSONObject(detail).getJSONObject(appId.toString()).getJSONObject("data")
        }.getOrNull() ?: return@withContext null
        val icon = data.optString("header_image").takeIf { it.isNotBlank() }
        val shots = ArrayList<String>()
        val arr = data.optJSONArray("screenshots")
        if (arr != null) {
            for (i in 0 until minOf(arr.length(), 3)) {
                shots.add(arr.getJSONObject(i).getString("path_full"))
            }
        }
        val media = SteamMedia(icon, shots)
        writeCache(title, media)
        media
    }
}
