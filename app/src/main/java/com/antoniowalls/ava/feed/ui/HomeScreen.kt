package com.antoniowalls.ava.feed.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment // Icono para comentarios
import androidx.compose.material.icons.filled.Favorite // Icono de corazón lleno
import androidx.compose.material.icons.filled.FavoriteBorder // Icono de corazón vacío
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R
import com.antoniowalls.ava.shared.ui.UserProfileHeader
import com.antoniowalls.ava.feed.model.Comment
import com.antoniowalls.ava.feed.model.Post
import com.antoniowalls.ava.feed.ui.CommentsBottomSheetContent
import com.antoniowalls.ava.feed.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth // Importar FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    feedViewModel: FeedViewModel = viewModel()
) {
    val posts by feedViewModel.posts.collectAsState()
    val isLoading by feedViewModel.isLoading.collectAsState()
    val errorMessage by feedViewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserProfile by feedViewModel.currentUserProfile.collectAsState() // Observa el perfil

    // --- ESTADO PARA EL BOTTOM SHEET DE COMENTARIOS ---
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showCommentsBottomSheet by remember { mutableStateOf(false) }
    val selectedPostIdForComments by feedViewModel.selectedPostIdForComments.collectAsState()
    val commentsForSelectedPost by feedViewModel.selectedPostComments.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            feedViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmallTopAppBar( // Usamos SmallTopAppBar para que sea más compacto
                title = { Text(stringResource(R.string.ui_feed)) },
                colors = TopAppBarDefaults.smallTopAppBarColors( // Colores para SmallTopAppBar
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("create_post_screen")
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Filled.Add, stringResource(R.string.ui_crear_nueva_publicacion))
            }
        }
    ) { scaffoldPaddingValues -> // Renombrado para claridad
        Column( // Envolvemos todo en un Column
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPaddingValues) // Aplicamos el padding del Scaffold aquí
        ) {
            // UserProfileHeader justo debajo de la TopAppBar
            UserProfileHeader(
                userProfile = currentUserProfile,
                onClick = {
                    navController.navigate("edit_profile_screen") // Navega a editar perfil
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)) // Fondo similar al TopAppBar
                    .padding(vertical = UiDimensions.base, horizontal = UiDimensions.large) // Padding para el header
            )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            if (isLoading && posts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (posts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(all = UiDimensions.medium),
                    verticalArrangement = Arrangement.spacedBy(UiDimensions.medium)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            currentUserId = FirebaseAuth.getInstance().currentUser?.uid, // Pasar el UID actual
                            onLikeClicked = {
                                feedViewModel.toggleLike(post.id)
                            },
                            onCommentClicked = {
                                feedViewModel.selectPostForComments(post.id) // Selecciona el post
                                showCommentsBottomSheet = true // Muestra el BottomSheet
                            }
                        )
                    }
                }
            } else if (errorMessage == null) {
                Text(
                    text = stringResource(R.string.ui_aun_no_hay_publicaciones_se_el_primero),
                    modifier = Modifier.align(Alignment.Center).padding(UiDimensions.large),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } }
        if (showCommentsBottomSheet && selectedPostIdForComments != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCommentsBottomSheet = false
                    feedViewModel.selectPostForComments(null) // Deselecciona el post
                },
                sheetState = sheetState
            ) {
                CommentsBottomSheetContent(
                    postId = selectedPostIdForComments!!,
                    comments = commentsForSelectedPost,
                    onAddComment = { postIdArg, textArg -> // Renombrados para evitar shadowing
                        feedViewModel.addComment(postIdArg, textArg)
                    },
                    onDismiss = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showCommentsBottomSheet = false
                                feedViewModel.selectPostForComments(null)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    currentUserId: String?,
    onLikeClicked: () -> Unit,
    onCommentClicked: () -> Unit // Este se usa ahora
) {
    // ... (código existente de PostItem con el botón de comentario) ...
    // Asegúrate de que el IconButton de comentario llame a onCommentClicked:
    // IconButton(onClick = onCommentClicked) { ... }
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val isLikedByCurrentUser = remember(post.likedBy, currentUserId) {
        currentUserId != null && post.likedBy.contains(currentUserId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiDimensions.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = UiDimensions.xs),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(UiDimensions.large)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(post.userProfileImageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .build(),
                    contentDescription = stringResource(R.string.ui_foto_de_perfil_de, post.userName.orEmpty()),
                    modifier = Modifier
                        .size(UiDimensions.size40)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(UiDimensions.medium))
                Column {
                    Text(
                        text = post.userName ?: stringResource(R.string.ui_anonimo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.timestamp?.let { dateFormat.format(it) } ?: stringResource(R.string.ui_hace_un_momento),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(UiDimensions.medium))
            if (!post.text.isNullOrBlank()) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(UiDimensions.medium))
            }
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(post.imageUrl).crossfade(true).build(),
                    contentDescription = stringResource(R.string.ui_imagen_de_la_publicacion),
                    modifier = Modifier.fillMaxWidth().heightIn(min = UiDimensions.size150, max = UiDimensions.size300).clip(RoundedCornerShape(UiDimensions.base)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(UiDimensions.medium))
            }
            if (!post.videoUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(UiDimensions.size200).background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(UiDimensions.base)).padding(UiDimensions.base),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.ui_video, post.videoUrl), color = Color.White)
                }
                Spacer(modifier = Modifier.height(UiDimensions.medium))
            }
            Divider(modifier = Modifier.padding(vertical = UiDimensions.base))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClicked) {
                        Icon(
                            imageVector = if (isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isLikedByCurrentUser) stringResource(R.string.ui_quitar_me_gusta) else stringResource(R.string.ui_dar_me_gusta),
                            tint = if (isLikedByCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCommentClicked) { // <--- LLAMA A onCommentClicked
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            contentDescription = stringResource(R.string.ui_comentar),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
