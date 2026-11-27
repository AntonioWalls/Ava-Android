package com.antoniowalls.ava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.navigation.RootNavigationGraph
import com.antoniowalls.ava.ui.theme.AvaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilita el modo edge-to-edge para la UI.

        // Instancia el AuthViewModel que será utilizado por el RootNavigationGraph
        // y potencialmente por otras partes de la UI que necesiten observar el estado de auth.
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            AvaTheme {
                // RootNavigationGraph es el punto de entrada para la navegación de la app.
                // Se le pasa el authViewModel para que pueda reaccionar a los cambios
                // en el estado de autenticación.
                RootNavigationGraph(authViewModel = authViewModel)
            }
        }
    }
}
