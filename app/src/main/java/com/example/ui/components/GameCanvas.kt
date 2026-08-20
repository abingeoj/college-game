package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.GameEngine
import com.example.model.AcademicYearStage
import com.example.model.CharacterProfile
import com.example.model.Collectible
import com.example.model.CollectibleType
import com.example.model.GameState
import com.example.model.Obstacle
import com.example.model.ObstacleHeight
import com.example.model.ObstacleType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDragX = 0f
                var totalDragY = 0f
                var isSwipeHandled = false

                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                        isSwipeHandled = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!isSwipeHandled) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            val threshold = 35f
                            if (abs(totalDragX) > threshold || abs(totalDragY) > threshold) {
                                if (abs(totalDragX) > abs(totalDragY)) {
                                    if (totalDragX > 0) {
                                        engine.moveLane(1)
                                    } else {
                                        engine.moveLane(-1)
                                    }
                                } else {
                                    if (totalDragY > 0) {
                                        engine.slide()
                                    } else {
                                        engine.jump()
                                    }
                                }
                                isSwipeHandled = true
                            }
                        }
                    },
                    onDragEnd = {
                        totalDragX = 0f
                        totalDragY = 0f
                        isSwipeHandled = false
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val horizonY = height * 0.40f
        val groundHeight = height - horizonY
        val roadTopWidth = width * 0.22f
        val roadBottomWidth = width * 0.88f
        val roadTopLeft = (width - roadTopWidth) / 2f
        val roadBottomLeft = (width - roadBottomWidth) / 2f

        // 1. Draw Sky & Campus Background
        drawCampusBackground(
            width = width,
            height = height,
            horizonY = horizonY,
            stage = gameState.currentStage,
            distance = gameState.distanceMeters
        )

        // 2. Draw 3D Track & Moving Lane Stripes
        drawRoadTrack(
            width = width,
            horizonY = horizonY,
            roadTopLeft = roadTopLeft,
            roadTopWidth = roadTopWidth,
            roadBottomLeft = roadBottomLeft,
            roadBottomWidth = roadBottomWidth,
            groundHeight = groundHeight,
            distance = gameState.distanceMeters,
            stage = gameState.currentStage
        )

        // 3. Draw Track Objects (Z-Sorted: Far to Near)
        // Transform 3D track coord (lane, zDistance) -> 2D Screen (x, y, scale)
        fun project3D(lane: Float, z: Float): Triple<Float, Float, Float> {
            val clampedZ = z.coerceIn(0.1f, 80f)
            val zRatio = (clampedZ / 65f).coerceIn(0f, 1f)
            // Perspective easing
            val perspective = (1f - zRatio) * (1f - zRatio)
            val y = horizonY + (groundHeight * perspective)
            val currentRoadWidth = roadTopWidth + (roadBottomWidth - roadTopWidth) * perspective
            val currentRoadLeft = (width - currentRoadWidth) / 2f
            val laneWidth = currentRoadWidth / 3f

            val laneCenter = currentRoadLeft + (lane + 1.5f) * laneWidth
            val scale = (0.25f + 0.95f * perspective).coerceIn(0.2f, 1.4f)
            return Triple(laneCenter, y, scale)
        }

        // Draw Collectibles
        for (item in gameState.collectibles) {
            val (screenX, screenY, scale) = project3D(item.lane.toFloat(), item.zDistance)
            if (item.zDistance in 0f..70f) {
                drawCollectibleItem(
                    x = screenX,
                    y = screenY,
                    scale = scale,
                    type = item.type,
                    time = gameState.runTimeSeconds
                )
            }
        }

        // Draw Obstacles
        for (obs in gameState.obstacles) {
            val (screenX, screenY, scale) = project3D(obs.lane.toFloat(), obs.zDistance)
            if (obs.zDistance in 0f..70f) {
                drawObstacleEntity(
                    x = screenX,
                    y = screenY,
                    scale = scale,
                    obstacle = obs,
                    time = gameState.runTimeSeconds
                )
            }
        }

        // 4. Draw Player Runner Avatar
        val (playerScreenX, playerGroundY, playerScale) = project3D(gameState.laneXOffset, 1.8f)
        val jumpOffsetY = gameState.jumpHeight * 180f
        val playerActualY = playerGroundY - jumpOffsetY

        drawRunnerCharacter(
            x = playerScreenX,
            groundY = playerGroundY,
            actualY = playerActualY,
            scale = playerScale,
            character = gameState.selectedCharacter,
            isJumping = gameState.isJumping,
            isSliding = gameState.isSliding,
            slideProgress = gameState.slideProgress,
            jumpProgress = gameState.jumpHeight,
            time = gameState.runTimeSeconds,
            isShielded = gameState.isShielded,
            isSprint = gameState.isInvincibleSprint,
            isMagnet = gameState.isMagnetActive
        )

        // 5. Draw Campus Archway Banners when transitioning between years
        drawYearMilestoneArchway(
            width = width,
            horizonY = horizonY,
            groundHeight = groundHeight,
            distance = gameState.distanceMeters
        )

        // 6. Draw Particles & Sparks
        for (p in engine.particles) {
            val center = Offset(playerScreenX + p.x, playerGroundY + p.y)
            drawCircle(
                color = Color(p.color).copy(alpha = p.alpha),
                radius = p.size,
                center = center
            )
        }

        // 7. Draw Floating Texts (+CGPA, +COIN, etc.)
        for (ft in engine.floatingTexts) {
            val (fx, fy, _) = project3D(ft.x, 2.5f)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 42f
                    isFakeBoldText = true
                    setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
                    alpha = (ft.alpha * 255).toInt().coerceIn(0, 255)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(ft.text, fx, fy + ft.offsetY - 140f, paint)
            }
        }
    }
}

