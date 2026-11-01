package com.example.simulation

import com.example.model.GameplayAttempt
import com.example.model.GdGameMode
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.PlayerInputEvent
import java.util.UUID
import kotlin.math.abs

data class SimulationState(
    val x: Float = 0f,
    val y: Float = 15f,
    val velocityY: Float = 0f,
    val isGrounded: Boolean = true,
    val isDead: Boolean = false,
    val isWon: Boolean = false,
    val deathReason: String? = null,
    val currentMode: GdGameMode = GdGameMode.CUBE,
    val gravityDirection: Float = 1.0f, // 1.0 = normal down, -1.0 = upside-down
    val attemptProgress: Float = 0f,
    val jumpCount: Int = 0,
    val trajectoryTrail: List<Pair<Float, Float>> = emptyList()
)

object GdPhysicsSimulator {

    const val FLOOR_Y = 15f
    const val BLOCK_SIZE = 30f
    const val GRAVITY_ACCEL = 0.95f
    const val JUMP_IMPULSE = 13.8f
    const val MOVE_SPEED = 6.2f // Units per frame at 60 FPS (approx 1x speed)

    fun tick(
        state: SimulationState,
        level: GdLevel,
        isHoldingTap: Boolean,
        hasJustTapped: Boolean
    ): SimulationState {
        if (state.isDead || state.isWon) return state

        val maxX = (level.objects.maxOfOrNull { it.x } ?: 1000f) + 120f
        if (state.x >= maxX) {
            return state.copy(isWon = true, attemptProgress = 100f)
        }

        var newX = state.x + MOVE_SPEED
        var newY = state.y
        var newVelY = state.velocityY
        var newGrounded = false
        var gravityDir = state.gravityDirection
        var mode = state.currentMode
        var newJumpCount = state.jumpCount

        // 1. Check Portals at current X
        val portals = level.objects.filter { 
            (it.type == GdObjectType.PORTAL_MODE || it.type == GdObjectType.PORTAL_GRAVITY) && 
            abs(it.x - newX) < 15f 
        }
        for (p in portals) {
            if (p.id == 108) gravityDir = -1.0f // Invert gravity
            if (p.id == 109) gravityDir = 1.0f  // Normal gravity
            if (p.id == 101) mode = GdGameMode.CUBE
            if (p.id == 102) mode = GdGameMode.SHIP
            if (p.id == 105) mode = GdGameMode.WAVE
        }

        // 2. Check Pads (auto-trigger when stepping on them)
        val pads = level.objects.filter { it.type == GdObjectType.PAD && abs(it.x - newX) < 18f && abs(it.y - state.y) < 20f }
        for (pad in pads) {
            when (pad.id) {
                10 -> newVelY = 14.5f * gravityDir // Yellow pad
                11 -> newVelY = 9.8f * gravityDir  // Pink pad
                12 -> newVelY = 18.2f * gravityDir // Red pad
                13 -> { gravityDir = -gravityDir; newVelY = 8f * gravityDir } // Blue pad
            }
        }

        // 3. Check Orbs (triggered on active tap when in range)
        if (hasJustTapped) {
            val orbs = level.objects.filter { it.type == GdObjectType.ORB && abs(it.x - newX) < 26f && abs(it.y - state.y) < 26f }
            for (orb in orbs) {
                when (orb.id) {
                    35 -> { newVelY = 14.2f * gravityDir; newJumpCount++ } // Yellow
                    36 -> { newVelY = 9.5f * gravityDir; newJumpCount++ }  // Pink
                    84 -> { newVelY = 18.0f * gravityDir; newJumpCount++ } // Red
                    67 -> { gravityDir = -gravityDir; newVelY = 7.0f * gravityDir; newJumpCount++ } // Blue gravity
                    140 -> { newVelY = 13.0f * gravityDir; newJumpCount++ } // Green
                }
            }
        }

        // 4. Apply Mode Movement & Jump
        when (mode) {
            GdGameMode.CUBE -> {
                if (hasJustTapped && state.isGrounded) {
                    newVelY = JUMP_IMPULSE * gravityDir
                    newJumpCount++
                }
                newVelY -= (GRAVITY_ACCEL * gravityDir)
                newY += newVelY

                // Check Floor
                if (gravityDir > 0 && newY <= FLOOR_Y) {
                    newY = FLOOR_Y
                    newVelY = 0f
                    newGrounded = true
                }

                // Check Solid Block Collisions (Top surface landing & side crashes)
                val nearbySolids = level.objects.filter { it.type == GdObjectType.SOLID && abs(it.x - newX) < 32f }
                for (b in nearbySolids) {
                    val blockLeft = b.x - 15f
                    val blockRight = b.x + 15f
                    val blockTop = b.y + 15f
                    val blockBottom = b.y - 15f

                    // Landing on top of block
                    if (gravityDir > 0 && state.y >= blockTop - 4f && newY <= blockTop && newX in blockLeft..blockRight) {
                        newY = blockTop
                        newVelY = 0f
                        newGrounded = true
                    }
                    // Head hitting underside
                    else if (gravityDir > 0 && state.y <= blockBottom && newY >= blockBottom && newX in blockLeft..blockRight) {
                        newY = blockBottom
                        newVelY = -1f
                    }
                    // Side wall crash
                    else if (newX in (blockLeft - 2f)..blockRight && newY in (blockBottom + 2f)..(blockTop - 2f)) {
                        return state.copy(
                            isDead = true,
                            deathReason = "Wall impact at ${newX.toInt()}u",
                            attemptProgress = (newX / maxX * 100f).coerceIn(0f, 100f)
                        )
                    }
                }
            }
            GdGameMode.SHIP -> {
                val shipThrust = if (isHoldingTap) 0.85f * gravityDir else -0.75f * gravityDir
                newVelY = (newVelY * 0.94f) + shipThrust
                newY += newVelY
                if (newY <= FLOOR_Y) {
                    newY = FLOOR_Y
                    newVelY = 0f
                }
            }
            else -> {
                if (hasJustTapped && state.isGrounded) {
                    newVelY = JUMP_IMPULSE * gravityDir
                    newJumpCount++
                }
                newVelY -= (GRAVITY_ACCEL * gravityDir)
                newY += newVelY
                if (newY <= FLOOR_Y) {
                    newY = FLOOR_Y
                    newVelY = 0f
                    newGrounded = true
                }
            }
        }

        // 5. Check Hazard Collisions (Spikes, Saws)
        val nearbyHazards = level.objects.filter { it.type == GdObjectType.HAZARD && abs(it.x - newX) < 22f }
        for (h in nearbyHazards) {
            // Precise spike hitbox (slightly forgiving triangle hitbox)
            val dx = abs(newX - h.x)
            val dy = abs(newY - h.y)
            if (dx < 14f && dy < 16f) {
                return state.copy(
                    isDead = true,
                    deathReason = "Spike collision at ${newX.toInt()}u",
                    attemptProgress = (newX / maxX * 100f).coerceIn(0f, 100f)
                )
            }
        }

        val updatedTrail = (state.trajectoryTrail + Pair(newX, newY)).takeLast(60)
        val progress = (newX / maxX * 100f).coerceIn(0f, 100f)

        return state.copy(
            x = newX,
            y = newY,
            velocityY = newVelY,
            isGrounded = newGrounded,
            gravityDirection = gravityDir,
            currentMode = mode,
            attemptProgress = progress,
            jumpCount = newJumpCount,
            trajectoryTrail = updatedTrail
        )
    }

    // Compute preview jump trajectory from current position for the Builder overlay
    fun calculateTrajectoryArc(startX: Float, startY: Float, gravityDir: Float = 1.0f): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        var curX = startX
        var curY = startY
        var curVelY = JUMP_IMPULSE * gravityDir

        for (step in 0..25) {
            points.add(Pair(curX, curY))
            curX += MOVE_SPEED
            curVelY -= (GRAVITY_ACCEL * gravityDir)
            curY += curVelY
            if (gravityDir > 0 && curY <= FLOOR_Y) {
                points.add(Pair(curX, FLOOR_Y))
                break
            }
        }
        return points
    }
}
