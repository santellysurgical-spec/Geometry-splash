package com.example.translator

import com.example.model.GdDifficulty
import com.example.model.GdGameMode
import com.example.model.GdLevel
import com.example.model.GdObject
import com.example.model.GdObjectType
import com.example.model.GdSpeed
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import android.util.Base64

enum class LevelFormat(val displayName: String, val extension: String, val description: String) {
    ROBTOP_STRING("RobTop Level String", ".gmd", "Official Geometry Dash 2.1/2.2 delimited object string"),
    GD_STUDIO_JSON("GD World JSON", ".json", "Modern open cross-platform JSON level schema"),
    CSV_TABLE("CSV Object Matrix", ".csv", "Tabular comma-separated data for level architects"),
    COMPACT_HEX("Compact Code", ".gdc", "Ultra-compressed string for quick copy-paste and QR sharing")
}

object GdFormatTranslator {

    fun detectFormat(rawInput: String): LevelFormat {
        val trimmed = rawInput.trim()
        return when {
            trimmed.startsWith("{") && trimmed.contains("\"objects\"") -> LevelFormat.GD_STUDIO_JSON
            trimmed.startsWith("GDW:") -> LevelFormat.COMPACT_HEX
            trimmed.startsWith("id,type,x,y") || trimmed.contains("\n") && trimmed.split("\n")[0].split(",").size >= 4 && !trimmed.contains(";") -> LevelFormat.CSV_TABLE
            trimmed.contains(";") || trimmed.contains("kS38") || trimmed.startsWith("H4sI") || trimmed.contains("1,") -> LevelFormat.ROBTOP_STRING
            else -> LevelFormat.ROBTOP_STRING
        }
    }

    fun encode(level: GdLevel, format: LevelFormat): String {
        return when (format) {
            LevelFormat.ROBTOP_STRING -> encodeToRobTop(level)
            LevelFormat.GD_STUDIO_JSON -> encodeToJson(level)
            LevelFormat.CSV_TABLE -> encodeToCsv(level)
            LevelFormat.COMPACT_HEX -> encodeToCompactCode(level)
        }
    }

    fun decode(rawInput: String, hint: LevelFormat? = null): Result<GdLevel> {
        return runCatching {
            val format = hint ?: detectFormat(rawInput)
            val level = when (format) {
                LevelFormat.ROBTOP_STRING -> decodeFromRobTop(rawInput)
                LevelFormat.GD_STUDIO_JSON -> decodeFromJson(rawInput)
                LevelFormat.CSV_TABLE -> decodeFromCsv(rawInput)
                LevelFormat.COMPACT_HEX -> decodeFromCompactCode(rawInput)
            }
            repairLevel(level)
        }
    }

    // ----------------------------------------------------
    // ROBTOP STRING ENCODER / DECODER
    // ----------------------------------------------------

    private fun encodeToRobTop(level: GdLevel): String {
        val sb = StringBuilder()
        // Header info: kA13: speed, kA4: mode, etc. (Embedded in RobTop metadata if desired)
        // Standard RobTop object sequence: 1,objId,2,x,3,y,4,flipH,5,flipV,6,rot,21,color,32,scale,57,group;
        for (obj in level.objects) {
            sb.append("1,").append(obj.id)
            sb.append(",2,").append(String.format(java.util.Locale.US, "%.1f", obj.x))
            sb.append(",3,").append(String.format(java.util.Locale.US, "%.1f", obj.y))
            if (obj.rotation != 0f) {
                sb.append(",6,").append(String.format(java.util.Locale.US, "%.1f", obj.rotation))
            }
            if (obj.scale != 1.0f) {
                sb.append(",32,").append(String.format(java.util.Locale.US, "%.2f", obj.scale))
            }
            if (obj.isFlippedX) sb.append(",4,1")
            if (obj.isFlippedY) sb.append(",5,1")
            if (obj.colorChannel != 1) sb.append(",21,").append(obj.colorChannel)
            if (obj.groupId != 0) sb.append(",57,").append(obj.groupId)
            sb.append(";")
        }
        return sb.toString()
    }

