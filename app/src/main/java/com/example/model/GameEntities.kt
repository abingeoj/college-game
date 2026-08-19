package com.example.model

enum class ObstacleType {
    ASSIGNMENT_STACK,   // Low -> Jump
    BACKPACK_DROP,      // Low -> Jump
    PROJECT_KIT,        // Low -> Jump
    CAMPUS_CONE,        // Low -> Jump
    CAMPUS_BANNER,      // High -> Slide
    VIVA_SCANNER,       // High -> Slide
    PIPE_DUCT,          // High -> Slide
    SUPPLY_EXAM_BOARD,  // Blocker -> Dodge
    COLLEGE_BUS,        // Blocker -> Dodge
    STRICT_PROFESSOR,   // Blocker -> Dodge
    DEADLINE_WALL       // Blocker -> Dodge
}

enum class ObstacleHeight {
    LOW,    // player must jump
    HIGH,   // player must slide
    FULL    // player must dodge (change lane)
}

data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    val lane: Int,            // -1 = Left, 0 = Center, 1 = Right
    val zDistance: Float,     // Distance ahead of player on track (e.g. 50f to 0f)
    val width: Float = 1.0f,
    val academicStage: AcademicYearStage,
    val heightType: ObstacleHeight = when (type) {
        ObstacleType.ASSIGNMENT_STACK,
        ObstacleType.BACKPACK_DROP,
        ObstacleType.PROJECT_KIT,
        ObstacleType.CAMPUS_CONE -> ObstacleHeight.LOW

        ObstacleType.CAMPUS_BANNER,
        ObstacleType.VIVA_SCANNER,
        ObstacleType.PIPE_DUCT -> ObstacleHeight.HIGH

        ObstacleType.SUPPLY_EXAM_BOARD,
        ObstacleType.COLLEGE_BUS,
        ObstacleType.STRICT_PROFESSOR,
        ObstacleType.DEADLINE_WALL -> ObstacleHeight.FULL
    },
    val title: String = when (type) {
        ObstacleType.ASSIGNMENT_STACK -> "Assignment Deadline"
        ObstacleType.BACKPACK_DROP -> "Heavy Backpack"
        ObstacleType.PROJECT_KIT -> "Project Hardware Bug"
        ObstacleType.CAMPUS_CONE -> "Lab Safety Cone"
        ObstacleType.CAMPUS_BANNER -> "Fest Banner"
        ObstacleType.VIVA_SCANNER -> "Viva Voce Bar"
        ObstacleType.PIPE_DUCT -> "Lab Overhead Duct"
        ObstacleType.SUPPLY_EXAM_BOARD -> "Supply / Arrear Hurdle"
        ObstacleType.COLLEGE_BUS -> "Campus Bus"
        ObstacleType.STRICT_PROFESSOR -> "Strict Invigilator"
        ObstacleType.DEADLINE_WALL -> "Portal Crash Barrier"
    }
)

enum class CollectibleType {
    COIN,
    TEXTBOOK,
    COFFEE_MUG,
    TROPHY,
    OFFER_LETTER,
    MAGNET,
    SHIELD,
    BOOST_SNEAKER
}

data class Collectible(
    val id: Long,
    val type: CollectibleType,
    val lane: Int,
    val zDistance: Float,
    val value: Int = when (type) {
        CollectibleType.COIN -> 10
        CollectibleType.TEXTBOOK -> 30
        CollectibleType.COFFEE_MUG -> 20
        CollectibleType.TROPHY -> 100
        CollectibleType.OFFER_LETTER -> 250
        CollectibleType.MAGNET,
        CollectibleType.SHIELD,
        CollectibleType.BOOST_SNEAKER -> 50
    },
    var isCollected: Boolean = false
)

enum class ActiveBuffType {
    NONE,
    MAGNET,
    SHIELD,
    BOOST_SPRINT,
    SPECIAL_ABILITY
}

data class ActiveBuff(
    val type: ActiveBuffType,
    val name: String,
    val remainingTimeSeconds: Float,
    val totalDurationSeconds: Float
) {
    val progress: Float
        get() = (remainingTimeSeconds / totalDurationSeconds).coerceIn(0f, 1f)
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Long,
    var alpha: Float = 1.0f,
    var size: Float = 6f,
    var lifespan: Float = 1.0f
)

data class FloatingText(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Long,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0f
)
