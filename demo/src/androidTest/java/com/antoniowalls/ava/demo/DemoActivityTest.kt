package com.antoniowalls.ava.demo

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.antoniowalls.ava.demo.practice.PRACTICE_PROGRESS_FILE
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class DemoActivityTest {
    @get:Rule
    val compose = createAndroidComposeRule<DemoActivity>()

    @Before
    fun resetLocalProgress() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        check(context.getSharedPreferences(PRACTICE_PROGRESS_FILE, Context.MODE_PRIVATE).edit().clear().commit())
        waitForText("Escuchar práctica")
    }

    @Test
    fun opensLocalLessonAndCanTurnThePage() {
        compose.onNodeWithText("Sistema de Meditación").assertIsDisplayed()
        val firstPage = compose.onNodeWithText("En muchas tradiciones budistas", substring = true)
        firstPage.assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        val secondPage = compose.onNodeWithText("Cuando notes que tu mente", substring = true)
        secondPage.assertIsDisplayed()
        compose.onNodeWithText("Anterior").performClick()
        firstPage.assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        secondPage.assertIsDisplayed()

        openPractice()
        compose.onNodeWithText("Volver a la enseñanza").performClick()
        secondPage.assertIsDisplayed()
    }

    @Test
    fun playsPausesSeeksAndReplaysLocalAudio() {
        openPractice()
        seekTo(60_000f)
        compose.waitUntil(10_000) { abs(playbackPosition() - 60_000f) < 1_000f }

        compose.onNodeWithText("Reproducir").performClick()
        waitForText("Pausar")
        compose.waitUntil(10_000) { playbackPosition() > 60_500f }
        compose.onNodeWithText("Pausar").performClick()
        waitForText("Reproducir")

        seekTo(119_000f)
        compose.waitUntil(10_000) { playbackPosition() >= 118_500f }
        compose.onNodeWithText("Reproducir").performClick()
        waitForText("Volver a reproducir")
        waitForText("Práctica completada")
        compose.onNodeWithText("Volver a reproducir").performClick()
        waitForText("Pausar")
        compose.waitUntil(10_000) { playbackPosition() in 0f..5_000f }
        compose.onNodeWithText("Pausar").performClick()
        waitForText("Reproducir")
        compose.onNodeWithText("Práctica completada").assertIsDisplayed()
    }

    @Test
    fun opensSavedPositionPausedAfterActivityRecreation() {
        openPractice()
        seekTo(45_000f)
        waitForText("0:45")
        compose.onNodeWithText("Volver a la enseñanza").performClick()
        waitForText("Continuar desde 0:45")

        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Continuar desde 0:45").performClick()
        waitForText("2:00")
        waitForText("0:45")
        compose.onNodeWithText("Reproducir").assertIsDisplayed()
    }

    @Test
    fun yieldsAndResumesAfterTransientAudioFocusLoss() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            assumeTrue("AudioFocusRequest requiere Android 8.0", false)
            return
        }
        openPractice()
        compose.onNodeWithText("Reproducir").performClick()
        compose.waitUntil(10_000) { playbackPosition() > 500f }

        val audioManager = compose.activity.getSystemService(AudioManager::class.java)
        val competingRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setOnAudioFocusChangeListener {}
            .build()
        assertEquals(AudioManager.AUDIOFOCUS_REQUEST_GRANTED, audioManager.requestAudioFocus(competingRequest))
        val positionWhenInterrupted = try {
            waitForText("Reproducción interrumpida.")
            playbackPosition()
        } finally {
            audioManager.abandonAudioFocusRequest(competingRequest)
        }
        compose.waitUntil(10_000) { playbackPosition() > positionWhenInterrupted + 500f }
        compose.onNodeWithText("Pausar").performClick()
        waitForText("Reproducir")
    }

    private fun openPractice() {
        compose.onNodeWithText("Escuchar práctica").performClick()
        compose.onNodeWithText("Seguimiento de la respiración").assertIsDisplayed()
        waitForText("2:00")
        waitForText("Reproducir")
    }

    private fun waitForText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun seekTo(milliseconds: Float) {
        compose.onNodeWithContentDescription("Posición del audio")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(milliseconds) }
    }

    private fun playbackPosition(): Float = compose.onNodeWithContentDescription("Posición del audio")
        .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
}
