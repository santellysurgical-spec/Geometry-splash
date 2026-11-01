package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.simulation.GdPhysicsSimulator
import com.example.simulation.SimulationState
import com.example.ui.components.drawGdObject
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdDemonRed
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.ui.theme.GdOrange
import com.example.ui.theme.GdYellow
import com.example.viewmodel.BuilderTool

data class PaletteItem(val id: Int, val name: String, val type: GdObjectType)

val PALETTE_PRESETS = listOf(
    // Blocks
    PaletteItem(1, "Solid Block", GdObjectType.SOLID),
    PaletteItem(2, "Half Slab", GdObjectType.SOLID),
    PaletteItem(6, "Slope", GdObjectType.SOLID),
    // Hazards
    PaletteItem(8, "Spike", GdObjectType.HAZARD),
    PaletteItem(9, "Small Spike", GdObjectType.HAZARD),
    PaletteItem(183, "Sawblade", GdObjectType.HAZARD),
    // Orbs
    PaletteItem(35, "Yellow Orb", GdObjectType.ORB),
    PaletteItem(36, "Pink Orb", GdObjectType.ORB),
    PaletteItem(84, "Red Orb", GdObjectType.ORB),
    PaletteItem(67, "Blue Orb", GdObjectType.ORB),
    PaletteItem(140, "Green Orb", GdObjectType.ORB),
    // Pads
    PaletteItem(10, "Yellow Pad", GdObjectType.PAD),
    PaletteItem(11, "Pink Pad", GdObjectType.PAD),
    PaletteItem(12, "Red Pad", GdObjectType.PAD),
    PaletteItem(13, "Blue Pad", GdObjectType.PAD),
    // Portals
    PaletteItem(101, "Cube Portal", GdObjectType.PORTAL_MODE),
    PaletteItem(102, "Ship Portal", GdObjectType.PORTAL_MODE),
    PaletteItem(105, "Wave Portal", GdObjectType.PORTAL_MODE),
    PaletteItem(108, "Gravity Portal", GdObjectType.PORTAL_GRAVITY),
    PaletteItem(112, "2x Speed", GdObjectType.PORTAL_SPEED),
    PaletteItem(113, "3x Speed", GdObjectType.PORTAL_SPEED),
    // Collectibles
    PaletteItem(1329, "Secret Coin", GdObjectType.COLLECTIBLE)
)

