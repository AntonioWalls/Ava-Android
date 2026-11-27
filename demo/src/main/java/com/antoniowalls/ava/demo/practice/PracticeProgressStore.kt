package com.antoniowalls.ava.demo.practice

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal const val PRACTICE_PROGRESS_FILE = "breathing_practice"
private const val POSITION_MS = "position_ms"
private const val COMPLETED = "completed"

internal data class PracticeProgress(val positionMs: Long, val completed: Boolean)

internal class PracticeProgressStore(context: Context) {
    private val preferences = context.getSharedPreferences(PRACTICE_PROGRESS_FILE, Context.MODE_PRIVATE)

    val updates = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(read()) }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun read() = PracticeProgress(
        positionMs = preferences.getLong(POSITION_MS, 0).coerceAtLeast(0),
        completed = preferences.getBoolean(COMPLETED, false)
    )

    fun savePosition(positionMs: Long) {
        if (preferences.getLong(POSITION_MS, 0) != positionMs) {
            preferences.edit().putLong(POSITION_MS, positionMs).apply()
        }
    }

    fun markCompleted() {
        val progress = read()
        if (!progress.completed || progress.positionMs != 0L) {
            preferences.edit()
                .putLong(POSITION_MS, 0)
                .putBoolean(COMPLETED, true)
                .apply()
        }
    }
}
