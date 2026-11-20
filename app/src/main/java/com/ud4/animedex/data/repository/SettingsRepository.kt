package com.ud4.animedex.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class SortOrder { RANK, SCORE, POPULARITY }

data class UserSettings(
    val sort: SortOrder,
    val minScore: Float,
    val darkTheme: Boolean,
    val favorites: Set<Int>
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val SORT = stringPreferencesKey("sort")
        val MIN_SCORE = floatPreferencesKey("min_score")
        val DARK = booleanPreferencesKey("dark_theme")
        val FAVORITES = stringSetPreferencesKey("favorites")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            sort = try {
                SortOrder.valueOf(p[Keys.SORT] ?: SortOrder.RANK.name)
            } catch (e: IllegalArgumentException) {
                SortOrder.RANK
            },
            minScore = p[Keys.MIN_SCORE] ?: 0f,
            darkTheme = p[Keys.DARK] ?: false,
            favorites = p[Keys.FAVORITES].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
        )
    }

    suspend fun setSort(sort: SortOrder) {
        context.dataStore.edit { it[Keys.SORT] = sort.name }
    }

    suspend fun setMinScore(minScore: Float) {
        context.dataStore.edit { it[Keys.MIN_SCORE] = minScore }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK] = dark }
    }

    suspend fun toggleFavorite(id: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.FAVORITES].orEmpty().toMutableSet()
            val idStr = id.toString()
            if (current.contains(idStr)) {
                current.remove(idStr)
            } else {
                current.add(idStr)
            }
            preferences[Keys.FAVORITES] = current
        }
    }
}
