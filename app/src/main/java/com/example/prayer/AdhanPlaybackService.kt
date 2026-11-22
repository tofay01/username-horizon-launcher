package com.example.prayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AdhanPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null
    private var fallbackJob: Job? = null
    private var fallbackTrack: AudioTrack? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Adhan"
                val customUri = intent.getStringExtra(EXTRA_AUDIO_URI)
                val city = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
                ensureChannel(this)
                startForeground(NOTIFICATION_ID, buildNotification(prayerName, city))
                startPlayback(customUri)
            }
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(customUri: String?) {
        stopPlayback()
        _isPlaying.value = true

        if (!customUri.isNullOrBlank()) {
            val startedCustom = runCatching {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(this@AdhanPlaybackService, Uri.parse(customUri))
                    setOnCompletionListener {
                        stopPlayback()
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    setOnErrorListener { _, _, _ ->
                        playBuiltInChime()
                        true
                    }
                    prepare()
                    start()
                }
                mediaPlayer = mp
                true
            }.getOrDefault(false)
            if (startedCustom) return
        }

        playBuiltInChime()
    }

    private fun playBuiltInChime() {
        fallbackJob = serviceScope.launch {
            val sampleRate = 22050
            // Melodic Hijaz-inspired gentle chime sequence (frequencies in Hz)
            val notes = listOf(
                293.66 to 0.8, // D4
                311.13 to 0.7, // Eb4
                392.00 to 0.9, // G4
                440.00 to 1.1, // A4
                392.00 to 0.8, // G4
                311.13 to 0.8, // Eb4
                293.66 to 1.4, // D4
            )
            val totalSamples = notes.sumOf { (it.second * sampleRate).toInt() }
            val pcm = ShortArray(totalSamples)
            var offset = 0
            for ((freq, durSec) in notes) {
                val count = (durSec * sampleRate).toInt()
                for (i in 0 until count) {
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - exp(-t * 18.0)) * exp(-t * 2.2)
                    val fundamental = sin(2.0 * PI * freq * t)
                    val harmonic = 0.28 * sin(2.0 * PI * freq * 2.0 * t)
                    val sample = ((fundamental + harmonic) * env * 0.45 * Short.MAX_VALUE).toInt()
                    pcm[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                offset += count
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            fallbackTrack = track
            runCatching {
                track.play()
                var pos = 0
                while (isActive && pos < pcm.size) {
                    val chunk = minOf(2048, pcm.size - pos)
                    val written = track.write(pcm, pos, chunk)
                    if (written <= 0) break
                    pos += written
                }
            }
            if (isActive) {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        fallbackJob?.cancel()
        fallbackJob = null
        runCatching {
            fallbackTrack?.stop()
            fallbackTrack?.release()
        }
        fallbackTrack = null
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }

    private fun buildNotification(prayerName: String, city: String): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AdhanPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val subtitle = if (city.isNotBlank()) "$prayerName · $city" else prayerName
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Adhan · $prayerName")
            .setContentText(subtitle)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.adhan_stop_action),
                stopIntent,
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    override fun onDestroy() {
        stopPlayback()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "horizon_adhan_channel"
        const val NOTIFICATION_ID = 4401
        const val ACTION_PLAY = "com.example.action.ADHAN_PLAY"
        const val ACTION_STOP = "com.example.action.ADHAN_STOP"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_AUDIO_URI = "extra_audio_uri"
        const val EXTRA_CITY_NAME = "extra_city_name"

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.adhan_channel_name),
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = context.getString(R.string.adhan_channel_desc)
                    }
                    nm.createNotificationChannel(channel)
                }
            }
        }

        fun markPrayerTriggeredIfNew(context: Context, prayerId: String, dateKey: String): Boolean {
            val prefs = context.getSharedPreferences("horizon_adhan_guard", Context.MODE_PRIVATE)
            val triggerKey = "${dateKey}_$prayerId"
            val previous = prefs.getString("last_trigger_key", null)
            if (previous == triggerKey) return false
            prefs.edit().putString("last_trigger_key", triggerKey).apply()
            return true
        }

        fun showPrayerNotificationOnly(
            context: Context,
            prayerName: String,
            cityName: String = "",
        ) {
            ensureChannel(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }
            val openAppIntent = PendingIntent.getActivity(
                context,
                prayerName.hashCode(),
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Adhan · $prayerName")
                .setContentText(if (cityName.isNotBlank()) "$prayerName · $cityName" else prayerName)
                .setContentIntent(openAppIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(4500 + (prayerName.hashCode() and 0xFF), notification)
        }

        fun startAdhan(
            context: Context,
            prayerName: String,
            customUri: String?,
            cityName: String = "",
        ) {
            ensureChannel(context)
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_AUDIO_URI, customUri)
                putExtra(EXTRA_CITY_NAME, cityName)
            }
            val started = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                true
            }.getOrDefault(false)
            if (!started) {
                showPrayerNotificationOnly(context, prayerName, cityName)
            }
        }

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
            _isPlaying.value = false
        }
    }
}
