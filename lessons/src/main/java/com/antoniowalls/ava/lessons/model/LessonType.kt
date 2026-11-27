package com.antoniowalls.ava.lessons.model

import androidx.compose.ui.graphics.Color

/**
 * Data class para representar una enseñanza. No contiene campo de imagen.
 *
 * @param id Identificador único de la enseñanza.
 * @param title Título de la enseñanza.
 * @param description Descripción breve de la enseñanza.
 * @param backgroundColor Color de fondo asociado con la enseñanza, usado para el estilo de la UI.
 * @param texts Lista de cadenas de texto que componen el contenido de la enseñanza.
 * @param category Categoría de la enseñanza (ej. "Fundamentos", "Bienestar Emocional").
 */
data class LessonType(
    val id: Int,
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val texts: List<String>,
    val category: String
)
