package com.example

import com.example.analyzer.GdGameplayAnalyzer
import com.example.data.SampleLevels
import com.example.model.GdDifficulty
import com.example.model.GdGameMode
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.GdSpeed
import com.example.simulation.GdPhysicsSimulator
import com.example.simulation.SimulationState
import com.example.translator.GdFormatTranslator
import com.example.translator.LevelFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GdFormatAndAnalyzerTest {

    @Test
    fun testRobTopFormatEncodeDecode() {
        val level = GdLevel(
            name = "Test World",
            objects = listOf(
                GdObject(id = 1, type = GdObjectType.SOLID, x = 60f, y = 15f),
                GdObject(id = 8, type = GdObjectType.HAZARD, x = 120f, y = 15f),
                GdObject(id = 35, type = GdObjectType.ORB, x = 180f, y = 45f)
            )
        )

        val encoded = GdFormatTranslator.encode(level, LevelFormat.ROBTOP_STRING)
        assertTrue(encoded.contains("1,1,2,60.0,3,15.0"))
        assertTrue(encoded.contains("1,8,2,120.0,3,15.0"))
        assertTrue(encoded.contains("1,35,2,180.0,3,45.0"))

        val decodedResult = GdFormatTranslator.decode(encoded, LevelFormat.ROBTOP_STRING)
        assertTrue(decodedResult.isSuccess)
        val decodedLevel = decodedResult.getOrThrow()
        assertEquals(3, decodedLevel.objects.size)
        assertEquals(60f, decodedLevel.objects[0].x, 0.5f)
        assertEquals(120f, decodedLevel.objects[1].x, 0.5f)
    }

    @Test
    fun testJsonFormatEncodeDecode() {
        val level = SampleLevels.stereoPulse
        val encoded = GdFormatTranslator.encode(level, LevelFormat.GD_STUDIO_JSON)
        assertTrue(encoded.contains("\"format\": \"GD_STUDIO_V1\""))
        assertTrue(encoded.contains("\"name\": \"Stereo Pulse\""))

        val decodedResult = GdFormatTranslator.decode(encoded, LevelFormat.GD_STUDIO_JSON)
        assertTrue(decodedResult.isSuccess)
        val decoded = decodedResult.getOrThrow()
        assertEquals(level.name, decoded.name)
        assertEquals(level.objects.size, decoded.objects.size)
    }

    @Test
    fun testCsvFormatEncodeDecode() {
        val level = SampleLevels.neonOdyssey
        val csv = GdFormatTranslator.encode(level, LevelFormat.CSV_TABLE)
        assertTrue(csv.contains("id,type,x,y"))

        val decodedResult = GdFormatTranslator.decode(csv, LevelFormat.CSV_TABLE)
        assertTrue(decodedResult.isSuccess)
        val decoded = decodedResult.getOrThrow()
        assertEquals(level.objects.size, decoded.objects.size)
    }

    @Test
    fun testFormatDetection() {
        assertEquals(LevelFormat.GD_STUDIO_JSON, GdFormatTranslator.detectFormat("{\"format\":\"GD_STUDIO_V1\",\"objects\":[]}"))
        assertEquals(LevelFormat.COMPACT_HEX, GdFormatTranslator.detectFormat("GDW:SGVsbG8gR0Q="))
        assertEquals(LevelFormat.ROBTOP_STRING, GdFormatTranslator.detectFormat("1,1,2,60,3,15;1,8,2,120,3,15;"))
    }

    @Test
    fun testGameplayAnalyzerOnInfernoChasm() {
        val level = SampleLevels.infernoChasm
        val attempts = SampleLevels.getSampleAttempts(level.id)
        val report = GdGameplayAnalyzer.analyze(level, attempts)

        assertNotNull(report)
        // Inferno chasm has critical deaths on spike and blind drop
        assertTrue(report.chokePoints.isNotEmpty())
        assertTrue(report.suggestions.isNotEmpty())
        assertTrue(report.pacingCurve.isNotEmpty())
    }

    @Test
    fun testPhysicsSimulationTick() {
        val level = SampleLevels.stereoPulse
        var state = SimulationState(x = 0f, y = 15f)

        // Advance 10 ticks with jump input
        for (i in 0 until 10) {
            state = GdPhysicsSimulator.tick(
                state = state,
                level = level,
                isHoldingTap = false,
                hasJustTapped = (i == 0)
            )
        }

        assertTrue(state.x > 0f)
        assertTrue(state.y >= 15f)
    }
}
