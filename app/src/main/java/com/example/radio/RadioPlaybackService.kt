package com.example.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.MainActivity
import com.example.R
import com.example.model.RadioPlaybackStatus
import com.example.model.RadioStation
import com.example.prayer.AdhanPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadioPlaybackState(
    val status: RadioPlaybackStatus = RadioPlaybackStatus.IDLE,
    val activeStation: RadioStation? = null,
    val errorMessage: String? = null,
)

class RadioPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            AdhanPlaybackService.isPlaying.collect { adhanActive ->
                if (adhanActive && _playbackState.value.status != RadioPlaybackStatus.IDLE) {
                    stopPlayback(clearError = true)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopPlayback(clearError = true)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty().trim()
                val name = intent.getStringExtra(EXTRA_STATION_NAME).orEmpty().ifBlank { "Horizon Radio" }
                val uuid = intent.getStringExtra(EXTRA_STATION_UUID).orEmpty()
                val country = intent.getStringExtra(EXTRA_STATION_COUNTRY).orEmpty()
                val countryCode = intent.getStringExtra(EXTRA_STATION_COUNTRY_CODE).orEmpty()
                val codec = intent.getStringExtra(EXTRA_STATION_CODEC).orEmpty()
                val bitrate = intent.getIntExtra(EXTRA_STATION_BITRATE, 0)

                if (url.isBlank()) {
                    stopPlayback(clearError = false)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }

                val station = RadioStation(
                    stationUuid = uuid.ifBlank { url },
                    name = name,
                    streamUrl = url,
                    country = country,
                    countryCode = countryCode,
                    codec = codec,
                    bitrate = bitrate,
                )

                ensureChannel(this)
                startForeground(NOTIFICATION_ID, buildNotification(station))
                startStream(station)
            }
        }
        return START_NOT_STICKY
    }

    private fun startStream(station: RadioStation) {
        if (AdhanPlaybackService.isPlaying.value) {
            AdhanPlaybackService.stopAdhan(this)
        }
        _playbackState.value = RadioPlaybackState(
            status = RadioPlaybackStatus.LOADING,
            activeStation = station,
            errorMessage = null,
        )

        val player = getOrCreatePlayer()
        if (player == null) {
            _playbackState.value = RadioPlaybackState(
                status = RadioPlaybackStatus.ERROR,
                activeStation = station,
                errorMessage = "Audio player unavailable",
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        runCatching {
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(station.streamUrl))
            player.prepare()
            player.play()
        }.onFailure { err ->
            _playbackState.value = RadioPlaybackState(
                status = RadioPlaybackStatus.ERROR,
                activeStation = station,
                errorMessage = err.localizedMessage ?: "Stream error",
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun getOrCreatePlayer(): ExoPlayer? {
        exoPlayer?.let { return it }
        return runCatching {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()

            ExoPlayer.Builder(this)
                .build()
                .apply {
                    setAudioAttributes(attrs, true)
                    setHandleAudioBecomingNoisy(true)
                    setWakeMode(C.WAKE_MODE_NETWORK)
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            val current = _playbackState.value
                            val station = current.activeStation ?: return
                            when (playbackState) {
                                Player.STATE_BUFFERING -> {
                                    _playbackState.value = current.copy(
                                        status = RadioPlaybackStatus.LOADING,
                                        errorMessage = null,
                                    )
                                }
                                Player.STATE_READY -> {
                                    val nextStatus = if (playWhenReady) {
                                        RadioPlaybackStatus.PLAYING
                                    } else {
                                        RadioPlaybackStatus.IDLE
                                    }
                                    _playbackState.value = current.copy(
                                        status = nextStatus,
                                        errorMessage = null,
                                    )
                                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    nm.notify(NOTIFICATION_ID, buildNotification(station))
                                }
                                Player.STATE_ENDED -> {
                                    stopPlayback(clearError = true)
                                    stopForeground(STOP_FOREGROUND_REMOVE)
                                    stopSelf()
                                }
                                Player.STATE_IDLE -> Unit
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            val current = _playbackState.value
                            if (isPlaying && current.status != RadioPlaybackStatus.PLAYING) {
                                _playbackState.value = current.copy(
                                    status = RadioPlaybackStatus.PLAYING,
                                    errorMessage = null,
                                )
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            val station = _playbackState.value.activeStation
                            runCatching { stop() }
                            _playbackState.value = RadioPlaybackState(
                                status = RadioPlaybackStatus.ERROR,
                                activeStation = station,
                                errorMessage = error.localizedMessage ?: "Stream error",
                            )
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    })
                }
        }.getOrNull()?.also { exoPlayer = it }
    }

    private fun stopPlayback(clearError: Boolean) {
        runCatching {
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
        }
        val prev = _playbackState.value
        _playbackState.value = RadioPlaybackState(
            status = RadioPlaybackStatus.IDLE,
            activeStation = prev.activeStation,
            errorMessage = if (clearError) null else prev.errorMessage,
        )
    }

    private fun buildNotification(station: RadioStation): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val meta = buildList {
            if (station.country.isNotBlank()) add(station.country)
            if (station.codec.isNotBlank()) add(station.codec)
            if (station.bitrate > 0) add("${station.bitrate} kbps")
        }.joinToString(" · ")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(station.name)
            .setContentText(meta.ifBlank { getString(R.string.radio_channel_name) })
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.radio_stop_action),
                stopIntent,
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        runCatching { exoPlayer?.release() }
        exoPlayer = null
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "horizon_radio_channel"
        const val NOTIFICATION_ID = 4402
        const val ACTION_PLAY = "com.example.action.RADIO_PLAY"
        const val ACTION_STOP = "com.example.action.RADIO_STOP"
        const val EXTRA_STATION_UUID = "extra_station_uuid"
        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_STATION_COUNTRY = "extra_station_country"
        const val EXTRA_STATION_COUNTRY_CODE = "extra_station_country_code"
        const val EXTRA_STATION_CODEC = "extra_station_codec"
        const val EXTRA_STATION_BITRATE = "extra_station_bitrate"

        private val _playbackState = MutableStateFlow(RadioPlaybackState())
        val playbackState: StateFlow<RadioPlaybackState> = _playbackState.asStateFlow()

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.radio_channel_name),
                        NotificationManager.IMPORTANCE_LOW,
                    ).apply {
                        description = context.getString(R.string.radio_channel_desc)
                        setShowBadge(false)
                    }
                    nm.createNotificationChannel(channel)
                }
            }
        }

        fun playStation(context: Context, station: RadioStation) {
            if (station.streamUrl.isBlank()) return
            ensureChannel(context)
            _playbackState.value = RadioPlaybackState(
                status = RadioPlaybackStatus.LOADING,
                activeStation = station,
                errorMessage = null,
            )
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_STATION_UUID, station.stationUuid)
                putExtra(EXTRA_STATION_NAME, station.name)
                putExtra(EXTRA_STREAM_URL, station.streamUrl)
                putExtra(EXTRA_STATION_COUNTRY, station.country)
                putExtra(EXTRA_STATION_COUNTRY_CODE, station.countryCode)
                putExtra(EXTRA_STATION_CODEC, station.codec)
                putExtra(EXTRA_STATION_BITRATE, station.bitrate)
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stopRadio(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            runCatching { context.startService(intent) }
            _playbackState.value = _playbackState.value.copy(
                status = RadioPlaybackStatus.IDLE,
                errorMessage = null,
            )
        }
    }
}