    private fun decodeFromRobTop(rawInput: String): GdLevel {
        var cleanInput = rawInput.trim()
        
        // Handle Base64 / Gzip decompressed string if encountered (e.g. standard exported CCLocalLevels or kS38)
        if (cleanInput.startsWith("kS38,")) {
            val parts = cleanInput.split(",")
            val index = parts.indexOf("kS38")
            if (index != -1 && index + 1 < parts.size) {
                cleanInput = parts[index + 1]
            }
        }
        
        // Try Base64 gzip decoding if it looks like encoded payload
        if (cleanInput.length > 20 && !cleanInput.contains(";") && !cleanInput.contains(",")) {
            try {
                val decodedBytes = Base64.decode(cleanInput, Base64.DEFAULT)
                val gzipStream = GZIPInputStream(ByteArrayInputStream(decodedBytes))
                cleanInput = gzipStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (_: Exception) {
                // Not gzipped, keep original
            }
        }

        val objects = mutableListOf<GdObject>()
        val records = cleanInput.split(";")
        
        for (record in records) {
            val trimmed = record.trim()
            if (trimmed.isEmpty()) continue
            val tokens = trimmed.split(",")
            if (tokens.size < 4) continue

            var id = 1
            var x = 0f
            var y = 0f
            var rot = 0f
            var scale = 1f
            var flipX = false
            var flipY = false
            var colorCh = 1
            var grp = 0

            var i = 0
            while (i < tokens.size - 1) {
                val key = tokens[i].trim()
                val value = tokens[i + 1].trim()
                when (key) {
                    "1" -> id = value.toIntOrNull() ?: 1
                    "2" -> x = value.toFloatOrNull() ?: 0f
                    "3" -> y = value.toFloatOrNull() ?: 0f
                    "4" -> flipX = value == "1"
                    "5" -> flipY = value == "1"
                    "6" -> rot = value.toFloatOrNull() ?: 0f
                    "21" -> colorCh = value.toIntOrNull() ?: 1
                    "32" -> scale = value.toFloatOrNull() ?: 1f
                    "57" -> grp = value.toIntOrNull() ?: 0
                }
                i += 2
            }

            val type = mapIdToType(id)
            objects.add(
                GdObject(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    rotation = rot,
                    scale = scale,
                    colorChannel = colorCh,
                    groupId = grp,
                    isFlippedX = flipX,
                    isFlippedY = flipY
                )
            )
        }

        return GdLevel(
            name = "Imported RobTop Level",
            creator = "GD Player",
            description = "Imported from RobTop string (${objects.size} objects)",
            difficulty = guessDifficulty(objects),
            objects = objects
        )
    }

    // ----------------------------------------------------
    // GD STUDIO JSON ENCODER / DECODER
    // ----------------------------------------------------

    private fun encodeToJson(level: GdLevel): String {
        val root = JSONObject()
        root.put("format", "GD_STUDIO_V1")
        root.put("id", level.id)
        root.put("name", level.name)
        root.put("creator", level.creator)
        root.put("description", level.description)
        root.put("difficulty", level.difficulty.name)
        root.put("songName", level.songName)
        root.put("songArtist", level.songArtist)
        root.put("bpm", level.bpm)
        root.put("songOffset", level.songOffset)
        root.put("initialGameMode", level.initialGameMode.name)
        root.put("initialSpeed", level.initialSpeed.name)
        root.put("backgroundColorHex", level.backgroundColorHex)
        root.put("groundColorHex", level.groundColorHex)
        root.put("lengthSeconds", level.lengthSeconds)

        val arr = JSONArray()
        for (obj in level.objects) {
            val item = JSONObject()
            item.put("id", obj.id)
            item.put("type", obj.type.name)
            item.put("x", obj.x)
            item.put("y", obj.y)
            if (obj.rotation != 0f) item.put("rot", obj.rotation)
            if (obj.scale != 1.0f) item.put("scale", obj.scale)
            if (obj.colorChannel != 1) item.put("col", obj.colorChannel)
            if (obj.groupId != 0) item.put("grp", obj.groupId)
            if (obj.isFlippedX) item.put("fx", true)
            if (obj.isFlippedY) item.put("fy", true)
            arr.put(item)
        }
        root.put("objects", arr)

        return root.toString(2)
    }

    private fun decodeFromJson(raw: String): GdLevel {
        val root = JSONObject(raw)
        val name = root.optString("name", "Imported Level")
        val creator = root.optString("creator", "Unknown")
        val desc = root.optString("description", "")
        val diffName = root.optString("difficulty", "NORMAL")
        val difficulty = runCatching { GdDifficulty.valueOf(diffName) }.getOrDefault(GdDifficulty.NORMAL)
        val songName = root.optString("songName", "Stereo Madness")
        val songArtist = root.optString("songArtist", "ForeverBound")
        val bpm = root.optDouble("bpm", 140.0).toFloat()
        val initialModeName = root.optString("initialGameMode", "CUBE")
        val initialMode = runCatching { GdGameMode.valueOf(initialModeName) }.getOrDefault(GdGameMode.CUBE)
        val initialSpeedName = root.optString("initialSpeed", "NORMAL")
        val initialSpeed = runCatching { GdSpeed.valueOf(initialSpeedName) }.getOrDefault(GdSpeed.NORMAL)
        val bg = root.optString("backgroundColorHex", "#0A0E17")
        val ground = root.optString("groundColorHex", "#00F0FF")

        val objects = mutableListOf<GdObject>()
        val arr = root.optJSONArray("objects") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val id = item.getInt("id")
            val typeStr = item.optString("type", mapIdToType(id).name)
            val type = runCatching { GdObjectType.valueOf(typeStr) }.getOrDefault(mapIdToType(id))
            val x = item.getDouble("x").toFloat()
            val y = item.getDouble("y").toFloat()
            val rot = item.optDouble("rot", 0.0).toFloat()
            val scale = item.optDouble("scale", 1.0).toFloat()
            val col = item.optInt("col", 1)
            val grp = item.optInt("grp", 0)
            val fx = item.optBoolean("fx", false)
            val fy = item.optBoolean("fy", false)

            objects.add(
                GdObject(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    rotation = rot,
                    scale = scale,
                    colorChannel = col,
                    groupId = grp,
                    isFlippedX = fx,
                    isFlippedY = fy
                )
            )
        }

        return GdLevel(
            name = name,
            creator = creator,
            description = desc,
            difficulty = difficulty,
            songName = songName,
            songArtist = songArtist,
            bpm = bpm,
            initialGameMode = initialMode,
            initialSpeed = initialSpeed,
            backgroundColorHex = bg,
            groundColorHex = ground,
            objects = objects
        )
    }

