package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferences
import com.example.engine.GameEngine
import com.example.model.CharacterProfile
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber400
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Red500
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
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTileBg

@Composable
fun CharacterSelectScreen(
    engine: GameEngine,
    preferences: GamePreferences,
    onStartGame: (CharacterProfile) -> Unit,
    onShowCareerStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChar by remember { mutableStateOf(engine.gameState.value.selectedCharacter) }
    var showHowToPlay by remember { mutableStateOf(false) }
    var totalCoins by remember { mutableStateOf(preferences.totalCoins) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Semester Title & Badge
                Column {
                    Text(
                        text = "CURRENT SEMESTER",
                        color = Slate500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "College Runner",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = Sky100,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "4-Year Loop",
                                color = Indigo700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right: Coins Pill, Career Stats & Help Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SleekSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shadowElevation = 1.dp
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
                                text = "$totalCoins",
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Trophies / Career Button
                    Surface(
                        shape = CircleShape,
                        color = SleekSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = onShowCareerStats,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("btn_career_stats")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Career",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Help / Guide Button
                    Surface(
                        shape = CircleShape,
                        color = SleekSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        shadowElevation = 1.dp,
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = { showHowToPlay = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("btn_help")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT STUDENT AVATAR",
                    color = Slate500,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Character Selection Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CharacterProfile.ALL_CHARACTERS) { profile ->
                    val isSelected = selectedChar.id == profile.id
                    val isUnlocked = preferences.isCharacterUnlocked(profile.id)

                    CharacterCard(
                        profile = profile,
                        isSelected = isSelected,
                        isUnlocked = isUnlocked,
                        onClick = {
                            selectedChar = profile
                            engine.selectCharacter(profile)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Character Detailed Dossier Card
            val isUnlocked = preferences.isCharacterUnlocked(selectedChar.id)

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_dossier")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SELECTED STUDENT",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedChar.name,
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = selectedChar.title.uppercase(),
                                color = Indigo600,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Special Power Pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Indigo100,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedChar.specialPowerName,
                                    color = Indigo700,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = selectedChar.description,
                        color = Slate600,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Special Power Effect Description Tile
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SleekTileBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Amber100,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Amber500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SPECIAL ABILITY",
                                    color = Slate400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = selectedChar.specialPowerDesc,
                                    color = Slate700,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleek Stat Grid Tiles (matching design HTML bg-[#f3f4f9] grid structure)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SleekStatTile(
                            label = "HEALTH",
                            value = selectedChar.health,
                            max = 4,
                            barColor = Rose500,
                            modifier = Modifier.weight(1f)
                        )
                        SleekStatTile(
                            label = "SPEED",
                            value = selectedChar.speed,
                            max = 5,
                            barColor = Indigo500,
                            modifier = Modifier.weight(1f)
                        )
                        SleekStatTile(
                            label = "MAGNET",
                            value = selectedChar.magnet,
                            max = 5,
                            barColor = Emerald500,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button: Start Game OR Unlock Character
            if (isUnlocked) {
                Button(
                    onClick = { onStartGame(selectedChar) },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_start_run")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Run",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START 4-YEAR JOURNEY",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            } else {
                // Unlock Button
                val canAfford = totalCoins >= selectedChar.unlockCost
                Button(
                    onClick = {
                        if (canAfford) {
                            preferences.totalCoins -= selectedChar.unlockCost
                            preferences.unlockCharacter(selectedChar.id)
                            totalCoins = preferences.totalCoins
                        }
                    },
                    enabled = canAfford,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        disabledContainerColor = Slate300
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_unlock_avatar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canAfford) "UNLOCK FOR ${selectedChar.unlockCost} COINS" else "NEED ${selectedChar.unlockCost} COINS TO UNLOCK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Academic Roadmap Mini Info
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SleekSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoadmapStep(number = "1", label = "Assignments")
                    Text("➔", color = Slate400, fontSize = 12.sp)
                    RoadmapStep(number = "2", label = "Exams")
                    Text("➔", color = Slate400, fontSize = 12.sp)
                    RoadmapStep(number = "3", label = "Projects")
                    Text("➔", color = Slate400, fontSize = 12.sp)
                    RoadmapStep(number = "4", label = "Placements")
                }
            }
        }

        // How To Play Dialog
        if (showHowToPlay) {
            HowToPlayDialog(onDismiss = { showHowToPlay = false })
        }
    }
}

@Composable
private fun CharacterCard(
    profile: CharacterProfile,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SleekSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Indigo600 else Slate200
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
            .testTag("avatar_card_${profile.id.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Character Thumbnail
            Box(
                modifier = Modifier
                    .size(114.dp, 134.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SleekTileBg)
            ) {
                Image(
                    painter = painterResource(id = profile.imageRes),
                    contentDescription = profile.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Lock Overlay if not unlocked
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate900.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Amber400,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${profile.unlockCost} 🪙",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = profile.name,
                color = if (isSelected) Indigo600 else Slate900,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = profile.title,
                color = Slate500,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun SleekStatTile(
    label: String,
    value: Int,
    max: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SleekTileBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Slate500,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Sleek Progress Bar capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Slate200)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (value.toFloat() / max.toFloat()).coerceIn(0f, 1f))
                        .height(6.dp)
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun RoadmapStep(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = SleekTileBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = Indigo600,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Slate600,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GAMEPLAY GUIDE",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "How to Play",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(SleekTileBg, CircleShape)
                    ) {
                        Text("✕", color = Slate700, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                InstructionItem(
                    icon = "🏃",
                    title = "Continuous 4-Year Loop",
                    desc = "Sprint through 4 academic modules: Year 1 (Assignments), Year 2 (Exams), Year 3 (Projects), Year 4 (Internships & Placements)."
                )

                InstructionItem(
                    icon = "⚠️",
                    title = "Avoid the Supply!",
                    desc = "Hitting obstacles like Supply Exam hurdles, unsubmitted assignment piles, or broken project bugs causes a Supply (Arrear) and ends the run!"
                )

                InstructionItem(
                    icon = "🎮",
                    title = "Controls",
                    desc = "Swipe Left / Right to dodge lanes. Swipe UP to Jump over low obstacles. Swipe DOWN to Slide under high banners."
                )

                InstructionItem(
                    icon = "🎓",
                    title = "Become an Engineer!",
                    desc = "Clearing the 4th Year placement drive unlocks the 'Become an Engineer' Graduation screen, awards bonus coins, and loops back to pick new avatars!"
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("GOT IT! LET'S RUN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun InstructionItem(icon: String, title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SleekTileBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = desc,
                    color = Slate600,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

