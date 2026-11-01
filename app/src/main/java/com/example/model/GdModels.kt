package com.example.model

import java.util.UUID

enum class GdObjectType(val displayName: String, val category: String) {
    SOLID("Solid Block", "Blocks"),
    HAZARD("Spike", "Hazards"),
    ORB("Jump Orb", "Orbs"),
    PAD("Jump Pad", "Pads"),
    PORTAL_MODE("Game Mode", "Portals"),
    PORTAL_GRAVITY("Gravity Portal", "Portals"),
    PORTAL_SPEED("Speed Portal", "Portals"),
    PORTAL_SIZE("Size Portal", "Portals"),
    PORTAL_DUAL("Dual Portal", "Portals"),
    TRIGGER("Trigger", "Triggers"),
    COLLECTIBLE("Coin", "Collectibles"),
    DECOR("Decoration", "Decor")
}

enum class GdGameMode(val title: String) {
    CUBE("Cube"),
    SHIP("Ship"),
    BALL("Ball"),
    UFO("UFO"),
    WAVE("Wave"),
    ROBOT("Robot"),
    SPIDER("Spider")
}

enum class GdSpeed(val multiplier: Float, val title: String) {
    HALF(0.7f, "0.5x Slow"),
    NORMAL(1.0f, "1x Normal"),
    DOUBLE(1.25f, "2x Fast"),
    TRIPLE(1.5f, "3x Very Fast"),
    QUAD(1.8f, "4x Insane")
}

enum class GdDifficulty(val title: String, val stars: Int, val colorHex: String) {
    AUTO("Auto", 1, "#B0BEC5"),
    EASY("Easy", 2, "#00E676"),
    NORMAL("Normal", 3, "#00E5FF"),
    HARD("Hard", 4, "#FFD600"),
    HARDER("Harder", 6, "#FF6D00"),
    INSANE("Insane", 8, "#FF1744"),
    EASY_DEMON("Easy Demon", 10, "#D500F9"),
    MEDIUM_DEMON("Medium Demon", 10, "#AA00FF"),
    HARD_DEMON("Hard Demon", 10, "#E91E63"),
    INSANE_DEMON("Insane Demon", 10, "#C51162"),
    EXTREME_DEMON("Extreme Demon", 10, "#880E4F")
}

data class GdObject(
    val id: Int,                     // RobTop Object ID (e.g. 1=block, 8=spike, 35=yellow orb, etc.)
    val type: GdObjectType,
    val x: Float,                    // Grid units (1 unit = 30px standard)
    val y: Float,
    val rotation: Float = 0f,        // degrees: 0, 90, 180, 270
    val scale: Float = 1.0f,
    val colorChannel: Int = 1,
    val groupId: Int = 0,
    val isFlippedX: Boolean = false,
    val isFlippedY: Boolean = false,
    val extraData: String = ""
)

data class GdLevel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val creator: String = "Player",
    val description: String = "",
    val difficulty: GdDifficulty = GdDifficulty.NORMAL,
    val songName: String = "Stereo Madness",
    val songArtist: String = "ForeverBound",
    val bpm: Float = 140f,
    val songOffset: Float = 0f,
    val initialGameMode: GdGameMode = GdGameMode.CUBE,
    val initialSpeed: GdSpeed = GdSpeed.NORMAL,
    val backgroundColorHex: String = "#0A0E17",
    val groundColorHex: String = "#00F0FF",
    val objects: List<GdObject> = emptyList(),
    val lengthSeconds: Float = 60f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalObjects: Int get() = objects.size
    val maxX: Float get() = objects.maxOfOrNull { it.x } ?: 30f
}

data class PlayerInputEvent(
    val timestampMs: Long,
    val x: Float,
    val y: Float,
    val isPress: Boolean,
    val durationMs: Long = 0L,
    val gameMode: GdGameMode = GdGameMode.CUBE
)

data class GameplayAttempt(
    val attemptId: String = UUID.randomUUID().toString(),
    val levelId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val progressPercent: Float,     // 0f to 100f
    val deathX: Float? = null,
    val deathY: Float? = null,
    val deathReason: String? = null,
    val inputs: List<PlayerInputEvent> = emptyList(),
    val isCompleted: Boolean = false,
    val jumpCount: Int = 0
)
