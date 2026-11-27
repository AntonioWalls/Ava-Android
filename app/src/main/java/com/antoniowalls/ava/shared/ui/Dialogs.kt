package com.antoniowalls.ava.shared.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.R
import android.Manifest // Import Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat // Import ContextCompat


@Composable
fun ImagePickerDialog(
    onDismissRequest: () -> Unit,
    onCameraClick: () -> Unit, // Esta lambda se ejecutará DESPUÉS de que el permiso sea concedido
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current // Obtener el contexto actual

    // Launcher para solicitar el permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                onCameraClick()
                onDismissRequest()
            } else {
                Toast.makeText(context, R.string.ui_permiso_de_camara_denegado_no_se_puede, Toast.LENGTH_LONG).show()
                onDismissRequest()
            }
        }
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(UiDimensions.large),
        ) {
            Column(
                modifier = Modifier.padding(UiDimensions.extraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.ui_seleccionar_imagen), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(UiDimensions.extraLarge))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        onCameraClick()
                                        onDismissRequest() // Cerrar diálogo
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                            .padding(UiDimensions.base)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.ui_camara), modifier = Modifier.size(UiDimensions.touchTarget))
                        Text(stringResource(R.string.ui_camara))
                    }

                    // Opción para Galería
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                onGalleryClick()
                                onDismissRequest() // Cerrar el diálogo
                            }
                            .padding(UiDimensions.base)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = stringResource(R.string.ui_galeria), modifier = Modifier.size(UiDimensions.touchTarget))
                        Text(stringResource(R.string.ui_galeria))
                    }
                }
                Spacer(modifier = Modifier.height(UiDimensions.large))
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.ui_cancelar))
                }
            }
        }
    }
}
