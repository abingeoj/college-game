package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GamePreferences
import com.example.engine.GameEngine
import com.example.engine.SoundSynthesizer
import com.example.model.GamePhase
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameHud
import com.example.ui.screens.CareerStatsDialog
import com.example.ui.screens.CharacterSelectScreen
import com.example.ui.screens.GraduationScreen
import com.example.ui.screens.SupplyGameOverDialog
import com.example.ui.theme.Indigo600
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CollegeRunnerApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CollegeRunnerApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferences = remember { GamePreferences(context) }
    val soundSynthesizer = remember { SoundSynthesizer(context) }
    val engine = remember { GameEngine(preferences, soundSynthesizer) }

    androidx.compose.runtime.DisposableEffect(soundSynthesizer) {
        onDispose {
            soundSynthesizer.release()
        }
    }

    val gameState by engine.gameState.collectAsState()
    var showCareerStats by remember { mutableStateOf(false) }

    // Game loop tick using withFrameNanos for smooth 60fps physics
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.RUNNING) {
            var lastTimeNanos = 0L
            while (gameState.phase == GamePhase.RUNNING) {
                withFrameNanos { frameTimeNanos ->
                    if (lastTimeNanos != 0L) {
                        val dt = (frameTimeNanos - lastTimeNanos) / 1_000_000_000.0f
                        engine.update(dt.coerceIn(0.001f, 0.05f))
                    }
                    lastTimeNanos = frameTimeNanos
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(SleekBg)) {
        when (gameState.phase) {
            GamePhase.CHARACTER_SELECT -> {
                CharacterSelectScreen(
                    engine = engine,
                    preferences = preferences,
                    onStartGame = { character ->
                        engine.startGame(character)
                    },
                    onShowCareerStats = { showCareerStats = true }
                )
            }

            GamePhase.RUNNING, GamePhase.PAUSED, GamePhase.SUPPLY_GAME_OVER -> {
                // Game Canvas 3D Perspective Runner Layer
                GameCanvas(
                    engine = engine,
                    gameState = gameState,
                    modifier = Modifier.fillMaxSize()
                )

                // HUD Layer (Progress Bar, Stats, Controls)
                GameHud(
                    engine = engine,
                    gameState = gameState,
                    modifier = Modifier.fillMaxSize()
                )

                // Pause Modal Overlay
                if (gameState.phase == GamePhase.PAUSED) {
                    PauseDialog(
                        onResume = { engine.resumeGame() },
                        onRestart = { engine.startGame() },
                        onSelectAvatar = { engine.returnToCharacterSelect() }
                    )
                }

                // Supply / Arrear Game Over Dialog
                if (gameState.phase == GamePhase.SUPPLY_GAME_OVER) {
                    SupplyGameOverDialog(
                        gameState = gameState,
                        onRetry = { engine.startGame() },
                        onSelectAvatar = { engine.returnToCharacterSelect() }
                    )
                }
            }

            GamePhase.BECOME_AN_ENGINEER -> {
                // Graduation Screen leading into Avatar selection loop
                GraduationScreen(
                    gameState = gameState,
                    onLoopToSelectAvatar = {
                        engine.returnToCharacterSelect()
                    }
                )
            }
        }

        // Career Records Dialog
        if (showCareerStats) {
            CareerStatsDialog(
                preferences = preferences,
                engine = engine,
                onDismiss = { showCareerStats = false }
            )
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSelectAvatar: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SEMESTER PAUSED",
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Catch your breath, Engineer!",
                    color = Slate500,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onResume,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_resume")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME SPRINT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_restart")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Slate700)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTART RUN", color = Slate700, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSelectAvatar,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_select_avatar_from_pause")
                ) {
                    Text("CHANGE AVATAR", color = Slate700, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}
