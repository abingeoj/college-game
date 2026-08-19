package com.example.model

enum class GamePhase {
    CHARACTER_SELECT,
    RUNNING,
    PAUSED,
    SUPPLY_GAME_OVER,
    BECOME_AN_ENGINEER
}

data class GameState(
    val phase: GamePhase = GamePhase.CHARACTER_SELECT,
    val selectedCharacter: CharacterProfile = CharacterProfile.ALL_CHARACTERS[0],
    val currentLane: Int = 0,               // -1 = Left, 0 = Center, 1 = Right
    val targetLane: Int = 0,
    val laneXOffset: Float = 0f,           // Continuous offset -1f to +1f for silky smooth visual sliding
    val jumpHeight: Float = 0f,            // 0f (ground) to 1f (peak jump)
    val isJumping: Boolean = false,
    val isSliding: Boolean = false,
    val slideProgress: Float = 0f,         // 0f to 1f
    val distanceMeters: Float = 0f,
    val score: Int = 0,
    val coins: Int = 0,
    val cgpa: Float = 8.5f,
    val comboMultiplier: Int = 1,
    val currentStage: AcademicYearStage = AcademicYearStage.YEAR_1,
    val obstacles: List<Obstacle> = emptyList(),
    val collectibles: List<Collectible> = emptyList(),
    val activeBuffs: List<ActiveBuff> = emptyList(),
    val baseSpeed: Float = 14f,            // meters per second on track
    val currentSpeed: Float = 14f,
    val lastSupplyObstacle: Obstacle? = null,
    val isShielded: Boolean = false,
    val isMagnetActive: Boolean = false,
    val isInvincibleSprint: Boolean = false,
    val specialPowerEnergy: Float = 1.0f,  // 0f to 1f
    val isSpecialPowerActive: Boolean = false,
    val assignmentsSubmitted: Int = 0,
    val examsCleared: Int = 0,
    val projectsCompleted: Int = 0,
    val internshipsSecured: Int = 0,
    val totalGraduationsCount: Int = 0,
    val runTimeSeconds: Float = 0f
) {
    val isSpecialPowerReady: Boolean
        get() = specialPowerEnergy >= 1.0f

    val yearProgressFraction: Float
        get() = currentStage.getProgressFraction(distanceMeters)

    val overallGraduationProgress: Float
        get() = (distanceMeters / AcademicYearStage.TOTAL_GRADUATION_DISTANCE).coerceIn(0f, 1f)
}
