package com.suncheng.onsite.geo

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.suncheng.onsite.data.NoteEntity
import kotlinx.coroutines.tasks.await

class GeofenceManager(private val context: Context) {
    private val client = LocationServices.getGeofencingClient(context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun register(note: NoteEntity) {
        val now = System.currentTimeMillis()
        val ttl = (note.expiresAt - now).coerceAtLeast(1_000L)
        val geofence = Geofence.Builder()
            .setRequestId(note.id)
            .setCircularRegion(note.latitude, note.longitude, note.radiusMeters.toFloat())
            .setExpirationDuration(ttl)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        runCatching {
            client.addGeofences(request, pendingIntent).await()
        }.onFailure { Log.w(TAG, "register failed ${note.id}", it) }
    }

    suspend fun unregister(id: String) {
        runCatching { client.removeGeofences(listOf(id)).await() }
            .onFailure { Log.w(TAG, "unregister failed $id", it) }
    }

    suspend fun replaceAll(notes: List<NoteEntity>) {
        runCatching { client.removeGeofences(pendingIntent).await() }
        val now = System.currentTimeMillis()
        notes
            .filter { it.expiresAt > now }
            .take(MAX_GEOFENCES)
            .forEach { register(it) }
    }

    companion object {
        const val MAX_GEOFENCES = 20
        private const val TAG = "GeofenceManager"
    }
}
