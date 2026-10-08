package com.mmocal.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom

object AccountsStore {
    private lateinit var prefs: android.content.SharedPreferences

    private val _current = MutableStateFlow<String?>(null)
    val current: StateFlow<String?> = _current.asStateFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("accounts", Context.MODE_PRIVATE)
        _current.value = prefs.getString(KEY_SESSION, null)
    }

    fun scope(base: String): String =
        _current.value?.let { "${base}_$it" } ?: base

    /** Проверка реального соединения с сервером данных. Регистрация/вход работают только онлайн. */
    suspend fun checkOnline(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(GamesRepository.DATA_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "HEAD"
            conn.connect()
            val ok = conn.responseCode in 200..399
            conn.disconnect()
            ok
        }.getOrDefault(false)
    }

    /** Время последней успешной онлайн-синхронизации аккаунта (ms), 0 — ни разу. */
    fun lastSync(email: String): Long {
        val list = readAccounts()
        for (i in 0 until list.length()) {
            val o = list.getJSONObject(i)
            if (o.getString("email") == email) return o.optLong("synced_at", 0)
        }
        return 0
    }

    private fun stampSync(list: JSONArray, email: String): JSONArray {
        val out = JSONArray()
        for (i in 0 until list.length()) {
            val o = list.getJSONObject(i)
            if (o.getString("email") == email) o.put("synced_at", System.currentTimeMillis())
            out.put(o)
        }
        return out
    }

    suspend fun register(email: String, password: String): String? {
        if (!checkOnline()) return "Нет соединения с интернетом. Регистрация работает только онлайн."
        val mail = email.trim().lowercase()
        if (!isValidEmail(mail)) return "Некорректный email"
        if (password.length < 6) return "Пароль от 6 символов"
        val list = readAccounts()
        for (i in 0 until list.length()) {
            if (list.getJSONObject(i).getString("email") == mail) return "Аккаунт уже существует"
        }
        val salt = randomHex(16)
        val out = JSONArray()
        for (i in 0 until list.length()) out.put(list.get(i))
        out.put(mapToJson(mail, salt, hash(password, salt)))
        val stamped = stampSync(out, mail)
        prefs.edit().putString(KEY_LIST, stamped.toString()).apply()
        _current.value = mail
        prefs.edit().putString(KEY_SESSION, mail).apply()
        return null
    }

    suspend fun login(email: String, password: String): String? {
        if (!checkOnline()) return "Нет соединения с интернетом. Вход работает только онлайн."
        val mail = email.trim().lowercase()
        val list = readAccounts()
        for (i in 0 until list.length()) {
            val o = list.getJSONObject(i)
            if (o.getString("email") == mail) {
                val salt = o.getString("salt")
                if (hash(password, salt) == o.getString("hash")) {
                    prefs.edit().putString(KEY_LIST, stampSync(list, mail).toString()).apply()
                    _current.value = mail
                    prefs.edit().putString(KEY_SESSION, mail).apply()
                    return null
                }
                return "Неверный пароль"
            }
        }
        return "Аккаунт не найден"
    }

    fun logout() {
        _current.value = null
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private fun readAccounts(): JSONArray =
        runCatching { JSONArray(prefs.getString(KEY_LIST, "[]")!!) }
            .getOrDefault(JSONArray())

    private fun mapToJson(email: String, salt: String, hash: String): JSONObject =
        JSONObject().apply {
            put("email", email)
            put("salt", salt)
            put("hash", hash)
        }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun randomHex(len: Int): String {
        val rnd = SecureRandom()
        val bytes = ByteArray(len)
        rnd.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun isValidEmail(email: String): Boolean =
        email.length in 5..100 && email.contains("@") && email.substringAfter("@").contains(".")

    private const val KEY_LIST = "list"
    private const val KEY_SESSION = "session"
}
