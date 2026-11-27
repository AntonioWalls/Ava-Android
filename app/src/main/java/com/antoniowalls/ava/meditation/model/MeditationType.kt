package com.antoniowalls.ava.meditation.model

import androidx.compose.ui.graphics.Color

data class MeditationType(
    val id: String,
    val duration: String,
    val time: Int,
    val teacher: String,
    val title: String,
    val description: String,
    val audioUrl: Int,
    val backgroundColor: Color,
    val contentColor: Color,
    val timeBackgroundColor: Color,
)
