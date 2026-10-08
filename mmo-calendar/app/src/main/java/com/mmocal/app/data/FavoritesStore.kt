package com.mmocal.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesStore(context: Context) {
    private val appContext = context.applicationContext

    private fun prefs() = appContext.getSharedPreferences(
        AccountsStore.scope("favorites"), Context.MODE_PRIVATE
    )

    private val _favorites = MutableStateFlow(load())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun reload() {
        _favorites.value = load()
    }

    fun toggle(id: String) {
        val next = _favorites.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        _favorites.value = next
        prefs().edit().putStringSet(KEY, next).apply()
    }

    fun isFavorite(id: String): Boolean = id in _favorites.value

    private fun load(): Set<String> =
        prefs().getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    private companion object {
        const val KEY = "watchlist"
    }
}
