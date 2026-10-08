package com.mmocal.app.data

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class EventType(val label: String, val emoji: String) {
    LAUNCH("Релиз", "🚀"),
    EARLY_ACCESS("Ранний доступ", "🔓"),
    BETA("ЗБТ / ОБТ", "🧪"),
    UPDATE("Обновление", "🛠")
}

enum class AccessModel(val label: String) {
    F2P("Free-to-play"),
    B2P("Buy-to-play"),
    SUBSCRIPTION("Подписка"),
    MIXED("Смешанная"),
    UNKNOWN("Неизвестно")
}

enum class Platform(val label: String) {
    PC("PC"),
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    SWITCH("Switch"),
    MOBILE("Mobile"),
    MAC("macOS"),
    BROWSER("Browser")
}

data class GameEvent(
    val id: String,
    val title: String,
    val developer: String,
    val date: LocalDate?,
    val windowLabel: String? = null,
    val type: EventType,
    val confirmed: Boolean = true,
    val platforms: Set<Platform>,
    val model: AccessModel,
    val description: String,
    val note: String? = null,
    val iconRes: Int? = null,
    val iconUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val custom: Boolean = false,
    val rating100: Int? = null,
    val aaa: Boolean = false,
    val source: String = "local",
    val genre: String = "",
    val publisher: String = "",
    val region: String = ""
) {
    val isDated: Boolean get() = date != null
    val windowText: String get() = windowLabel ?: date?.let { formatDate(it) }.orEmpty()

    fun daysUntil(today: LocalDate): Long? =
        date?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it) }
}

fun formatDate(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.SHORT_STANDALONE, Locale("ru"))
    return "${date.dayOfMonth} $month ${date.year}"
}

fun formatMonthYear(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
    val capitalized = month.replaceFirstChar { it.uppercase(Locale.ROOT) }
    return "$capitalized ${date.year}"
}
