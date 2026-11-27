package com.antoniowalls.ava.profile.model

data class ChatUser(
    val userid: String = "",
    val status: String? = "Offline", // Estado inicial por defecto
    val imageUrl: String? = "",      // URL de imagen por defecto o vacía
    val userName: String? = null,
    val userMail: String? = "",
    val fullName: String? = "",      // Nombre completo
    val phone: String? = ""          // Nuevo campo para el teléfono
) {
    // Constructor sin argumentos necesario para la deserialización de Firestore
    constructor() : this("", "Offline", "", null, "", "", "")
}
