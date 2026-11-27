package com.antoniowalls.ava.meditation.data

import androidx.compose.ui.graphics.Color
import com.antoniowalls.ava.R
import com.antoniowalls.ava.meditation.model.MeditationType
import com.antoniowalls.ava.ui.theme.Black
import com.antoniowalls.ava.ui.theme.Celeste
import com.antoniowalls.ava.ui.theme.Celeste2
import com.antoniowalls.ava.ui.theme.Green
import com.antoniowalls.ava.ui.theme.PurpleLighter
import com.antoniowalls.ava.ui.theme.VioletMedium
import com.antoniowalls.ava.ui.theme.Yellow

private val newCardColorPalette = listOf(
    Color(0xFFFBE9E7), // Light Peach
    Color(0xFFE0F2F1), // Light Teal
    Color(0xFFEDE7F6), // Light Purple
    Color(0xFFFFF9C4), // Light Yellow
    Color(0xFFC8E6C9), // Light Green
    Color(0xFFB3E5FC)  // Light Blue
)


fun meditationOptions(resources: android.content.res.Resources) = listOf(
    MeditationType(
        "1",
        resources.getString(R.string.meditation_three_minutes),
         3,
        resources.getString(R.string.meditation_teacher_padmabandhu),
        resources.getString(R.string.meditation_awareness_title),
        resources.getString(R.string.meditation_awareness_description),
        R.raw.consciencia,
        newCardColorPalette[0 % newCardColorPalette.size], // Anterior: PurpleLighter
        Black, //text color
        Yellow // time frame color
    ),
    MeditationType(
        "2",
        resources.getString(R.string.meditation_twenty_five_minutes),
        25,
        resources.getString(R.string.meditation_teacher_padmabandhu),
        resources.getString(R.string.meditation_metta_title),
        resources.getString(R.string.meditation_metta_description),
        R.raw.mettabhavana,
        newCardColorPalette[1 % newCardColorPalette.size], // Anterior: Celeste
        Black,
        Yellow
    ),
    MeditationType(
        "3",
        resources.getString(R.string.meditation_twenty_five_minutes),
        25,
        resources.getString(R.string.meditation_teacher_padmabandhu),
        resources.getString(R.string.meditation_breath_title),
        resources.getString(R.string.meditation_breath_description),
        R.raw.seguimientorespiracion,
        newCardColorPalette[2 % newCardColorPalette.size], // Anterior: VioletMedium
        Black,
        Yellow
    ),
    MeditationType(
        "4",
        resources.getString(R.string.meditation_five_minutes),
        5,
        resources.getString(R.string.meditation_teacher_padmabandhu),
        resources.getString(R.string.meditation_just_be_title),
        resources.getString(R.string.meditation_just_be_description),
        R.raw.soloestar,
        newCardColorPalette[3 % newCardColorPalette.size], // Anterior: Celeste2
        Black,
        Yellow
    )
)
