@file:OptIn(ExperimentalMaterial3Api::class)

package com.antoniowalls.ava.auth.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.ui.theme.sequelRegular

@Preview
@Composable
fun SignupPagePreview() {
    // Pasamos valores predeterminados para los parámetros
    SignupPage(navController = NavController(context = LocalContext.current), authViewModel = androidx.lifecycle.viewmodel.compose.viewModel())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var names by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.background_blur),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UiDimensions.large),
            verticalArrangement = Arrangement.Top, // Mueve todos los elementos hacia la parte superior
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(UiDimensions.size30))
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = sequelRegular
                ),
                fontSize = UiDimensions.text64,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(UiDimensions.size130))  // Ajuste reducido entre logo y formulario
            Text(
                text = stringResource(R.string.ui_registrate),
                fontSize = UiDimensions.text20,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.8f)  // Ocupa el mismo ancho que el campo de entrada
                    .align(Alignment.Start) // Alinea el texto a la izquierda
                    .padding(start = UiDimensions.size34) // Ajusta este valor para que coincida con el borde del input de correo
            )

            // Campo de correo electrónico
            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = email,
                onValueChange = { newEmail -> email = newEmail },
                label = { Text(stringResource(R.string.ui_correo_electronico), color = Color.White) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Campo de teléfono
            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = phone,
                onValueChange = { newPhone -> phone = newPhone },
                label = { Text(stringResource(R.string.ui_telefono), color = Color.White) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Campo de contraseña
            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = password,
                onValueChange = { newPassword -> password = newPassword },
                label = { Text(stringResource(R.string.ui_contrasena), color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Campo de nombre completo
            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = names,
                onValueChange = { newNames -> names = newNames },
                label = { Text(stringResource(R.string.ui_nombre_completo), color = Color.White) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            // Campo de nombre de usuario
            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = user,
                onValueChange = { newUser -> user = newUser },
                label = { Text(stringResource(R.string.ui_nombre_de_usuario_2), color = Color.White) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(UiDimensions.wide))
            Button(
                enabled = !uiState.isSubmitting,
                onClick = {
                    // Nueva llamada con todos los campos
                    authViewModel.signup(
                        email = email,
                        password = password,
                        fullName = names,
                        username = user,
                        phone = phone
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = stringResource(R.string.ui_crear_cuenta))
            }
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(UiDimensions.extraLarge)
            )
        }

        if (uiState.isSubmitting) {
            Text(
                text = stringResource(R.string.ui_registrando),
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(UiDimensions.extraLarge)
            )
        }
    }
}
