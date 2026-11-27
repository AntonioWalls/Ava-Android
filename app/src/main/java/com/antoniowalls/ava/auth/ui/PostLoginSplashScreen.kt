package com.antoniowalls.ava.auth.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.antoniowalls.ava.R
import kotlinx.coroutines.delay


@Composable
fun PostLoginSplashScreen(navController: NavController, nextRoute: String) {
    LaunchedEffect(Unit) {
        delay(2000) // Show this loading screen for 2 seconds.

        // Get the current route of this splash screen instance to pop up to it.
        val currentSplashRouteWithArg = navController.currentBackStackEntry?.destination?.route

        navController.navigate(nextRoute) {
            // Remove the current splash screen instance from the back stack.
            // This ensures the user cannot navigate back to this specific splash instance.
            if (currentSplashRouteWithArg != null) {
                popUpTo(currentSplashRouteWithArg) { inclusive = true }
            }

            launchSingleTop = true // Avoid multiple instances of the nextRoute
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.noblelogo),
                    contentDescription = stringResource(R.string.ui_ava_logo),
                    modifier = Modifier.size(UiDimensions.size200)
                )
                Spacer(modifier = Modifier.height(UiDimensions.large))
                Text(stringResource(R.string.ui_cargando_tu_espacio), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
