package com.antoniowalls.ava.meditation.ui

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.ui.res.stringResource
import android.media.MediaPlayer
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.antoniowalls.ava.R
import com.antoniowalls.ava.ui.theme.bakbakone
import com.antoniowalls.ava.ui.theme.nunitoMedium
import com.antoniowalls.ava.ui.theme.nunitoRegular
import com.antoniowalls.ava.meditation.data.meditationOptions
import com.antoniowalls.ava.meditation.model.MeditationType
import kotlinx.coroutines.delay


@Preview(showBackground = true)
@Composable
fun MeditationPlayerScreenPreview() {
    MeditationPlayerScreen(navController = rememberNavController(), meditationId = "1")
}

@Composable
fun MeditationPlayerScreen(navController: NavController, meditationId: String?) {
    val context = LocalContext.current
    val meditation = meditationOptions(LocalContext.current.resources).find { it.id == meditationId }
    if (meditation == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { navController.popBackStack() }) {
                Text(stringResource(R.string.meditation_unavailable))
            }
        }
        return
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    var phase by remember { mutableStateOf("idle") }
    var countdownNumber by remember { mutableStateOf(3) }

    val totalDuration = remember { mutableStateOf(0) }
    val currentTime = remember { mutableStateOf(0) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(key1 = phase) {
        if (phase == "countdown") {
            for (count in 3 downTo 1) {
                countdownNumber = count
                delay(1000L)
            }
            phase = "meditation"
        }
    }

    LaunchedEffect(key1 = phase) {
        if (phase == "meditation") {
            mediaPlayer = MediaPlayer.create(context, meditation.audioUrl)
            totalDuration.value = mediaPlayer?.duration?.div(1000) ?: ((meditation?.time ?: 0) * 60)
            mediaPlayer?.start()
            isPlaying = true

            while (mediaPlayer != null && phase == "meditation") {
                if (isPlaying && !isSeeking) {
                    val currentPos = mediaPlayer?.currentPosition ?: 0
                    currentTime.value = currentPos / 1000
                    sliderPosition = currentTime.value.toFloat()
                }
                delay(200)

                if (totalDuration.value > 0 && currentTime.value >= totalDuration.value) {
                    isPlaying = false
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    phase = "idle"
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = {
                mediaPlayer?.release()
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(UiDimensions.large)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ui_regresar),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(UiDimensions.size32)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (phase == "idle") {
                    AnimatedArcus()
                }
                MeditationTitle(
                    meditation = meditation,
                    phase = phase,
                    countdownNumber = countdownNumber
                )
            }

            Spacer(modifier = Modifier.height(UiDimensions.large))

            if (phase == "idle") {
                MeditationDescription(meditation = meditation)
            }

            Spacer(modifier = Modifier.height(UiDimensions.size40))

            if (phase == "meditation") {
                PlayerSeekBar(
                    currentTime = currentTime.value,
                    totalDuration = totalDuration.value,
                    sliderPosition = sliderPosition,
                    onSeekChange = { newPosition ->
                        isSeeking = true
                        sliderPosition = newPosition
                    },
                    onSeekFinished = {
                        isSeeking = false
                        currentTime.value = sliderPosition.toInt()
                        mediaPlayer?.seekTo(sliderPosition.toInt() * 1000)
                    }
                )
            }

            Buttons(
                phase = phase,
                isPlaying = isPlaying,
                onPlayClick = {
                    if (phase == "idle") {
                        phase = "countdown"
                    } else if (phase == "meditation") {
                        if (isPlaying) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                        isPlaying = !isPlaying
                    }
                },
                onSkipClick = {
                    mediaPlayer?.release()
                    mediaPlayer = null
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun AnimatedArcus() {
    val isDarkTheme = isSystemInDarkTheme()
    val gradientColors = if (isDarkTheme) {
        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    } else {
        listOf(Color(0xFFF5D547), Color(0xFFB1F5B0)) // Yellow, Green
    }

    Box(
        modifier = Modifier
            .padding(UiDimensions.medium)
            .size(UiDimensions.size300),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(UiDimensions.size50)
            ) {
                val canvasSize = size
                val gradientBrush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, canvasSize.height),
                    end = Offset(canvasSize.width, 0f)
                )
                drawCircle(
                    brush = gradientBrush,
                    radius = canvasSize.width / 2,
                    center = Offset(canvasSize.width / 2, canvasSize.height / 2)
                )
            }
        }

        val color1 = MaterialTheme.colorScheme.primary
        val transition = rememberInfiniteTransition(label = "ArcusTransition")
        val radii = listOf(
            Pair(380f, 450f),
            Pair(380f, 580f),
            Pair(380f, 710f)
        )
        val animatedRadii = radii.map { (initialRadius, targetRadius) ->
            transition.animateFloat(
                initialValue = initialRadius,
                targetValue = targetRadius,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = 200,
                        easing = FastOutLinearInEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ), label = "RadiusAnimation"
            )
        }
        val animatedAlphas = radii.map {
            transition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000,
                        delayMillis = 100,
                        easing = FastOutLinearInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ), label = "AlphaAnimation"
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            animatedRadii.forEachIndexed { index, animatedRadius ->
                val animatedAlpha = animatedAlphas[index].value
                drawCircle(
                    color = color1.copy(alpha = animatedAlpha),
                    radius = animatedRadius.value,
                    style = Stroke(width = 8f),
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }
    }
}

@Composable
fun MeditationTitle(
    meditation: MeditationType?,
    phase: String,
    countdownNumber: Int
) {
    val displayText = when (phase) {
        "idle", "meditation" -> {
            meditation?.let { med ->
                val words = med.title.split(" ")
                if (words.size > 1) {
                    val midIndex = (words.size + 1) / 2
                    val firstLine = words.take(midIndex).joinToString(" ")
                    val secondLine = words.drop(midIndex).joinToString(" ")
                    if (secondLine.isNotEmpty()) "$firstLine\n$secondLine" else firstLine
                } else {
                    med.title
                }
            } ?: ""
        }
        "countdown" -> countdownNumber.toString()
        else -> meditation?.title ?: ""
    }

    Text(
        text = displayText,
        fontFamily = bakbakone,
        fontSize = UiDimensions.text34,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(UiDimensions.extraLargePlus)
    )
}

@Composable
fun MeditationDescription(meditation: MeditationType?) {
    meditation?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = it.description,
                fontFamily = nunitoRegular,
                fontSize = UiDimensions.text18,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlayerSeekBar(
    currentTime: Int,
    totalDuration: Int,
    sliderPosition: Float,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit
) {
    val formatTime: (Int) -> String = { timeInSeconds ->
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UiDimensions.size32)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = onSeekChange,
            onValueChangeFinished = onSeekFinished,
            valueRange = 0f..(totalDuration.toFloat().takeIf { it > 0 } ?: 0f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentTime), fontFamily = nunitoRegular, color = MaterialTheme.colorScheme.onBackground)
            Text(text = formatTime(totalDuration), fontFamily = nunitoRegular, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun Buttons(
    phase: String,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "PlayButtonScale")

    Row(
        modifier = Modifier.padding(top = UiDimensions.size50, start = UiDimensions.beforeLarge),
        horizontalArrangement = Arrangement.spacedBy(UiDimensions.size30),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onSkipClick() },
            modifier = Modifier
                .padding(UiDimensions.small)
                .size(UiDimensions.size150, UiDimensions.size70),
            shape = RoundedCornerShape(UiDimensions.beforeLarge),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = stringResource(R.string.ui_skip),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontFamily = nunitoMedium
            )
        }
        Button(
            onClick = { onPlayClick() },
            modifier = Modifier
                .padding(UiDimensions.small)
                .size(UiDimensions.size70, UiDimensions.size70)
                .scale(scale),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(UiDimensions.size40),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            val iconTint = MaterialTheme.colorScheme.onPrimary
            when (phase) {
                "idle" -> {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = stringResource(R.string.ui_play),
                        tint = iconTint
                    )
                }
                "meditation" -> {
                    if (isPlaying) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_pause_24),
                            contentDescription = stringResource(R.string.ui_pause),
                            tint = iconTint
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                            contentDescription = stringResource(R.string.ui_play),
                            tint = iconTint
                        )
                    }
                }
                "countdown" -> {
                    Text(
                        text = stringResource(R.string.ui_wait),
                        fontFamily = nunitoMedium,
                        color = iconTint
                    )
                }
                else -> {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_play_arrow_24),
                        contentDescription = stringResource(R.string.ui_play),
                        tint = iconTint
                    )
                }
            }
        }
    }
}