// Background campus environment renderer
private fun DrawScope.drawCampusBackground(
    width: Float,
    height: Float,
    horizonY: Float,
    stage: AcademicYearStage,
    distance: Float
) {
    // Sky gradient tailored to academic stage
    val skyColors = when (stage) {
        AcademicYearStage.YEAR_1 -> listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFF93C5FD))
        AcademicYearStage.YEAR_2 -> listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFDE68A))
        AcademicYearStage.YEAR_3 -> listOf(Color(0xFF3B0764), Color(0xFF7C3AED), Color(0xFFC4B5FD))
        AcademicYearStage.YEAR_4 -> listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF6EE7B7))
    }

    drawRect(
        brush = Brush.verticalGradient(skyColors, startY = 0f, endY = horizonY),
        size = Size(width, horizonY)
    )

    // Distant Campus Buildings & University Towers
    val buildingWidth = width * 0.16f
    for (i in 0..7) {
        val bX = (i * buildingWidth * 0.9f - (distance * 0.3f) % (width + buildingWidth))
        val bHeight = (70f + (i % 3) * 45f)
        val bY = horizonY - bHeight

        // Brick academic buildings with windows
        drawRoundRect(
            color = if (i % 2 == 0) Color(0xFF334155) else Color(0xFF1E293B),
            topLeft = Offset(bX, bY),
            size = Size(buildingWidth * 0.85f, bHeight + 10f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Windows
        for (row in 0..3) {
            for (col in 0..2) {
                val winX = bX + 12f + (col * 18f)
                val winY = bY + 12f + (row * 15f)
                if (winY < horizonY - 10f) {
                    drawRect(
                        color = Color(0xFFFEF08A).copy(alpha = 0.6f),
                        topLeft = Offset(winX, winY),
                        size = Size(10f, 8f)
                    )
                }
            }
        }
    }

    // Lush Campus Green Lawns on sides of road
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF15803D), Color(0xFF166534), Color(0xFF14532D)),
            startY = horizonY,
            endY = height
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(width, height - horizonY)
    )
}

