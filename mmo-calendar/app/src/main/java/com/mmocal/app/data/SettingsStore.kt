package com.mmocal.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppStyle { MATERIAL_YOU, CLASSIC, MONO }

data class AccentColor(
    val id: String,
    val name: String,
    val value: Long
)

object AccentPalette {
    val colors = listOf(
        AccentColor("blue", "Синий", 0xFF2563EB),
        AccentColor("violet", "Фиолетовый", 0xFF7C5CFF),
        AccentColor("teal", "Бирюзовый", 0xFF0D9488),
        AccentColor("green", "Зелёный", 0xFF16A34A),
        AccentColor("orange", "Оранжевый", 0xFFEA580C),
        AccentColor("rose", "Розовый", 0xFFDB2777),
        AccentColor("red", "Красный", 0xFFDC2626)
    )

    val defaultIndex = 0

    fun at(index: Int): AccentColor = colors.getOrElse(index) { colors[defaultIndex] }
}

class SettingsStore(context: Context) {
    private val appContext = context.applicationContext

    private fun prefs() = appContext.getSharedPreferences(
        AccountsStore.scope("settings"), Context.MODE_PRIVATE
    )

    private val _themeMode = MutableStateFlow(readTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _style = MutableStateFlow(readStyle())
    val style: StateFlow<AppStyle> = _style.asStateFlow()

    private val _colorIndex = MutableStateFlow(
        prefs().getInt(KEY_COLOR, AccentPalette.defaultIndex)
    )
    val colorIndex: StateFlow<Int> = _colorIndex.asStateFlow()

    private val _notifications = MutableStateFlow(prefs().getBoolean(KEY_NOTIFY, false))
    val notifications: StateFlow<Boolean> = _notifications.asStateFlow()

    private val _autoUpdate = MutableStateFlow(prefs().getBoolean(KEY_AUTO, true))
    val autoUpdate: StateFlow<Boolean> = _autoUpdate.asStateFlow()

    fun reload() {
        _themeMode.value = readTheme()
        _style.value = readStyle()
        _colorIndex.value = prefs().getInt(KEY_COLOR, AccentPalette.defaultIndex)
        _notifications.value = prefs().getBoolean(KEY_NOTIFY, false)
        _autoUpdate.value = prefs().getBoolean(KEY_AUTO, true)
    }

    private fun readTheme(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs().getString(KEY_THEME, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)

    private fun readStyle(): AppStyle =
        runCatching { AppStyle.valueOf(prefs().getString(KEY_STYLE, AppStyle.MATERIAL_YOU.name)!!) }
            .getOrDefault(AppStyle.MATERIAL_YOU)

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs().edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setStyle(style: AppStyle) {
        _style.value = style
        prefs().edit().putString(KEY_STYLE, style.name).apply()
    }

    fun setColorIndex(index: Int) {
        _colorIndex.value = index
        prefs().edit().putInt(KEY_COLOR, index).apply()
    }

    fun setNotifications(enabled: Boolean) {
        _notifications.value = enabled
        prefs().edit().putBoolean(KEY_NOTIFY, enabled).apply()
    }

    fun setAutoUpdate(enabled: Boolean) {
        _autoUpdate.value = enabled
        prefs().edit().putBoolean(KEY_AUTO, enabled).apply()
    }

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_STYLE = "style"
        const val KEY_COLOR = "accent_color"
        const val KEY_NOTIFY = "notifications"
        const val KEY_AUTO = "auto_update"
    }
}
