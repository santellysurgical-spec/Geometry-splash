package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.GameplayAttempt
import com.example.model.GdLevel
import com.example.translator.GdFormatTranslator
import com.example.translator.LevelFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val creator: String,
    val description: String,
    val difficulty: String,
    val songName: String,
    val bpm: Float,
    val objectCount: Int,
    val jsonPayload: String,
    val updatedAt: Long
)

@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey val attemptId: String,
    val levelId: String,
    val timestamp: Long,
    val progressPercent: Float,
    val deathX: Float?,
    val deathY: Float?,
    val deathReason: String?,
    val isCompleted: Boolean,
    val jumpCount: Int
)

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY updatedAt DESC")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    suspend fun getLevelById(id: String): LevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity)

    @Query("DELETE FROM levels WHERE id = :id")
    suspend fun deleteLevel(id: String)

    @Query("SELECT * FROM attempts WHERE levelId = :levelId ORDER BY timestamp DESC")
    fun getAttemptsForLevel(levelId: String): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE levelId = :levelId")
    suspend fun getAttemptsSnapshot(levelId: String): List<AttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: AttemptEntity)
}

@Database(entities = [LevelEntity::class, AttemptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gd_analyzer.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LevelRepository(private val dao: LevelDao) {

    val allLevels: Flow<List<GdLevel>> = dao.getAllLevels().map { entities ->
        entities.mapNotNull { entity ->
            GdFormatTranslator.decode(entity.jsonPayload, LevelFormat.GD_STUDIO_JSON).getOrNull()
        }
    }

    suspend fun getLevel(id: String): GdLevel? {
        val entity = dao.getLevelById(id) ?: return null
        return GdFormatTranslator.decode(entity.jsonPayload, LevelFormat.GD_STUDIO_JSON).getOrNull()
    }

    suspend fun saveLevel(level: GdLevel) {
        val json = GdFormatTranslator.encode(level, LevelFormat.GD_STUDIO_JSON)
        val entity = LevelEntity(
            id = level.id,
            name = level.name,
            creator = level.creator,
            description = level.description,
            difficulty = level.difficulty.name,
            songName = level.songName,
            bpm = level.bpm,
            objectCount = level.objects.size,
            jsonPayload = json,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertLevel(entity)
    }

    suspend fun deleteLevel(id: String) {
        dao.deleteLevel(id)
    }

    fun getAttempts(levelId: String): Flow<List<GameplayAttempt>> {
        return dao.getAttemptsForLevel(levelId).map { list ->
            list.map {
                GameplayAttempt(
                    attemptId = it.attemptId,
                    levelId = it.levelId,
                    timestamp = it.timestamp,
                    progressPercent = it.progressPercent,
                    deathX = it.deathX,
                    deathY = it.deathY,
                    deathReason = it.deathReason,
                    isCompleted = it.isCompleted,
                    jumpCount = it.jumpCount
                )
            }
        }
    }

    suspend fun getAttemptsSnapshot(levelId: String): List<GameplayAttempt> {
        return dao.getAttemptsSnapshot(levelId).map {
            GameplayAttempt(
                attemptId = it.attemptId,
                levelId = it.levelId,
                timestamp = it.timestamp,
                progressPercent = it.progressPercent,
                deathX = it.deathX,
                deathY = it.deathY,
                deathReason = it.deathReason,
                isCompleted = it.isCompleted,
                jumpCount = it.jumpCount
            )
        }
    }

    suspend fun recordAttempt(attempt: GameplayAttempt) {
        dao.insertAttempt(
            AttemptEntity(
                attemptId = attempt.attemptId,
                levelId = attempt.levelId,
                timestamp = attempt.timestamp,
                progressPercent = attempt.progressPercent,
                deathX = attempt.deathX,
                deathY = attempt.deathY,
                deathReason = attempt.deathReason,
                isCompleted = attempt.isCompleted,
                jumpCount = attempt.jumpCount
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        // Pre-populate with our 3 sample levels and their attempt data if database is empty
        val sampleLevels = listOf(SampleLevels.stereoPulse, SampleLevels.neonOdyssey, SampleLevels.infernoChasm)
        for (lvl in sampleLevels) {
            val existing = dao.getLevelById(lvl.id)
            if (existing == null) {
                saveLevel(lvl)
                val sampleAttempts = SampleLevels.getSampleAttempts(lvl.id)
                for (att in sampleAttempts) {
                    recordAttempt(att)
                }
            }
        }
    }
}
