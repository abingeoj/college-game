package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundSynthesizer(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    private var isEnabled = true

    private val audioChannel = Channel<ShortArray>(capacity = 64)
    private var audioTrack: AudioTrack? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(sampleRate / 2) // ~500ms buffer capacity

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                for (buffer in audioChannel) {
                    if (isEnabled && track.state == AudioTrack.STATE_INITIALIZED) {
                        track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun vibrate(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun playJump() {
        if (!isEnabled) return
        scope.launch {
            // Rising frequency chirp 400Hz -> 850Hz
            val durationMs = 120
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 400.0 + 450.0 * progress
                val envelope = 1.0 - progress
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
                buffer[i] = sample
            }
            playPcm(buffer)
        }
    }

    fun playSlide() {
        if (!isEnabled) return
        scope.launch {
            // Sweeping low whoosh 450Hz -> 180Hz
            val durationMs = 150
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 450.0 - 270.0 * progress
                val envelope = sin(progress * PI)
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.35).toInt().toShort()
                buffer[i] = sample
            }
            playPcm(buffer)
        }
    }

    fun playLaneChange() {
        if (!isEnabled) return
        scope.launch {
            val durationMs = 60
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 600.0 + 100.0 * progress
                val envelope = 1.0 - progress
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.25).toInt().toShort()
                buffer[i] = sample
            }
            playPcm(buffer)
        }
    }

    fun playCoin() {
        if (!isEnabled) return
        scope.launch {
            // Dual chime 987Hz -> 1318Hz
            val durationMs = 140
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            val half = numSamples / 2
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val freq = if (i < half) 987.77 else 1318.51
                val progressInTone = if (i < half) i.toDouble() / half else (i - half).toDouble() / half
                val envelope = 1.0 - progressInTone
                val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.4).toInt().toShort()
                buffer[i] = sample
            }
            playPcm(buffer)
        }
    }

    fun playPowerUp() {
        if (!isEnabled) return
        scope.launch {
            // Arpeggio: C5 (523), E5 (659), G5 (784), C6 (1046)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            val noteDurationMs = 80
            val totalSamples = (sampleRate * noteDurationMs * notes.size / 1000)
            val buffer = ShortArray(totalSamples)
            val samplesPerNote = totalSamples / notes.size

            for (n in notes.indices) {
                val freq = notes[n]
                for (i in 0 until samplesPerNote) {
                    val idx = n * samplesPerNote + i
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / samplesPerNote
                    val envelope = 1.0 - (progress * 0.5)
                    val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.35).toInt().toShort()
                    buffer[idx] = sample
                }
            }
            playPcm(buffer)
        }
    }

    fun playSupplyCrash() {
        vibrate(200)
        if (!isEnabled) return
        scope.launch {
            // Harsh buzz crunch 180Hz -> 50Hz with harmonics
            val durationMs = 350
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val freq = 180.0 - 130.0 * progress
                val envelope = 1.0 - progress
                val wave = sin(2.0 * PI * freq * t) + 0.5 * sin(2.0 * PI * (freq * 2.0) * t)
                val sample = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                buffer[i] = sample
            }
            playPcm(buffer)
        }
    }

    fun playGraduationFanfare() {
        vibrate(400)
        if (!isEnabled) return
        scope.launch {
            // Grand celebratory major chord progression fanfare!
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1174.66, 1318.51, 1567.98)
            val noteDurationMs = 120
            val totalSamples = (sampleRate * noteDurationMs * notes.size / 1000)
            val buffer = ShortArray(totalSamples)
            val samplesPerNote = totalSamples / notes.size

            for (n in notes.indices) {
                val freq = notes[n]
                for (i in 0 until samplesPerNote) {
                    val idx = n * samplesPerNote + i
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / samplesPerNote
                    val envelope = 1.0 - (progress * 0.3)
                    val sample = (sin(2.0 * PI * freq * t) * envelope * Short.MAX_VALUE * 0.45).toInt().toShort()
                    buffer[idx] = sample
                }
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        if (!isEnabled) return
        audioChannel.trySend(buffer)
    }

    fun release() {
        try {
            audioChannel.close()
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
            scope.cancel()
        } catch (_: Exception) {}
    }
}