// 3D Road Track & Dynamic Road Markings
private fun DrawScope.drawRoadTrack(
    width: Float,
    horizonY: Float,
    roadTopLeft: Float,
    roadTopWidth: Float,
    roadBottomLeft: Float,
    roadBottomWidth: Float,
    groundHeight: Float,
    distance: Float,
    stage: AcademicYearStage
) {
    // Main asphalt road trapezoid
    val roadPath = Path().apply {
        moveTo(roadTopLeft, horizonY)
        lineTo(roadTopLeft + roadTopWidth, horizonY)
        lineTo(roadBottomLeft + roadBottomWidth, horizonY + groundHeight)
        lineTo(roadBottomLeft, horizonY + groundHeight)
        close()
    }

    drawPath(
        path = roadPath,
        brush = Brush.verticalGradient(
            listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
            startY = horizonY,
            endY = horizonY + groundHeight
        )
    )

    // Curbs / Sidewalk strips (Red/White or Stage Colors)
    val curbWidthTop = 6f
    val curbWidthBottom = 22f

    // Left Curb
    val leftCurb = Path().apply {
        moveTo(roadTopLeft - curbWidthTop, horizonY)
        lineTo(roadTopLeft, horizonY)
        lineTo(roadBottomLeft, horizonY + groundHeight)
        lineTo(roadBottomLeft - curbWidthBottom, horizonY + groundHeight)
        close()
    }
    drawPath(path = leftCurb, color = Color(stage.themeColor))

    // Right Curb
    val rightCurb = Path().apply {
        moveTo(roadTopLeft + roadTopWidth, horizonY)
        lineTo(roadTopLeft + roadTopWidth + curbWidthTop, horizonY)
        lineTo(roadBottomLeft + roadBottomWidth + curbWidthBottom, horizonY + groundHeight)
        lineTo(roadBottomLeft + roadBottomWidth, horizonY + groundHeight)
        close()
    }
    drawPath(path = rightCurb, color = Color(stage.themeColor))

    // 2 White dashed lane divider lines
    for (laneDivider in 1..2) {
        val dividerFraction = laneDivider / 3f
        val topX = roadTopLeft + roadTopWidth * dividerFraction
        val bottomX = roadBottomLeft + roadBottomWidth * dividerFraction

        for (seg in 0..9) {
            val offsetZ = ((seg * 7f) - (distance % 7f)) / 63f
            if (offsetZ in 0f..1f) {
                val p = (1f - offsetZ) * (1f - offsetZ)
                val y1 = horizonY + (groundHeight * p)
                val p2 = (1f - (offsetZ + 0.05f).coerceAtMost(1f)) * (1f - (offsetZ + 0.05f).coerceAtMost(1f))
                val y2 = horizonY + (groundHeight * p2)

                val x1 = topX + (bottomX - topX) * p
                val x2 = topX + (bottomX - topX) * p2

                drawLine(
                    color = Color.White.copy(alpha = (p * 0.8f).coerceIn(0.2f, 0.9f)),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = (2f + 5f * p)
                )
            }
        }
    }
}

// Collectible items renderer
private fun DrawScope.drawCollectibleItem(
    x: Float,
    y: Float,
    scale: Float,
    type: CollectibleType,
    time: Float
) {
    val size = 28f * scale
    val floatOffset = sin(time * 6f + x) * 6f * scale
    val centerY = y - size - floatOffset

    when (type) {
        CollectibleType.COIN -> {
            // Golden star coin with 3D wobble
            val spinScaleX = cos(time * 5f).coerceIn(-1f, 1f)
            val coinWidth = size * abs(spinScaleX).coerceAtLeast(0.25f)

            // Outer gold ring
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFCA8A04)),
                    center = Offset(x, centerY),
                    radius = size
                ),
                topLeft = Offset(x - coinWidth / 2f, centerY - size / 2f),
                size = Size(coinWidth, size)
            )
            // Star center
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = size * 0.28f * abs(spinScaleX).coerceAtLeast(0.2f),
                center = Offset(x, centerY)
            )
        }

        CollectibleType.TEXTBOOK -> {
            // 3D Hardcover Engineering Textbook
            val bookWidth = size * 1.3f
            val bookHeight = size * 0.9f
            drawRoundRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(x - bookWidth / 2f, centerY - bookHeight / 2f),
                size = Size(bookWidth, bookHeight),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // Pages stripe
            drawRect(
                color = Color(0xFFF8FAFC),
                topLeft = Offset(x - bookWidth / 2f + 4f * scale, centerY - bookHeight / 2f + 3f * scale),
                size = Size(bookWidth - 8f * scale, bookHeight - 6f * scale)
            )
            // Bookmark ribbon
            drawRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(x - 2f * scale, centerY - bookHeight / 2f),
                size = Size(4f * scale, bookHeight)
            )
        }

        CollectibleType.COFFEE_MUG -> {
            // White mug with coffee & steam
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                topLeft = Offset(x - size * 0.4f, centerY - size * 0.45f),
                size = Size(size * 0.8f, size * 0.9f),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // Steam
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(x, centerY - size * 0.5f),
                end = Offset(x + sin(time * 8f) * 4f, centerY - size * 0.9f),
                strokeWidth = 2.5f * scale
            )
        }

        CollectibleType.TROPHY -> {
            // Golden Hackathon Winner Trophy Cup
            val cupWidth = size * 1.2f
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = size * 0.45f,
                center = Offset(x, centerY - size * 0.2f)
            )
            drawRect(
                color = Color(0xFFB45309),
                topLeft = Offset(x - size * 0.35f, centerY + size * 0.25f),
                size = Size(size * 0.7f, size * 0.3f)
            )
        }

        CollectibleType.OFFER_LETTER -> {
            // Formal Offer Letter with green seal
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(x - size * 0.6f, centerY - size * 0.5f),
                size = Size(size * 1.2f, size * 1.0f),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = size * 0.25f,
                center = Offset(x, centerY)
            )
        }

        CollectibleType.MAGNET -> {
            // Red & Blue Magnet
            drawArc(
                color = Color(0xFFEF4444),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - size * 0.5f, centerY - size * 0.5f),
                size = Size(size, size),
                style = Stroke(width = 6f * scale)
            )
            // Magnetic pulse ring
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = (sin(time * 10f) * 0.4f + 0.4f)),
                radius = size * 0.8f,
                center = Offset(x, centerY),
                style = Stroke(width = 2f * scale)
            )
        }

        CollectibleType.SHIELD -> {
            // Glowing cyan protective shield
            val shieldPath = Path().apply {
                moveTo(x, centerY - size * 0.6f)
                lineTo(x + size * 0.6f, centerY - size * 0.2f)
                lineTo(x + size * 0.45f, centerY + size * 0.5f)
                lineTo(x, centerY + size * 0.75f)
                lineTo(x - size * 0.45f, centerY + size * 0.5f)
                lineTo(x - size * 0.6f, centerY - size * 0.2f)
                close()
            }
            drawPath(path = shieldPath, color = Color(0xFF10B981).copy(alpha = 0.85f))
            drawPath(path = shieldPath, color = Color(0xFF34D399), style = Stroke(width = 2.5f * scale))
        }

        CollectibleType.BOOST_SNEAKER -> {
            // Golden running shoe with wing streaks
            drawRoundRect(
                color = Color(0xFFF43F5E),
                topLeft = Offset(x - size * 0.6f, centerY - size * 0.3f),
                size = Size(size * 1.2f, size * 0.6f),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            // Speed trail lines
            drawLine(
                color = Color(0xFFFDE047),
                start = Offset(x - size * 0.8f, centerY),
                end = Offset(x - size * 0.3f, centerY),
                strokeWidth = 3f * scale
            )
        }
    }
}

