package com.antoniowalls.ava.feed.ui // O el paquete que elijas

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.antoniowalls.ava.R // Para placeholder
import com.antoniowalls.ava.shared.ui.ImagePickerDialog
import com.antoniowalls.ava.shared.viewmodel.AppViewModelFactory
import com.antoniowalls.ava.feed.viewmodel.CreatePostUiState
import com.antoniowalls.ava.feed.viewmodel.CreatePostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    createPostViewModel: CreatePostViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val postText by createPostViewModel.postText.collectAsState()
    val imageUri by createPostViewModel.imageUri.collectAsState()
    val imageBitmap by createPostViewModel.imageBitmap.collectAsState()
    val uiState by createPostViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Para seleccionar imagen de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        createPostViewModel.imageUri.value = uri
        createPostViewModel.imageBitmap.value = null // Reset bitmap if gallery image is chosen
    }

    // Para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        createPostViewModel.imageBitmap.value = bitmap
        createPostViewModel.imageUri.value = null // Reset uri if camera image is chosen
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreatePostUiState.Success -> {
                snackbarHostState.showSnackbar(context.getString(R.string.ui_publicacion_creada_con_exito), duration = SnackbarDuration.Short)
                // Navegar atrás o al feed después de un pequeño delay
                kotlinx.coroutines.delay(1000) // Pequeño delay para que se vea el snackbar
                navController.popBackStack()
                createPostViewModel.resetState() // Resetea el estado para la próxima vez
            }
            is CreatePostUiState.Error -> {
                snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
                createPostViewModel.resetState() // Permite reintentar
            }
            else -> { /* Idle o Loading */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_crear_publicacion)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = { createPostViewModel.createPost() },
                        enabled = uiState !is CreatePostUiState.Loading && (postText.isNotBlank() || imageUri != null || imageBitmap != null)
                    ) {
                        if (uiState is CreatePostUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(UiDimensions.extraLarge), strokeWidth = UiDimensions.xs, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(stringResource(R.string.ui_publicar))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UiDimensions.large)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = postText,
                onValueChange = { createPostViewModel.postText.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = UiDimensions.size120),
                label = { Text(stringResource(R.string.ui_que_estas_pensando)) },
                placeholder = { Text(stringResource(R.string.ui_escribe_algo)) }
            )
            Spacer(modifier = Modifier.height(UiDimensions.large))

            // Previsualización de imagen seleccionada/capturada
            val displayImage: Any? = imageBitmap ?: imageUri

            if (displayImage != null) {
                Spacer(modifier = Modifier.height(UiDimensions.base))
                if (displayImage is Bitmap) {
                    Image(
                        bitmap = displayImage.asImageBitmap(),
                        contentDescription = stringResource(R.string.ui_imagen_seleccionada),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UiDimensions.size200)
                            .clip(RoundedCornerShape(UiDimensions.base)),
                        contentScale = ContentScale.Crop
                    )
                } else if (displayImage is Uri) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = stringResource(R.string.ui_imagen_seleccionada),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(UiDimensions.size200)
                            .clip(RoundedCornerShape(UiDimensions.base)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(UiDimensions.base))
                TextButton(onClick = {
                    createPostViewModel.imageUri.value = null
                    createPostViewModel.imageBitmap.value = null
                }) {
                    Text(stringResource(R.string.ui_quitar_imagen))
                }
            }


            Spacer(modifier = Modifier.height(UiDimensions.large))

            // Botón para añadir imagen
            OutlinedButton(
                onClick = { showImagePickerDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.ui_anadir_imagen), modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.ui_anadir_imagen_2))
            }

            // Aquí podrías añadir un botón similar para video en el futuro
        }
    }

    if (showImagePickerDialog) {
        // Reutilizamos el ImagePickerDialog de EditProfileScreen o creamos uno similar
        ImagePickerDialog( // Asegúrate de que este composable exista y funcione como esperas
            onDismissRequest = { showImagePickerDialog = false },
            onCameraClick = {
                showImagePickerDialog = false
                // TODO: Verificar permisos de cámara antes de lanzar
                cameraLauncher.launch()
            },
            onGalleryClick = {
                showImagePickerDialog = false
                // TODO: Verificar permisos de galería antes de lanzar
                galleryLauncher.launch("image/*")
            }
        )
    }
}

// Si no has movido ImagePickerDialog a un archivo común, puedes copiarlo aquí
// desde EditProfileScreen.kt o importarlo si lo moviste.
// Por ahora, asumo que lo tienes accesible (ej. en el mismo paquete o en uno común).
