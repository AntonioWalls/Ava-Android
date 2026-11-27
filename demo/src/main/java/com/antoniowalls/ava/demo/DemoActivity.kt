package com.antoniowalls.ava.demo

import com.antoniowalls.ava.design.UiDimensions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.antoniowalls.ava.demo.practice.OPEN_PRACTICE_ACTION
import com.antoniowalls.ava.demo.practice.PracticeProgress
import com.antoniowalls.ava.demo.practice.PracticeProgressStore
import com.antoniowalls.ava.demo.practice.PracticeRoute
import com.antoniowalls.ava.demo.practice.formatPracticeTime
import com.antoniowalls.ava.lessons.ui.LessonCarouselScreen

private const val PRACTICE_OPEN = "practice_open"

class DemoActivity : ComponentActivity() {
    private var practiceOpen by mutableStateOf(false)
    private val progressStore by lazy { PracticeProgressStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        practiceOpen = savedInstanceState?.getBoolean(PRACTICE_OPEN)
            ?: (intent.action == OPEN_PRACTICE_ACTION)

        setContent {
            val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                val progress by progressStore.updates.collectAsState(
                    initial = remember { progressStore.read() }
                )
                val readerState = rememberSaveableStateHolder()
                BackHandler(enabled = practiceOpen) { practiceOpen = false }
                if (practiceOpen) {
                    PracticeRoute(
                        completed = progress.completed,
                        onBack = { practiceOpen = false }
                    )
                } else {
                    readerState.SaveableStateProvider("lesson") {
                        LessonCarouselScreen(
                            lesson = demoMeditationLesson(resources),
                            bottomBar = {
                                PracticeFooter(progress, onOpen = { practiceOpen = true })
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == OPEN_PRACTICE_ACTION) practiceOpen = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PRACTICE_OPEN, practiceOpen)
    }
}

@Composable
private fun PracticeFooter(progress: PracticeProgress, onOpen: () -> Unit) {
    val action = when {
        progress.positionMs >= 1_000L -> stringResource(
            R.string.continue_practice, formatPracticeTime(progress.positionMs)
        )
        progress.completed -> stringResource(R.string.repeat_practice)
        else -> stringResource(R.string.open_practice)
    }
    Surface(tonalElevation = UiDimensions.tiny) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = UiDimensions.extraLarge, vertical = UiDimensions.base),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiDimensions.small)
        ) {
            if (progress.completed) {
                Text(
                    stringResource(R.string.practice_completed),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text(action)
            }
        }
    }
}
