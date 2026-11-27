package com.antoniowalls.ava.auth.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.ui.theme.sequelRegular

@Composable
fun ForgotScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.clearFeedback()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = sequelRegular),
                fontSize = UiDimensions.text64,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(UiDimensions.size150))

            Text(
                text = stringResource(R.string.ui_recuperar_contrasena),
                fontSize = UiDimensions.text20,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(UiDimensions.medium))

            Text(
                text = stringResource(R.string.ui_escribe_el_correo_asociado_a_tu_cuenta),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(UiDimensions.wide))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.clearFeedback()
                },
                label = { Text(stringResource(R.string.ui_correo_electronico)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                enabled = !uiState.isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    disabledBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    disabledLabelColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(UiDimensions.wide))

            Button(
                onClick = {
                    authViewModel.sendPasswordResetEmail(email)
                },
                enabled = !uiState.isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.7f),
                    disabledContentColor = Color.Black.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(UiDimensions.wide),
                        color = Color.Black,
                        strokeWidth = UiDimensions.xs
                    )
                } else {
                    Text(stringResource(R.string.ui_enviar_enlace))
                }
            }

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(UiDimensions.large))
                Text(
                    text = message,
                    color = Color(0xFFFFB4AB),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            uiState.noticeMessage?.let { message ->
                Spacer(modifier = Modifier.height(UiDimensions.large))
                Text(
                    text = message,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(UiDimensions.large))

            TextButton(
                onClick = {
                    authViewModel.clearFeedback()
                    navController.popBackStack()
                },
                enabled = !uiState.isSubmitting
            ) {
                Text(
                    text = stringResource(R.string.ui_volver_al_inicio_de_sesion),
                    color = Color.White
                )
            }
        }
    }
}
