package com.suncheng.onsite.geo

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.suncheng.onsite.MainActivity
import com.suncheng.onsite.OnSiteApp
import com.suncheng.onsite.R
import com.suncheng.onsite.data.NoteEntity
import com.suncheng.onsite.data.Prefs
import com.suncheng.onsite.notify.ArriveNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * 系统定位轮询，走进半径后发本地通知。不走 Play 服务围栏。
 */
class FenceWatchService : Service(), LocationListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val manager by lazy { getSystemService(LocationManager::class.java) }
    private val prefs by lazy { Prefs(this) }
    private var notes: List<NoteEntity> = emptyList()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureWatchChannel()
        startAsForeground()
        startUpdates()
        reloadNotes()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        when (intent?.action) {
            ACTION_DROP -> intent.getStringExtra(GeofenceManager.EXTRA_NOTE_ID)?.let { prefs.clearArrived(it) }
        }
        reloadNotes()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { manager.removeUpdates(this) }
        scope.cancel()
        super.onDestroy()
    }

    override fun onLocationChanged(location: Location) {
        checkFences(location)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    override fun onProviderDisabled(provider: String) = Unit

    private fun reloadNotes() {
        val app = application as? OnSiteApp ?: return
        scope.launch {
            val now = System.currentTimeMillis()
            val active = app.container.notes.activeNotes()
                .filter { it.expiresAt > now && it.unlockedAt == null }
                .take(GeofenceManager.MAX_GEOFENCES)
            notes = active
            if (active.isEmpty()) {
                stopSelf()
            }
        }
    }

    private fun checkFences(location: Location) {
        val snapshot = notes
        if (snapshot.isEmpty()) return
        val out = FloatArray(1)
        val now = System.currentTimeMillis()
        snapshot.forEach { note ->
            if (note.expiresAt <= now) return@forEach
            if (prefs.wasArrived(note.id)) return@forEach
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                note.latitude,
                note.longitude,
                out,
            )
            if (out[0] <= note.radiusMeters) {
                prefs.markArrived(note.id)
                ArriveNotifier.notifyArrived(this, note.id)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startUpdates() {
        val providers = buildList {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        }
        if (providers.isEmpty()) return
        providers.forEach { provider ->
            runCatching {
                manager.requestLocationUpdates(
                    provider,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    this,
                    Looper.getMainLooper(),
                )
            }
        }
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()?.let(::checkFences)
        }
    }

    private fun startAsForeground() {
        val tap = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, WATCH_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(getString(R.string.watch_notification_title))
            .setContentText(getString(R.string.watch_notification_body))
            .setContentIntent(tap)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(WATCH_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(WATCH_ID, notification)
        }
    }

    private fun ensureWatchChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                WATCH_CHANNEL,
                getString(R.string.watch_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.watch_channel_desc) },
        )
    }

    companion object {
        const val ACTION_DROP = "com.suncheng.onsite.DROP_FENCE"
        private const val WATCH_CHANNEL = "place_note_watch"
        private const val WATCH_ID = 41
        private const val MIN_TIME_MS = 20_000L
        private const val MIN_DISTANCE_M = 15f

        fun start(context: Context, intent: Intent = Intent(context, FenceWatchService::class.java)) {
            intent.setClass(context, FenceWatchService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
