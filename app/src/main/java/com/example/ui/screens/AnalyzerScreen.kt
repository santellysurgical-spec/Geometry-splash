package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnalysisReport
import com.example.model.GdLevel
import com.example.model.LevelImprovementSuggestion
import com.example.model.SuggestionSeverity
import com.example.ui.components.ChokePointHeatmapCanvas
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PacingCurveCanvas
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdDemonRed
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.ui.theme.GdOrange
import com.example.ui.theme.GdYellow

@Composable
fun AnalyzerScreen(
    level: GdLevel,
    report: AnalysisReport?,
    onApplySuggestion: (LevelImprovementSuggestion) -> Unit,
    onNavigateToBuilder: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProgress by remember { mutableStateOf<Float?>(null) }
    var appliedSuggestions by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analyzer_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Level Overview Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = level.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                color = GdCyan
                            )
                            Text(
                                text = "By ${level.creator} • ${level.songName} (${level.bpm.toInt()} BPM)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DifficultyBadge(difficulty = report?.calculatedDifficulty ?: level.difficulty)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quality Score Bar
                    val score = report?.overallQualityScore ?: 75
                    val scoreColor = if (score >= 80) GdLime else if (score >= 60) GdCyan else GdOrange
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level Architecture Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$score / 100",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = scoreColor
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C1322))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(score / 100f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(scoreColor)
                        )
                    }

                    if (report != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = report.generatedSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 4 Core Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Sight-Reading",
                    value = "${report?.sightReadabilityScore?.toInt() ?: 85}%",
                    subtitle = "Reaction margin",
                    accentColor = GdCyan,
                    icon = Icons.Default.Visibility,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Rhythm Sync",
                    value = "${report?.rhythmSyncScore?.toInt() ?: 90}%",
                    subtitle = "${level.bpm.toInt()} BPM grid",
                    accentColor = GdLime,
                    icon = Icons.Default.MusicNote,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Clear Rate",
                    value = "${String.format(java.util.Locale.US, "%.1f", report?.estimatedClearRate ?: 12.5f)}%",
                    subtitle = "${report?.totalAttemptsAnalyzed ?: 0} attempts",
                    accentColor = GdYellow,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Choke Points",
                    value = "${report?.chokePoints?.size ?: 0}",
                    subtitle = "Sudden mortality",
                    accentColor = if ((report?.chokePoints?.size ?: 0) > 0) GdDemonRed else GdLime,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Choke Point & Death Heatmap Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gameplay Attempt Heatmap",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "0% to 100% Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ChokePointHeatmapCanvas(
                        chokePoints = report?.chokePoints ?: emptyList(),
                        totalAttempts = report?.totalAttemptsAnalyzed ?: 0,
                        selectedProgress = selectedProgress,
                        onScrub = { selectedProgress = it }
                    )
                }
            }
        }

        // Pacing & Difficulty Tension Curve
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Obstacle Density & Tension Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PacingCurveCanvas(segments = report?.pacingCurve ?: emptyList())
                }
            }
        }

        // Tap Timing & Micro-Consistency Radar
        if (report?.tapMetrics != null) {
            val tap = report.tapMetrics
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Player Micro-Timing & Jitter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Avg Tap Duration", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${tap.averageTapDurationMs}ms", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = GdCyan)
                            }
                            Column {
                                Text("Timing Jitter", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("±${String.format(java.util.Locale.US, "%.1f", tap.tapJitterMs)}ms", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = GdYellow)
                            }
                            Column {
                                Text("Frame Margin", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("${String.format(java.util.Locale.US, "%.1f", tap.frameWindowMarginMs)}ms", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = GdLime)
                            }
                        }
                    }
                }
            }
        }

        // Actionable Level Improvement Suggestions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suggested Level Improvements (${report?.suggestions?.size ?: 0})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        // Suggestions List
        val suggestions = report?.suggestions ?: emptyList()
        if (suggestions.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GdLime)
                        Text(
                            "No critical flaws detected! The level has balanced pacing, clean sightlines, and solid rhythm sync.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(suggestions) { sug ->
                val isApplied = appliedSuggestions.contains(sug.id)
                SuggestionCard(
                    suggestion = sug,
                    isApplied = isApplied,
                    onApply = {
                        onApplySuggestion(sug)
                        appliedSuggestions = appliedSuggestions + sug.id
                    },
                    onLocate = {
                        onNavigateToBuilder(sug.xPosition)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SuggestionCard(
    suggestion: LevelImprovementSuggestion,
    isApplied: Boolean,
    onApply: () -> Unit,
    onLocate: () -> Unit
) {
    val severityColor = when (suggestion.severity) {
        SuggestionSeverity.CRITICAL -> GdDemonRed
        SuggestionSeverity.RECOMMENDED -> GdOrange
        SuggestionSeverity.POLISH -> GdCyan
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("suggestion_card_${suggestion.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(severityColor)
                    )
                    Text(
                        text = suggestion.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = severityColor
                    )
                }

                Surface(
                    color = Color(0xFF0C1322),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Pos: ${suggestion.xPosition.toInt()}u",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8FA0BF),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actionable fix callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A101D))
                    .border(1.dp, Color(0xFF1F2B48), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Fix: ${suggestion.actionableFix}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = GdLime,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLocate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("locate_button_${suggestion.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Locate", fontSize = 12.sp)
                }

                if (suggestion.autoFixType != null) {
                    Button(
                        onClick = onApply,
                        enabled = !isApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isApplied) Color.Gray else GdCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).testTag("apply_fix_button_${suggestion.id}")
                    ) {
                        Icon(
                            imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isApplied) "Applied" else "Apply Fix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
