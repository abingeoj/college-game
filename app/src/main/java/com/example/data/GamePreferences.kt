package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CharacterId

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("college_runner_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOTAL_COINS = "key_total_coins"
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_BEST_DISTANCE = "key_best_distance"
        private const val KEY_BEST_CGPA = "key_best_cgpa"
        private const val KEY_TOTAL_GRADUATIONS = "key_total_graduations"
        private const val KEY_UNLOCKED_CHARACTERS = "key_unlocked_characters"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
    }

    var totalCoins: Int
        get() = prefs.getInt(KEY_TOTAL_COINS, 50) // start with 50 coins welcome bonus
        set(value) = prefs.edit().putInt(KEY_TOTAL_COINS, value).apply()

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    var bestDistance: Float
        get() = prefs.getFloat(KEY_BEST_DISTANCE, 0f)
        set(value) = prefs.edit().putFloat(KEY_BEST_DISTANCE, value).apply()

    var bestCgpa: Float
        get() = prefs.getFloat(KEY_BEST_CGPA, 8.0f)
        set(value) = prefs.edit().putFloat(KEY_BEST_CGPA, value).apply()

    var totalGraduations: Int
        get() = prefs.getInt(KEY_TOTAL_GRADUATIONS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_GRADUATIONS, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    fun getUnlockedCharacters(): Set<CharacterId> {
        val raw = prefs.getStringSet(
            KEY_UNLOCKED_CHARACTERS,
            setOf(CharacterId.SWAGGER.name, CharacterId.RUSHER.name)
        ) ?: setOf(CharacterId.SWAGGER.name, CharacterId.RUSHER.name)

        return raw.mapNotNull {
            try {
                CharacterId.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    fun unlockCharacter(id: CharacterId) {
        val current = getUnlockedCharacters().map { it.name }.toMutableSet()
        current.add(id.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_CHARACTERS, current).apply()
    }

    fun isCharacterUnlocked(id: CharacterId): Boolean {
        if (id == CharacterId.SWAGGER || id == CharacterId.RUSHER) return true
        return getUnlockedCharacters().contains(id)
    }

    fun recordRunResult(
        score: Int,
        coinsEarned: Int,
        distance: Float,
        finalCgpa: Float,
        graduated: Boolean
    ) {
        totalCoins += coinsEarned
        if (score > highScore) highScore = score
        if (distance > bestDistance) bestDistance = distance
        if (finalCgpa > bestCgpa) bestCgpa = finalCgpa
        if (graduated) {
            totalGraduations += 1
        }
    }
}
