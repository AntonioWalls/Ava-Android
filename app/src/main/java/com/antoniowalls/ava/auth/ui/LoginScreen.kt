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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.ui.theme.sequelRegular

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = NavController(context = LocalContext.current), authViewModel = viewModel())
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_img),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UiDimensions.large),
            verticalArrangement = Arrangement.Center,
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

            Spacer(modifier = Modifier.height(UiDimensions.size200))  // Spacer que no se debe modificar
            Text(text = stringResource(R.string.ui_inicia_sesion), fontSize = UiDimensions.text20, color = Color.White)

            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = email,
                onValueChange = { newEmail -> email = newEmail },
                label = { Text(stringResource(R.string.ui_correo_electronico), color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(UiDimensions.base))
            OutlinedTextField(
                value = password,
                onValueChange = { newPassword -> password = newPassword },
                label = { Text(stringResource(R.string.ui_contrasena), color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(UiDimensions.medium))
            Button(
                enabled = !uiState.isSubmitting,
                onClick = {
                    authViewModel.login(email, password)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = stringResource(R.string.ui_iniciar_sesion))
            }

            Spacer(modifier = Modifier.height(UiDimensions.base))

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier.padding(UiDimensions.base),
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.isSubmitting) {
                Text(
                    text = stringResource(R.string.ui_iniciando_sesion),
                    color = Color.Gray,
                    modifier = Modifier.padding(UiDimensions.base)
                )
            }

            Spacer(modifier = Modifier.height(UiDimensions.base))
            Text(
                text = stringResource(R.string.ui_olvidaste_tu_contrasena),
                modifier = Modifier.clickable {
                    authViewModel.clearFeedback()
                    navController.navigate("forgot")
                },
                color = Color.White
            )

            Spacer(modifier = Modifier.height(UiDimensions.base))
            Text(text = stringResource(R.string.ui_o), color = Color.White)

            Spacer(modifier = Modifier.height(UiDimensions.base))
            Button(
                onClick = {
                    authViewModel.clearFeedback()
                    navController.navigate("signup")
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
    }
}
