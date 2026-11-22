package com.example.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.runtime.compositionLocalOf
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

val LocalNavigationSoundEnabled = compositionLocalOf { true }

/**
 * Low-latency synthesized UI focus-navigation sound player for Android TV & remote D-pad control.
 * Pre-generates a crisp, pleasant 32ms glassy tick WAV in app cache once and plays via SoundPool,
 * safely wrapped so it never crashes if audio hardware is unavailable (e.g. in JVM/Robolectric tests).
 */
object FocusSoundPlayer {

    @Volatile
    private var soundPool: SoundPool? = null

    @Volatile
    private var loadedSoundId: Int = 0

    @Volatile
    private var isSoundReady: Boolean = false

    @Volatile
    private var isInitializing: Boolean = false

    @Volatile
    private var lastPlayedUptimeMs: Long = 0L

    @Volatile
    private var suppressUntilUptimeMs: Long = 0L

    internal val playCountForTest = AtomicInteger(0)

    fun markTransition(suppressDurationMs: Long = 180L) {
        suppressUntilUptimeMs = SystemClock.uptimeMillis() + suppressDurationMs
    }

    fun warmUp(context: Context) {
        if (soundPool != null || isInitializing) return
        synchronized(this) {
            if (soundPool != null || isInitializing) return
            isInitializing = true
            runCatching {
                val appContext = context.applicationContext
                val pool = SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0 && sampleId == loadedSoundId) {
                        isSoundReady = true
                    }
                }
                soundPool = pool

                Thread {
                    runCatching {
                        val wavFile = ensureWavFile(appContext)
                        if (wavFile.exists()) {
                            val id = pool.load(wavFile.absolutePath, 1)
                            loadedSoundId = id
                        }
                    }
                    isInitializing = false
                }.start()
            }.onFailure {
                isInitializing = false
            }
        }
    }

    fun playFocusMove(context: Context, view: View?, enabled: Boolean) {
        if (!enabled) return
        val now = SystemClock.uptimeMillis()
        if (now < suppressUntilUptimeMs) return
        if (now - lastPlayedUptimeMs < 40L) return
        lastPlayedUptimeMs = now
        playCountForTest.incrementAndGet()

        warmUp(context)

        var played = false
        val pool = soundPool
        if (pool != null && isSoundReady && loadedSoundId != 0) {
            runCatching {
                val streamId = pool.play(loadedSoundId, 0.55f, 0.55f, 1, 0, 1.0f)
                played = streamId != 0
            }
        }
        if (!played && view != null) {
            runCatching {
                view.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
            }
        }
    }

    private fun ensureWavFile(context: Context): File {
        val outFile = File(context.cacheDir, "horizon_nav_tick_v1.wav")
        if (outFile.exists() && outFile.length() > 100L) return outFile

        val sampleRate = 44100
        val durationMs = 34
        val sampleCount = (sampleRate * durationMs) / 1000
        val pcmBytes = ByteArray(sampleCount * 2)

        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleRate.toDouble()
            val attack = (i / (sampleRate * 0.002)).coerceAtMost(1.0)
            val decay = exp(-t * 145.0)
            val wave = 0.65 * sin(2.0 * PI * 980.0 * t) + 0.35 * sin(2.0 * PI * 1480.0 * t)
            val sample = (wave * attack * decay * 14000.0).toInt().coerceIn(-32767, 32767)
            pcmBytes[i * 2] = (sample and 0xFF).toByte()
            pcmBytes[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }

        val wavStream = ByteArrayOutputStream(44 + pcmBytes.size)
        val totalDataLen = pcmBytes.size + 36
        val byteRate = sampleRate * 2

        fun writeAscii(s: String) = wavStream.write(s.toByteArray(Charsets.US_ASCII))
        fun writeIntLE(v: Int) {
            wavStream.write(v and 0xFF)
            wavStream.write((v shr 8) and 0xFF)
            wavStream.write((v shr 16) and 0xFF)
            wavStream.write((v shr 24) and 0xFF)
        }
        fun writeShortLE(v: Int) {
            wavStream.write(v and 0xFF)
            wavStream.write((v shr 8) and 0xFF)
        }

        writeAscii("RIFF")
        writeIntLE(totalDataLen)
        writeAscii("WAVE")
        writeAscii("fmt ")
        writeIntLE(16)
        writeShortLE(1) // PCM
        writeShortLE(1) // Mono
        writeIntLE(sampleRate)
        writeIntLE(byteRate)
        writeShortLE(2) // Block align
        writeShortLE(16) // Bits per sample
        writeAscii("data")
        writeIntLE(pcmBytes.size)
        wavStream.write(pcmBytes)

        FileOutputStream(outFile).use { fos ->
            fos.write(wavStream.toByteArray())
        }
        return outFile
    }
}
