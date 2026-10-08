package com.mmocal.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

private const val GOHA_URL = "https://www.goha.ru/mmorpg"

private val gohaMonths = mapOf(
    "января" to 1, "февраля" to 2, "марта" to 3, "апреля" to 4,
    "мая" to 5, "июня" to 6, "июля" to 7, "августа" to 8,
    "сентября" to 9, "октября" to 10, "ноября" to 11, "декабря" to 12
)

data class GohaNews(
    val url: String,
    val title: String,
    val desc: String,
    val dateTime: LocalDateTime,
    val imageUrl: String?
)

private fun gohaRx(p: String) = Regex(p, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

private val reGohaSplit = gohaRx("article-snippet articles-snippets__snippet")
private val reGohaImg = gohaRx("article-snippet__image-img\" src=\"([^\"]+)\"")
private val reGohaTitle = gohaRx("article-snippet__body-title-link\" href=\"([^\"]+)\">([^<]+)</a>")
private val reGohaDesc = gohaRx("article-snippet__body-shortly-label\">(.*?)</span>")
private val reGohaDate = gohaRx("article-snippet__body-date-label\">(.*?)</span>")

private fun decodeEntities(s: String): String = s
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&nbsp;", " ")

fun parseGohaDate(raw: String, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
    val t = raw.trim()
    val hm = Regex("(\\d{1,2}):(\\d{2})").find(t) ?: return null
    val h = hm.groupValues[1].toInt()
    val m = hm.groupValues[2].toInt()
    val today = now.toLocalDate()
    return when {
        t.startsWith("Сегодня") -> today.atTime(h, m)
        t.startsWith("Вчера") -> today.minusDays(1).atTime(h, m)
        else -> {
            val dm = Regex("(\\d{1,2})\\s+(\\S+)").find(t) ?: return null
            val month = gohaMonths[dm.groupValues[2].lowercase()] ?: return null
            var date = LocalDate.of(today.year, month, dm.groupValues[1].toInt())
            if (date.isAfter(today)) date = date.minusYears(1)
            date.atTime(h, m)
        }
    }
}

fun parseGohaPage(html: String): List<GohaNews> {
    val out = ArrayList<GohaNews>()
    for (p in reGohaSplit.split(html).drop(1)) {
        val title = reGohaTitle.find(p) ?: continue
        val dateRaw = reGohaDate.find(p)?.groupValues?.get(1) ?: continue
        val dateTime = parseGohaDate(dateRaw) ?: continue
        val desc = reGohaDesc.find(p)?.groupValues?.get(1)
            ?.replace(Regex("<[^>]+>"), "")?.let { decodeEntities(it) }?.trim().orEmpty()
        val img = reGohaImg.find(p)?.groupValues?.get(1)
        out.add(
            GohaNews(
                url = title.groupValues[1],
                title = decodeEntities(title.groupValues[2].trim()),
                desc = desc,
                dateTime = dateTime,
                imageUrl = img
            )
        )
    }
    return out
}

private fun gohaToJson(list: List<GohaNews>): String {
    val arr = JSONArray()
    for (n in list) {
        arr.put(JSONObject().apply {
            put("url", n.url)
            put("title", n.title)
            put("desc", n.desc)
            put("dt", n.dateTime.toString())
            put("img", n.imageUrl ?: "")
        })
    }
    return arr.toString()
}

private fun gohaFromJson(text: String): List<GohaNews> = runCatching {
    val arr = JSONArray(text)
    (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        GohaNews(
            url = o.getString("url"),
            title = o.getString("title"),
            desc = o.optString("desc"),
            dateTime = LocalDateTime.parse(o.getString("dt")),
            imageUrl = o.optString("img").takeIf { it.isNotBlank() }
        )
    }
}.getOrDefault(emptyList())

object GohaNewsSource {
    private val _news = MutableStateFlow<List<GohaNews>>(emptyList())
    val news: StateFlow<List<GohaNews>> = _news.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("goha_cache", android.content.Context.MODE_PRIVATE)
        prefs?.getString("json", null)?.let { _news.value = gohaFromJson(it) }
    }

    fun lastSync(): Long = prefs?.getLong("ts", 0) ?: 0

    suspend fun refresh(force: Boolean): Boolean = withContext(Dispatchers.IO) {
        val stale = System.currentTimeMillis() - lastSync() > 6L * 60 * 60 * 1000
        if (!force && !stale && _news.value.isNotEmpty()) return@withContext false
        _loading.value = true
        try {
            val all = LinkedHashMap<String, GohaNews>()
            for (page in 1..2) {
                val url = if (page == 1) GOHA_URL else "$GOHA_URL?page=$page"
                val html = runCatching {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 15000
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                    val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    conn.disconnect()
                    text
                }.getOrNull() ?: break
                val list = parseGohaPage(html)
                if (list.isEmpty()) break
                for (n in list) all.putIfAbsent(n.url, n)
            }
            if (all.isNotEmpty()) {
                val sorted = all.values.sortedByDescending { it.dateTime }
                _news.value = sorted
                prefs?.edit()
                    ?.putString("json", gohaToJson(sorted))
                    ?.putLong("ts", System.currentTimeMillis())
                    ?.apply()
                true
            } else false
        } catch (_: Exception) {
            false
        } finally {
            _loading.value = false
        }
    }
}
