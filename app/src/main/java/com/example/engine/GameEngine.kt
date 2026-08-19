package com.example.engine

import com.example.data.GamePreferences
import com.example.model.AcademicYearStage
import com.example.model.ActiveBuff
import com.example.model.ActiveBuffType
import com.example.model.CharacterProfile
import com.example.model.Collectible
import com.example.model.CollectibleType
import com.example.model.FloatingText
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.Obstacle
import com.example.model.ObstacleHeight
import com.example.model.ObstacleType
import com.example.model.Particle
import com.example.model.SpecialAbility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine(
    private val preferences: GamePreferences,
    val soundSynthesizer: SoundSynthesizer
) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var nextObstacleId = 1L
    private var nextCollectibleId = 1L
    private var nextFloatingTextId = 1L

    private var jumpVelocity = 0f
    private var slideTimer = 0f
    private val slideDuration = 0.85f

    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    private var nextSpawnZ = 30f
    private var lastMilestoneAnnounced = AcademicYearStage.YEAR_1

    init {
        // Load default character
        val initialChar = CharacterProfile.ALL_CHARACTERS[0]
        _gameState.update {
            it.copy(
                selectedCharacter = initialChar,
                coins = preferences.totalCoins,
                totalGraduationsCount = preferences.totalGraduations
            )
        }
    }

    fun selectCharacter(character: CharacterProfile) {
        _gameState.update { it.copy(selectedCharacter = character) }
    }

    fun startGame(character: CharacterProfile = _gameState.value.selectedCharacter) {
        jumpVelocity = 0f
        slideTimer = 0f
        nextSpawnZ = 35f
        lastMilestoneAnnounced = AcademicYearStage.YEAR_1
        particles.clear()
        floatingTexts.clear()

        val baseSpeed = 13f + (character.speed * 0.7f)

        _gameState.value = GameState(
            phase = GamePhase.RUNNING,
            selectedCharacter = character,
            currentLane = 0,
            targetLane = 0,
            laneXOffset = 0f,
            jumpHeight = 0f,
            isJumping = false,
            isSliding = false,
            distanceMeters = 0f,
            score = 0,
            coins = 0,
            cgpa = 8.5f,
            comboMultiplier = 1,
            currentStage = AcademicYearStage.YEAR_1,
            obstacles = emptyList(),
            collectibles = emptyList(),
            activeBuffs = emptyList(),
            baseSpeed = baseSpeed,
            currentSpeed = baseSpeed,
            specialPowerEnergy = 1.0f,
            totalGraduationsCount = preferences.totalGraduations
        )

        // Seed initial friendly items on track
        seedInitialTrack()
    }

    private fun seedInitialTrack() {
        val initialCollectibles = mutableListOf<Collectible>()
        // Spawn 3 coin rows
        for (i in 0 until 5) {
            val z = 20f + (i * 3f)
            initialCollectibles.add(
                Collectible(
                    id = nextCollectibleId++,
                    type = CollectibleType.COIN,
                    lane = 0,
                    zDistance = z
                )
            )
        }
        _gameState.update { it.copy(collectibles = initialCollectibles) }
    }

    fun pauseGame() {
        if (_gameState.value.phase == GamePhase.RUNNING) {
            _gameState.update { it.copy(phase = GamePhase.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.phase == GamePhase.PAUSED) {
            _gameState.update { it.copy(phase = GamePhase.RUNNING) }
        }
    }

    fun returnToCharacterSelect() {
        _gameState.update {
            it.copy(
                phase = GamePhase.CHARACTER_SELECT,
                obstacles = emptyList(),
                collectibles = emptyList(),
                activeBuffs = emptyList(),
                totalGraduationsCount = preferences.totalGraduations
            )
        }
    }

    // Input actions
    fun moveLane(direction: Int) {
        val state = _gameState.value
        if (state.phase != GamePhase.RUNNING) return

        val newLane = (state.targetLane + direction).coerceIn(-1, 1)
        if (newLane != state.targetLane) {
            _gameState.update { it.copy(targetLane = newLane) }
            soundSynthesizer.playLaneChange()
        }
    }

    fun jump() {
        val state = _gameState.value
        if (state.phase != GamePhase.RUNNING) return

        if (!state.isJumping) {
            jumpVelocity = 4.6f
            slideTimer = 0f // cancel slide if jumping
            _gameState.update {
                it.copy(
                    isJumping = true,
                    isSliding = false,
                    slideProgress = 0f
                )
            }
            soundSynthesizer.playJump()
            soundSynthesizer.vibrate(20)
        }
    }

    fun slide() {
        val state = _gameState.value
        if (state.phase != GamePhase.RUNNING) return

        slideTimer = slideDuration
        // Quick drop if currently in mid-air
        if (state.isJumping) {
            jumpVelocity = -6.0f
        }
        _gameState.update {
            it.copy(
                isSliding = true,
                slideProgress = 1f
            )
        }
        soundSynthesizer.playSlide()
        soundSynthesizer.vibrate(20)
    }

    fun activateSpecialPower() {
        val state = _gameState.value
        if (state.phase != GamePhase.RUNNING || !state.isSpecialPowerReady) return

        soundSynthesizer.playPowerUp()
        soundSynthesizer.vibrate(60)

        val character = state.selectedCharacter
        val newBuffs = state.activeBuffs.toMutableList()

        when (character.ability) {
            SpecialAbility.SWAGGER_DASH -> {
                // High speed & invincibility dash
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.BOOST_SPRINT,
                        name = "Swagger Dash",
                        remainingTimeSeconds = 8.0f,
                        totalDurationSeconds = 8.0f
                    )
                )
            }
            SpecialAbility.SCHOOL_DASH -> {
                // Super speed & magnet
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.BOOST_SPRINT,
                        name = "School Dash",
                        remainingTimeSeconds = 7.0f,
                        totalDurationSeconds = 7.0f
                    )
                )
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.MAGNET,
                        name = "Book Magnet",
                        remainingTimeSeconds = 7.0f,
                        totalDurationSeconds = 7.0f
                    )
                )
            }
            SpecialAbility.TROPHY_BOOST -> {
                // Giant coin magnet + bonus score
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.MAGNET,
                        name = "Trophy Magnet Aura",
                        remainingTimeSeconds = 10.0f,
                        totalDurationSeconds = 10.0f
                    )
                )
            }
            SpecialAbility.STUDY_BOOST -> {
                // Shield & Study magnet
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.SHIELD,
                        name = "Study Shield",
                        remainingTimeSeconds = 12.0f,
                        totalDurationSeconds = 12.0f
                    )
                )
                newBuffs.add(
                    ActiveBuff(
                        type = ActiveBuffType.MAGNET,
                        name = "Study Magnet",
                        remainingTimeSeconds = 10.0f,
                        totalDurationSeconds = 10.0f
                    )
                )
            }
        }

        _gameState.update {
            it.copy(
                specialPowerEnergy = 0f,
                isSpecialPowerActive = true,
                activeBuffs = newBuffs
            )
        }
    }

    // Main Update Tick (Called every frame at ~60fps)
    fun update(dt: Float) {
        val state = _gameState.value
        if (state.phase != GamePhase.RUNNING) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // 1. Update Distance and Current Academic Year Stage
        val speedModifier = if (state.isInvincibleSprint) 1.4f else 1.0f
        val stageSpeedBonus = when (state.currentStage) {
            AcademicYearStage.YEAR_1 -> 0f
            AcademicYearStage.YEAR_2 -> 2.5f
            AcademicYearStage.YEAR_3 -> 5.0f
            AcademicYearStage.YEAR_4 -> 7.5f
        }
        val targetSpeed = (state.baseSpeed + stageSpeedBonus) * speedModifier
        val effectiveSpeed = state.currentSpeed + (targetSpeed - state.currentSpeed) * (5f * clampedDt)

        val traveled = effectiveSpeed * clampedDt
        val newDistance = state.distanceMeters + traveled
        val newStage = AcademicYearStage.getStageForDistance(newDistance)

        // Check Year Promotion
        if (newStage != lastMilestoneAnnounced) {
            lastMilestoneAnnounced = newStage
            soundSynthesizer.playPowerUp()
            addFloatingText(newStage.bannerText, 0f, 0xFFF59E0B)
        }

        // Check Graduation (Completion of 4 Years = 6000m)
        if (newDistance >= AcademicYearStage.TOTAL_GRADUATION_DISTANCE) {
            triggerGraduation(state, newDistance)
            return
        }

        // 2. Update Lane Lerp
        val laneLerpSpeed = 14f * clampedDt
        val targetX = state.targetLane.toFloat()
        val currentX = state.laneXOffset
        val newLaneX = currentX + (targetX - currentX) * laneLerpSpeed.coerceAtMost(1f)
        val currentDiscreteLane = when {
            newLaneX < -0.4f -> -1
            newLaneX > 0.4f -> 1
            else -> 0
        }

        // 3. Update Jump Physics
        var newJumpHeight = state.jumpHeight
        var isJumping = state.isJumping
        if (isJumping) {
            newJumpHeight += jumpVelocity * clampedDt
            jumpVelocity -= 13.5f * clampedDt // Gravity
            if (newJumpHeight <= 0f) {
                newJumpHeight = 0f
                isJumping = false
                jumpVelocity = 0f
            }
        }

        // 4. Update Slide
        var isSliding = state.isSliding
        var slideProgress = state.slideProgress
        if (isSliding) {
            slideTimer -= clampedDt
            slideProgress = (slideTimer / slideDuration).coerceIn(0f, 1f)
            if (slideTimer <= 0f) {
                isSliding = false
                slideProgress = 0f
            }
        }

        // 5. Update Active Buffs
        val updatedBuffs = mutableListOf<ActiveBuff>()
        var isMagnetActive = false
        var isShielded = false
        var isSprint = false

        for (buff in state.activeBuffs) {
            val rem = buff.remainingTimeSeconds - clampedDt
            if (rem > 0f) {
                updatedBuffs.add(buff.copy(remainingTimeSeconds = rem))
                when (buff.type) {
                    ActiveBuffType.MAGNET -> isMagnetActive = true
                    ActiveBuffType.SHIELD -> isShielded = true
                    ActiveBuffType.BOOST_SPRINT -> isSprint = true
                    else -> {}
                }
            }
        }

        // Charge Special Power energy gradually (takes ~25s to fill)
        val newSpecialEnergy = if (state.isSpecialPowerActive) {
            if (updatedBuffs.isEmpty()) 0.05f else 0f
        } else {
            (state.specialPowerEnergy + (clampedDt / 25f)).coerceAtMost(1.0f)
        }

        // 6. Move Obstacles & Collectibles towards player (reduce zDistance)
        val updatedObstacles = mutableListOf<Obstacle>()
        var collisionObstacle: Obstacle? = null

        for (obs in state.obstacles) {
            val newZ = obs.zDistance - (effectiveSpeed * clampedDt)
            if (newZ > -5f) {
                updatedObstacles.add(obs.copy(zDistance = newZ))

                // Collision detection zone: z is between 0.3f and 2.5f
                if (newZ in 0.2f..2.2f && obs.lane == currentDiscreteLane) {
                    val isSafe = when (obs.heightType) {
                        ObstacleHeight.LOW -> newJumpHeight > 0.38f
                        ObstacleHeight.HIGH -> isSliding
                        ObstacleHeight.FULL -> false // full blocker in current lane
                    }

                    if (!isSafe) {
                        if (isSprint || isShielded) {
                            // Break obstacle with sparks!
                            createObstacleBreakSparks(obs.lane)
                            soundSynthesizer.playCoin()
                            addFloatingText("SHIELD BLOCKED!", 0f, 0xFF10B981)
                            // Remove shield if it was shield
                            if (!isSprint) {
                                updatedBuffs.removeAll { it.type == ActiveBuffType.SHIELD }
                            }
                        } else {
                            collisionObstacle = obs
                        }
                    }
                }
            }
        }

        // Check if collision causes Game Over (Supply)
        if (collisionObstacle != null) {
            triggerSupplyGameOver(state, collisionObstacle, newDistance)
            return
        }

        // 7. Move & Collect Collectibles
        val updatedCollectibles = mutableListOf<Collectible>()
        var addedCoins = 0
        var addedScore = (traveled * 2).toInt()
        var cgpaDelta = 0f
        var assignmentsInc = 0
        var examsInc = 0
        var projectsInc = 0
        var internshipsInc = 0

        for (item in state.collectibles) {
            var itemLane = item.lane
            var newZ = item.zDistance - (effectiveSpeed * clampedDt)

            // Magnet pulling effect
            if (isMagnetActive && newZ in 0f..25f) {
                itemLane = currentDiscreteLane
                newZ -= 8f * clampedDt
            }

            // Collection Check
            if (newZ in 0.0f..3.0f && abs(itemLane - currentDiscreteLane) == 0 && !item.isCollected) {
                item.isCollected = true
                soundSynthesizer.playCoin()
                soundSynthesizer.vibrate(15)

                val points = item.value * state.comboMultiplier
                addedScore += points

                when (item.type) {
                    CollectibleType.COIN -> {
                        addedCoins += 1
                        addFloatingText("+1 COIN", itemLane.toFloat(), 0xFFFBBF24)
                    }
                    CollectibleType.TEXTBOOK -> {
                        cgpaDelta += 0.04f
                        assignmentsInc += 1
                        addFloatingText("+0.04 CGPA", itemLane.toFloat(), 0xFF60A5FA)
                    }
                    CollectibleType.COFFEE_MUG -> {
                        addedScore += 50
                        addFloatingText("ALL-NIGHTER BOOST!", itemLane.toFloat(), 0xFFF97316)
                    }
                    CollectibleType.TROPHY -> {
                        addedCoins += 5
                        cgpaDelta += 0.10f
                        projectsInc += 1
                        soundSynthesizer.playPowerUp()
                        addFloatingText("HACKATHON TROPHY!", itemLane.toFloat(), 0xFFA855F7)
                    }
                    CollectibleType.OFFER_LETTER -> {
                        addedCoins += 15
                        cgpaDelta += 0.20f
                        internshipsInc += 1
                        soundSynthesizer.playPowerUp()
                        addFloatingText("OFFER LETTER!", itemLane.toFloat(), 0xFF10B981)
                    }
                    CollectibleType.MAGNET -> {
                        updatedBuffs.add(
                            ActiveBuff(
                                type = ActiveBuffType.MAGNET,
                                name = "Coin Magnet",
                                remainingTimeSeconds = 8.0f,
                                totalDurationSeconds = 8.0f
                            )
                        )
                        soundSynthesizer.playPowerUp()
                        addFloatingText("MAGNET ACTIVE!", 0f, 0xFF38BDF8)
                    }
                    CollectibleType.SHIELD -> {
                        updatedBuffs.add(
                            ActiveBuff(
                                type = ActiveBuffType.SHIELD,
                                name = "Academic Shield",
                                remainingTimeSeconds = 15.0f,
                                totalDurationSeconds = 15.0f
                            )
                        )
                        soundSynthesizer.playPowerUp()
                        addFloatingText("SHIELD EQUIPPED!", 0f, 0xFF34D399)
                    }
                    CollectibleType.BOOST_SNEAKER -> {
                        updatedBuffs.add(
                            ActiveBuff(
                                type = ActiveBuffType.BOOST_SPRINT,
                                name = "Sprint Dash",
                                remainingTimeSeconds = 6.0f,
                                totalDurationSeconds = 6.0f
                            )
                        )
                        soundSynthesizer.playPowerUp()
                        addFloatingText("SUPER SPRINT!", 0f, 0xFFF43F5E)
                    }
                }
            } else if (newZ > -4f && !item.isCollected) {
                updatedCollectibles.add(item.copy(zDistance = newZ, lane = itemLane))
            }
        }

        // 8. Spawn Ahead Generator
        val furthestZ = max(
            updatedObstacles.maxOfOrNull { it.zDistance } ?: 0f,
            updatedCollectibles.maxOfOrNull { it.zDistance } ?: 0f
        )

        if (furthestZ < 75f) {
            spawnNextSegment(furthestZ + 16f, newStage, updatedObstacles, updatedCollectibles)
        }

        // 9. Update particles & floating texts
        updateParticles(clampedDt)

        val newCgpa = (state.cgpa + cgpaDelta).coerceIn(4.0f, 10.0f)
        val finalCoins = state.coins + addedCoins
        val finalScore = state.score + addedScore

        _gameState.update {
            it.copy(
                distanceMeters = newDistance,
                currentStage = newStage,
                score = finalScore,
                coins = finalCoins,
                cgpa = newCgpa,
                laneXOffset = newLaneX,
                currentLane = currentDiscreteLane,
                jumpHeight = newJumpHeight,
                isJumping = isJumping,
                isSliding = isSliding,
                slideProgress = slideProgress,
                currentSpeed = effectiveSpeed,
                obstacles = updatedObstacles,
                collectibles = updatedCollectibles,
                activeBuffs = updatedBuffs,
                isMagnetActive = isMagnetActive,
                isShielded = isShielded,
                isInvincibleSprint = isSprint,
                specialPowerEnergy = newSpecialEnergy,
                isSpecialPowerActive = updatedBuffs.any { b -> b.type == ActiveBuffType.SPECIAL_ABILITY || b.type == ActiveBuffType.BOOST_SPRINT },
                assignmentsSubmitted = state.assignmentsSubmitted + assignmentsInc,
                examsCleared = state.examsCleared + examsInc,
                projectsCompleted = state.projectsCompleted + projectsInc,
                internshipsSecured = state.internshipsSecured + internshipsInc,
                runTimeSeconds = state.runTimeSeconds + clampedDt
            )
        }
    }

    private fun spawnNextSegment(
        spawnZ: Float,
        stage: AcademicYearStage,
        obstacles: MutableList<Obstacle>,
        collectibles: MutableList<Collectible>
    ) {
        val patternChoice = Random.nextInt(100)

        // Select obstacles relevant to the academic year
        val availableObstacleTypes = when (stage) {
            AcademicYearStage.YEAR_1 -> listOf(
                ObstacleType.ASSIGNMENT_STACK,
                ObstacleType.BACKPACK_DROP,
                ObstacleType.CAMPUS_CONE,
                ObstacleType.CAMPUS_BANNER
            )
            AcademicYearStage.YEAR_2 -> listOf(
                ObstacleType.SUPPLY_EXAM_BOARD,
                ObstacleType.STRICT_PROFESSOR,
                ObstacleType.BACKPACK_DROP,
                ObstacleType.PIPE_DUCT
            )
            AcademicYearStage.YEAR_3 -> listOf(
                ObstacleType.PROJECT_KIT,
                ObstacleType.VIVA_SCANNER,
                ObstacleType.COLLEGE_BUS,
                ObstacleType.DEADLINE_WALL
            )
            AcademicYearStage.YEAR_4 -> listOf(
                ObstacleType.SUPPLY_EXAM_BOARD,
                ObstacleType.STRICT_PROFESSOR,
                ObstacleType.VIVA_SCANNER,
                ObstacleType.DEADLINE_WALL
            )
        }

        when {
            // Pattern 1: Single lane obstacle with coin trail in adjacent lanes
            patternChoice < 40 -> {
                val obsLane = Random.nextInt(-1, 2)
                val obsType = availableObstacleTypes.random()
                obstacles.add(
                    Obstacle(
                        id = nextObstacleId++,
                        type = obsType,
                        lane = obsLane,
                        zDistance = spawnZ,
                        academicStage = stage
                    )
                )

                // Coin trail in open lane
                val coinLane = if (obsLane == 0) (if (Random.nextBoolean()) -1 else 1) else 0
                for (i in 0 until 3) {
                    collectibles.add(
                        Collectible(
                            id = nextCollectibleId++,
                            type = CollectibleType.COIN,
                            lane = coinLane,
                            zDistance = spawnZ + (i * 3.5f)
                        )
                    )
                }
            }

            // Pattern 2: Jump/Slide obstacle across 2 lanes with power-up in 3rd
            patternChoice < 70 -> {
                val safeLane = Random.nextInt(-1, 2)
                val obsType = if (Random.nextBoolean()) ObstacleType.ASSIGNMENT_STACK else ObstacleType.CAMPUS_BANNER
                for (lane in -1..1) {
                    if (lane != safeLane) {
                        obstacles.add(
                            Obstacle(
                                id = nextObstacleId++,
                                type = obsType,
                                lane = lane,
                                zDistance = spawnZ,
                                academicStage = stage
                            )
                        )
                    }
                }

                // Academic item or PowerUp in safe lane
                val itemType = when (stage) {
                    AcademicYearStage.YEAR_1 -> CollectibleType.TEXTBOOK
                    AcademicYearStage.YEAR_2 -> if (Random.nextBoolean()) CollectibleType.COFFEE_MUG else CollectibleType.SHIELD
                    AcademicYearStage.YEAR_3 -> if (Random.nextBoolean()) CollectibleType.TROPHY else CollectibleType.MAGNET
                    AcademicYearStage.YEAR_4 -> if (Random.nextBoolean()) CollectibleType.OFFER_LETTER else CollectibleType.BOOST_SNEAKER
                }
                collectibles.add(
                    Collectible(
                        id = nextCollectibleId++,
                        type = itemType,
                        lane = safeLane,
                        zDistance = spawnZ
                    )
                )
            }

            // Pattern 3: Rich Reward Arch (Triple coin arcs or powerups)
            else -> {
                val powerType = when (Random.nextInt(4)) {
                    0 -> CollectibleType.MAGNET
                    1 -> CollectibleType.SHIELD
                    2 -> CollectibleType.BOOST_SNEAKER
                    else -> CollectibleType.TROPHY
                }
                collectibles.add(
                    Collectible(
                        id = nextCollectibleId++,
                        type = powerType,
                        lane = 0,
                        zDistance = spawnZ
                    )
                )
                collectibles.add(
                    Collectible(
                        id = nextCollectibleId++,
                        type = CollectibleType.COIN,
                        lane = -1,
                        zDistance = spawnZ + 2f
                    )
                )
                collectibles.add(
                    Collectible(
                        id = nextCollectibleId++,
                        type = CollectibleType.COIN,
                        lane = 1,
                        zDistance = spawnZ + 2f
                    )
                )
            }
        }
    }

    private fun triggerSupplyGameOver(
        state: GameState,
        obstacle: Obstacle,
        finalDistance: Float
    ) {
        soundSynthesizer.playSupplyCrash()
        preferences.recordRunResult(
            score = state.score,
            coinsEarned = state.coins,
            distance = finalDistance,
            finalCgpa = state.cgpa,
            graduated = false
        )

        _gameState.update {
            it.copy(
                phase = GamePhase.SUPPLY_GAME_OVER,
                lastSupplyObstacle = obstacle,
                distanceMeters = finalDistance,
                totalGraduationsCount = preferences.totalGraduations
            )
        }
    }

    private fun triggerGraduation(state: GameState, finalDistance: Float) {
        soundSynthesizer.playGraduationFanfare()
        // Award 50 bonus graduation coins
        val bonusCoins = 50
        val totalEarned = state.coins + bonusCoins

        preferences.recordRunResult(
            score = state.score + 5000,
            coinsEarned = totalEarned,
            distance = finalDistance,
            finalCgpa = min(10.0f, state.cgpa + 0.5f),
            graduated = true
        )

        // Check automatic unlocks
        if (preferences.totalGraduations >= 1) {
            preferences.unlockCharacter(com.example.model.CharacterId.CHAMP)
        }
        if (preferences.totalGraduations >= 2) {
            preferences.unlockCharacter(com.example.model.CharacterId.CHASER)
        }

        _gameState.update {
            it.copy(
                phase = GamePhase.BECOME_AN_ENGINEER,
                coins = totalEarned,
                score = state.score + 5000,
                cgpa = min(10.0f, state.cgpa + 0.5f),
                distanceMeters = finalDistance,
                totalGraduationsCount = preferences.totalGraduations
            )
        }
    }

    private fun createObstacleBreakSparks(lane: Int) {
        for (i in 0 until 15) {
            particles.add(
                Particle(
                    x = (lane * 150f) + Random.nextFloat() * 40f - 20f,
                    y = 500f + Random.nextFloat() * 50f,
                    vx = (Random.nextFloat() - 0.5f) * 300f,
                    vy = (Random.nextFloat() - 0.7f) * 350f,
                    color = 0xFFF59E0B,
                    size = Random.nextFloat() * 8f + 4f,
                    lifespan = 0.6f
                )
            )
        }
    }

    private fun addFloatingText(text: String, laneX: Float, color: Long) {
        floatingTexts.add(
            FloatingText(
                id = nextFloatingTextId++,
                text = text,
                x = laneX,
                y = 0f,
                color = color
            )
        )
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.lifespan -= dt
            p.alpha = (p.lifespan / 0.6f).coerceIn(0f, 1f)
            if (p.lifespan <= 0f) {
                pIter.remove()
            }
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.offsetY -= 60f * dt
            t.alpha -= 0.8f * dt
            if (t.alpha <= 0f) {
                tIter.remove()
            }
        }
    }
}
