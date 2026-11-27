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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.ContentAlpha
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R // Para placeholder
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.profile.viewmodel.EditProfileUiState
import com.antoniowalls.ava.profile.viewmodel.EditProfileViewModel
import com.antoniowalls.ava.shared.ui.ImagePickerDialog
import com.antoniowalls.ava.shared.viewmodel.AppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    editProfileViewModel: EditProfileViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val uiState by editProfileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para los campos del formulario, vinculados al ViewModel
    val fullName by editProfileViewModel.fullName.collectAsState()
    val userName by editProfileViewModel.userName.collectAsState()
    val phone by editProfileViewModel.phone.collectAsState()
    val currentImageUrl by editProfileViewModel.currentImageUrl.collectAsState()
    val email by editProfileViewModel.email.collectAsState()

    // Para manejar la selección de imagen
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Launchers para galería y cámara
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri; imageBitmap = null
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        imageBitmap = bitmap; imageUri = null
    }

    var showScreenContent by remember { mutableStateOf(false) }
    var isUpdatingOrLoadingProfile by remember { mutableStateOf(false) }


    // LaunchedEffect para reaccionar a los cambios de uiState
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EditProfileUiState.Idle -> {
                isUpdatingOrLoadingProfile = false
                // Si venimos de un UpdateSuccess, ProfileLoaded ya debería tener los datos.
                // Si el estado Idle se alcanza por otras razones, podría ser necesario
                // explícitamente llamar a loadUserProfile si showScreenContent es false.
            }
            is EditProfileUiState.LoadingUserProfile -> {
                isUpdatingOrLoadingProfile = true
                showScreenContent = false // Ocultar contenido mientras carga el perfil inicial
            }
            is EditProfileUiState.ProfileLoaded -> {
                isUpdatingOrLoadingProfile = false
                showScreenContent = true // Mostrar contenido una vez que el perfil está cargado
                // Los campos del formulario ya se actualizan a través de StateFlows en el ViewModel
            }
            is EditProfileUiState.UpdatingProfile -> {
                isUpdatingOrLoadingProfile = true
            }
            is EditProfileUiState.UpdateSuccess -> {
                isUpdatingOrLoadingProfile = false
                showScreenContent = true // Asegurarse de que el contenido es visible
                state.successMessage?.let {
                    snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                }
                // Limpiar la selección de imagen local después de guardar
                imageUri = null
                imageBitmap = null
                // Volver al estado ProfileLoaded para reflejar que la "operación" terminó
                // y la pantalla está mostrando el perfil actualizado.
                editProfileViewModel.resetUiStateToProfileLoaded()
            }
            is EditProfileUiState.Error -> {
                isUpdatingOrLoadingProfile = false
                showScreenContent = editProfileViewModel.canEdit
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_editar_perfil)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_volver))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState is EditProfileUiState.Error && !editProfileViewModel.canEdit) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Button(onClick = editProfileViewModel::loadUserProfile) {
                    Text(stringResource(R.string.profile_retry))
                }
            }
        } else if (isUpdatingOrLoadingProfile && !showScreenContent) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else { // Muestra el contenido del perfil
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(UiDimensions.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de Imagen de Perfil
                Box(
                    contentAlignment = Alignment.BottomEnd, // Para el icono de la cámara
                    modifier = Modifier
                        .size(UiDimensions.size120)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImagePickerDialog = true }
                ) {
                    // Muestra la imagen seleccionada localmente (imageBitmap o imageUri)
                    // o la imagen actual del perfil (currentImageUrl)
                    val displayImage: Any? = imageBitmap ?: imageUri ?: currentImageUrl
                    val placeholderRes = R.drawable.ic_launcher_foreground // Define tu placeholder

                    if (displayImage != null && displayImage.toString().isNotBlank()) {
                        if (displayImage is Bitmap) {
                            Image(
                                bitmap = displayImage.asImageBitmap(),
                                contentDescription = stringResource(R.string.ui_foto_de_perfil),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else { // Uri o String URL
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(displayImage)
                                    .crossfade(true)
                                    .placeholder(placeholderRes)
                                    .error(placeholderRes)
                                    .build(),
                                contentDescription = stringResource(R.string.ui_foto_de_perfil),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Icono de placeholder si no hay imagen
                        Icon(
                            painter = painterResource(id = placeholderRes),
                            contentDescription = stringResource(R.string.ui_foto_de_perfil_por_defecto),
                            modifier = Modifier.fillMaxSize().padding(UiDimensions.extraLarge), // Ajusta padding para el tamaño del icono
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Icono pequeño de cámara superpuesto
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = stringResource(R.string.ui_cambiar_foto),
                        modifier = Modifier
                            .size(UiDimensions.size36)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                            .padding(UiDimensions.compact),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(UiDimensions.extraLarge))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = editProfileViewModel::changeName,
                    label = { Text(stringResource(R.string.ui_nombre_completo)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUpdatingOrLoadingProfile
                )
                Spacer(modifier = Modifier.height(UiDimensions.large))
                OutlinedTextField(
                    value = userName,
                    onValueChange = editProfileViewModel::changeUsername,
                    label = { Text(stringResource(R.string.ui_nombre_de_usuario)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUpdatingOrLoadingProfile
                )
                Spacer(modifier = Modifier.height(UiDimensions.large))
                OutlinedTextField(
                    value = phone,
                    onValueChange = editProfileViewModel::changePhone,
                    label = { Text(stringResource(R.string.ui_telefono)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUpdatingOrLoadingProfile
                )
                Spacer(modifier = Modifier.height(UiDimensions.large))
                OutlinedTextField(
                    value = email,
                    onValueChange = { /* No hacer nada */ },
                    label = { Text(stringResource(R.string.ui_correo_electronico_no_editable)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false, // Deshabilitar explícitamente
                    colors = TextFieldDefaults.colors( // Usar colors en lugar de outlinedTextFieldColors para M3
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Color de fondo cuando está deshabilitado
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled) // Color del borde cuando está deshabilitado
                    )
                )

                Spacer(modifier = Modifier.height(UiDimensions.size32))

                Button(
                    onClick = {
                        editProfileViewModel.updateProfile(
                            newImageUri = imageUri,
                            newImageBitmap = imageBitmap
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdatingOrLoadingProfile && editProfileViewModel.canEdit
                ) {
                    if (isUpdatingOrLoadingProfile && uiState is EditProfileUiState.UpdatingProfile) { // Mostrar loader solo si se está actualizando
                        CircularProgressIndicator(modifier = Modifier.size(UiDimensions.extraLarge), strokeWidth = UiDimensions.xs, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.ui_guardar_cambios))
                    }
                }
                Spacer(modifier = Modifier.height(UiDimensions.large))
                OutlinedButton(
                    onClick = {
                        editProfileViewModel.signOut(authViewModel)
                        // La navegación fuera de esta pantalla debería ser manejada
                        // por el RootNavigationGraph observando el cambio en AuthState.
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    enabled = !isUpdatingOrLoadingProfile
                ) {
                    Text(stringResource(R.string.ui_cerrar_sesion))
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
