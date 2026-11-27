package com.antoniowalls.ava.auth.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Importación añadida si se usa texto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun LoadingScreen() {
    // Contenedor principal que ocupa toda la pantalla.
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // Box para centrar el indicador de progreso.
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
             Text(stringResource(R.string.ui_cargando_noble_sendero), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = UiDimensions.size70))
        }
    }
}
