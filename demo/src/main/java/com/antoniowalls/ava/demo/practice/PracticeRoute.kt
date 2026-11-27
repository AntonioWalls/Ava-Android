package com.antoniowalls.ava.demo.practice

import com.antoniowalls.ava.design.UiDimensions
import android.content.ComponentName
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.antoniowalls.ava.demo.R
import kotlinx.coroutines.delay
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun PracticeRoute(completed: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    var attempt by remember { mutableStateOf(0) }
    var connectionFailed by remember(attempt) { mutableStateOf(false) }
    var activeController by remember(attempt) { mutableStateOf<MediaController?>(null) }

    DisposableEffect(context, attempt) {
        var disposed = false
        val token = SessionToken(context, ComponentName(context, PracticePlaybackService::class.java))
        val future = MediaController.Builder(context, token)
            .setListener(object : MediaController.Listener {
                override fun onDisconnected(controller: MediaController) {
                    if (!disposed) {
                        connectionFailed = true
                        activeController = null
                    }
                }
            })
            .buildAsync()
        future.addListener({
            if (!disposed) {
                try {
                    activeController = future.get()
                } catch (_: ExecutionException) {
                    connectionFailed = true
                } catch (_: CancellationException) {
                    connectionFailed = true
                }
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            disposed = true
            MediaController.releaseFuture(future)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.practice_title)) }) }
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(UiDimensions.extraLarge),
            verticalArrangement = Arrangement.spacedBy(UiDimensions.large)
        ) {
            Text(stringResource(R.string.practice_guide), style = MaterialTheme.typography.bodyLarge)
            val player = activeController
            when {
                connectionFailed -> PlaybackError(onRetry = { attempt++ })
                player != null -> PracticePlayer(player)
                else -> Text(stringResource(R.string.audio_loading))
            }
            if (completed) {
                Text(
                    stringResource(R.string.practice_completed),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back_to_lesson))
            }
        }
    }
}

@Composable
private fun PracticePlayer(player: Player) {
    var state by remember(player) { mutableStateOf(player.readPracticeStatus()) }
    var dragPosition by remember(player) { mutableStateOf<Float?>(null) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                state = player.readPracticeStatus()
            }
        }
        player.addListener(listener)
        state = player.readPracticeStatus()
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(player, state.playing) {
        while (state.playing) {
            delay(250)
            state = player.readPracticeStatus()
        }
    }

    if (state.failed) {
        PlaybackError(onRetry = {
            player.pause()
            player.prepare()
        })
        return
    }

    val seekDescription = stringResource(R.string.audio_seek)
    val duration = state.durationMs
    val position = (dragPosition?.roundToLong() ?: state.positionMs).coerceIn(0, duration)
    Slider(
        value = position.toFloat(),
        onValueChange = { dragPosition = it },
        onValueChangeFinished = {
            dragPosition?.let { player.seekTo(it.roundToLong()) }
            dragPosition = null
        },
        enabled = duration > 0 && state.canSeek,
        valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = seekDescription }
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatPracticeTime(position))
        if (duration > 0) Text(formatPracticeTime(duration))
    }

    if (state.interrupted) {
        Text(stringResource(R.string.audio_interrupted))
    } else if (state.loading) {
        Text(stringResource(R.string.audio_loading))
    }
    val buttonLabel = when {
        state.playRequested -> R.string.audio_pause
        state.finished -> R.string.audio_replay
        else -> R.string.audio_play
    }
    Button(
        onClick = {
            if (player.playWhenReady && player.playbackState != Player.STATE_ENDED) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                if (player.playbackState == Player.STATE_IDLE) player.prepare()
                player.play()
            }
        },
        enabled = state.playRequested || !state.loading,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(buttonLabel))
    }
}

@Composable
private fun PlaybackError(onRetry: () -> Unit) {
    Text(stringResource(R.string.audio_unavailable), color = MaterialTheme.colorScheme.error)
    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
}

private data class PracticeStatus(
    val durationMs: Long,
    val positionMs: Long,
    val playing: Boolean,
    val playRequested: Boolean,
    val interrupted: Boolean,
    val loading: Boolean,
    val finished: Boolean,
    val failed: Boolean,
    val canSeek: Boolean
)

private fun Player.readPracticeStatus(): PracticeStatus = PracticeStatus(
    durationMs = duration.coerceAtLeast(0),
    positionMs = currentPosition,
    playing = isPlaying,
    playRequested = playWhenReady && playbackState != Player.STATE_ENDED,
    interrupted = playWhenReady && playbackSuppressionReason != Player.PLAYBACK_SUPPRESSION_REASON_NONE,
    loading = playbackState == Player.STATE_BUFFERING,
    finished = playbackState == Player.STATE_ENDED,
    failed = playerError != null,
    canSeek = isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
)

internal fun formatPracticeTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