    // ----------------------------------------------------
    // CSV TABLE ENCODER / DECODER
    // ----------------------------------------------------

    private fun encodeToCsv(level: GdLevel): String {
        val sb = StringBuilder()
        sb.append("# GD Level: ${level.name} | Creator: ${level.creator} | Difficulty: ${level.difficulty.name}\n")
        sb.append("id,type,x,y,rotation,scale,colorChannel,groupId,flipX,flipY\n")
        for (o in level.objects) {
            sb.append("${o.id},${o.type.name},${o.x},${o.y},${o.rotation},${o.scale},${o.colorChannel},${o.groupId},${o.isFlippedX},${o.isFlippedY}\n")
        }
        return sb.toString()
    }

    private fun decodeFromCsv(raw: String): GdLevel {
        val objects = mutableListOf<GdObject>()
        val lines = raw.lines()
        var levelName = "Imported CSV Level"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#")) {
                if (trimmed.contains("GD Level:")) {
                    levelName = trimmed.substringAfter("GD Level:").substringBefore("|").trim()
                }
                continue
            }
            if (trimmed.startsWith("id,") || trimmed.isEmpty()) continue

            val p = trimmed.split(",")
            if (p.size >= 4) {
                val id = p[0].toIntOrNull() ?: 1
                val type = runCatching { GdObjectType.valueOf(p[1]) }.getOrDefault(mapIdToType(id))
                val x = p[2].toFloatOrNull() ?: 0f
                val y = p[3].toFloatOrNull() ?: 0f
                val rot = p.getOrNull(4)?.toFloatOrNull() ?: 0f
                val scale = p.getOrNull(5)?.toFloatOrNull() ?: 1.0f
                val col = p.getOrNull(6)?.toIntOrNull() ?: 1
                val grp = p.getOrNull(7)?.toIntOrNull() ?: 0
                val fx = p.getOrNull(8)?.toBooleanStrictOrNull() ?: false
                val fy = p.getOrNull(9)?.toBooleanStrictOrNull() ?: false

                objects.add(
                    GdObject(
                        id = id,
                        type = type,
                        x = x,
                        y = y,
                        rotation = rot,
                        scale = scale,
                        colorChannel = col,
                        groupId = grp,
                        isFlippedX = fx,
                        isFlippedY = fy
                    )
                )
            }
        }

        return GdLevel(
            name = levelName,
            objects = objects,
            difficulty = guessDifficulty(objects)
        )
    }

    // ----------------------------------------------------
    // COMPACT HEX / SHORT CODE ENCODER / DECODER
    // ----------------------------------------------------

    private fun encodeToCompactCode(level: GdLevel): String {
        val robTop = encodeToRobTop(level)
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzip ->
            gzip.write(robTop.toByteArray(Charsets.UTF_8))
        }
        val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        return "GDW:$b64"
    }

    private fun decodeFromCompactCode(raw: String): GdLevel {
        val payload = if (raw.startsWith("GDW:")) raw.substring(4) else raw
        val compressedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val decompressed = GZIPInputStream(ByteArrayInputStream(compressedBytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val decoded = decodeFromRobTop(decompressed)
        return decoded.copy(name = "Decompressed World Level")
    }

    // ----------------------------------------------------
    // LEVEL REPAIR & VALIDATION
    // ----------------------------------------------------

    fun repairLevel(level: GdLevel): GdLevel {
        // 1. Sort by X coordinate
        // 2. Remove identical duplicates (same ID at exact same x, y within 0.1 units)
        // 3. Round coordinates to 0.5 units to prevent micro-stutter
        val seen = mutableSetOf<String>()
        val filtered = mutableListOf<GdObject>()

        val sorted = level.objects.sortedBy { it.x }
        for (obj in sorted) {
            val key = "${obj.id}_${(obj.x * 2).toInt()}_${(obj.y * 2).toInt()}"
            if (seen.add(key)) {
                filtered.add(
                    obj.copy(
                        x = kotlin.math.round(obj.x * 10f) / 10f,
                        y = kotlin.math.round(obj.y * 10f) / 10f
                    )
                )
            }
        }

        return level.copy(
            objects = filtered,
            difficulty = if (level.difficulty == GdDifficulty.NORMAL) guessDifficulty(filtered) else level.difficulty
        )
    }

    fun mapIdToType(id: Int): GdObjectType {
        return when (id) {
            1, 2, 3, 4, 6, 7 -> GdObjectType.SOLID
            8, 9, 39, 103, 134, 183 -> GdObjectType.HAZARD
            35, 36, 67, 84, 140, 141 -> GdObjectType.ORB
            10, 11, 12, 13 -> GdObjectType.PAD
            101, 102, 103, 104, 105, 106, 107 -> GdObjectType.PORTAL_MODE
            108, 109 -> GdObjectType.PORTAL_GRAVITY
            110, 111, 112, 113, 114 -> GdObjectType.PORTAL_SPEED
            115, 116 -> GdObjectType.PORTAL_SIZE
            117, 118 -> GdObjectType.PORTAL_DUAL
            1329 -> GdObjectType.COLLECTIBLE
            1330, 1331, 1332 -> GdObjectType.TRIGGER
            else -> if (id in 200..300) GdObjectType.DECOR else GdObjectType.SOLID
        }
    }

    private fun guessDifficulty(objects: List<GdObject>): GdDifficulty {
        val total = objects.size
        if (total == 0) return GdDifficulty.AUTO
        val hazards = objects.count { it.type == GdObjectType.HAZARD }
        val hazardRatio = hazards.toFloat() / total.coerceAtLeast(1)

        val tightSpikes = objects.windowed(2).count { pair ->
            pair[0].type == GdObjectType.HAZARD && pair[1].type == GdObjectType.HAZARD && (pair[1].x - pair[0].x) in 25f..35f
        }

        return when {
            total < 25 -> GdDifficulty.EASY
            tightSpikes > 15 || hazardRatio > 0.45f -> GdDifficulty.HARD_DEMON
            tightSpikes > 6 || hazardRatio > 0.35f -> GdDifficulty.INSANE
            tightSpikes > 2 || hazardRatio > 0.25f -> GdDifficulty.HARD
            hazardRatio > 0.15f -> GdDifficulty.NORMAL
            else -> GdDifficulty.EASY
        }
    }
}
