package com.linuxh2o.whiterose.presentation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "whiterose_pref")

data class ChimeState(
    val intervalMinutes: Int,
    val isActive: Boolean,
    val nextChimeAt: Long
)

class ChimePref(private val context: Context) {
    val intervalOptions = listOf(1, 5, 10, 15, 30, 60)

    val chimeState: Flow<ChimeState> = context.dataStore.data.map { prefs ->
        ChimeState(
            intervalMinutes = prefs[KEY_INTERVAL] ?: DEFAULT_INTERVAL,
            isActive = prefs[KEY_IS_ACTIVE] ?: false,
            nextChimeAt = prefs[KEY_NEXT_CHIME_AT] ?: 0L
        )
    }

    suspend fun save(state: ChimeState) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INTERVAL] = state.intervalMinutes
            prefs[KEY_NEXT_CHIME_AT] = state.nextChimeAt
            prefs[KEY_IS_ACTIVE] = state.isActive
        }
    }

    suspend fun read(): ChimeState {
        return chimeState.first()
    }

    companion object {
        val KEY_INTERVAL = intPreferencesKey("interval_min")
        val KEY_NEXT_CHIME_AT = longPreferencesKey("next_chime_at")

        val KEY_IS_ACTIVE = booleanPreferencesKey("is_active")
        const val DEFAULT_INTERVAL = 1
    }
}