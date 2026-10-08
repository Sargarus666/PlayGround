package com.mmocal.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

private const val MMO13_BASE = "https://mmo13.ru"
private const val MMO13_CAL = "$MMO13_BASE/gamestest/calendar"

private val monthNames = listOf(
    "january", "february", "march", "april", "may", "june",
    "july", "august", "september", "october", "november", "december"
)

// Курируемый список AAA-проектов (подстроки в нижнем регистре). Дополняется вручную.
private val aaaKeywords = listOf(
    "aion", "archeage", "chrono odyssey", "crimson desert", "ashes of creation",
    "throne and liberty", "pax dei", "soulframe", "guild wars", "path of exile",
    "dune", "gta", "warcraft", "everquest", "maplestory", "lost ark",
    "black desert", "final fantasy", "ragnarok", "lineage", "blue protocol",
    "new world", "tarisland", "bless", "elyon", "tera",
    "night crows", "odin", "ni no kuni", "diablo immortal", "tower of fantasy",
    "hytale", "palia", "corepunk", "quinfall",
    "bellatores", "eternal tombs", "velmora", "laryen", "loftia", "scapewatch",
    "broken ranks", "eclipse", "architect", "aniimo", "dragonwilds",
    "aria eternal", "gloria victis", "mabinogi", "albion", "mir4",
    "star resonance", "ares", "icarus", "rf online", "wonderking", "hunter immortal"
)

private val cjkRx = Regex("[\u4E00-\u9FFF\u3400-\u4DBF\u3040-\u30FF\uAC00-\uD7AF]")

private fun normTitle(s: String): String =
    s.lowercase().filter { it.isLetterOrDigit() }

// Строгое совпадение AAA: ключевое слово целиком (границы слов),
// чтобы "mir" не срабатывал на "admire", а "aion" — на что попало.
fun isAaaTitle(title: String): Boolean {
    val t = title.lowercase()
    return aaaKeywords.any { kw ->
        Regex("\\b" + Regex.escape(kw) + "\\b").containsMatchIn(t)
    }
}

private fun absUrl(u: String): String = when {
    u.startsWith("http") -> u
    u.startsWith("//") -> "https:$u"
    u.startsWith("/") -> MMO13_BASE + u
    else -> "$MMO13_BASE/$u"
}

private fun stripTags(s: String): String =
    s.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()

private fun decodeEntities(s: String): String = s
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&nbsp;", " ")

private fun fetchText(url: String): String? = runCatching {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connectTimeout = 10000
    conn.readTimeout = 15000
    conn.setRequestProperty("User-Agent", "Mozilla/5.0")
    val text = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    conn.disconnect()
    text
}.getOrNull()

private data class MmoEntry(
    val mmoId: String,
    val slug: String,
    val title: String,
    val date: LocalDate?,
    val type: EventType?,
    val rating100: Int?,
    val iconUrl: String?,
    val region: String,
    val genre: String,
    val platforms: Set<Platform>,
    val model: AccessModel
)

private data class GameDetails(
    val developer: String,
    val publisher: String,
    val description: String,
    val shots: List<String>
)

