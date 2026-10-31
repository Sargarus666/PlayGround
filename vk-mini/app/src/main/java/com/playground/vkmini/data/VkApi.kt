package com.playground.vkmini.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import kotlin.random.Random

class VkApi(private val token: String) {

    suspend fun profile(): Profile = withContext(Dispatchers.IO) {
        val root = call("users.get", mapOf("fields" to "photo_200,status,city,counters"))
        val user = root.getJSONArray("response").getJSONObject(0)
        Profile(
            id = user.optLong("id"),
            name = listOf(user.str("first_name"), user.str("last_name"))
                .filterNotNull().joinToString(" ").ifBlank { "Профиль" },
            photo = user.str("photo_200") ?: user.str("photo_100"),
            status = user.str("status"),
            city = user.optJSONObject("city")?.str("title"),
            counters = user.optJSONObject("counters")?.let { counters ->
                counters.keys().asSequence()
                    .filter { counters.opt(it) is Number }
                    .associate { it to counters.optInt(it) }
            } ?: emptyMap(),
        )
    }

    suspend fun feed(from: String? = null): Pair<List<FeedPost>, String?> = withContext(Dispatchers.IO) {
        val params = mutableMapOf(
            "filters" to "post",
            "count" to "20",
            "return_banned" to "0",
        )
        if (from != null) params["start_from"] = from
        val root = call("newsfeed.get", params)
        val resp = root.getJSONObject("response")

        val users = HashMap<Long, Pair<String, String?>>()
        resp.optJSONArray("profiles")?.let { array ->
            for (i in 0 until array.length()) {
                val p = array.optJSONObject(i) ?: continue
                val name = listOf(p.str("first_name"), p.str("last_name"))
                    .filterNotNull().joinToString(" ").ifBlank { "Пользователь" }
                users[p.optLong("id")] = name to (p.str("photo_100") ?: p.str("photo_200"))
            }
        }
        val groups = HashMap<Long, Pair<String, String?>>()
        resp.optJSONArray("groups")?.let { array ->
            for (i in 0 until array.length()) {
                val g = array.optJSONObject(i) ?: continue
                groups[g.optLong("id")] = (g.str("name") ?: "Сообщество") to
                    (g.str("photo_200") ?: g.str("photo_100"))
            }
        }

        val items = resp.optJSONArray("items") ?: JSONArray()
        val posts = ArrayList<FeedPost>(items.length())
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            if (item.optString("type") != "post") continue

            val sourceId = item.optLong("source_id")
            val author = if (sourceId < 0) groups[-sourceId] else users[sourceId]
            val copy = item.optJSONArray("copy_history")?.optJSONObject(0)
            val text = item.str("text") ?: copy?.str("text").orEmpty()
            val image = firstPhoto(item.optJSONArray("attachments"))
                ?: firstPhoto(copy?.optJSONArray("attachments"))

            posts += FeedPost(
                id = sourceId * 1_000_000_000L + item.optLong("post_id"),
                author = author?.first ?: "VK",
                avatar = author?.second,
                date = item.optLong("date"),
                text = text,
                image = image,
                likes = item.optJSONObject("likes")?.optInt("count") ?: 0,
                comments = item.optJSONObject("comments")?.optInt("count") ?: 0,
                reposts = item.optJSONObject("reposts")?.optInt("count") ?: 0,
            )
        }
        posts to resp.str("next_from")
    }

    suspend fun conversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val root = call(
            "messages.getConversations",
            mapOf("count" to "40", "extended" to "1", "fields" to "photo_100"),
        )
        val resp = root.getJSONObject("response")

        val users = HashMap<Long, Pair<String, String?>>()
        resp.optJSONArray("profiles")?.let { array ->
            for (i in 0 until array.length()) {
                val p = array.optJSONObject(i) ?: continue
                val name = listOf(p.str("first_name"), p.str("last_name"))
                    .filterNotNull().joinToString(" ").ifBlank { "Пользователь" }
                users[p.optLong("id")] = name to p.str("photo_100")
            }
        }
        val groups = HashMap<Long, Pair<String, String?>>()
        resp.optJSONArray("groups")?.let { array ->
            for (i in 0 until array.length()) {
                val g = array.optJSONObject(i) ?: continue
                groups[g.optLong("id")] = (g.str("name") ?: "Сообщество") to g.str("photo_100")
            }
        }

        val result = ArrayList<Conversation>()
        val items = resp.optJSONArray("items") ?: JSONArray()
        for (i in 0 until items.length()) {
            val wrapper = items.optJSONObject(i) ?: continue
            val conv = wrapper.optJSONObject("conversation") ?: continue
            val peer = conv.optJSONObject("peer") ?: continue
            val peerId = peer.optLong("id")
            val type = peer.optString("type")
            val chat = conv.optJSONObject("chat_settings")
            val chatPhoto = chat?.optJSONObject("photo")?.str("photo_100")

            val fallback = when (type) {
                "user" -> users[peerId]
                "group" -> groups[peerId]
                else -> null
            }
            val title = chat?.str("title") ?: fallback?.first ?: "Диалог"
            val avatar = chatPhoto ?: fallback?.second

            val last = wrapper.optJSONObject("last_message")
            val lastText = last?.str("text") ?: attachmentLabel(last)
            result += Conversation(
                peerId = peerId,
                title = title,
                avatar = avatar,
                lastText = lastText,
                date = last?.optLong("date") ?: 0L,
                unread = conv.optInt("unread_count"),
            )
        }
        result.sortedByDescending { it.date }
    }

    suspend fun history(peerId: Long): List<ChatMessage> = withContext(Dispatchers.IO) {
        val root = call(
            "messages.getHistory",
            mapOf(
                "peer_id" to peerId.toString(),
                "count" to "50",
                "extended" to "1",
                "fields" to "photo_100",
            ),
        )
        val resp = root.getJSONObject("response")
        val names = HashMap<Long, String>()
        resp.optJSONArray("profiles")?.let { array ->
            for (i in 0 until array.length()) {
                val p = array.optJSONObject(i) ?: continue
                names[p.optLong("id")] = listOf(p.str("first_name"), p.str("last_name"))
                    .filterNotNull().joinToString(" ")
            }
        }
        val items = resp.optJSONArray("items") ?: JSONArray()
        val result = ArrayList<ChatMessage>(items.length())
        for (i in 0 until items.length()) {
            val m = items.optJSONObject(i) ?: continue
            val out = m.optInt("out") == 1
            val text = m.str("text") ?: attachmentLabel(m)
            result += ChatMessage(
                id = m.optLong("id"),
                peerId = peerId,
                out = out,
                text = text,
                date = m.optLong("date"),
                author = if (out) null else names[m.optLong("from_id")],
            )
        }
        result.sortedBy { it.date }
    }

    suspend fun send(peerId: Long, text: String) {
        withContext(Dispatchers.IO) {
            call(
                "messages.send",
                mapOf(
                    "peer_id" to peerId.toString(),
                    "message" to text,
                    "random_id" to Random.nextInt(1, Int.MAX_VALUE).toString(),
                ),
            )
        }
    }

    suspend fun markRead(peerId: Long) {
        withContext(Dispatchers.IO) {
            call("messages.markAsRead", mapOf("peer_id" to peerId.toString()))
        }
    }

    suspend fun tracks(ownerId: Long): List<Track> = withContext(Dispatchers.IO) {
        val root = call(
            "audio.get",
            mapOf("owner_id" to ownerId.toString(), "count" to "100"),
            version = "5.131",
        )
        val response = root.opt("response")
        val items = when (response) {
            is JSONObject -> response.optJSONArray("items") ?: response.optJSONArray("audios")
            is JSONArray -> response
            else -> null
        } ?: JSONArray()

        val result = ArrayList<Track>(items.length())
        for (i in 0 until items.length()) {
            val a = items.optJSONObject(i) ?: continue
            result += Track(
                id = a.optLong("id"),
                artist = a.str("artist") ?: "Неизвестный исполнитель",
                title = a.str("title") ?: "Без названия",
                duration = a.optInt("duration"),
                url = a.str("url"),
            )
        }
        result
    }

    private fun call(
        method: String,
        params: Map<String, String>,
        version: String = API_VERSION,
    ): JSONObject {
        val query = StringBuilder()
        fun add(key: String, value: String) {
            if (query.isNotEmpty()) query.append('&')
            query.append(URLEncoder.encode(key, "UTF-8"))
                .append('=')
                .append(URLEncoder.encode(value, "UTF-8"))
        }
        add("access_token", token)
        add("v", version)
        params.forEach { (key, value) -> add(key, value) }

        val usePost = method == "messages.send" || query.length > 1800
        val endpoint = if (usePost) {
            "https://api.vk.com/method/$method"
        } else {
            "https://api.vk.com/method/$method?$query"
        }
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Accept-Encoding", "gzip")
        connection.setRequestProperty("User-Agent", "VKMini/0.1 (Android)")
        if (usePost) {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8",
            )
            connection.outputStream.use { output ->
                output.write(query.toString().toByteArray(Charsets.UTF_8))
            }
        }

        try {
            val code = connection.responseCode
            val raw = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = readBody(raw, connection.contentEncoding)
            if (body.isBlank()) throw IOException("Пустой ответ VK ($code)")
            val json = JSONObject(body)
            json.optJSONObject("error")?.let { error ->
                val errorCode = error.optInt("error_code", -1)
                throw VkApiException(errorCode, describeError(errorCode, error.optString("error_msg")))
            }
            return json
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(stream: InputStream?, encoding: String?): String {
        if (stream == null) return ""
        val decoded = if (encoding?.contains("gzip", ignoreCase = true) == true) {
            GZIPInputStream(stream)
        } else {
            stream
        }
        return decoded.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun firstPhoto(attachments: JSONArray?): String? {
        if (attachments == null) return null
        for (i in 0 until attachments.length()) {
            val item = attachments.optJSONObject(i) ?: continue
            if (item.optString("type") != "photo") continue
            val photo = item.optJSONObject("photo") ?: continue
            val sizes = photo.optJSONArray("sizes") ?: continue
            var best: String? = null
            var bestWidth = -1
            for (j in 0 until sizes.length()) {
                val size = sizes.optJSONObject(j) ?: continue
                val width = size.optInt("width")
                val url = size.str("url") ?: continue
                if (width > bestWidth) {
                    bestWidth = width
                    best = url
                }
            }
            if (best != null) return best
        }
        return null
    }

    private fun attachmentLabel(message: JSONObject?): String {
        val attachments = message?.optJSONArray("attachments")
        if (attachments == null || attachments.length() == 0) return ""
        return when (attachments.optJSONObject(0)?.optString("type")) {
            "photo" -> "Фото"
            "video" -> "Видео"
            "audio" -> "Аудио"
            "audio_message" -> "Голосовое сообщение"
            "doc" -> "Документ"
            "sticker" -> "Стикер"
            "wall" -> "Запись на стене"
            "link" -> "Ссылка"
            "market" -> "Товар"
            else -> "Вложение"
        }
    }

    companion object {
        const val API_VERSION = "5.199"

        fun describeError(code: Int, raw: String): String = when (code) {
            5 -> "Токен недействителен или истёк. Войди заново."
            6 -> "Слишком много запросов. Подожди пару секунд."
            15 -> "VK закрыл этот метод для сторонних клиентов."
            18 -> "Аккаунт заблокирован."
            100 -> "Неверные параметры запроса."
            200 -> "Доступ запрещён: у токена нет нужных прав."
            else -> "VK API: $raw (код $code)"
        }

        fun friendlyMessage(error: Throwable): String = when (error) {
            is VkApiException -> error.message ?: "Ошибка VK API"
            is IOException -> "Нет соединения с VK"
            else -> error.message ?: "Неизвестная ошибка"
        }
    }
}

private fun JSONObject.str(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val value = optString(key)
    return value.ifBlank { null }
}
