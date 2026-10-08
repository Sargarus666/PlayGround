package com.mmocal.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupHelper {

    fun export(context: Context): Uri? {
        return runCatching {
            val app = context.applicationContext
            val settings = app.getSharedPreferences(
                AccountsStore.scope("settings"), Context.MODE_PRIVATE
            )
            val favorites = app.getSharedPreferences(
                AccountsStore.scope("favorites"), Context.MODE_PRIVATE
            )
            val userEvents = app.getSharedPreferences(
                AccountsStore.scope("user_events"), Context.MODE_PRIVATE
            )
            val root = JSONObject().apply {
                put("version", 1)
                put("account", AccountsStore.current.value)
                put("settings", JSONObject().apply {
                    put("theme_mode", settings.getString("theme_mode", "SYSTEM"))
                    put("style", settings.getString("style", "MATERIAL_YOU"))
                    put("accent_color", settings.getInt("accent_color", 0))
                    put("notifications", settings.getBoolean("notifications", false))
                    put("auto_update", settings.getBoolean("auto_update", true))
                })
                put("favorites", JSONArray(favorites.getStringSet("watchlist", emptySet())!!.toList()))
                put("userEvents", userEvents.getString("events", "[]"))
            }
            val file = File(app.cacheDir, "mmo-calendar-backup.json")
            file.writeText(root.toString(), Charsets.UTF_8)
            FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        }.getOrNull()
    }

    fun shareBackup(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Отправить копию"))
    }

    fun importBackup(context: Context, uri: Uri): String? {
        return try {
            val app = context.applicationContext
            val text = app.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return "Не удалось прочитать файл"
            val root = JSONObject(text)
            val settingsJson = root.optJSONObject("settings") ?: return "Файл не похож на копию MMO Calendar"
            app.getSharedPreferences(AccountsStore.scope("settings"), Context.MODE_PRIVATE).edit()
                .putString("theme_mode", settingsJson.optString("theme_mode", "SYSTEM"))
                .putString("style", settingsJson.optString("style", "MATERIAL_YOU"))
                .putInt("accent_color", settingsJson.optInt("accent_color", 0))
                .putBoolean("notifications", settingsJson.optBoolean("notifications", false))
                .putBoolean("auto_update", settingsJson.optBoolean("auto_update", true))
                .apply()
            val favArr = root.optJSONArray("favorites") ?: JSONArray()
            val favSet = (0 until favArr.length()).map { favArr.getString(it) }.toSet()
            app.getSharedPreferences(AccountsStore.scope("favorites"), Context.MODE_PRIVATE).edit()
                .putStringSet("watchlist", favSet)
                .apply()
            val eventsRaw = root.optString("userEvents", "[]")
            JSONArray(eventsRaw)
            app.getSharedPreferences(AccountsStore.scope("user_events"), Context.MODE_PRIVATE).edit()
                .putString("events", eventsRaw)
                .apply()
            null
        } catch (e: Exception) {
            "Ошибка импорта: ${e.message}"
        }
    }
}
