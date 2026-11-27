package com.antoniowalls.ava.chat.model // Asegúrate que este sea el paquete

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Modelo para representar un chat reciente en una lista.
// Basado en tu recentChats.kt original de la desarrolladora.
data class RecentChatItem(
    val friendId: String = "",
    val friendsImage: String? = "",
    @ServerTimestamp
    val time: Date? = null, // Usar Date y ServerTimestamp para consistencia
    val name: String? = "Desconocido",
    val lastMessage: String? = "",
    val lastMessageSenderId: String? = "" // Quién envió el último mensaje
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this("", "", null, "Desconocido", "", "")
}
