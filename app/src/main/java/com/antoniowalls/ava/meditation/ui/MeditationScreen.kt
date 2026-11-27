package com.antoniowalls.ava.meditation.ui

import androidx.compose.ui.platform.LocalContext

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.antoniowalls.ava.shared.ui.UserProfileHeader
import com.antoniowalls.ava.feed.viewmodel.FeedViewModel
import com.antoniowalls.ava.ui.theme.nunitoBold
import com.antoniowalls.ava.ui.theme.nunitoLight
import com.antoniowalls.ava.ui.theme.nunitoMedium
import com.antoniowalls.ava.meditation.data.meditationOptions
import com.antoniowalls.ava.meditation.model.MeditationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    feedViewModel: FeedViewModel = viewModel()
) {
    val currentUserProfile by feedViewModel.currentUserProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_meditacion)) },
                actions = {
                    UserProfileHeader(
                        userProfile = currentUserProfile,
                        onClick = { navController.navigate("edit_profile_screen") }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            MeditationTypesComponent(navController = navController)
        }
    }
}


@Composable
fun MeditationTypesComponent(navController: NavController) {
    val meditationOptions = meditationOptions(LocalContext.current.resources)
    LazyColumn(
        modifier = Modifier.padding(UiDimensions.beforeLarge),
        verticalArrangement = Arrangement.spacedBy(UiDimensions.beforeLarge)
    ) {
        // Se utiliza itemsIndexed para aplicar una animación escalonada
        itemsIndexed(meditationOptions) { index, meditationType ->
            // Cada tarjeta ahora está envuelta en AnimatedVisibility para la animación de entrada
            AnimatedVisibility(
                visible = true, // La animación se ejecuta cuando el elemento entra en la composición
                enter = slideInVertically(
                    initialOffsetY = { it / 2 }, // Comienza desde la mitad de su altura
                    animationSpec = tween(durationMillis = 500, delayMillis = index * 80) // Animación suave con un pequeño retraso por cada elemento
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 500, delayMillis = index * 80)
                )
            ) {
                MeditationOptionComponent(meditationTypes = meditationType, navController = navController)
            }
        }
    }
}

@Composable
fun MeditationOptionComponent(meditationTypes: MeditationType, navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()

    val cardBackgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        meditationTypes.backgroundColor
    }

    val contentColor = if(isDarkTheme) {
        MaterialTheme.colorScheme.onSurface
    } else {
        meditationTypes.contentColor
    }

    Card(
        shape = RoundedCornerShape(UiDimensions.mediumPlus),
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                navController.navigate("meditation_player/${meditationTypes.id}")
            },
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)

    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(UiDimensions.wide)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(UiDimensions.basePlus)) {
                val buttonBackgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                val buttonContentColor = MaterialTheme.colorScheme.onSurface

                Button(
                    onClick = { /* Acción */ },
                    modifier = Modifier.padding(UiDimensions.small),
                    shape = RoundedCornerShape(UiDimensions.base),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBackgroundColor,
                        contentColor = buttonContentColor
                    )
                ) {
                    Text(
                        text = meditationTypes.duration,
                        fontFamily = nunitoMedium
                    )
                }

                Button(
                    onClick = { /* Acción */ },
                    modifier = Modifier.padding(UiDimensions.small),
                    shape = RoundedCornerShape(UiDimensions.base),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBackgroundColor,
                        contentColor = buttonContentColor
                    )
                ) {
                    Text(
                        text = meditationTypes.teacher,
                        fontFamily = nunitoMedium
                    )
                }
            }

            Text(
                text = meditationTypes.title,
                fontFamily = nunitoBold,
                fontSize = UiDimensions.text18,
                color = contentColor,
                textAlign = TextAlign.Start
            )

            Text(
                text = meditationTypes.description,
                fontFamily = nunitoLight,
                fontSize = UiDimensions.text16,
                color = contentColor,
                textAlign = TextAlign.Start
            )
        }
    }
}