@Composable
fun BuilderScreen(
    level: GdLevel,
    builderTool: BuilderTool,
    paletteSelectedObject: GdObject,
    selectedObject: GdObject?,
    zoomScale: Float,
    panOffset: Pair<Float, Float>,
    isTestPlaying: Boolean,
    simState: SimulationState,
    onToolSelected: (BuilderTool) -> Unit,
    onPaletteSelect: (Int, GdObjectType) -> Unit,
    onGridTap: (Float, Float) -> Unit,
    onRotateSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onStartTestPlay: () -> Unit,
    onStopTestPlay: () -> Unit,
    onTestPlayInputDown: () -> Unit,
    onTestPlayInputUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Blocks", "Hazards", "Orbs", "Pads", "Portals", "Collectibles")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .testTag("builder_screen")
    ) {
        // Main Editor Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isTestPlaying) {
                    if (isTestPlaying) {
                        detectTapGestures(
                            onPress = {
                                onTestPlayInputDown()
                                tryAwaitRelease()
                                onTestPlayInputUp()
                            }
                        )
                    } else {
                        detectTapGestures { tapOffset ->
                            // Convert tap screen coordinates into world grid units
                            val canvasH = size.height
                            val originY = canvasH * 0.75f - panOffset.second
                            val worldX = (tapOffset.x - panOffset.first) / zoomScale
                            val worldY = (originY - tapOffset.y) / zoomScale
                            onGridTap(worldX, worldY)
                        }
                    }
                }
                .pointerInput(isTestPlaying) {
                    if (!isTestPlaying) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onPanChange(panOffset.first + dragAmount.x, panOffset.second + dragAmount.y)
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val originY = canvasH * 0.75f - panOffset.second
            val unitPx = 30f * zoomScale

            // 1. Draw Grid Lines
            val startGridX = (panOffset.first % unitPx)
            var gx = startGridX
            while (gx < canvasW) {
                drawLine(
                    color = Color(0xFF141F33),
                    start = Offset(gx, 0f),
                    end = Offset(gx, canvasH),
                    strokeWidth = 1f
                )
                gx += unitPx
            }

            var gy = originY % unitPx
            while (gy < canvasH) {
                drawLine(
                    color = Color(0xFF141F33),
                    start = Offset(0f, gy),
                    end = Offset(canvasW, gy),
                    strokeWidth = 1f
                )
                gy += unitPx
            }

            // 2. Draw Ground Floor Baseline (Y = 15f)
            val groundScreenY = originY - (15f * zoomScale)
            drawLine(
                color = GdCyan,
                start = Offset(0f, groundScreenY),
                end = Offset(canvasW, groundScreenY),
                strokeWidth = 3.dp.toPx()
            )
            // Ground underfill
            drawRect(
                color = Color(0xFF06101D),
                topLeft = Offset(0f, groundScreenY),
                size = Size(canvasW, canvasH - groundScreenY)
            )

            // 3. Draw All Placed Level Objects
            for (obj in level.objects) {
                val screenX = panOffset.first + (obj.x * zoomScale)
                val screenY = originY - (obj.y * zoomScale)
                val isSelected = (obj == selectedObject)

                if (screenX in -100f..(canvasW + 100f)) {
                    drawGdObject(
                        obj = obj,
                        centerX = screenX,
                        centerY = screenY,
                        sizePx = 30f * zoomScale * obj.scale,
                        isSelected = isSelected
                    )
                }
            }

            // 4. Draw Trajectory Arc Prediction Overlay (when not in play mode)
            if (!isTestPlaying) {
                val arc = GdPhysicsSimulator.calculateTrajectoryArc(startX = 60f, startY = 15f)
                val arcPath = Path()
                arc.forEachIndexed { idx, pt ->
                    val sx = panOffset.first + (pt.first * zoomScale)
                    val sy = originY - (pt.second * zoomScale)
                    if (idx == 0) arcPath.moveTo(sx, sy) else arcPath.lineTo(sx, sy)
                }
                drawPath(
                    path = arcPath,
                    color = GdCyan.copy(alpha = 0.45f),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // 5. Test Play Active Simulation Render
            if (isTestPlaying) {
                // Draw player trajectory trail
                val trailPath = Path()
                simState.trajectoryTrail.forEachIndexed { i, p ->
                    val tx = panOffset.first + (p.first * zoomScale)
                    val ty = originY - (p.second * zoomScale)
                    if (i == 0) trailPath.moveTo(tx, ty) else trailPath.lineTo(tx, ty)
                }
                drawPath(
                    path = trailPath,
                    color = GdLime.copy(alpha = 0.6f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Animated Player Character
                val playerScreenX = panOffset.first + (simState.x * zoomScale)
                val playerScreenY = originY - (simState.y * zoomScale)
                val playerSize = 30f * zoomScale

                // Player Cube
                drawRoundRect(
                    color = GdCyan,
                    topLeft = Offset(playerScreenX - (playerSize / 2f), playerScreenY - (playerSize / 2f)),
                    size = Size(playerSize, playerSize),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = GdLime,
                    topLeft = Offset(playerScreenX - (playerSize / 4f), playerScreenY - (playerSize / 4f)),
                    size = Size(playerSize / 2f, playerSize / 2f),
                    cornerRadius = CornerRadius(2f, 2f)
                )

                // Death / Crash effect
                if (simState.isDead) {
                    drawCircle(
                        color = GdDemonRed.copy(alpha = 0.7f),
                        radius = playerSize * 1.5f,
                        center = Offset(playerScreenX, playerScreenY)
                    )
                }
            }
        }

        // Top Control Bar (HUD, Tools, Undo/Redo)
        Surface(
            color = Color(0xFF0F1728).copy(alpha = 0.94f),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314E)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level info & stats
                    Column {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = GdCyan
                        )
                        Text(
                            text = "${level.totalObjects} objects • ${level.initialGameMode.title} (${level.initialSpeed.title})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8FA0BF),
                            fontSize = 11.sp
                        )
                    }

                    // Tool selector & Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onUndo,
                            modifier = Modifier.size(36.dp).testTag("builder_undo")
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                        }
                        IconButton(
                            onClick = onRedo,
                            modifier = Modifier.size(36.dp).testTag("builder_redo")
                        ) {
                            Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.White)
                        }

                        // Playtest button
                        Button(
                            onClick = if (isTestPlaying) onStopTestPlay else onStartTestPlay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestPlaying) GdDemonRed else GdLime,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp).testTag("builder_test_play_toggle")
                        ) {
                            Icon(
                                imageVector = if (isTestPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTestPlaying) "Stop" else "Test Play",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (!isTestPlaying) {
                    Spacer(modifier = Modifier.height(6.dp))
                    // Tools row: Select, Place, Erase, Zoom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ToolChip(
                                label = "Place",
                                isSelected = builderTool == BuilderTool.PLACE,
                                onClick = { onToolSelected(BuilderTool.PLACE) },
                                tag = "tool_place"
                            )
                            ToolChip(
                                label = "Select",
                                isSelected = builderTool == BuilderTool.SELECT,
                                onClick = { onToolSelected(BuilderTool.SELECT) },
                                tag = "tool_select"
                            )
                            ToolChip(
                                label = "Erase",
                                isSelected = builderTool == BuilderTool.ERASE,
                                onClick = { onToolSelected(BuilderTool.ERASE) },
                                tag = "tool_erase"
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { onZoomChange(-0.2f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                            }
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            IconButton(onClick = { onZoomChange(0.2f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Test Play HUD (when active)
        if (isTestPlaying) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GdLime.copy(alpha = 0.6f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Progress: ${simState.attemptProgress.toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = GdLime
                    )
                    Text(
                        text = "Jumps: ${simState.jumpCount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GdCyan
                    )
                    if (simState.isDead) {
                        Text(
                            text = simState.deathReason ?: "Crashed!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = GdDemonRed
                        )
                    }
                }
            }
        }

        // Selected Object Inspector Bar (floats when an object is selected)
        if (selectedObject != null && !isTestPlaying) {
            Surface(
                color = Color(0xFF141E33),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedObject.type.displayName} (#${selectedObject.id})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "X: ${selectedObject.x.toInt()}u, Y: ${selectedObject.y.toInt()}u • Rot: ${selectedObject.rotation.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8FA0BF),
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onRotateSelected,
                            modifier = Modifier.size(36.dp).testTag("inspector_rotate")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = GdCyan)
                        }
                        IconButton(
                            onClick = onDeleteSelected,
                            modifier = Modifier.size(36.dp).testTag("inspector_delete")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GdDemonRed)
                        }
                    }
                }
            }
        }

        // Bottom Object Palette Dock (when in edit mode)
        if (!isTestPlaying) {
            Surface(
                color = Color(0xFF0C1322),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D2A45)),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    // Category Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = cat == selectedCategory
                            Surface(
                                color = if (isSel) GdCyan else Color(0xFF162137),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Object items scroll
                    val filteredPalette = PALETTE_PRESETS.filter {
                        selectedCategory == "All" || it.type.category.equals(selectedCategory, ignoreCase = true)
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPalette) { item ->
                            val isCurrentSelected = (paletteSelectedObject.id == item.id)
                            PaletteItemCard(
                                item = item,
                                isSelected = isCurrentSelected,
                                onClick = { onPaletteSelect(item.id, item.type) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = if (isSelected) GdCyan else Color(0xFF1B263E),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PaletteItemCard(
    item: PaletteItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF003842) else Color(0xFF131C30),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) GdCyan else Color(0xFF22314E)
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("palette_item_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(32.dp)) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawGdObject(
                        obj = GdObject(id = item.id, type = item.type, x = 0f, y = 0f),
                        centerX = size.width / 2f,
                        centerY = size.height / 2f,
                        sizePx = 22f
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) GdCyan else Color(0xFFB0C2DE),
                fontSize = 9.sp
            )
        }
    }
}
