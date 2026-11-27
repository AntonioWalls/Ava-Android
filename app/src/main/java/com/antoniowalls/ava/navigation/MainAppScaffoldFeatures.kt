package com.antoniowalls.ava.navigation

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.chat.ui.ChatScreen
import com.antoniowalls.ava.profile.ui.EditProfileScreen
import com.antoniowalls.ava.chat.ui.UserListScreen
import com.antoniowalls.ava.feed.ui.CreatePostScreen
import com.antoniowalls.ava.feed.ui.HomeScreen
import com.antoniowalls.ava.lessons.ui.LessonCarouselScreen
import com.antoniowalls.ava.lessons.data.ConstantsLessons
import com.antoniowalls.ava.lessons.ui.LessonsScreen
import com.antoniowalls.ava.meditation.ui.MeditationPlayerScreen
import com.antoniowalls.ava.meditation.ui.MeditationScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController


data class MainNavItem(val label: String, val icon: Any, val route: String)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainAppScaffoldFeatures(
    appNavController: NavHostController,
    initialScreenRoute: String,
    authViewModel: AuthViewModel
) {
    val featuresNavController = rememberAnimatedNavController() // Usar el NavController animado

    val navItems = listOf(
        MainNavItem(stringResource(R.string.ui_foro), painterResource(id = R.drawable.foro), "home"),
        MainNavItem(stringResource(R.string.ui_ensenanzas), painterResource(id = R.drawable.libro), "lessons"),
        MainNavItem(stringResource(R.string.ui_meditacion), painterResource(id = R.drawable.flordeloto), "meditation"),
        MainNavItem(stringResource(R.string.ui_chat), painterResource(id = R.drawable.chat), "user_list_screen")
    )

    // Estado para recordar el índice de la pestaña anterior y así determinar la dirección de la animación
    var previousTabIndex by remember { mutableStateOf(navItems.indexOfFirst { it.route == initialScreenRoute }) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by featuresNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val routesWithoutBottomBar = setOf(
                "meditation_player/{meditationId}",
                "chat_screen/{friendId}",
                "edit_profile_screen",
                "create_post_screen",
                "lesson_carousel_screen/{lessonId}"
            )

            val shouldShowBottomBar = routesWithoutBottomBar.none { routePattern ->
                val basePattern = routePattern.substringBefore("/{")
                currentRoute?.startsWith(basePattern) == true
            }


            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = currentRoute == item.route

                        val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    // Guardar el índice actual antes de navegar
                                    previousTabIndex = navItems.indexOfFirst { it.route == currentRoute }.takeIf { it != -1 } ?: previousTabIndex
                                    featuresNavController.navigate(item.route) {
                                        popUpTo(featuresNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { IconRenderer(icon = item.icon, tint = iconTint) },
                            label = { Text(text = item.label, color = iconTint) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Usar AnimatedNavHost en lugar de NavHost
        AnimatedNavHost(
            navController = featuresNavController,
            startDestination = initialScreenRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Definir las pantallas principales con sus animaciones
            navItems.forEachIndexed { index, item ->
                composable(
                    route = item.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { if (index > previousTabIndex) it else -it },
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { if (index > previousTabIndex) -it else it },
                            animationSpec = tween(300)
                        )
                    }
                ) {
                    when (item.route) {
                        "home" -> HomeScreen(navController = featuresNavController)
                        "lessons" -> LessonsScreen(navController = featuresNavController)
                        "meditation" -> MeditationScreen(navController = featuresNavController)
                        "user_list_screen" -> UserListScreen(navController = featuresNavController)
                    }
                }
            }

            // Definir las pantallas secundarias sin animación de carrusel (usarán la animación por defecto)
            composable(
                route = "lesson_carousel_screen/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getInt("lessonId")
                LessonCarouselScreen(
                    lesson = ConstantsLessons.lessonList(LocalContext.current.resources).find { it.id == lessonId },
                    onBack = { featuresNavController.popBackStack() }
                )
            }
            composable("meditation_player/{meditationId}") { backStackEntry ->
                MeditationPlayerScreen(navController = featuresNavController, meditationId = backStackEntry.arguments?.getString("meditationId"))
            }
            composable(
                route = "chat_screen/{friendId}",
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) { backStackEntry ->
                ChatScreen(navController = featuresNavController, friendId = backStackEntry.arguments?.getString("friendId"))
            }
            composable("edit_profile_screen") {
                EditProfileScreen(navController = featuresNavController, authViewModel = authViewModel)
            }
            composable("create_post_screen") {
                CreatePostScreen(navController = featuresNavController)
            }
        }
    }
}

@Composable
fun IconRenderer(icon: Any, tint: Color) {
    when (icon) {
        is ImageVector -> Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(UiDimensions.extraLarge)
        )
        is Painter -> Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(UiDimensions.extraLarge),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}
