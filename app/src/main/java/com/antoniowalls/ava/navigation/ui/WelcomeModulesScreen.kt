package com.antoniowalls.ava.navigation.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme // Import for dark mode check
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.antoniowalls.ava.R
import com.antoniowalls.ava.auth.viewmodel.AuthViewModel
import com.antoniowalls.ava.feed.viewmodel.FeedViewModel
import com.antoniowalls.ava.navigation.IconRenderer

 data class AppModule(
    val title: String,
    val description: String,
    val icon: Any,
    val route: String
 )

// Lista de colores pastel solo para el tema claro
private val lightPastelColors = listOf(
    Color(0xFFFADADD), // Light Pink
    Color(0xFFFFF3AD), // Light Yellow
    Color(0xFFD4F0F0), // Light Cyan
    Color(0xFFE6E0F8), // Light Lavender
    Color(0xFFD9EAD3), // Light Green
    Color(0xFFFFE0B2)  // Light Orange/Peach
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeModulesScreen(navController: NavController, authViewModel: AuthViewModel) {
    val feedViewModel: FeedViewModel = viewModel()
    val currentUserProfile by feedViewModel.currentUserProfile.collectAsState()

    val modules = listOf(
        AppModule(stringResource(R.string.ui_foro), stringResource(R.string.ui_explora_publicaciones_interactua_con_la_comunidad_y), painterResource(id = R.drawable.foro), "home"),
        AppModule(stringResource(R.string.ui_ensenanzas), stringResource(R.string.ui_aprende_sobre_budismo_meditacion_y_desarrollo_personal), painterResource(id = R.drawable.libro), "lessons"),
        AppModule(stringResource(R.string.ui_meditacion), stringResource(R.string.ui_encuentra_calma_y_claridad_con_nuestras_meditaciones), painterResource(id = R.drawable.flordeloto), "meditation"),
        AppModule(stringResource(R.string.ui_chat), stringResource(R.string.ui_conectate_y_conversa_con_otros_usuarios_de), painterResource(id = R.drawable.chat), "user_list_screen")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentUserProfile?.userName?.let { name -> stringResource(R.string.ui_bienvenido_a_2, name) } ?: stringResource(R.string.ui_bienvenido_a),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    Box(modifier = Modifier.padding(end = UiDimensions.base)) {
                        if (currentUserProfile != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentUserProfile?.imageUrl)
                                    .crossfade(true)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .build(),
                                contentDescription = stringResource(R.string.ui_foto_de_perfil),
                                modifier = Modifier
                                    .size(UiDimensions.size40)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Spacer(modifier = Modifier.size(UiDimensions.size40))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UiDimensions.large, vertical = UiDimensions.base),
            verticalArrangement = Arrangement.spacedBy(UiDimensions.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    stringResource(R.string.ui_selecciona_un_modulo_para_comenzar),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = UiDimensions.large)
                )
            }
            itemsIndexed(modules) { index, module ->
                ModuleCard(
                    module = module,
                    onClick = {
                        navController.navigate("main_app_container/${module.route}") {
                            popUpTo("welcome_modules") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    itemIndex = index
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleCard(
    module: AppModule,
    onClick: () -> Unit,
    itemIndex: Int
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Determinar el color de fondo de la tarjeta
    val cardBackgroundColor = if (isDarkTheme) {
        // Usar surfaceVariant para modo oscuro como alternativa a surfaceContainerHigh
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        lightPastelColors[itemIndex % lightPastelColors.size]
    }

    // Color para el borde, que se adapte al modo claro/oscuro
    val borderColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(UiDimensions.large),
        elevation = CardDefaults.cardElevation(defaultElevation = UiDimensions.none),
        border = BorderStroke(UiDimensions.xxs, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = UiDimensions.large, vertical = UiDimensions.wide)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(UiDimensions.touchTarget)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDarkTheme) 0.7f else 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconRenderer(
                    icon = module.icon,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(UiDimensions.large))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    module.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(UiDimensions.compact))
                Text(
                    module.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = UiDimensions.text20
                )
            }
        }
    }
}
