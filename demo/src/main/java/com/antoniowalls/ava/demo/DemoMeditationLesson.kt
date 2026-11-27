package com.antoniowalls.ava.demo

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.antoniowalls.ava.lessons.model.LessonType

internal fun demoMeditationLesson(resources: Resources) = LessonType(
    id = 6,
    title = resources.getString(R.string.demo_lesson_title),
    description = resources.getString(R.string.demo_lesson_description),
    backgroundColor = Color(0xFFE8F1EB),
    texts = listOf(
        resources.getString(R.string.demo_lesson_page_1),
        resources.getString(R.string.demo_lesson_page_2)
    ),
    category = resources.getString(R.string.demo_lesson_category)
)
