package com.antoniowalls.ava.chat.utils // Paquete corregido

import android.content.Context
import android.content.SharedPreferences
// import androidx.compose.ui.input.key.Key // Este import no parece ser necesario aquí

class SharedPrefsHelper (context: Context) { // Nombre de clase cambiado
    private val prefs: SharedPreferences = context.getSharedPreferences("ava_chat_prefs", Context.MODE_PRIVATE) // Nombre de archivo de prefs cambiado para evitar colisiones

    fun setValue(key: String, value: String?) { // Permitir que value sea nullable
        prefs.edit().putString(key, value).apply()
    }

    fun getValue(key: String, defaultValue: String? = null): String? { // Añadido defaultValue
        return prefs.getString(key, defaultValue)
    }

    fun setIntValue(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getIntValue(key: String, defaultValue: Int): Int { // Nombre de parámetro 'i' cambiado a 'defaultValue'
        return prefs.getInt(key, defaultValue)
    }

    fun clearValue(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
