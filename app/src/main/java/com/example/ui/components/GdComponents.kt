package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChokePoint
import com.example.model.GdDifficulty
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.PacingSegment
import com.example.model.SuggestionSeverity
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdDemonRed
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.ui.theme.GdOrange
import com.example.ui.theme.GdYellow

@Composable
fun DifficultyBadge(
    difficulty: GdDifficulty,
    modifier: Modifier = Modifier
) {
    val color = when (difficulty) {
        GdDifficulty.AUTO -> Color(0xFF90A4AE)
        GdDifficulty.EASY -> GdLime
        GdDifficulty.NORMAL -> GdCyan
        GdDifficulty.HARD -> GdYellow
        GdDifficulty.HARDER -> GdOrange
        GdDifficulty.INSANE -> Color(0xFFFF3366)
        GdDifficulty.EASY_DEMON, GdDifficulty.MEDIUM_DEMON -> Color(0xFFD500F9)
        GdDifficulty.HARD_DEMON, GdDifficulty.INSANE_DEMON, GdDifficulty.EXTREME_DEMON -> GdDemonRed
    }

    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = modifier.testTag("difficulty_badge_${difficulty.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = difficulty.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            if (difficulty.stars > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${difficulty.stars}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = color,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    fontSize = 22.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ChokePointHeatmapCanvas(
    chokePoints: List<ChokePoint>,
    totalAttempts: Int,
    selectedProgress: Float?,
    onScrub: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B12))
            .border(1.dp, Color(0xFF1E2840), RoundedCornerShape(12.dp))
            .clickable { /* interactive scrub support */ }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw subtle grid background
            for (i in 0..10) {
                val x = (i / 10f) * width
                drawLine(
                    color = Color(0xFF162035),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // 2. Draw safe baseline line
            drawLine(
                color = GdCyan.copy(alpha = 0.4f),
                start = Offset(0f, height * 0.85f),
                end = Offset(width, height * 0.85f),
                strokeWidth = 2f
            )

            // 3. Draw Heatmap clusters
            if (chokePoints.isEmpty()) {
                // Smooth default progression
                val safePath = Path().apply {
                    moveTo(0f, height * 0.85f)
                    lineTo(width, height * 0.85f)
                }
                drawPath(safePath, GdLime.copy(alpha = 0.7f), style = Stroke(width = 3f))
            } else {
                for (cp in chokePoints) {
                    val x = (cp.progressPercent / 100f) * width
                    val barWidth = 24.dp.toPx()
                    val barHeight = (cp.mortalityRate * height * 0.75f).coerceIn(12f, height * 0.8f)
                    val barTop = height * 0.85f - barHeight

                    val col = when (cp.severity) {
                        SuggestionSeverity.CRITICAL -> GdDemonRed
                        SuggestionSeverity.RECOMMENDED -> GdOrange
                        SuggestionSeverity.POLISH -> GdYellow
                    }

                    // Heat column glow
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(col.copy(alpha = 0.85f), col.copy(alpha = 0.15f))
                        ),
                        topLeft = Offset(x - (barWidth / 2f), barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Top indicator point
                    drawCircle(
                        color = col,
                        radius = 4.dp.toPx(),
                        center = Offset(x, barTop)
                    )
                }
            }

            // 4. Scrub indicator line if active
            if (selectedProgress != null) {
                val scrubX = (selectedProgress / 100f) * width
                drawLine(
                    color = Color.White,
                    start = Offset(scrubX, 0f),
                    end = Offset(scrubX, height),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Percentage markers overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7D9B), fontSize = 9.sp)
            Text("25%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7D9B), fontSize = 9.sp)
            Text("50%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7D9B), fontSize = 9.sp)
            Text("75%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7D9B), fontSize = 9.sp)
            Text("100%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7D9B), fontSize = 9.sp)
        }
    }
}

@Composable
fun PacingCurveCanvas(
    segments: List<PacingSegment>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070B12))
            .border(1.dp, Color(0xFF1E2840), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (segments.isEmpty()) return@Canvas
            val width = size.width
            val height = size.height

            val tensionPath = Path()
            val fillPath = Path()

            val stepX = width / (segments.size - 1).coerceAtLeast(1)

            for (i in segments.indices) {
                val seg = segments[i]
                val x = i * stepX
                val y = height - (seg.tensionLevel * height * 0.85f) - 6f

                if (i == 0) {
                    tensionPath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    tensionPath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(width, height)
            fillPath.close()

            // Fill gradient under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(GdCyan.copy(alpha = 0.28f), Color.Transparent)
                )
            )

            // Stroke curve line
            drawPath(
                path = tensionPath,
                color = GdCyan,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points
            for (i in segments.indices) {
                val seg = segments[i]
                val x = i * stepX
                val y = height - (seg.tensionLevel * height * 0.85f) - 6f
                val ptColor = if (seg.tensionLevel > 0.7f) GdDemonRed else if (seg.tensionLevel > 0.45f) GdYellow else GdLime
                drawCircle(color = ptColor, radius = 3.5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

// Visual drawing for objects on builder grid or palette
fun DrawScope.drawGdObject(
    obj: GdObject,
    centerX: Float,
    centerY: Float,
    sizePx: Float,
    isSelected: Boolean = false
) {
    val half = sizePx / 2f
    when (obj.type) {
        GdObjectType.SOLID -> {
            drawRoundRect(
                color = Color(0xFF00C8D6),
                topLeft = Offset(centerX - half, centerY - half),
                size = Size(sizePx, sizePx),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF00383D),
                topLeft = Offset(centerX - half + 3f, centerY - half + 3f),
                size = Size(sizePx - 6f, sizePx - 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
        GdObjectType.HAZARD -> {
            val spikePath = Path().apply {
                if (obj.rotation == 180f) {
                    moveTo(centerX - half, centerY - half)
                    lineTo(centerX + half, centerY - half)
                    lineTo(centerX, centerY + half)
                } else {
                    moveTo(centerX - half, centerY + half)
                    lineTo(centerX + half, centerY + half)
                    lineTo(centerX, centerY - half)
                }
                close()
            }
            drawPath(spikePath, color = GdDemonRed)
            drawPath(spikePath, color = Color(0xFFFF8099), style = Stroke(width = 2f))
        }
        GdObjectType.ORB -> {
            val orbColor = when (obj.id) {
                35 -> GdYellow
                36 -> Color(0xFFFF4081)
                84 -> GdDemonRed
                67 -> Color(0xFF00E5FF)
                140 -> GdLime
                else -> GdYellow
            }
            drawCircle(color = orbColor.copy(alpha = 0.35f), radius = half * 1.25f, center = Offset(centerX, centerY))
            drawCircle(color = orbColor, radius = half * 0.8f, center = Offset(centerX, centerY))
            drawCircle(color = Color.White, radius = half * 0.35f, center = Offset(centerX, centerY))
        }
        GdObjectType.PAD -> {
            val padColor = when (obj.id) {
                10 -> GdYellow
                11 -> Color(0xFFFF4081)
                12 -> GdDemonRed
                13 -> Color(0xFF00E5FF)
                else -> GdYellow
            }
            drawRoundRect(
                color = padColor,
                topLeft = Offset(centerX - half, centerY + half - 6f),
                size = Size(sizePx, 6f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawCircle(color = padColor, radius = 5f, center = Offset(centerX, centerY + half - 3f))
        }
        GdObjectType.PORTAL_MODE, GdObjectType.PORTAL_GRAVITY, GdObjectType.PORTAL_SPEED -> {
            val portalColor = when (obj.type) {
                GdObjectType.PORTAL_MODE -> Color(0xFF9D4EDD)
                GdObjectType.PORTAL_GRAVITY -> Color(0xFF00E5FF)
                GdObjectType.PORTAL_SPEED -> Color(0xFFFF9100)
                else -> Color.White
            }
            drawRoundRect(
                color = portalColor.copy(alpha = 0.4f),
                topLeft = Offset(centerX - 8f, centerY - half * 1.4f),
                size = Size(16f, sizePx * 1.4f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = portalColor,
                topLeft = Offset(centerX - 8f, centerY - half * 1.4f),
                size = Size(16f, sizePx * 1.4f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.5f)
            )
        }
        GdObjectType.COLLECTIBLE -> {
            drawCircle(color = Color(0xFFFFD700), radius = half * 0.9f, center = Offset(centerX, centerY))
            drawCircle(color = Color(0xFFFFAB00), radius = half * 0.65f, center = Offset(centerX, centerY))
            drawCircle(color = Color(0xFFFFF9C4), radius = half * 0.3f, center = Offset(centerX, centerY))
        }
        else -> {
            drawRect(
                color = Color.Gray,
                topLeft = Offset(centerX - half, centerY - half),
                size = Size(sizePx, sizePx)
            )
        }
    }

    if (isSelected) {
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(centerX - half - 4f, centerY - half - 4f),
            size = Size(sizePx + 8f, sizePx + 8f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2f)
        )
    }
}
