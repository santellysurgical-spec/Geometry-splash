package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyzerScreen
import com.example.ui.screens.BuilderScreen
import com.example.ui.screens.LevelLibraryScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.theme.GdAppTheme
import com.example.ui.theme.GdCyan
import com.example.ui.theme.GdLime
import com.example.ui.theme.GdMagenta
import com.example.viewmodel.AppTab
import com.example.viewmodel.GdAppViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GdAppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GdAppTheme {
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val activeLevel by viewModel.currentLevel.collectAsStateWithLifecycle()
                val allLevels by viewModel.allLevels.collectAsStateWithLifecycle()
                val analysisReport by viewModel.analysisReport.collectAsStateWithLifecycle()

                // Builder states
                val builderTool by viewModel.builderTool.collectAsStateWithLifecycle()
                val paletteSelectedObject by viewModel.paletteSelectedObject.collectAsStateWithLifecycle()
                val selectedObject by viewModel.selectedObject.collectAsStateWithLifecycle()
                val zoomScale by viewModel.zoomScale.collectAsStateWithLifecycle()
                val panOffset by viewModel.panOffset.collectAsStateWithLifecycle()
                val isTestPlaying by viewModel.isTestPlaying.collectAsStateWithLifecycle()
                val simState by viewModel.simState.collectAsStateWithLifecycle()

                // Translator states
                val sourceFormat by viewModel.sourceFormat.collectAsStateWithLifecycle()
                val targetFormat by viewModel.targetFormat.collectAsStateWithLifecycle()
                val translatorInput by viewModel.translatorInput.collectAsStateWithLifecycle()
                val translatorOutput by viewModel.translatorOutput.collectAsStateWithLifecycle()
                val translatorStatus by viewModel.translatorStatus.collectAsStateWithLifecycle()

                // Back navigation: if in Builder, Translator, or Library, return to Analyzer
                BackHandler(enabled = currentTab != AppTab.ANALYZER) {
                    viewModel.selectTab(AppTab.ANALYZER)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!isTestPlaying) {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF090D16),
                                    titleContentColor = Color.White
                                ),
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Cube icon marker
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(GdCyan)
                                                .border(1.5.dp, Color.White, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(GdLime)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "GD World Studio",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Active: ${activeLevel.name}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GdCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    Surface(
                                        color = Color(0xFF141F33),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GdCyan.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .clickable { viewModel.selectTab(AppTab.LIBRARY) }
                                    ) {
                                        Text(
                                            text = "Switch Level",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GdCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isTestPlaying) {
                            NavigationBar(
                                containerColor = Color(0xFF0A0E18),
                                contentColor = Color.White,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AppTab.ANALYZER,
                                    onClick = { viewModel.selectTab(AppTab.ANALYZER) },
                                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analyzer") },
                                    label = { Text("Analyzer") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GdCyan,
                                        indicatorColor = GdCyan,
                                        unselectedIconColor = Color(0xFF7E92B4),
                                        unselectedTextColor = Color(0xFF7E92B4)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_analyzer")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.BUILDER,
                                    onClick = { viewModel.selectTab(AppTab.BUILDER) },
                                    icon = { Icon(Icons.Default.Build, contentDescription = "Builder") },
                                    label = { Text("Builder") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GdLime,
                                        indicatorColor = GdLime,
                                        unselectedIconColor = Color(0xFF7E92B4),
                                        unselectedTextColor = Color(0xFF7E92B4)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_builder")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.TRANSLATOR,
                                    onClick = {
                                        viewModel.convertCurrentLevelToTarget()
                                        viewModel.selectTab(AppTab.TRANSLATOR)
                                    },
                                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Translator") },
                                    label = { Text("Translator") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = GdMagenta,
                                        indicatorColor = GdMagenta,
                                        unselectedIconColor = Color(0xFF7E92B4),
                                        unselectedTextColor = Color(0xFF7E92B4)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_translator")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppTab.LIBRARY,
                                    onClick = { viewModel.selectTab(AppTab.LIBRARY) },
                                    icon = { Icon(Icons.Default.Folder, contentDescription = "Worlds") },
                                    label = { Text("Worlds") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = Color.White,
                                        indicatorColor = Color(0xFF2B3A5A),
                                        unselectedIconColor = Color(0xFF7E92B4),
                                        unselectedTextColor = Color(0xFF7E92B4)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_library")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.ANALYZER -> {
                                AnalyzerScreen(
                                    level = activeLevel,
                                    report = analysisReport,
                                    onApplySuggestion = { viewModel.applySuggestion(it) },
                                    onNavigateToBuilder = { targetX ->
                                        // Center builder pan near target coordinate
                                        viewModel.setPan(-targetX + 120f, 0f)
                                        viewModel.selectTab(AppTab.BUILDER)
                                    }
                                )
                            }
                            AppTab.BUILDER -> {
                                BuilderScreen(
                                    level = activeLevel,
                                    builderTool = builderTool,
                                    paletteSelectedObject = paletteSelectedObject,
                                    selectedObject = selectedObject,
                                    zoomScale = zoomScale,
                                    panOffset = panOffset,
                                    isTestPlaying = isTestPlaying,
                                    simState = simState,
                                    onToolSelected = { viewModel.setBuilderTool(it) },
                                    onPaletteSelect = { id, type -> viewModel.selectPaletteObject(id, type) },
                                    onGridTap = { x, y -> viewModel.onGridTap(x, y) },
                                    onRotateSelected = { viewModel.rotateSelectedObject() },
                                    onDeleteSelected = { viewModel.deleteSelectedObject() },
                                    onZoomChange = { delta -> viewModel.setZoom(delta) },
                                    onPanChange = { px, py -> viewModel.setPan(px, py) },
                                    onUndo = { viewModel.undo() },
                                    onRedo = { viewModel.redo() },
                                    onStartTestPlay = { viewModel.startTestPlay() },
                                    onStopTestPlay = { viewModel.stopTestPlay() },
                                    onTestPlayInputDown = { viewModel.onTestPlayInputDown() },
                                    onTestPlayInputUp = { viewModel.onTestPlayInputUp() }
                                )
                            }
                            AppTab.TRANSLATOR -> {
                                TranslatorScreen(
                                    currentLevel = activeLevel,
                                    sourceFormat = sourceFormat,
                                    targetFormat = targetFormat,
                                    inputValue = translatorInput,
                                    outputValue = translatorOutput,
                                    statusMessage = translatorStatus,
                                    onSourceFormatChange = { viewModel.setSourceFormat(it) },
                                    onTargetFormatChange = { viewModel.setTargetFormat(it) },
                                    onInputChange = { viewModel.setTranslatorInput(it) },
                                    onTranslate = { viewModel.performTranslation() },
                                    onConvertCurrentLevel = { viewModel.convertCurrentLevelToTarget() },
                                    onImportToLibrary = { viewModel.importTranslatedLevelAsActive() }
                                )
                            }
                            AppTab.LIBRARY -> {
                                LevelLibraryScreen(
                                    levels = allLevels,
                                    activeLevel = activeLevel,
                                    onSelectLevel = {
                                        viewModel.selectLevel(it)
                                        viewModel.selectTab(AppTab.ANALYZER)
                                    },
                                    onOpenAnalyzer = {
                                        viewModel.selectLevel(it)
                                        viewModel.selectTab(AppTab.ANALYZER)
                                    },
                                    onOpenBuilder = {
                                        viewModel.selectLevel(it)
                                        viewModel.selectTab(AppTab.BUILDER)
                                    },
                                    onOpenTranslator = {
                                        viewModel.selectLevel(it)
                                        viewModel.convertCurrentLevelToTarget()
                                        viewModel.selectTab(AppTab.TRANSLATOR)
                                    },
                                    onCreateNewLevel = { name, diff ->
                                        viewModel.createNewEmptyLevel(name, diff)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