// Obstacles renderer with distinct academic cues
private fun DrawScope.drawObstacleEntity(
    x: Float,
    y: Float,
    scale: Float,
    obstacle: Obstacle,
    time: Float
) {
    val baseWidth = 50f * scale
    val baseHeight = 45f * scale

    when (obstacle.heightType) {
        ObstacleHeight.LOW -> {
            // Low obstacle placed on the ground -> Jump over!
            val objY = y - baseHeight * 0.7f

            when (obstacle.type) {
                ObstacleType.ASSIGNMENT_STACK -> {
                    // Huge stack of assignment folders with "DEADLINE" tag
                    for (i in 0..2) {
                        val stackY = objY - (i * 9f * scale)
                        val folderColor = when (i) {
                            0 -> Color(0xFFDC2626)
                            1 -> Color(0xFF2563EB)
                            else -> Color(0xFFEAB308)
                        }
                        drawRoundRect(
                            color = folderColor,
                            topLeft = Offset(x - baseWidth * 0.55f, stackY),
                            size = Size(baseWidth * 1.1f, 14f * scale),
                            cornerRadius = CornerRadius(3f * scale, 3f * scale)
                        )
                    }
                    // Yellow Jump Indicator Arrow pointing UP
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = 8f * scale,
                        center = Offset(x, objY - 24f * scale)
                    )
                }

                ObstacleType.BACKPACK_DROP -> {
                    // Fallen blue backpack with red caution light
                    drawRoundRect(
                        color = Color(0xFF1E3A8A),
                        topLeft = Offset(x - baseWidth * 0.45f, objY),
                        size = Size(baseWidth * 0.9f, baseHeight * 0.8f),
                        cornerRadius = CornerRadius(8f * scale, 8f * scale)
                    )
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(x - baseWidth * 0.2f, objY + 4f * scale),
                        size = Size(baseWidth * 0.4f, 8f * scale)
                    )
                }

                ObstacleType.PROJECT_KIT -> {
                    // Arduino / Breadboard circuit with electric sparks
                    drawRect(
                        color = Color(0xFF047857),
                        topLeft = Offset(x - baseWidth * 0.5f, objY),
                        size = Size(baseWidth, baseHeight * 0.5f)
                    )
                    // Sparks
                    val sparkX = x + sin(time * 20f) * 12f * scale
                    drawCircle(
                        color = Color(0xFF38BDF8),
                        radius = 5f * scale,
                        center = Offset(sparkX, objY - 4f * scale)
                    )
                }

                else -> {
                    // Campus Safety Cone
                    val conePath = Path().apply {
                        moveTo(x, objY - baseHeight * 0.6f)
                        lineTo(x + baseWidth * 0.4f, objY + baseHeight * 0.4f)
                        lineTo(x - baseWidth * 0.4f, objY + baseHeight * 0.4f)
                        close()
                    }
                    drawPath(path = conePath, color = Color(0xFFEA580C))
                    drawLine(
                        color = Color.White,
                        start = Offset(x - baseWidth * 0.2f, objY),
                        end = Offset(x + baseWidth * 0.2f, objY),
                        strokeWidth = 4f * scale
                    )
                }
            }
        }

        ObstacleHeight.HIGH -> {
            // High obstacle positioned overhead -> Slide under!
            val overheadY = y - 110f * scale
            val poleHeight = 110f * scale

            // Left and Right Support Poles
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(x - baseWidth * 0.6f, y),
                end = Offset(x - baseWidth * 0.6f, overheadY),
                strokeWidth = 5f * scale
            )
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(x + baseWidth * 0.6f, y),
                end = Offset(x + baseWidth * 0.6f, overheadY),
                strokeWidth = 5f * scale
            )

            // Overhead Banner / Barrier Bar
            val bannerColor = if (obstacle.type == ObstacleType.VIVA_SCANNER) Color(0xFFDC2626) else Color(0xFF7C3AED)
            drawRoundRect(
                color = bannerColor,
                topLeft = Offset(x - baseWidth * 0.7f, overheadY - 15f * scale),
                size = Size(baseWidth * 1.4f, 30f * scale),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )

            // Caution stripes
            for (i in 0..4) {
                val stripeX = x - baseWidth * 0.6f + (i * 18f * scale)
                drawLine(
                    color = Color(0xFFFEF08A),
                    start = Offset(stripeX, overheadY - 12f * scale),
                    end = Offset(stripeX + 10f * scale, overheadY + 12f * scale),
                    strokeWidth = 3f * scale
                )
            }
        }

        ObstacleHeight.FULL -> {
            // Full Lane Blocker -> Must Dodge to Left or Right!
            val objY = y - baseHeight * 1.4f

            when (obstacle.type) {
                ObstacleType.SUPPLY_EXAM_BOARD -> {
                    // DREADED SUPPLY / ARREAR EXAM HURDLE
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFEF4444), Color(0xFF991B1B)),
                            startY = objY,
                            endY = objY + baseHeight * 1.4f
                        ),
                        topLeft = Offset(x - baseWidth * 0.65f, objY),
                        size = Size(baseWidth * 1.3f, baseHeight * 1.4f),
                        cornerRadius = CornerRadius(6f * scale, 6f * scale)
                    )
                    // Warning border
                    drawRoundRect(
                        color = Color(0xFFFEF08A),
                        topLeft = Offset(x - baseWidth * 0.65f, objY),
                        size = Size(baseWidth * 1.3f, baseHeight * 1.4f),
                        cornerRadius = CornerRadius(6f * scale, 6f * scale),
                        style = Stroke(width = 3f * scale)
                    )
                    // Skull / Cross alert icon
                    drawLine(
                        color = Color.White,
                        start = Offset(x - 12f * scale, objY + 15f * scale),
                        end = Offset(x + 12f * scale, objY + 39f * scale),
                        strokeWidth = 4f * scale
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(x + 12f * scale, objY + 15f * scale),
                        end = Offset(x - 12f * scale, objY + 39f * scale),
                        strokeWidth = 4f * scale
                    )
                }

                ObstacleType.COLLEGE_BUS -> {
                    // Yellow College Bus
                    drawRoundRect(
                        color = Color(0xFFEAB308),
                        topLeft = Offset(x - baseWidth * 0.65f, objY),
                        size = Size(baseWidth * 1.3f, baseHeight * 1.5f),
                        cornerRadius = CornerRadius(8f * scale, 8f * scale)
                    )
                    // Windshield
                    drawRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(x - baseWidth * 0.5f, objY + 8f * scale),
                        size = Size(baseWidth * 1.0f, baseHeight * 0.45f)
                    )
                    // Headlights
                    drawCircle(
                        color = Color(0xFFFEF08A),
                        radius = 5f * scale,
                        center = Offset(x - baseWidth * 0.45f, objY + baseHeight * 1.1f)
                    )
                    drawCircle(
                        color = Color(0xFFFEF08A),
                        radius = 5f * scale,
                        center = Offset(x + baseWidth * 0.45f, objY + baseHeight * 1.1f)
                    )
                }

                else -> {
                    // Strict Invigilator / Barrier
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(x - baseWidth * 0.55f, objY),
                        size = Size(baseWidth * 1.1f, baseHeight * 1.4f),
                        cornerRadius = CornerRadius(6f * scale, 6f * scale)
                    )
                    drawRect(
                        color = Color(0xFFDC2626),
                        topLeft = Offset(x - baseWidth * 0.45f, objY + 10f * scale),
                        size = Size(baseWidth * 0.9f, 8f * scale)
                    )
                }
            }
        }
    }
}

