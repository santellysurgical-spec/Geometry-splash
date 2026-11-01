package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GdDifficulty
import com.example.model.GdLevel
import com.example.ui.components.DifficultyBadge
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.ui.theme.GdYellow

@Composable
fun LevelLibraryScreen(
    levels: List<GdLevel>,
    activeLevel: GdLevel,
    onSelectLevel: (GdLevel) -> Unit,
    onOpenAnalyzer: (GdLevel) -> Unit,
    onOpenBuilder: (GdLevel) -> Unit,
    onOpenTranslator: (GdLevel) -> Unit,
    onCreateNewLevel: (String, GdDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Easy", "Normal", "Hard", "Demon")

    val filteredLevels = levels.filter { lvl ->
        when (selectedFilter) {
            "Easy" -> lvl.difficulty == GdDifficulty.EASY
            "Normal" -> lvl.difficulty == GdDifficulty.NORMAL
            "Hard" -> lvl.difficulty == GdDifficulty.HARD || lvl.difficulty == GdDifficulty.HARDER
            "Demon" -> lvl.difficulty.name.contains("DEMON")
            else -> true
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("library_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Banner & Create Action
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Geometry Dash World Library",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = GdCyan
                                )
                                Text(
                                    text = "${levels.size} custom & showcase worlds ready to analyze or build",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GdLime, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("create_new_level_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New World", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { f ->
                        val isSel = f == selectedFilter
                        Surface(
                            color = if (isSel) GdCyan else Color(0xFF131C2E),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) GdCyan else Color(0xFF22314E)
                            ),
                            modifier = Modifier.clickable { selectedFilter = f }
                        ) {
                            Text(
                                text = f,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Level Cards
            items(filteredLevels) { lvl ->
                val isActive = lvl.id == activeLevel.id
                LevelCard(
                    level = lvl,
                    isActive = isActive,
                    onSelect = { onSelectLevel(lvl) },
                    onAnalyze = { onOpenAnalyzer(lvl) },
                    onBuild = { onOpenBuilder(lvl) },
                    onTranslate = { onOpenTranslator(lvl) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Create New Level Dialog
        if (showCreateDialog) {
            var newName by remember { mutableStateOf("") }
            var newDifficulty by remember { mutableStateOf(GdDifficulty.NORMAL) }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = Color(0xFF101726),
                title = {
                    Text(
                        text = "New Geometry Dash World",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GdCyan
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Configure initial level parameters:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("World Name") },
                            placeholder = { Text("e.g. Cyber Pulse") },
                            modifier = Modifier.fillMaxWidth().testTag("new_level_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GdCyan,
                                unfocusedBorderColor = Color(0xFF243656)
                            )
                        )

                        Text("Select Target Difficulty:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(GdDifficulty.EASY, GdDifficulty.NORMAL, GdDifficulty.HARD, GdDifficulty.HARD_DEMON).forEach { d ->
                                val isSel = d == newDifficulty
                                Surface(
                                    color = if (isSel) GdCyan else Color(0xFF19253B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { newDifficulty = d }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = d.title.split(" ")[0],
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) Color.Black else Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCreateNewLevel(newName, newDifficulty)
                            showCreateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GdLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_create_level_btn")
                    ) {
                        Text("Create World", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showCreateDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun LevelCard(
    level: GdLevel,
    isActive: Boolean,
    onSelect: () -> Unit,
    onAnalyze: () -> Unit,
    onBuild: () -> Unit,
    onTranslate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isActive) GdCyan else Color(0xFF22304D)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("level_card_${level.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isActive) GdCyan else Color.White
                        )
                        if (isActive) {
                            Surface(
                                color = GdCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GdCyan,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Creator: ${level.creator} • ${level.totalObjects} objects",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8FA0BF),
                        fontSize = 11.sp
                    )
                }

                DifficultyBadge(difficulty = level.difficulty)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Song & Mode info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = GdYellow, modifier = Modifier.size(14.dp))
                Text(
                    text = "${level.songName} (${level.bpm.toInt()} BPM) • Mode: ${level.initialGameMode.title} • Speed: ${level.initialSpeed.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            if (level.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAnalyze,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003844), contentColor = GdCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("action_analyze_${level.id}")
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyze", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBuild,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003618), contentColor = GdLime),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp).testTag("action_build_${level.id}")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTranslate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.1f).height(36.dp).testTag("action_translate_${level.id}")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Translate", fontSize = 11.sp)
                }
            }
        }
    }
}
