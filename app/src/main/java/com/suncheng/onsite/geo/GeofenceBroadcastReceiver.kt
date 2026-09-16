package com.suncheng.onsite.geo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.suncheng.onsite.notify.ArriveNotifier

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL
        ) {
            return
        }
        val id = event.triggeringGeofences?.firstOrNull()?.requestId ?: return
        ArriveNotifier.notifyArrived(context, id)
    }
}
