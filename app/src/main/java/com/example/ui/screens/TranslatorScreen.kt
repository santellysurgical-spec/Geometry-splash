package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GdLevel
import com.example.translator.LevelFormat
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.ui.theme.GdYellow

@Composable
fun TranslatorScreen(
    currentLevel: GdLevel,
    sourceFormat: LevelFormat,
    targetFormat: LevelFormat,
    inputValue: String,
    outputValue: String,
    statusMessage: String?,
    onSourceFormatChange: (LevelFormat) -> Unit,
    onTargetFormatChange: (LevelFormat) -> Unit,
    onInputChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onConvertCurrentLevel: () -> Unit,
    onImportToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("translator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan.copy(alpha = 0.3f)),
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
                                text = "Cross-Platform Level Translator",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = GdCyan
                            )
                            Text(
                                text = "Convert level data seamlessly between RobTop, JSON, CSV & Compact codes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = GdCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Active Level Export Quick-Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Loaded World: \"${currentLevel.name}\" (${currentLevel.totalObjects} objects)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Quickly export your active level into any format for sharing or backup:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConvertCurrentLevel,
                            colors = ButtonDefaults.buttonColors(containerColor = GdCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("export_active_level_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Loaded Level", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Source & Target Format Selectors
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Formats",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Source format chips
                Column {
                    Text("Source Format (Import):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LevelFormat.values().forEach { fmt ->
                            FormatChip(
                                label = fmt.displayName,
                                isSelected = fmt == sourceFormat,
                                onClick = { onSourceFormatChange(fmt) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Target format chips
                Column {
                    Text("Target Format (Export):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LevelFormat.values().forEach { fmt ->
                            FormatChip(
                                label = fmt.displayName,
                                isSelected = fmt == targetFormat,
                                onClick = { onTargetFormatChange(fmt) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Input Field
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Input Data (${sourceFormat.displayName})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    // Quick Paste sample button
                    Text(
                        text = "Paste Preset",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GdCyan,
                        modifier = Modifier
                            .clickable {
                                val sample = when (sourceFormat) {
                                    LevelFormat.ROBTOP_STRING -> "1,1,2,60,3,15;1,1,2,90,3,15;1,8,2,150,3,15;1,10,2,210,3,15;1,35,2,270,3,45;1,1,2,330,3,60;1,1329,2,330,3,90;"
                                    LevelFormat.GD_STUDIO_JSON -> "{\n  \"name\": \"Custom Shared Orbit\",\n  \"bpm\": 145.0,\n  \"difficulty\": \"HARD\",\n  \"objects\": [\n    {\"id\": 1, \"x\": 60.0, \"y\": 15.0},\n    {\"id\": 8, \"x\": 120.0, \"y\": 15.0}\n  ]\n}"
                                    LevelFormat.CSV_TABLE -> "id,type,x,y,rotation,scale,colorChannel,groupId\n1,SOLID,60.0,15.0,0.0,1.0,1,0\n8,HAZARD,120.0,15.0,0.0,1.0,1,0\n35,ORB,180.0,45.0,0.0,1.0,1,0"
                                    LevelFormat.COMPACT_HEX -> "GDW:H4sICDYt71kAA215bHZsAKtWSkksS1WygjCrlGq1VAgk6wUAQz4oQikAAAA="
                                }
                                onInputChange(sample)
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = onInputChange,
                    placeholder = { Text("Paste raw level string, JSON, CSV, or GDW code here...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("translator_input_field"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF070B14),
                        unfocusedContainerColor = Color(0xFF070B14),
                        focusedBorderColor = GdCyan,
                        unfocusedBorderColor = Color(0xFF1E2A42)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTranslate,
                        colors = ButtonDefaults.buttonColors(containerColor = GdLime, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("translate_btn")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Translate", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onImportToLibrary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("import_to_library_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import & Open", fontSize = 12.sp)
                    }
                }
            }
        }

        // Status Feedback Banner
        if (statusMessage != null) {
            item {
                Surface(
                    color = Color(0xFF0F1E33),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GdCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Output Result Field
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output Result (${targetFormat.displayName})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    if (outputValue.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("GD Level Export", outputValue)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GdLime, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GdLime
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF070B14))
                        .border(1.dp, Color(0xFF1E2A42), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (outputValue.isBlank()) "Translation output will appear here..." else outputValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (outputValue.isBlank()) Color.Gray else Color(0xFFD6E4FF),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Format Compatibility Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Format Compatibility Specs",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• RobTop String: Semicolon delimited object key-values (1=id, 2=x, 3=y, 6=rot). Compatible with GD 2.1 & 2.2.\n" +
                               "• GD World JSON: Open web schema with full object array and music BPM metadata.\n" +
                               "• CSV Table: Comma-separated spreadsheet format for level procedural scripts.\n" +
                               "• Compact GDW: Gzip compressed Base64 code for fast sharing on messaging apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormatChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) GdCyan else Color(0xFF131C30),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) GdCyan else Color(0xFF22314E)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.split(" ")[0], // Short name
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
