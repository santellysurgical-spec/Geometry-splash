package com.example.model

enum class SuggestionCategory(val label: String, val iconName: String) {
    PACING("Pacing & Flow", "Speed"),
    SIGHTREADING("Sight-Reading", "Visibility"),
    RHYTHM_SYNC("Rhythm Sync", "MusicNote"),
    BUG_PREVENTION("Physics & Hitbox", "BugReport"),
    DIFFICULTY_SPIKE("Difficulty Balance", "Warning"),
    REWARD_PLACEMENT("Coins & Rewards", "Stars")
}

enum class SuggestionSeverity(val label: String, val colorHex: String) {
    CRITICAL("Critical", "#FF1744"),
    RECOMMENDED("Recommended", "#FF9100"),
    POLISH("Polish Tip", "#00E5FF")
}

enum class AutoFixType {
    ADD_SIGHTREAD_COIN,
    BUFFER_SPIKE_GAP,
    ALIGN_ORB_TO_BEAT,
    REMOVE_BLIND_JUMP,
    SMOOTH_PORTAL_TRANSITION
}

data class LevelImprovementSuggestion(
    val id: String,
    val title: String,
    val category: SuggestionCategory,
    val severity: SuggestionSeverity,
    val xPosition: Float,
    val yPosition: Float,
    val description: String,
    val actionableFix: String,
    val autoFixType: AutoFixType? = null
)

data class ChokePoint(
    val xStart: Float,
    val xEnd: Float,
    val progressPercent: Float,
    val deathCount: Int,
    val mortalityRate: Float, // 0.0 to 1.0
    val primaryCause: String,
    val severity: SuggestionSeverity
)

data class PacingSegment(
    val progressPercent: Float,
    val obstacleDensity: Float, // objects per 10 blocks
    val jumpFrequency: Float,   // required clicks per second
    val tensionLevel: Float     // 0.0 to 1.0
)

data class TapConsistencyMetrics(
    val averageTapDurationMs: Long,
    val tapJitterMs: Float,
    val earlyTapPercentage: Float,
    val lateTapPercentage: Float,
    val frameWindowMarginMs: Float,
    val holdingVsSpammingRatio: Float
)

data class AnalysisReport(
    val levelId: String,
    val calculatedDifficulty: GdDifficulty,
    val overallQualityScore: Int, // 0 to 100
    val rhythmSyncScore: Float,   // 0.0 to 100.0%
    val sightReadabilityScore: Float, // 0.0 to 100.0%
    val physicsReliabilityScore: Float, // 0.0 to 100.0%
    val pacingScore: Float, // 0.0 to 100.0%
    val chokePoints: List<ChokePoint>,
    val suggestions: List<LevelImprovementSuggestion>,
    val pacingCurve: List<PacingSegment>,
    val tapMetrics: TapConsistencyMetrics?,
    val totalAttemptsAnalyzed: Int,
    val estimatedClearRate: Float, // e.g. 1.2%
    val generatedSummary: String
)