// 3D Animated Runner Character Renderer matching user photos
private fun DrawScope.drawRunnerCharacter(
    x: Float,
    groundY: Float,
    actualY: Float,
    scale: Float,
    character: CharacterProfile,
    isJumping: Boolean,
    isSliding: Boolean,
    slideProgress: Float,
    jumpProgress: Float,
    time: Float,
    isShielded: Boolean,
    isSprint: Boolean,
    isMagnet: Boolean
) {
    val charScale = scale * 1.15f

    // 1. Ground Shadow (Shrinks and fades as character jumps)
    val shadowAlpha = if (isJumping) (0.6f - jumpProgress * 0.4f).coerceIn(0.1f, 0.6f) else 0.6f
    val shadowWidth = (44f * charScale) * (if (isJumping) (1f - jumpProgress * 0.4f) else if (isSliding) 1.5f else 1.0f)
    val shadowHeight = 14f * charScale

    drawOval(
        color = Color.Black.copy(alpha = shadowAlpha),
        topLeft = Offset(x - shadowWidth / 2f, groundY - shadowHeight / 2f),
        size = Size(shadowWidth, shadowHeight)
    )

    // 2. Power-up Auras (Shield, Sprint, Magnet)
    if (isShielded) {
        drawCircle(
            color = Color(0xFF10B981).copy(alpha = 0.35f + sin(time * 12f) * 0.15f),
            radius = 58f * charScale,
            center = Offset(x, actualY - 50f * charScale)
        )
        drawCircle(
            color = Color(0xFF34D399),
            radius = 58f * charScale,
            center = Offset(x, actualY - 50f * charScale),
            style = Stroke(width = 3f * charScale)
        )
    }

    if (isSprint) {
        // Rainbow motion trails
        for (trail in 1..3) {
            val trailY = actualY + (trail * 14f * charScale)
            drawOval(
                color = Color(0xFFF43F5E).copy(alpha = 0.3f / trail),
                topLeft = Offset(x - 30f * charScale, trailY - 40f * charScale),
                size = Size(60f * charScale, 70f * charScale)
            )
        }
    }

    if (isMagnet) {
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.25f),
            radius = 65f * charScale,
            center = Offset(x, actualY - 50f * charScale),
            style = Stroke(width = 2f * charScale, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), time * 30f))
        )
    }

    // 3. Draw Character Geometry
    val runCycle = time * 18f
    val legAngle = if (isJumping) 0.3f else if (isSliding) 1.2f else sin(runCycle) * 0.65f
    val armAngle = if (isJumping) -0.8f else if (isSliding) -1.0f else -sin(runCycle) * 0.65f
    val torsoBob = if (isJumping) 0f else if (isSliding) 25f * charScale else abs(sin(runCycle)) * 4f * charScale

    val rootY = actualY + torsoBob

    if (isSliding) {
        // Sliding Pose (Leaned low backward / forward slide)
        val slideY = actualY + 25f * charScale

        // Pants / Legs extended
        drawLine(
            color = Color(character.pantsColor),
            start = Offset(x - 10f * charScale, slideY),
            end = Offset(x + 35f * charScale, slideY + 12f * charScale),
            strokeWidth = 14f * charScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // White sneakers sliding
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(x + 28f * charScale, slideY + 5f * charScale),
            size = Size(20f * charScale, 10f * charScale),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Torso leaning low
        drawRoundRect(
            color = Color(character.shirtColor),
            topLeft = Offset(x - 30f * charScale, slideY - 20f * charScale),
            size = Size(35f * charScale, 24f * charScale),
            cornerRadius = CornerRadius(6f * charScale, 6f * charScale)
        )

        // Head
        val headCenter = Offset(x - 32f * charScale, slideY - 32f * charScale)
        drawHeadAndAccessories(headCenter, charScale, character)

        // Sliding dust particles
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(x - 40f * charScale, groundY - 4f),
            end = Offset(x - 15f * charScale, groundY - 4f),
            strokeWidth = 3f * charScale
        )
    } else {
        // Standing / Running Pose

        // --- BACKPACK (Behind torso) ---
        drawRoundRect(
            color = Color(character.backpackColor),
            topLeft = Offset(x - 18f * charScale, rootY - 78f * charScale),
            size = Size(36f * charScale, 36f * charScale),
            cornerRadius = CornerRadius(8f * charScale, 8f * charScale)
        )

        // --- LEGS ---
        val hipY = rootY - 42f * charScale
        val leftLegEndX = x - 12f * charScale + sin(legAngle) * 18f * charScale
        val leftLegEndY = rootY + cos(legAngle).coerceAtLeast(0f) * 10f * charScale
        val rightLegEndX = x + 12f * charScale - sin(legAngle) * 18f * charScale
        val rightLegEndY = rootY + cos(-legAngle).coerceAtLeast(0f) * 10f * charScale

        // Left Leg & Sneaker
        drawLine(
            color = Color(character.pantsColor),
            start = Offset(x - 8f * charScale, hipY),
            end = Offset(leftLegEndX, leftLegEndY),
            strokeWidth = 11f * charScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(leftLegEndX - 8f * charScale, leftLegEndY - 4f * charScale),
            size = Size(16f * charScale, 9f * charScale),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Right Leg & Sneaker
        drawLine(
            color = Color(character.pantsColor),
            start = Offset(x + 8f * charScale, hipY),
            end = Offset(rightLegEndX, rightLegEndY),
            strokeWidth = 11f * charScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(rightLegEndX - 8f * charScale, rightLegEndY - 4f * charScale),
            size = Size(16f * charScale, 9f * charScale),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // --- TORSO / SHIRT ---
        val torsoTopY = rootY - 80f * charScale
        val torsoHeight = 40f * charScale
        val torsoWidth = 32f * charScale

        drawRoundRect(
            color = Color(character.shirtColor),
            topLeft = Offset(x - torsoWidth / 2f, torsoTopY),
            size = Size(torsoWidth, torsoHeight),
            cornerRadius = CornerRadius(8f * charScale, 8f * charScale)
        )

        // Shirt Collar / V-neck detail
        val collarPath = Path().apply {
            moveTo(x - 8f * charScale, torsoTopY)
            lineTo(x, torsoTopY + 12f * charScale)
            lineTo(x + 8f * charScale, torsoTopY)
        }
        drawPath(
            path = collarPath,
            color = Color(0xFFE2E8F0),
            style = Stroke(width = 2.5f * charScale)
        )

        // Belt
        drawRect(
            color = Color(0xFF475569),
            topLeft = Offset(x - torsoWidth / 2f, torsoTopY + torsoHeight - 5f * charScale),
            size = Size(torsoWidth, 5f * charScale)
        )

        // --- ARMS ---
        val shoulderY = torsoTopY + 8f * charScale
        val leftArmEndX = x - 22f * charScale + sin(armAngle) * 16f * charScale
        val leftArmEndY = shoulderY + 22f * charScale + cos(armAngle) * 8f * charScale
        val rightArmEndX = x + 22f * charScale - sin(armAngle) * 16f * charScale
        val rightArmEndY = shoulderY + 22f * charScale + cos(-armAngle) * 8f * charScale

        // Left Arm
        drawLine(
            color = Color(character.shirtColor),
            start = Offset(x - 14f * charScale, shoulderY),
            end = Offset(leftArmEndX, leftArmEndY),
            strokeWidth = 8f * charScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Left Hand
        drawCircle(
            color = Color(0xFFD4A373),
            radius = 4.5f * charScale,
            center = Offset(leftArmEndX, leftArmEndY)
        )

        // Right Arm
        drawLine(
            color = Color(character.shirtColor),
            start = Offset(x + 14f * charScale, shoulderY),
            end = Offset(rightArmEndX, rightArmEndY),
            strokeWidth = 8f * charScale,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // Right Hand
        drawCircle(
            color = Color(0xFFD4A373),
            radius = 4.5f * charScale,
            center = Offset(rightArmEndX, rightArmEndY)
        )

        // --- HEAD & ACCESSORIES ---
        val headCenter = Offset(x, torsoTopY - 18f * charScale)
        drawHeadAndAccessories(headCenter, charScale, character)
    }
}

// Head, Stylized Hair, Spectacles/Sunglasses, Mustache, and Cap
private fun DrawScope.drawHeadAndAccessories(
    center: Offset,
    scale: Float,
    character: CharacterProfile
) {
    val hX = center.x
    val hY = center.y
    val headRadius = 16f * scale

    // Face Skin
    drawCircle(
        color = Color(0xFFD4A373),
        radius = headRadius,
        center = center
    )

    // Stylish Wavy Black Hair
    drawArc(
        color = Color(0xFF18181B),
        startAngle = 160f,
        sweepAngle = 220f,
        useCenter = true,
        topLeft = Offset(hX - headRadius * 1.15f, hY - headRadius * 1.35f),
        size = Size(headRadius * 2.3f, headRadius * 2.1f)
    )

    // Baseball Cap (If character YouTuber)
    if (character.hasCap) {
        drawRoundRect(
            color = Color(0xFFD97706),
            topLeft = Offset(hX - headRadius * 1.1f, hY - headRadius * 1.2f),
            size = Size(headRadius * 2.2f, headRadius * 0.9f),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )
        // Cap Visor
        drawRoundRect(
            color = Color(0xFFB45309),
            topLeft = Offset(hX - headRadius * 0.4f, hY - headRadius * 0.6f),
            size = Size(headRadius * 1.8f, 6f * scale),
            cornerRadius = CornerRadius(3f * scale, 3f * scale)
        )
    }

    // Sunglasses (Srihari) or Glasses (Rino / Aditya)
    if (character.hasSunglasses) {
        // Dark aviator shades
        drawRoundRect(
            color = Color(0xFF09090B),
            topLeft = Offset(hX - headRadius * 0.85f, hY - headRadius * 0.2f),
            size = Size(headRadius * 0.75f, headRadius * 0.55f),
            cornerRadius = CornerRadius(3f * scale, 3f * scale)
        )
        drawRoundRect(
            color = Color(0xFF09090B),
            topLeft = Offset(hX + headRadius * 0.1f, hY - headRadius * 0.2f),
            size = Size(headRadius * 0.75f, headRadius * 0.55f),
            cornerRadius = CornerRadius(3f * scale, 3f * scale)
        )
        // Bridge
        drawLine(
            color = Color(0xFFF59E0B),
            start = Offset(hX - 2f * scale, hY),
            end = Offset(hX + 2f * scale, hY),
            strokeWidth = 2f * scale
        )
    } else if (character.hasGlasses) {
        // Spectacles
        val frameColor = if (character.id == com.example.model.CharacterId.CHASER) Color(0xFF2563EB) else Color(0xFF38BDF8)
        drawCircle(
            color = frameColor,
            radius = headRadius * 0.35f,
            center = Offset(hX - headRadius * 0.45f, hY),
            style = Stroke(width = 2.5f * scale)
        )
        drawCircle(
            color = frameColor,
            radius = headRadius * 0.35f,
            center = Offset(hX + headRadius * 0.45f, hY),
            style = Stroke(width = 2.5f * scale)
        )
        drawLine(
            color = frameColor,
            start = Offset(hX - headRadius * 0.1f, hY),
            end = Offset(hX + headRadius * 0.1f, hY),
            strokeWidth = 2f * scale
        )
    }

    // Neat Mustache
    if (character.hasMustache) {
        drawRoundRect(
            color = Color(0xFF27272A),
            topLeft = Offset(hX - headRadius * 0.45f, hY + headRadius * 0.35f),
            size = Size(headRadius * 0.9f, 4f * scale),
            cornerRadius = CornerRadius(2f * scale, 2f * scale)
        )
    }
}

// Grand Milestone Campus Archways (e.g. "COLLEGE CAMPUS", "SEMESTER EXAMS", "PLACEMENTS")
private fun DrawScope.drawYearMilestoneArchway(
    width: Float,
    horizonY: Float,
    groundHeight: Float,
    distance: Float
) {
    val milestones = listOf(
        0f to "COLLEGE CAMPUS",
        1200f to "SEMESTER 2 EXAM HALL",
        2600f to "CAPSTONE INNOVATION LABS",
        4200f to "CAMPUS PLACEMENT DRIVE"
    )

    for ((mDist, bannerTitle) in milestones) {
        val relDist = mDist - distance
        if (relDist in -10f..40f) {
            val zRatio = (relDist / 40f).coerceIn(0f, 1f)
            val p = (1f - zRatio) * (1f - zRatio)
            val archY = horizonY + (groundHeight * p) - (140f * p)
            val archWidth = width * (0.35f + 0.55f * p)
            val archLeft = (width - archWidth) / 2f

            // Grand Arch Banner
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(archLeft, archY),
                size = Size(archWidth, 38f * (0.4f + 0.6f * p)),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFFF59E0B),
                topLeft = Offset(archLeft, archY),
                size = Size(archWidth, 38f * (0.4f + 0.6f * p)),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2.5f)
            )

            // Text on banner
            if (p > 0.4f) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f * p
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(bannerTitle, width / 2f, archY + (26f * p), paint)
                }
            }
        }
    }
}
