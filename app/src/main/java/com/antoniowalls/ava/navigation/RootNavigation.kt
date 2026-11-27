@file:OptIn(ExperimentalAnimationApi::class)

package com.antoniowalls.ava.navigation


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.antoniowalls.ava.auth.ui.ForgotScreen
import com.antoniowalls.ava.auth.ui.LoadingScreen
import com.antoniowalls.ava.auth.ui.LoginScreen
import com.antoniowalls.ava.auth.ui.PostLoginSplashScreen
import com.antoniowalls.ava.auth.ui.SignupPage
import com.antoniowalls.ava.auth.viewmodel.AuthState
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.navigation.ui.WelcomeModulesScreen
import com.antoniowalls.ava.profile.ui.UploadProfilePictureScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootNavigationGraph(authViewModel: AuthViewModel) {
    val navController = rememberAnimatedNavController() // Usar el NavController animado
    val authState by authViewModel.authState.collectAsState()

    val startDestination = remember(authState) {
        when (authState) {
            is AuthState.Authenticated -> "post_login_splash_router"
            is AuthState.Unauthenticated -> "auth_flow"
            is AuthState.Loading -> "loading_screen"

        }
    }

    AnimatedNavHost(navController = navController, startDestination = startDestination) {
        composable("loading_screen") {
            LoadingScreen()
        }
        composable("auth_flow") {
            AuthScreensNavHost(authViewModel = authViewModel)
        }

        composable("post_login_splash_router") {
            LaunchedEffect(key1 = authState) {
                val currentAuthState = authViewModel.authState.value
                if (currentAuthState is AuthState.Authenticated) {
                    val nextDestinationForSplash = if (currentAuthState.isNewUser) {
                        "upload_profile_picture"
                    } else {
                        "welcome_modules"
                    }
                    navController.navigate("post_login_splash/$nextDestinationForSplash") {
                        popUpTo("post_login_splash_router") { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate("auth_flow") {
                        popUpTo("post_login_splash_router") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(
            route = "post_login_splash/{next_destination}",
            arguments = listOf(navArgument("next_destination") { type = NavType.StringType })
        ) { backStackEntry ->
            val nextDest = backStackEntry.arguments?.getString("next_destination")
                ?: "welcome_modules"
            PostLoginSplashScreen(navController = navController, nextRoute = nextDest)
        }

        composable("upload_profile_picture") {
            UploadProfilePictureScreen(navController = navController)
        }

        // --- Pantalla de Selección de Módulos con Animación ---
        composable(
            "welcome_modules",
            exitTransition = {
                scaleOut(animationSpec = tween(400), targetScale = 0.9f) +
                        fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                scaleIn(animationSpec = tween(400, delayMillis = 100), initialScale = 0.9f) +
                        fadeIn(animationSpec = tween(400, delayMillis = 100))
            }
        ) {
            WelcomeModulesScreen(navController = navController, authViewModel = authViewModel)
        }

        // --- Pantalla Principal de la App con Animación ---
        composable(
            route = "main_app_container/{start_route}",
            arguments = listOf(navArgument("start_route") { defaultValue = "home" }),
            enterTransition = {
                scaleIn(animationSpec = tween(400, delayMillis = 100), initialScale = 1.1f) +
                        fadeIn(animationSpec = tween(400, delayMillis = 100))
            },
            popExitTransition = {
                scaleOut(animationSpec = tween(400), targetScale = 1.1f) +
                        fadeOut(animationSpec = tween(400))
            }
        ) { backStackEntry ->
            val initialRouteForMainApp = backStackEntry.arguments?.getString("start_route") ?: "home"
            MainAppScaffoldFeatures(
                appNavController = navController,
                initialScreenRoute = initialRouteForMainApp,
                authViewModel = authViewModel
            )
        }
    }


    LaunchedEffect(authState, navController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        when (val state = authState) {
            is AuthState.Authenticated -> {
                if (currentRoute == "auth_flow" || currentRoute == "loading_screen") {
                    navController.navigate("post_login_splash_router") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                if (currentRoute != "auth_flow" && currentRoute != "loading_screen") {
                    navController.navigate("auth_flow") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is AuthState.Loading -> {
                if (currentRoute != null && currentRoute != "loading_screen" && currentRoute != "auth_flow" && currentRoute != "post_login_splash_router") {
                    navController.navigate("loading_screen") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreensNavHost(authViewModel: AuthViewModel) {
    val authScreensNavController = rememberNavController()

    NavHost(
        navController = authScreensNavController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = authScreensNavController,
                authViewModel = authViewModel
            )
        }

        composable("signup") {
            SignupPage(
                navController = authScreensNavController,
                authViewModel = authViewModel
            )
        }

        composable("forgot") {
            ForgotScreen(
                navController = authScreensNavController,
                authViewModel = authViewModel
            )
        }
    }
}
