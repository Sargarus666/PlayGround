package com.mmocal.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class UserEventsStore(context: Context) {
    private val appContext = context.applicationContext

    private fun prefs() = appContext.getSharedPreferences(
        AccountsStore.scope("user_events"), Context.MODE_PRIVATE
    )

    private val _events = MutableStateFlow(load())
    val events: StateFlow<List<GameEvent>> = _events.asStateFlow()

    fun reload() {
        _events.value = load()
    }

    fun add(title: String, date: LocalDate, type: EventType) {
        val event = GameEvent(
            id = "user_${System.currentTimeMillis()}",
            title = title.trim(),
            developer = "Ваш релиз",
            date = date,
            type = type,
            confirmed = true,
            platforms = setOf(Platform.PC),
            model = AccessModel.UNKNOWN,
            description = "Событие, добавленное вами в календарь.",
            custom = true
        )
        val list = _events.value + event
        save(list)
        _events.value = list
    }

    fun remove(id: String) {
        val list = _events.value.filterNot { it.id == id }
        save(list)
        _events.value = list
    }

    private fun save(list: List<GameEvent>) {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(
                JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("date", e.date.toString())
                    put("type", e.type.name)
                }
            )
        }
        prefs().edit().putString("events", arr.toString()).apply()
    }

    private fun load(): List<GameEvent> {
        val raw = prefs().getString("events", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                GameEvent(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    developer = "Ваш релиз",
                    date = LocalDate.parse(o.getString("date")),
                    type = runCatching { EventType.valueOf(o.getString("type")) }
                        .getOrDefault(EventType.LAUNCH),
                    confirmed = true,
                    platforms = setOf(Platform.PC),
                    model = AccessModel.UNKNOWN,
                    description = "Событие, добавленное вами в календарь.",
                    custom = true
                )
            }
        }.getOrDefault(emptyList())
    }
}
