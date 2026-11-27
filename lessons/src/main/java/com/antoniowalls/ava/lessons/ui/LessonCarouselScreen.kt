package com.antoniowalls.ava.lessons.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.lessons.R
import com.antoniowalls.ava.lessons.model.LessonType
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra el contenido de una enseñanza en un carrusel de texto.
 * Adaptada para tema oscuro y claro, usando fallbacks para roles de color no disponibles en M3 v1.1.0.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LessonCarouselScreen(
    lesson: LessonType?,
    onBack: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    // Alternativa para surfaceContainer en M3 v1.1.0
    val topAppBarContainerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(UiDimensions.tiny) // Simula un contenedor elevado
    } else {
        lesson?.backgroundColor?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.surface
    }

    val scaffoldBackgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        lesson?.backgroundColor?.let {
            if (it.luminance() > 0.8f) it else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } ?: MaterialTheme.colorScheme.surface
    }

    // Alternativa para surfaceContainerLowest en M3 v1.1.0
    val textCardBackgroundColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(UiDimensions.xxs) // Un poco elevado, pero menos que la TopAppBar
        // o simplemente MaterialTheme.colorScheme.surface si quieres que sea igual al fondo del scaffold
    } else {
        MaterialTheme.colorScheme.surface // Fondo de tarjeta estándar en modo claro
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: stringResource(R.string.ui_ensenanza)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.ui_volver)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = bottomBar,
        containerColor = scaffoldBackgroundColor
    ) { paddingValues ->
        if (lesson == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.ui_leccion_no_encontrada), fontSize = UiDimensions.text18, color = MaterialTheme.colorScheme.onSurface)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UiDimensions.base, vertical = UiDimensions.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pagerState = rememberPagerState(pageCount = { lesson.texts.size })
            val scope = rememberCoroutineScope()
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = UiDimensions.base, vertical = UiDimensions.small),
                    shape = RoundedCornerShape(UiDimensions.medium),
                    elevation = CardDefaults.cardElevation(defaultElevation = UiDimensions.xxs),
                    colors = CardDefaults.cardColors(
                        containerColor = textCardBackgroundColor // Usar la alternativa definida
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(UiDimensions.wide)
                    ) {
                        Text(
                            text = lesson.texts[page],
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = UiDimensions.text28,
                            color = MaterialTheme.colorScheme.onSurface // Texto principal sobre el color de la tarjeta
                        )
                    }
                }
            }

            if (lesson.texts.size > 1) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = UiDimensions.base),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = pagerState.currentPage > 0 && !pagerState.isScrollInProgress,
                        onClick = { scope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) } }
                    ) { Text(stringResource(R.string.lesson_previous)) }
                    Text(
                        stringResource(R.string.lesson_page, pagerState.currentPage + 1, pagerState.pageCount),
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextButton(
                        enabled = pagerState.currentPage < pagerState.pageCount - 1 && !pagerState.isScrollInProgress,
                        onClick = { scope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) } }
                    ) { Text(stringResource(R.string.lesson_next)) }
                }
            }
        }
    }
}

// Función de extensión para calcular la luminancia de un Color
// (Se mantiene igual, puede ser útil para futuras decisiones de color)
fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    // Formula estándar para luminancia percibida
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}
