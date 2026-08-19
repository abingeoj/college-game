package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GamePreferences
import com.example.engine.GameEngine
import com.example.engine.SoundSynthesizer
import com.example.model.AcademicYearStage
import com.example.model.CharacterId
import com.example.model.CharacterProfile
import com.example.model.GamePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("College Runner", appName)
  }

  @Test
  fun `verify four academic stages distance progression`() {
    assertEquals(AcademicYearStage.YEAR_1, AcademicYearStage.getStageForDistance(0f))
    assertEquals(AcademicYearStage.YEAR_1, AcademicYearStage.getStageForDistance(1199f))
    assertEquals(AcademicYearStage.YEAR_2, AcademicYearStage.getStageForDistance(1200f))
    assertEquals(AcademicYearStage.YEAR_2, AcademicYearStage.getStageForDistance(2599f))
    assertEquals(AcademicYearStage.YEAR_3, AcademicYearStage.getStageForDistance(2600f))
    assertEquals(AcademicYearStage.YEAR_3, AcademicYearStage.getStageForDistance(4199f))
    assertEquals(AcademicYearStage.YEAR_4, AcademicYearStage.getStageForDistance(4200f))
    assertEquals(AcademicYearStage.YEAR_4, AcademicYearStage.getStageForDistance(5999f))
  }

  @Test
  fun `verify character profiles and starter unlocks`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)

    assertTrue(preferences.isCharacterUnlocked(CharacterId.SWAGGER))
    assertTrue(preferences.isCharacterUnlocked(CharacterId.RUSHER))
    assertFalse(preferences.isCharacterUnlocked(CharacterId.CHAMP))

    // 4 characters exist
    assertEquals(4, CharacterProfile.ALL_CHARACTERS.size)
  }

  @Test
  fun `verify game engine start and lane change`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)
    val soundSynthesizer = SoundSynthesizer(context)
    val engine = GameEngine(preferences, soundSynthesizer)

    engine.startGame()
    assertEquals(GamePhase.RUNNING, engine.gameState.value.phase)
    assertEquals(0, engine.gameState.value.targetLane)

    engine.moveLane(1)
    assertEquals(1, engine.gameState.value.targetLane)

    engine.moveLane(1)
    assertEquals(1, engine.gameState.value.targetLane) // clamped

    engine.moveLane(-1)
    assertEquals(0, engine.gameState.value.targetLane)
  }

  @Test
  fun `verify jump and slide mechanics`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = GamePreferences(context)
    val soundSynthesizer = SoundSynthesizer(context)
    val engine = GameEngine(preferences, soundSynthesizer)

    engine.startGame()
    engine.jump()
    assertTrue(engine.gameState.value.isJumping)

    engine.slide()
    assertTrue(engine.gameState.value.isSliding)
  }
}
