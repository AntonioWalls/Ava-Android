package com.antoniowalls.ava.demo.practice

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.antoniowalls.ava.demo.DemoActivity
import com.antoniowalls.ava.demo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val OPEN_PRACTICE_ACTION = "com.antoniowalls.ava.demo.OPEN_PRACTICE"
private const val BREATHING_MEDIA_ID = "breathing"
private const val CHECKPOINT_INTERVAL_MS = 5_000L

@OptIn(UnstableApi::class)
class PracticePlaybackService : MediaSessionService() {
    private val playbackScope = CoroutineScope(Dispatchers.Main.immediate)
    private val progress by lazy { PracticeProgressStore(applicationContext) }
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val attributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(attributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        val audio = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .appendPath(R.raw.breathing_cues.toString())
            .build()
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (player.currentMediaItem?.mediaId != BREATHING_MEDIA_ID) return
                if (player.playbackState == Player.STATE_ENDED) {
                    if (player.playWhenReady && events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                        progress.markCompleted()
                        player.pause()
                    }
                } else if (events.containsAny(
                        Player.EVENT_POSITION_DISCONTINUITY,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED
                    )
                ) {
                    checkpoint(player)
                }
            }
        })
        player.setMediaItem(
            MediaItem.Builder()
                .setMediaId(BREATHING_MEDIA_ID)
                .setUri(audio)
                .setMediaMetadata(
                    MediaMetadata.Builder().setTitle(getString(R.string.practice_title)).build()
                )
                .build(),
            progress.read().positionMs
        )
        player.prepare()

        val openPractice = Intent(this, DemoActivity::class.java)
            .setAction(OPEN_PRACTICE_ACTION)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val sessionActivity = PendingIntent.getActivity(
            this, 0, openPractice, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        session = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()

        playbackScope.launch {
            while (isActive) {
                delay(CHECKPOINT_INTERVAL_MS)
                if (player.isPlaying) checkpoint(player)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        playbackScope.cancel()
        session?.let {
            checkpoint(it.player)
            it.player.release()
            it.release()
        }
        session = null
        super.onDestroy()
    }

    private fun checkpoint(player: Player) {
        val duration = player.duration
        if (duration > 0 &&
            player.currentMediaItem?.mediaId == BREATHING_MEDIA_ID &&
            player.playbackState != Player.STATE_ENDED &&
            player.playerError == null
        ) {
            progress.savePosition(player.currentPosition.coerceIn(0, duration - 1))
        }
    }
}
