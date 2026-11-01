package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analyzer.GdGameplayAnalyzer
import com.example.data.AppDatabase
import com.example.data.LevelRepository
import com.example.data.SampleLevels
import com.example.model.AnalysisReport
import com.example.model.AutoFixType
import com.example.model.GameplayAttempt
import com.example.model.GdDifficulty
import com.example.model.GdGameMode
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.GdSpeed
import com.example.model.LevelImprovementSuggestion
import com.example.model.PlayerInputEvent
import com.example.simulation.GdPhysicsSimulator
import com.example.simulation.SimulationState
import com.example.translator.GdFormatTranslator
import com.example.translator.LevelFormat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab(val title: String) {
    LIBRARY("Levels"),
    ANALYZER("Analyzer"),
    BUILDER("Builder"),
    TRANSLATOR("Translator")
}

enum class BuilderTool {
    SELECT,
    PLACE,
    ERASE
}

class GdAppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LevelRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = LevelRepository(db.levelDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val allLevels: StateFlow<List<GdLevel>> = repository.allLevels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(SampleLevels.stereoPulse, SampleLevels.neonOdyssey, SampleLevels.infernoChasm)
    )

    private val _currentTab = MutableStateFlow(AppTab.ANALYZER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _currentLevel = MutableStateFlow<GdLevel>(SampleLevels.infernoChasm)
    val currentLevel: StateFlow<GdLevel> = _currentLevel.asStateFlow()

    private val _attempts = MutableStateFlow<List<GameplayAttempt>>(SampleLevels.getSampleAttempts(SampleLevels.infernoChasm.id))
    val attempts: StateFlow<List<GameplayAttempt>> = _attempts.asStateFlow()

    private val _analysisReport = MutableStateFlow<AnalysisReport?>(null)
    val analysisReport: StateFlow<AnalysisReport?> = _analysisReport.asStateFlow()

    // ----------------------------------------------------
    // BUILDER STATE
    // ----------------------------------------------------
    private val _builderTool = MutableStateFlow(BuilderTool.PLACE)
    val builderTool: StateFlow<BuilderTool> = _builderTool.asStateFlow()

    private val _paletteSelectedObject = MutableStateFlow(
        GdObject(id = 1, type = GdObjectType.SOLID, x = 0f, y = 0f)
    )
    val paletteSelectedObject: StateFlow<GdObject> = _paletteSelectedObject.asStateFlow()

    private val _selectedObject = MutableStateFlow<GdObject?>(null)
    val selectedObject: StateFlow<GdObject?> = _selectedObject.asStateFlow()

    private val _zoomScale = MutableStateFlow(1.0f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    private val _panOffset = MutableStateFlow(Pair(0f, 0f))
    val panOffset: StateFlow<Pair<Float, Float>> = _panOffset.asStateFlow()

    private val undoStack = mutableListOf<List<GdObject>>()
    private val redoStack = mutableListOf<List<GdObject>>()

    // Test Play Mode inside Builder
    private val _isTestPlaying = MutableStateFlow(false)
    val isTestPlaying: StateFlow<Boolean> = _isTestPlaying.asStateFlow()

    private val _simState = MutableStateFlow(SimulationState())
    val simState: StateFlow<SimulationState> = _simState.asStateFlow()

    private var simJob: Job? = null
    private var isHoldingJump = false
    private var hasJustTapped = false

    // ----------------------------------------------------
    // TRANSLATOR STATE
    // ----------------------------------------------------
    private val _sourceFormat = MutableStateFlow(LevelFormat.ROBTOP_STRING)
    val sourceFormat: StateFlow<LevelFormat> = _sourceFormat.asStateFlow()

    private val _targetFormat = MutableStateFlow(LevelFormat.GD_STUDIO_JSON)
    val targetFormat: StateFlow<LevelFormat> = _targetFormat.asStateFlow()

    private val _translatorInput = MutableStateFlow("")
    val translatorInput: StateFlow<String> = _translatorInput.asStateFlow()

    private val _translatorOutput = MutableStateFlow("")
    val translatorOutput: StateFlow<String> = _translatorOutput.asStateFlow()

    private val _translatorStatus = MutableStateFlow<String?>(null)
    val translatorStatus: StateFlow<String?> = _translatorStatus.asStateFlow()

    init {
        // Initialize analysis
        recalculateAnalysis()
    }

    fun selectTab(tab: AppTab) {
        if (_isTestPlaying.value && tab != AppTab.BUILDER) {
            stopTestPlay()
        }
        _currentTab.value = tab
    }

    fun selectLevel(level: GdLevel) {
        _currentLevel.value = level
        viewModelScope.launch {
            val loadedAttempts = repository.getAttemptsSnapshot(level.id)
            _attempts.value = if (loadedAttempts.isNotEmpty()) loadedAttempts else SampleLevels.getSampleAttempts(level.id)
            recalculateAnalysis()
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun recalculateAnalysis() {
        val level = _currentLevel.value
        val atts = _attempts.value
        _analysisReport.value = GdGameplayAnalyzer.analyze(level, atts)
    }

    // ----------------------------------------------------
    // BUILDER ACTIONS
    // ----------------------------------------------------

    fun setBuilderTool(tool: BuilderTool) {
        _builderTool.value = tool
        if (tool != BuilderTool.SELECT) {
            _selectedObject.value = null
        }
    }

    fun selectPaletteObject(id: Int, type: GdObjectType) {
        _paletteSelectedObject.value = GdObject(id = id, type = type, x = 0f, y = 0f)
        _builderTool.value = BuilderTool.PLACE
    }

    fun onGridTap(gridX: Float, gridY: Float) {
        val snapX = (kotlin.math.round(gridX / 30f) * 30f).coerceAtLeast(30f)
        val snapY = (kotlin.math.round(gridY / 15f) * 15f).coerceAtLeast(15f)

        when (_builderTool.value) {
            BuilderTool.PLACE -> {
                saveUndoState()
                val template = _paletteSelectedObject.value
                val newObj = template.copy(x = snapX, y = snapY)
                val updated = _currentLevel.value.objects.filterNot { it.x == snapX && it.y == snapY } + newObj
                updateLevelObjects(updated)
            }
            BuilderTool.ERASE -> {
                val existing = _currentLevel.value.objects.firstOrNull { kotlin.math.abs(it.x - snapX) < 18f && kotlin.math.abs(it.y - snapY) < 18f }
                if (existing != null) {
                    saveUndoState()
                    val updated = _currentLevel.value.objects.filterNot { it == existing }
                    updateLevelObjects(updated)
                }
            }
            BuilderTool.SELECT -> {
                val found = _currentLevel.value.objects.firstOrNull { kotlin.math.abs(it.x - snapX) < 22f && kotlin.math.abs(it.y - snapY) < 22f }
                _selectedObject.value = found
            }
        }
    }

    fun rotateSelectedObject() {
        val cur = _selectedObject.value ?: return
        val newRot = (cur.rotation + 90f) % 360f
        val updatedObj = cur.copy(rotation = newRot)
        _selectedObject.value = updatedObj
        saveUndoState()
        val list = _currentLevel.value.objects.map { if (it == cur) updatedObj else it }
        updateLevelObjects(list)
    }

    fun deleteSelectedObject() {
        val cur = _selectedObject.value ?: return
        saveUndoState()
        val list = _currentLevel.value.objects.filterNot { it == cur }
        _selectedObject.value = null
        updateLevelObjects(list)
    }

    fun setZoom(delta: Float) {
        _zoomScale.value = (_zoomScale.value + delta).coerceIn(0.5f, 2.5f)
    }

    fun setPan(newX: Float, newY: Float) {
        _panOffset.value = Pair(newX, newY)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_currentLevel.value.objects)
            _currentLevel.value = _currentLevel.value.copy(objects = prev)
            recalculateAnalysis()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_currentLevel.value.objects)
            _currentLevel.value = _currentLevel.value.copy(objects = next)
            recalculateAnalysis()
        }
    }

    private fun saveUndoState() {
        undoStack.add(_currentLevel.value.objects)
        redoStack.clear()
        if (undoStack.size > 30) undoStack.removeAt(0)
    }

    private fun updateLevelObjects(newObjects: List<GdObject>) {
        val updated = _currentLevel.value.copy(
            objects = newObjects,
            updatedAt = System.currentTimeMillis()
        )
        _currentLevel.value = updated
        viewModelScope.launch {
            repository.saveLevel(updated)
            recalculateAnalysis()
        }
    }

    // ----------------------------------------------------
    // TEST PLAY & ATTEMPT SIMULATION
    // ----------------------------------------------------

    fun startTestPlay() {
        _isTestPlaying.value = true
        _simState.value = SimulationState(
            x = 0f,
            y = 15f,
            currentMode = _currentLevel.value.initialGameMode,
            gravityDirection = 1.0f
        )
        isHoldingJump = false
        hasJustTapped = false

        simJob?.cancel()
        simJob = viewModelScope.launch {
            val level = _currentLevel.value
            while (isActive && _isTestPlaying.value) {
                delay(16) // ~60 FPS
                val currentState = _simState.value
                val nextState = GdPhysicsSimulator.tick(
                    state = currentState,
                    level = level,
                    isHoldingTap = isHoldingJump,
                    hasJustTapped = hasJustTapped
                )
                hasJustTapped = false
                _simState.value = nextState

                if (nextState.isDead || nextState.isWon) {
                    recordSimulationAttempt(nextState)
                    delay(800)
                    // Auto-restart run
                    _simState.value = SimulationState(
                        x = 0f,
                        y = 15f,
                        currentMode = level.initialGameMode,
                        gravityDirection = 1.0f
                    )
                }
            }
        }
    }

    fun stopTestPlay() {
        _isTestPlaying.value = false
        simJob?.cancel()
        simJob = null
    }

    fun onTestPlayInputDown() {
        isHoldingJump = true
        hasJustTapped = true
    }

    fun onTestPlayInputUp() {
        isHoldingJump = false
    }

    private fun recordSimulationAttempt(finalState: SimulationState) {
        val level = _currentLevel.value
        val attempt = GameplayAttempt(
            levelId = level.id,
            progressPercent = finalState.attemptProgress,
            deathX = if (finalState.isDead) finalState.x else null,
            deathY = if (finalState.isDead) finalState.y else null,
            deathReason = finalState.deathReason,
            isCompleted = finalState.isWon,
            jumpCount = finalState.jumpCount
        )
        viewModelScope.launch {
            repository.recordAttempt(attempt)
            _attempts.value = _attempts.value + attempt
            recalculateAnalysis()
        }
    }

    // ----------------------------------------------------
    // ACTIONABLE SUGGESTION FIX APPLICATION
    // ----------------------------------------------------

    fun applySuggestion(suggestion: LevelImprovementSuggestion) {
        saveUndoState()
        val curObjects = _currentLevel.value.objects.toMutableList()
        val fixType = suggestion.autoFixType ?: return

        when (fixType) {
            AutoFixType.BUFFER_SPIKE_GAP -> {
                // Move or eliminate overlapping spike around suggestion coordinate
                val nearbyHazard = curObjects.firstOrNull { it.type == GdObjectType.HAZARD && kotlin.math.abs(it.x - suggestion.xPosition) < 35f }
                if (nearbyHazard != null) {
                    curObjects.remove(nearbyHazard)
                    // Move 30 units further right to create safe buffer
                    curObjects.add(nearbyHazard.copy(x = nearbyHazard.x + 30f))
                }
            }
            AutoFixType.ADD_SIGHTREAD_COIN -> {
                // Place a guide coin / indicator to telegraph safe path
                curObjects.add(
                    GdObject(
                        id = 1329,
                        type = GdObjectType.COLLECTIBLE,
                        x = suggestion.xPosition,
                        y = suggestion.yPosition + 25f
                    )
                )
            }
            AutoFixType.ALIGN_ORB_TO_BEAT -> {
                // Shift orb to musical beat grid
                val orb = curObjects.firstOrNull { it.type == GdObjectType.ORB && kotlin.math.abs(it.x - suggestion.xPosition) < 20f }
                if (orb != null) {
                    curObjects.remove(orb)
                    val bpm = _currentLevel.value.bpm
                    val beatInterval = (311.4f * _currentLevel.value.initialSpeed.multiplier) * (60f / bpm) / 2f
                    val snappedX = kotlin.math.round(orb.x / beatInterval) * beatInterval
                    curObjects.add(orb.copy(x = snappedX))
                }
            }
            AutoFixType.SMOOTH_PORTAL_TRANSITION -> {
                // Shift any hazard that is too close to a portal by +45 units
                val hazards = curObjects.filter { it.type == GdObjectType.HAZARD && it.x in suggestion.xPosition..(suggestion.xPosition + 60f) }
                for (h in hazards) {
                    curObjects.remove(h)
                    curObjects.add(h.copy(x = h.x + 45f))
                }
            }
            AutoFixType.REMOVE_BLIND_JUMP -> {
                // Add a landing platform
                curObjects.add(
                    GdObject(
                        id = 1,
                        type = GdObjectType.SOLID,
                        x = suggestion.xPosition + 30f,
                        y = suggestion.yPosition
                    )
                )
            }
        }

        updateLevelObjects(curObjects)
    }

    // ----------------------------------------------------
    // FORMAT TRANSLATION & IMPORT / EXPORT
    // ----------------------------------------------------

    fun setSourceFormat(fmt: LevelFormat) {
        _sourceFormat.value = fmt
    }

    fun setTargetFormat(fmt: LevelFormat) {
        _targetFormat.value = fmt
        // Auto convert if current level or input is available
        convertCurrentLevelToTarget()
    }

    fun setTranslatorInput(text: String) {
        _translatorInput.value = text
        _translatorStatus.value = null
    }

    fun convertCurrentLevelToTarget() {
        val level = _currentLevel.value
        val result = GdFormatTranslator.encode(level, _targetFormat.value)
        _translatorOutput.value = result
    }

    fun performTranslation() {
        val raw = _translatorInput.value.trim()
        if (raw.isEmpty()) {
            _translatorStatus.value = "Input is empty. Paste level data to translate."
            return
        }

        val decodedResult = GdFormatTranslator.decode(raw, _sourceFormat.value)
        decodedResult.onSuccess { parsedLevel ->
            val converted = GdFormatTranslator.encode(parsedLevel, _targetFormat.value)
            _translatorOutput.value = converted
            _translatorStatus.value = "Successfully translated ${parsedLevel.totalObjects} objects to ${_targetFormat.value.displayName}!"
        }.onFailure { err ->
            _translatorStatus.value = "Translation error: ${err.message ?: "Invalid format"}"
        }
    }

    fun importTranslatedLevelAsActive() {
        val raw = _translatorInput.value.trim()
        val decodedResult = if (raw.isNotEmpty()) {
            GdFormatTranslator.decode(raw, _sourceFormat.value)
        } else {
            Result.success(_currentLevel.value)
        }

        decodedResult.onSuccess { imported ->
            val fullLevel = imported.copy(
                id = UUID.randomUUID().toString(),
                name = if (imported.name.startsWith("Imported")) imported.name else "Imported - ${imported.name}"
            )
            viewModelScope.launch {
                repository.saveLevel(fullLevel)
                _currentLevel.value = fullLevel
                _attempts.value = emptyList()
                recalculateAnalysis()
                _translatorStatus.value = "Imported \"${fullLevel.name}\" into World Library!"
                _currentTab.value = AppTab.ANALYZER
            }
        }.onFailure {
            _translatorStatus.value = "Failed to import: ${it.message}"
        }
    }

    fun createNewEmptyLevel(name: String, difficulty: GdDifficulty) {
        val newLvl = GdLevel(
            name = name.ifBlank { "New Geometry World" },
            difficulty = difficulty,
            objects = listOf(
                GdObject(id = 1, type = GdObjectType.SOLID, x = 30f, y = 15f),
                GdObject(id = 1, type = GdObjectType.SOLID, x = 60f, y = 15f),
                GdObject(id = 8, type = GdObjectType.HAZARD, x = 120f, y = 15f),
                GdObject(id = 10, type = GdObjectType.PAD, x = 180f, y = 15f)
            )
        )
        viewModelScope.launch {
            repository.saveLevel(newLvl)
            selectLevel(newLvl)
            _currentTab.value = AppTab.BUILDER
        }
    }
}
