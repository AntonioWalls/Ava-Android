package com.antoniowalls.ava.profile.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R
import com.antoniowalls.ava.profile.viewmodel.EditProfileViewModel
import com.antoniowalls.ava.shared.ui.ImagePickerDialog
import com.antoniowalls.ava.shared.viewmodel.AppViewModelFactory
import com.antoniowalls.ava.profile.viewmodel.EditProfileUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadProfilePictureScreen(
    navController: NavController,
    editProfileViewModel: EditProfileViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // No necesitamos coroutineScope aquí si la navegación es directa o manejada por LaunchedEffect

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val uiState by editProfileViewModel.uiState.collectAsState()
    var isLoadingIndicator by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri; imageBitmap = null
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        imageBitmap = bitmap; imageUri = null
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EditProfileUiState.UpdatingProfile -> isLoadingIndicator = true
            is EditProfileUiState.UpdateSuccess -> {
                isLoadingIndicator = false
                // Navegar después de que la foto se haya guardado exitosamente
                navController.navigate("welcome_modules") {
                    popUpTo("upload_profile_picture") { inclusive = true }
                    launchSingleTop = true
                }
            }
            is EditProfileUiState.Error -> {
                isLoadingIndicator = false
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
            }
            is EditProfileUiState.LoadingUserProfile -> isLoadingIndicator = true
            is EditProfileUiState.ProfileLoaded -> isLoadingIndicator = false
            is EditProfileUiState.Idle -> isLoadingIndicator = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_elige_tu_foto_de_perfil)) }, // Título más descriptivo
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiDimensions.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround // Distribuir espacio
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { // Agrupar texto y foto
                Text(
                    stringResource(R.string.ui_bienvenido_a_personaliza_tu_perfil),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(UiDimensions.base))
                Text(
                    stringResource(R.string.ui_sube_una_foto_para_que_otros_te),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = UiDimensions.large)
                )
                Spacer(modifier = Modifier.height(UiDimensions.size32))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(UiDimensions.size160) // Un poco más grande
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(UiDimensions.xxs, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { showImagePickerDialog = true }
                ) {
                    val displayImage: Any? = imageBitmap ?: imageUri ?: (uiState as? EditProfileUiState.ProfileLoaded)?.user?.imageUrl
                    if (displayImage != null && displayImage.toString().isNotBlank()) {
                        if (displayImage is Bitmap) {
                            Image(bitmap = displayImage.asImageBitmap(), contentDescription = stringResource(R.string.ui_foto_de_perfil), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            AsyncImage(model = displayImage, contentDescription = stringResource(R.string.ui_foto_de_perfil), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), placeholder = painterResource(id = R.drawable.ic_launcher_foreground), error = painterResource(id = R.drawable.ic_launcher_foreground))
                        }
                    } else {
                        Icon(Icons.Filled.CameraAlt, contentDescription = stringResource(R.string.ui_anadir_foto), modifier = Modifier.size(UiDimensions.size70), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(UiDimensions.medium))
                TextButton(onClick = { showImagePickerDialog = true }) {
                    Text(if (imageUri != null || imageBitmap != null) stringResource(R.string.ui_cambiar_foto_2) else stringResource(R.string.ui_seleccionar_foto))
                }
            }

            // Botones de acción separados
            Column(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        editProfileViewModel.updateProfile(
                            newImageUri = imageUri,
                            newImageBitmap = imageBitmap
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingIndicator && editProfileViewModel.canEdit &&
                        (imageUri != null || imageBitmap != null)
                ) {
                    if (isLoadingIndicator) {
                        CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.ui_guardar_foto_y_continuar))
                    }
                }
                Spacer(modifier = Modifier.height(UiDimensions.medium))
                if (uiState is EditProfileUiState.Error && !editProfileViewModel.canEdit) {
                    TextButton(onClick = editProfileViewModel::loadUserProfile) {
                        Text(stringResource(R.string.profile_retry))
                    }
                }
                OutlinedButton(
                    onClick = {
                        // Navegación directa al omitir, sin delay, opcionalmente un snackbar
                        // Snackbar se puede quitar si se prefiere una transición más rápida
                        // coroutineScope.launch {
                        //    snackbarHostState.showSnackbar("Foto omitida.", duration = SnackbarDuration.Short)
                        // }
                        navController.navigate("welcome_modules") {
                            popUpTo("upload_profile_picture") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingIndicator
                ) {
                    Text(stringResource(R.string.ui_omitir_por_ahora))
                }
            }
        }
    }

    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismissRequest = { showImagePickerDialog = false },
            onCameraClick = { showImagePickerDialog = false; cameraLauncher.launch() },
            onGalleryClick = { showImagePickerDialog = false; galleryLauncher.launch("image/*") }
        )
    }
}
