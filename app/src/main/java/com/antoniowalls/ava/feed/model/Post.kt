package com.antoniowalls.ava.feed.model // O el paquete que elijas

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String? = null,
    val userProfileImageUrl: String? = null,
    val text: String? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    // --- CAMPOS NUEVOS PARA LIKES ---
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(), // Lista de UIDs de usuarios a los que les gustó
    // --- FIN CAMPOS NUEVOS PARA LIKES ---
    val commentsCount: Int = 0 // Dejamos este para cuando implementemos comentarios
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", null, null, null, null, null, null, 0, emptyList(), 0)
}
