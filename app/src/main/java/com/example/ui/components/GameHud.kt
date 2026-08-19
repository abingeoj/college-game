package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.model.AcademicYearStage
import com.example.model.GameState
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Rose500
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTileBg
import java.util.Locale

@Composable
fun GameHud(
    engine: GameEngine,
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "powerup_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // TOP SECTION: Academic Year Progress & Sleek Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // Stats Row: Coins, CGPA, Distance, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SleekSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("hud_coins")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Coins",
                            tint = Amber500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${gameState.coins}",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Credits / GPA Pill (Sleek Interface style)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SleekSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("hud_cgpa")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "CGPA",
                            tint = Indigo600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "CREDITS (GPA)",
                                color = Slate400,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f", gameState.cgpa),
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Distance & Pause
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SleekSurface.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${gameState.distanceMeters.toInt()}m",
                            color = Emerald600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = SleekSurface.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = { engine.pauseGame() },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("pause_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Academic Year 4-Segment Progress Bar (Sleek Pill Container)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SleekSurface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academic_year_bar")
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CURRENT: ",
                                color = Slate400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = gameState.currentStage.title.uppercase(),
                                color = Indigo600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Indigo100
                        ) {
                            Text(
                                text = gameState.currentStage.moduleName,
                                color = Indigo700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 smooth capsule segments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AcademicYearStage.entries.forEach { stage ->
                            val isCompleted = gameState.distanceMeters >= stage.endDistance
                            val isCurrent = gameState.currentStage == stage
                            val fraction = stage.getProgressFraction(gameState.distanceMeters)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Slate200)
                            ) {
                                if (isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(stage.themeColor))
                                    )
                                } else if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .fillMaxSize()
                                            .background(Indigo600)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active Buffs Row (Shield, Magnet, Sprint)
            AnimatedVisibility(visible = gameState.activeBuffs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    gameState.activeBuffs.forEach { buff ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurface.copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Sky400),
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${buff.name}: ${buff.remainingTimeSeconds.toInt()}s",
                                    color = Slate800,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM SECTION: Sleek On-Screen Arcade Controls & Special Power Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left & Right Lane Shift Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Move Left
                ArcadeTouchButton(
                    testTag = "btn_move_left",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "LEFT",
                    onClick = { engine.moveLane(-1) }
                )

                // Move Right
                ArcadeTouchButton(
                    testTag = "btn_move_right",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    label = "RIGHT",
                    onClick = { engine.moveLane(1) }
                )
            }

            // Special Ability Button in Center
            val specialReady = gameState.isSpecialPowerReady
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(if (specialReady) glowScale else 1.0f)
                    .size(66.dp)
                    .testTag("btn_special_power")
                    .clickable(
                        enabled = specialReady,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { engine.activateSpecialPower() }
            ) {
                // Background Glow
                Surface(
                    shape = CircleShape,
                    color = if (specialReady) Amber500 else SleekSurface.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = if (specialReady) Amber400 else Slate300
                    ),
                    shadowElevation = if (specialReady) 4.dp else 2.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = gameState.selectedCharacter.specialPowerName,
                            tint = if (specialReady) Color.White else Slate400,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = if (specialReady) "READY!" else "${(gameState.specialPowerEnergy * 100).toInt()}%",
                            color = if (specialReady) Color.White else Slate600,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                if (!specialReady) {
                    CircularProgressIndicator(
                        progress = { gameState.specialPowerEnergy },
                        modifier = Modifier.fillMaxSize(),
                        color = Indigo600,
                        strokeWidth = 3.dp,
                        trackColor = Color.Transparent
                    )
                }
            }

            // Jump & Slide Actions
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Slide Button
                ArcadeTouchButton(
                    testTag = "btn_slide",
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "SLIDE",
                    onClick = { engine.slide() },
                    accentColor = Rose500
                )

                // Jump Button
                ArcadeTouchButton(
                    testTag = "btn_jump",
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "JUMP",
                    onClick = { engine.jump() },
                    accentColor = Indigo600
                )
            }
        }
    }
}

@Composable
fun ArcadeTouchButton(
    testTag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color = Slate500
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SleekSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.8f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(54.dp)
            .testTag(testTag)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Slate800,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = Slate600,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

