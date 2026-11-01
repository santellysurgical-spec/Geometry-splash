package com.example.data

import com.example.model.GameplayAttempt
import com.example.model.GdDifficulty
import com.example.model.GdGameMode
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.GdSpeed
import com.example.model.PlayerInputEvent
import java.util.UUID

object SampleLevels {

    val stereoPulse: GdLevel by lazy {
        val objects = mutableListOf<GdObject>()
        // Introductory jumps
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 120f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 150f, y = 15f))
        
        // Solid stepping blocks
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 210f, y = 30f))
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 240f, y = 30f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 270f, y = 15f))
        
        // Yellow Jump Pad
        objects.add(GdObject(id = 10, type = GdObjectType.PAD, x = 330f, y = 15f))
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 390f, y = 75f))
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 420f, y = 75f))
        
        // Yellow Jump Orb
        objects.add(GdObject(id = 35, type = GdObjectType.ORB, x = 480f, y = 90f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 480f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 510f, y = 15f))
        
        // Landing platform & Collectible Coin
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 570f, y = 45f))
        objects.add(GdObject(id = 1329, type = GdObjectType.COLLECTIBLE, x = 570f, y = 75f))
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 600f, y = 45f))

        // Ship Portal transition
        objects.add(GdObject(id = 102, type = GdObjectType.PORTAL_MODE, x = 690f, y = 60f))
        // Ship corridor with ceiling blocks
        for (x in 720..960 step 30) {
            objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = x.toFloat(), y = 105f))
        }
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 780f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 870f, y = 15f))
        // Return to Cube
        objects.add(GdObject(id = 101, type = GdObjectType.PORTAL_MODE, x = 990f, y = 60f))

        GdLevel(
            id = "sample_stereo_pulse",
            name = "Stereo Pulse",
            creator = "GeometryArchitect",
            description = "A well-balanced introductory world demonstrating standard rhythm jumps, pad timing, and a spacious ship tunnel.",
            difficulty = GdDifficulty.NORMAL,
            songName = "Stereo Madness",
            songArtist = "ForeverBound",
            bpm = 140f,
            initialGameMode = GdGameMode.CUBE,
            initialSpeed = GdSpeed.NORMAL,
            backgroundColorHex = "#0D1B2A",
            groundColorHex = "#00F0FF",
            objects = objects
        )
    }

    val neonOdyssey: GdLevel by lazy {
        val objects = mutableListOf<GdObject>()
        // Speed 2x portal
        objects.add(GdObject(id = 112, type = GdObjectType.PORTAL_SPEED, x = 60f, y = 30f))

        // Fast rhythm orbs
        objects.add(GdObject(id = 35, type = GdObjectType.ORB, x = 120f, y = 45f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 120f, y = 15f))
        objects.add(GdObject(id = 36, type = GdObjectType.ORB, x = 180f, y = 45f)) // Pink orb
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 180f, y = 15f))
        objects.add(GdObject(id = 84, type = GdObjectType.ORB, x = 240f, y = 60f)) // Red orb

        // High ledge
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 300f, y = 90f))
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 330f, y = 90f))

        // Gravity flip portal
        objects.add(GdObject(id = 108, type = GdObjectType.PORTAL_GRAVITY, x = 390f, y = 90f))
        // Ceiling spikes
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 450f, y = 105f, rotation = 180f))
        objects.add(GdObject(id = 67, type = GdObjectType.ORB, x = 480f, y = 75f)) // Blue gravity orb

        // Wave mode transition
        objects.add(GdObject(id = 105, type = GdObjectType.PORTAL_MODE, x = 570f, y = 60f))
        objects.add(GdObject(id = 113, type = GdObjectType.PORTAL_SPEED, x = 600f, y = 60f)) // 3x speed

        // Wave zig-zag obstacles
        for (i in 0..6) {
            val wx = 660f + (i * 60f)
            val wy = if (i % 2 == 0) 90f else 30f
            objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = wx, y = wy))
        }

        GdLevel(
            id = "sample_neon_odyssey",
            name = "Neon Odyssey",
            creator = "VortexSync",
            description = "High-energy fast-paced level with 2x and 3x speed portals, reverse gravity drops, and high-frequency wave slaloms.",
            difficulty = GdDifficulty.HARD,
            songName = "Jumper",
            songArtist = "Waterflame",
            bpm = 150f,
            initialGameMode = GdGameMode.CUBE,
            initialSpeed = GdSpeed.DOUBLE,
            backgroundColorHex = "#1A0B2E",
            groundColorHex = "#FF007F",
            objects = objects
        )
    }

    val infernoChasm: GdLevel by lazy {
        val objects = mutableListOf<GdObject>()
        // Intentional Demon design with choke points and blind transitions for Analyzer detection
        // 1. Extreme triple spikes
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 90f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 120f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 150f, y = 15f))

        // 2. Blind gravity drop
        objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = 210f, y = 90f))
        objects.add(GdObject(id = 108, type = GdObjectType.PORTAL_GRAVITY, x = 240f, y = 90f))
        // Blind hazard right after portal (less than 40 units!)
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 270f, y = 105f, rotation = 180f))

        // 3. Quad speed portal into tight corridor
        objects.add(GdObject(id = 114, type = GdObjectType.PORTAL_SPEED, x = 360f, y = 45f))
        for (x in 390..600 step 30) {
            objects.add(GdObject(id = 1, type = GdObjectType.SOLID, x = x.toFloat(), y = 75f))
            if (x % 60 == 0) {
                objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = x.toFloat(), y = 15f))
            }
        }

        // 4. Frame-perfect blue orb
        objects.add(GdObject(id = 67, type = GdObjectType.ORB, x = 660f, y = 45f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 660f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 690f, y = 15f))
        objects.add(GdObject(id = 8, type = GdObjectType.HAZARD, x = 720f, y = 15f))

        GdLevel(
            id = "sample_inferno_chasm",
            name = "Inferno Chasm",
            creator = "DemonCrafter",
            description = "Extreme demon level engineered with rapid gravity reversals, 4x speed chokepoints, and micro-timing windows.",
            difficulty = GdDifficulty.HARD_DEMON,
            songName = "At The Speed of Light",
            songArtist = "Dimrain47",
            bpm = 168f,
            initialGameMode = GdGameMode.CUBE,
            initialSpeed = GdSpeed.TRIPLE,
            backgroundColorHex = "#260505",
            groundColorHex = "#FF3300",
            objects = objects
        )
    }

    fun getSampleAttempts(levelId: String): List<GameplayAttempt> {
        return when (levelId) {
            "sample_inferno_chasm" -> {
                val list = mutableListOf<GameplayAttempt>()
                // 15 deaths on the first triple spike (X ~ 120-150)
                repeat(12) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 14f + (it % 3),
                            deathX = 135f + ((it % 5) * 4f),
                            deathY = 15f,
                            deathReason = "Triple spike collision",
                            isCompleted = false,
                            jumpCount = 1
                        )
                    )
                }
                // 20 deaths on blind gravity drop (X ~ 270) -> Major Choke Point!
                repeat(18) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 32f + (it % 2),
                            deathX = 270f + ((it % 3) * 3f),
                            deathY = 105f,
                            deathReason = "Blind ceiling spike after gravity portal",
                            isCompleted = false,
                            jumpCount = 4
                        )
                    )
                }
                // 10 deaths in 4x speed corridor
                repeat(8) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 58f,
                            deathX = 480f + ((it % 4) * 15f),
                            deathY = 15f,
                            deathReason = "Quad speed corridor crash",
                            isCompleted = false,
                            jumpCount = 7
                        )
                    )
                }
                // 1 successful completion
                list.add(
                    GameplayAttempt(
                        levelId = levelId,
                        progressPercent = 100f,
                        isCompleted = true,
                        jumpCount = 16
                    )
                )
                list
            }
            "sample_neon_odyssey" -> {
                val list = mutableListOf<GameplayAttempt>()
                repeat(6) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 28f,
                            deathX = 180f,
                            deathY = 15f,
                            deathReason = "Pink orb timing late",
                            isCompleted = false,
                            jumpCount = 3
                        )
                    )
                }
                repeat(8) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 74f,
                            deathX = 720f,
                            deathY = 60f,
                            deathReason = "Wave slalom boundary clip",
                            isCompleted = false,
                            jumpCount = 12
                        )
                    )
                }
                repeat(4) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 100f,
                            isCompleted = true,
                            jumpCount = 18
                        )
                    )
                }
                list
            }
            else -> {
                // Stereo pulse
                val list = mutableListOf<GameplayAttempt>()
                repeat(3) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 45f,
                            deathX = 480f,
                            deathY = 15f,
                            deathReason = "Missed yellow orb",
                            isCompleted = false,
                            jumpCount = 4
                        )
                    )
                }
                repeat(12) {
                    list.add(
                        GameplayAttempt(
                            levelId = levelId,
                            progressPercent = 100f,
                            isCompleted = true,
                            jumpCount = 9
                        )
                    )
                }
                list
            }
        }
    }
}