private fun rx(p: String) = Regex(p, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

private val entrySplit = Regex("<div id=\"games-\\d+\"")
private val reGameLink = rx("href=\"/games/(\\d+)_([a-z0-9-]+)\"")
private val reTitle = rx("<div class=\"title atxt\">\\s*<a[^>]*>(.*?)</a>")
private val reLogo = rx("<img class=\"logo\"[^>]*src=\"([^\"]+)\"")
private val reRating = rx("game-info-rating mmo13[^\"]*\"[^>]*>\\s*<span class=\"value[^\"]*\">([\\d.]+)</span>")
private val reStatus = rx("<span class=\"teststatus\">(.*?)</span>")
private val reDate = rx("<span class=\"date\">(\\d{2})\\.(\\d{2})\\.(\\d{4})</span>")
private val rePlatform = rx("<span class=\"value\" title=\"Платформа\">(.*?)</span>")
private val rePayment = rx("payment\"[^>]*><span>.*?</span><span>(.*?)</span>")
private val reGenre = rx("ganre bcell\"[^>]*>(.*?)</div>")
private val reRegion = rx("title=\"([^\"]*регион[^\"]*)\"")

private val reDev = rx("<span class=\"label\">Разработчик</span>\\s*<div class=\"value\">(.*?)</div>")
private val rePub = rx("game-view-row publisher\".*?<span class=\"value\">(.*?)</span>")
private val reReview = rx("<h2 class=\"label\">Обзор игры.*?</h2>\\s*<div class=\"view-body[^\"]*\">\\s*<p>(.*?)</p>")
private val reShot = rx("\"(/images/thumb/(?:news|content)/[^\"]+\\.(?:jpg|webp)[^\"]*)\"")
private val reYt = rx("data-poster=\"(https://img\\.youtube\\.com/vi/[^\"]+)\"")

private fun mapStatus(s: String): EventType? = when {
    "ЗБТ" in s || "ОБТ" in s -> EventType.BETA
    "Релиз" in s -> EventType.LAUNCH
    "РНД" in s -> EventType.EARLY_ACCESS
    else -> null
}

private fun mapPlatforms(s: String): Set<Platform> {
    val out = mutableSetOf<Platform>()
    if ("PC" in s || "ПК" in s) out.add(Platform.PC)
    if ("Mobile" in s || "Android" in s || "iOS" in s || "Моби" in s) out.add(Platform.MOBILE)
    if ("PlayStation" in s) out.add(Platform.PLAYSTATION)
    if ("Xbox" in s) out.add(Platform.XBOX)
    if ("Switch" in s) out.add(Platform.SWITCH)
    return out
}

private fun mapModel(s: String): AccessModel = when {
    "Бесплат" in s -> AccessModel.F2P
    "Разовая покупка" in s -> AccessModel.B2P
    "Подписка" in s -> AccessModel.SUBSCRIPTION
    else -> AccessModel.UNKNOWN
}

private fun parseCalendarPage(html: String): List<MmoEntry> {
    val out = ArrayList<MmoEntry>()
    val parts = entrySplit.split(html).drop(1)
    for (p in parts) {
        if ("cl-block-rk" in p.take(300)) continue
        val link = reGameLink.find(p) ?: continue
        val titleRaw = reTitle.find(p)?.groupValues?.get(1) ?: continue
        val title = decodeEntities(stripTags(titleRaw))
        if (title.isEmpty() || cjkRx.containsMatchIn(title)) continue
        val status = reStatus.find(p)?.groupValues?.get(1) ?: continue
        val type = mapStatus(status) ?: continue
        val dm = reDate.find(p) ?: continue
        val date = runCatching {
            LocalDate.of(dm.groupValues[3].toInt(), dm.groupValues[2].toInt(), dm.groupValues[1].toInt())
        }.getOrNull() ?: continue
        val rating = reRating.find(p)?.groupValues?.get(1)?.toFloatOrNull()
            ?.let { (it * 10).toInt().coerceIn(1, 100) }
        val logo = reLogo.find(p)?.groupValues?.get(1)?.let { absUrl(it.split("?")[0]) }
        val region = reRegion.find(p)?.groupValues?.get(1)
            ?.replace(" (регион)", "")?.trim().orEmpty()
        val genre = reGenre.find(p)?.groupValues?.get(1)
            ?.let { decodeEntities(stripTags(it)).split("|").map { g -> g.trim() }.filter { it.isNotEmpty() }.joinToString(", ") }
            .orEmpty()
        val plat = rePlatform.find(p)?.groupValues?.get(1) ?: ""
        val pay = rePayment.find(p)?.groupValues?.get(1) ?: ""
        out.add(
            MmoEntry(
                mmoId = link.groupValues[1],
                slug = link.groupValues[2],
                title = title,
                date = date,
                type = type,
                rating100 = rating,
                iconUrl = logo,
                region = region,
                genre = genre,
                platforms = mapPlatforms(stripTags(plat)),
                model = mapModel(stripTags(pay))
            )
        )
    }
    return out
}

private fun parseGameDetails(html: String): GameDetails {
    val dev = reDev.find(html)?.groupValues?.get(1)?.let { decodeEntities(stripTags(it)) }.orEmpty()
    val pub = rePub.find(html)?.groupValues?.get(1)?.let { decodeEntities(stripTags(it)) }.orEmpty()
    val desc = reReview.find(html)?.groupValues?.get(1)
        ?.let { decodeEntities(stripTags(it)) }?.take(600).orEmpty()
    val shots = LinkedHashSet<String>()
    for (m in reShot.findAll(html)) {
        shots.add(absUrl(m.groupValues[1].split("?")[0]))
        if (shots.size >= 2) break
    }
    for (m in reYt.findAll(html)) {
        shots.add(m.groupValues[1])
        if (shots.size >= 3) break
    }
    return GameDetails(dev, pub, desc, shots.toList().take(3))
}

object Mmo13Source {
    private val _progress = MutableStateFlow<String?>(null)
    val progress: StateFlow<String?> = _progress.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences("mmo13_extra", android.content.Context.MODE_PRIVATE)
    }

    private fun readDetails(): MutableMap<String, GameDetails> {
        val map = LinkedHashMap<String, GameDetails>()
        val root = runCatching {
            JSONObject(prefs?.getString("details", "{}") ?: "{}")
        }.getOrDefault(JSONObject())
        for (k in root.keys()) {
            val o = root.optJSONObject(k) ?: continue
            val arr = o.optJSONArray("shots")
            val shots = if (arr == null) emptyList()
            else (0 until arr.length()).map { arr.getString(it) }
            map[k] = GameDetails(
                o.optString("dev"),
                o.optString("pub"),
                o.optString("desc"),
                shots
            )
        }
        return map
    }

    private fun writeDetails(map: Map<String, GameDetails>) {
        val root = JSONObject()
        for ((k, v) in map) {
            root.put(k, JSONObject().apply {
                put("dev", v.developer)
                put("pub", v.publisher)
                put("desc", v.description)
                put("shots", org.json.JSONArray(v.shots))
            })
        }
        prefs?.edit()?.putString("details", root.toString())?.apply()
    }

    private fun fetchDetails(slug: String, mmoId: String): GameDetails? {
        val html = fetchText("$MMO13_BASE/games/$mmoId-$slug") ?: return null
        return parseGameDetails(html)
    }

    suspend fun fetchAll(): List<GameEvent> = withContext(Dispatchers.IO) {
        val entries = LinkedHashMap<String, MmoEntry>()
        val today = LocalDate.now()
        val months = ArrayList<Pair<Int, String>>()
        var d = today.minusMonths(1).withDayOfMonth(1)
        repeat(4) {
            months.add(d.year to monthNames[d.monthValue - 1])
            d = d.plusMonths(1)
        }
        coroutineScope {
            months.mapIndexed { index, (year, month) ->
                async {
                    _progress.value = "Тесты: ${index + 1}/${months.size} ($month $year)…"
                    val local = ArrayList<MmoEntry>()
                    val seen = HashSet<String>()
                    for (page in 1..3) {
                        val url = if (page == 1) "$MMO13_CAL/$year/$month/2-mmorpg"
                        else "$MMO13_CAL/$year/$month/2-mmorpg/page_$page"
                        val html = fetchText(url) ?: break
                        val list = parseCalendarPage(html)
                        if (list.isEmpty()) break
                        var fresh = 0
                        for (e in list) {
                            if (seen.add(e.mmoId + "|" + e.date.toString())) {
                                fresh++
                                local.add(e)
                            }
                        }
                        if (fresh == 0) break
                    }
                    local
                }
            }.awaitAll().flatten().forEach { e ->
                entries.putIfAbsent(e.mmoId + "|" + e.date.toString(), e)
            }
        }
        _progress.value = "Тесты: описания…"
        val details = readDetails()
        var fetched = 0
        for (e in entries.values) {
            if (!details.containsKey(e.mmoId) && fetched < 12) {
                val det = runCatching { fetchDetails(e.slug, e.mmoId) }.getOrNull()
                if (det != null) {
                    details[e.mmoId] = det
                    fetched++
                }
            }
        }
        writeDetails(details)
        _progress.value = null
        val result = ArrayList<GameEvent>()
        var steamUsed = 0
        val cutoff = LocalDate.now().minusDays(45)
        for (e in entries.values) {
            // Проверка актуальности по дате: протухшие тесты (старше 45 дней)
            // не берём, только грядущие и свежие.
            if (e.date!!.isBefore(cutoff)) continue
            val det = details[e.mmoId]
            val dev = det?.developer?.takeIf { it.isNotBlank() }.orEmpty()
            val pub = det?.publisher?.takeIf { it.isNotBlank() }.orEmpty()
            var desc = det?.description?.takeIf { it.isNotBlank() }.orEmpty()
            if (desc.isBlank()) {
                val g = e.genre.ifBlank { "MMORPG" }
                val dd = "%02d.%02d.%d".format(e.date!!.dayOfMonth, e.date.monthValue, e.date.year)
                val sb = StringBuilder("$g. ${e.type!!.label} — $dd.")
                val plats = e.platforms.joinToString(", ") { it.label }
                if (plats.isNotBlank()) sb.append(" Платформы: $plats.")
                if (e.region.isNotBlank()) sb.append(" Регион: ${e.region.lowercase()}.")
                if (e.model != AccessModel.UNKNOWN) sb.append(" Модель: ${e.model.label}.")
                if (e.rating100 != null) sb.append(" Рейтинг: ${e.rating100}/100.")
                desc = sb.toString()
            }
            var shots = det?.shots ?: emptyList()
            var icon = e.iconUrl
            if (passesGate(e.title, e.rating100) && (shots.isEmpty() || icon == null) && steamUsed < 8) {
                val media = runCatching { SteamSource.findMedia(e.title) }.getOrNull()
                if (media != null) {
                    steamUsed++
                    if (icon == null) icon = media.iconUrl
                    if (shots.isEmpty()) shots = media.shots
                }
            }
            result.add(
                GameEvent(
                    id = "mmo13-" + e.mmoId,
                    title = e.title,
                    developer = dev,
                    date = e.date,
                    type = e.type!!,
                    confirmed = true,
                    platforms = e.platforms,
                    model = e.model,
                    description = desc,
                    iconUrl = icon,
                    screenshots = shots,
                    rating100 = e.rating100,
                    aaa = isAaaTitle(e.title),
                    source = "mmo13",
                    genre = e.genre,
                    publisher = pub,
                    region = e.region
                )
            )
        }
        result
    }

    fun titlesMatch(a: String, b: String): Boolean {
        val x = normTitle(a)
        val y = normTitle(b)
        if (x.length < 5 || y.length < 5) return false
        if (x in y || y in x) return true
        // Пословное: все значимые слова короткого названия должны
        // встречаться в длинном. "WoW: Forever" и "World of Warcraft:
        // Forever" сливаются, а "Ragnarok Origin" и "Ragnarok X" — нет.
        // Одиночные общие слова ("online", "world") совпадением не считаются.
        val generic = setOf(
            "online", "world", "worlds", "game", "games", "mobile", "legends",
            "classic", "saga", "war", "wars", "age", "new", "dark", "eternal",
            "fantasy", "craft", "stars", "star", "king", "lord", "knight"
        )
        fun words(s: String) = s.lowercase()
            .split(Regex("[^a-z0-9а-яё]+"))
            .filter { it.length >= 4 }
        val wx = words(a)
        val wy = words(b)
        if (wx.isEmpty() || wy.isEmpty()) return false
        val (short, longS) = if (wx.size <= wy.size) wx to y else wy to x
        val meaningful = short.filter { it !in generic }
        if (meaningful.isEmpty()) return false
        if (short.size == 1 && short[0].length < 6) return false
        return meaningful.all { it in longS }
    }

    // В ленту попадают только ожидаемые AAA и игры с высоким рейтингом.
    // Остальное обогащает базу, но отдельными событиями не становится.
    fun passesGate(title: String, rating100: Int?): Boolean =
        isAaaTitle(title) || (rating100 ?: 0) >= 65

    private val excludedTitles = setOf("crimsondesert")

    fun isExcluded(title: String): Boolean {
        val n = normTitle(title)
        return excludedTitles.any { it in n }
    }
}
