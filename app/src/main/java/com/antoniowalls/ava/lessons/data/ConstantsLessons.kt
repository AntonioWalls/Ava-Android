package com.antoniowalls.ava.lessons.data

import com.antoniowalls.ava.R
import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.antoniowalls.ava.lessons.model.LessonType

/**
 * Objeto que contiene una lista predefinida de enseñanzas para la aplicación,
 * con contenido real extraído del documento proporcionado.
 */
object ConstantsLessons {
    fun lessonList(resources: Resources): List<LessonType> = listOf(
        LessonType(
            id = 1,
            title = resources.getString(R.string.lesson_1_title),
            description = resources.getString(R.string.lesson_1_description),
            backgroundColor = Color(0xFFFBE9E7), // Light Peach
            texts = listOf(
                resources.getString(R.string.lesson_1_page_1),
                resources.getString(R.string.lesson_1_page_2),
                resources.getString(R.string.lesson_1_page_3),
                resources.getString(R.string.lesson_1_page_4)
            ),
            category = resources.getString(R.string.lesson_1_category)
        ),
        LessonType(
            id = 2,
            title = resources.getString(R.string.lesson_2_title),
            description = resources.getString(R.string.lesson_2_description),
            backgroundColor = Color(0xFFE0F2F1), // Light Teal
            texts = listOf(
                resources.getString(R.string.lesson_2_page_1),
                resources.getString(R.string.lesson_2_page_2),
                resources.getString(R.string.lesson_2_page_3),
                resources.getString(R.string.lesson_2_page_4),
                resources.getString(R.string.lesson_2_page_5),
                resources.getString(R.string.lesson_2_page_6),
                resources.getString(R.string.lesson_2_page_7),
                resources.getString(R.string.lesson_2_page_8)
            ),
            category = resources.getString(R.string.lesson_2_category)
        ),
        LessonType(
            id = 3,
            title = resources.getString(R.string.lesson_3_title),
            description = resources.getString(R.string.lesson_3_description),
            backgroundColor = Color(0xFFEDE7F6), // Light Purple
            texts = listOf(
                resources.getString(R.string.lesson_3_page_1),
                resources.getString(R.string.lesson_3_page_2),
                resources.getString(R.string.lesson_3_page_3)
            ),
            category = resources.getString(R.string.lesson_3_category)
        ),
        LessonType(
            id = 4,
            title = resources.getString(R.string.lesson_4_title),
            description = resources.getString(R.string.lesson_4_description),
            backgroundColor = Color(0xFFFFF9C4), // Light Yellow
            texts = listOf(
                resources.getString(R.string.lesson_4_page_1),
                resources.getString(R.string.lesson_4_page_2),
                resources.getString(R.string.lesson_4_page_3),
                resources.getString(R.string.lesson_4_page_4),
                resources.getString(R.string.lesson_4_page_5),
                resources.getString(R.string.lesson_4_page_6)
            ),
            category = resources.getString(R.string.lesson_4_category)
        ),
        LessonType(
            id = 5,
            title = resources.getString(R.string.lesson_5_title),
            description = resources.getString(R.string.lesson_5_description),
            backgroundColor = Color(0xFFC8E6C9), // Light Green
            texts = listOf(
                resources.getString(R.string.lesson_5_page_1),
                resources.getString(R.string.lesson_5_page_2),
                resources.getString(R.string.lesson_5_page_3),
                resources.getString(R.string.lesson_5_page_4)
            ),
            category = resources.getString(R.string.lesson_5_category)
        ),
        LessonType(
            id = 6,
            title = resources.getString(R.string.lesson_6_title),
            description = resources.getString(R.string.lesson_6_description),
            backgroundColor = Color(0xFFB3E5FC), // Light Blue
            texts = listOf(
                resources.getString(R.string.lesson_6_page_1),
                resources.getString(R.string.lesson_6_page_2),
                resources.getString(R.string.lesson_6_page_3),
                resources.getString(R.string.lesson_6_page_4),
                resources.getString(R.string.lesson_6_page_5)
            ),
            category = resources.getString(R.string.ui_meditacion)
        )
    )
}
