package com.antoniowalls.ava.chat.model // Asegúrate que este sea el paquete

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatMessage(
    val sender: String? = "",
    val receiver: String? = "",
    val message: String? = "", // Contenido del mensaje
    @ServerTimestamp // Firestore asignará la marca de tiempo del servidor al crear
    val time: Date? = null, // Cambiado a Date para mejor manejo, Firestore lo soporta
    val id: String = "" // ID único del mensaje, puede ser generado por Firestore o cliente
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this("", "", "", null, "")
}
