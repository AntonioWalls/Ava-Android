package com.antoniowalls.ava.feed.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val postId: String = "", // ID del post al que pertenece
    val userId: String = "",
    val userName: String? = null,
    val userProfileImageUrl: String? = null,
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", "", null, null, "", null)
}
