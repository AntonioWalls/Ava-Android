package com.antoniowalls.ava.lessons.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Cambiado a itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.antoniowalls.ava.shared.ui.UserProfileHeader
import com.antoniowalls.ava.feed.viewmodel.FeedViewModel
import com.antoniowalls.ava.lessons.data.ConstantsLessons
import com.antoniowalls.ava.lessons.model.LessonType

// Lista de colores pastel para el tema claro (puedes moverla a un archivo de constantes de UI si lo prefieres)
private val lightPastelLessonCardColors = listOf(
    Color(0xFFFBE9E7), // Light Peach
    Color(0xFFE0F2F1), // Light Teal
    Color(0xFFEDE7F6), // Light Purple
    Color(0xFFFFF9C4), // Light Yellow
    Color(0xFFC8E6C9), // Light Green
    Color(0xFFB3E5FC)  // Light Blue
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(navController: NavController, feedViewModel: FeedViewModel = viewModel()) {
    val lessonsToDisplay = ConstantsLessons.lessonList(LocalContext.current.resources)
    val currentUserProfile by feedViewModel.currentUserProfile.collectAsState()

    // Ya no forzamos LightColorScheme, AvaTheme se encargará del modo oscuro/claro.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_ensenanzas)) }, // El color se adaptará con el tema
                actions = {
                    UserProfileHeader(
                        userProfile = currentUserProfile,
                        onClick = { navController.navigate("edit_profile_screen") }
                    )
                },
                // Los colores de TopAppBar se tomarán del tema global
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // o primaryContainer si prefieres
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface, // Fondo general de la pantalla
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(all = UiDimensions.large),
            verticalArrangement = Arrangement.spacedBy(UiDimensions.large)
        ) {
            itemsIndexed(lessonsToDisplay) { index, lesson ->
                LessonCard(
                    lesson = lesson,
                    navController = navController,
                    cardIndex = index // Pasar el índice para la selección de color
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCard(lesson: LessonType, navController: NavController, cardIndex: Int) {
    val isDark = isSystemInDarkTheme()

    val cardBackgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant // Buen contraste para tarjetas en modo oscuro
        // Si surfaceContainerHigh no está, usa surfaceVariant
    } else {
        // Usa el backgroundColor definido en LessonType si es un color pastel y no un rol de M3
        // O usa la lista de pasteles cíclicamente como en WelcomeModulesScreen
        lightPastelLessonCardColors[cardIndex % lightPastelLessonCardColors.size]
    }

    val cardBorderColor = if (isDark) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(UiDimensions.large)) // Aplicar clip antes de clickable puede ser mejor para el ripple
            .clickable {
                navController.navigate("lesson_carousel_screen/${lesson.id}")
            },
        shape = RoundedCornerShape(UiDimensions.large),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) UiDimensions.xxs else UiDimensions.xs), // Sutil elevación o ninguna
        border = BorderStroke(UiDimensions.xxs, cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = UiDimensions.size120) // Altura mínima
                .padding(UiDimensions.large),
            verticalArrangement = Arrangement.SpaceBetween // Distribuye espacio para que el contenido no se aplaste
        ) {
            Column { // Columna para título y descripción
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleLarge, // Usar titleLarge para más impacto
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Se adaptará al fondo de la tarjeta
                )
                Spacer(modifier = Modifier.height(UiDimensions.base))
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Color secundario para descripción
                    maxLines = 3,
                )
            }
            // Opcional: Podrías añadir un "Leer más" o un icono aquí
            // Spacer(modifier = Modifier.height(UiDimensions.base)) // Añadir espacio si hay más elementos abajo
        }
    }
}
