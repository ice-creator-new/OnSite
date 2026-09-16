package com.suncheng.onsite.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationClient(private val context: Context) {
    private val fused = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun current(): Location? {
        val fresh = requestCurrent()
        if (fresh != null) return fresh
        return lastKnown()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrent(): Location? = suspendCancellableCoroutine { cont ->
        val token = CancellationTokenSource()
        cont.invokeOnCancellation { token.cancel() }
        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } catch (_: SecurityException) {
            cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnown(): Location? = suspendCancellableCoroutine { cont ->
        try {
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } catch (_: SecurityException) {
            cont.resume(null)
        }
    }

    suspend fun labelFor(location: Location): String = withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            val addr = list?.firstOrNull()
            val line = listOfNotNull(
                addr?.featureName?.takeIf { it.isNotBlank() && it != addr.thoroughfare },
                addr?.thoroughfare,
                addr?.subLocality,
                addr?.locality,
            ).distinct().take(2).joinToString(" · ")
            line.ifBlank { fallbackCoord(location) }
        }.getOrElse { fallbackCoord(location) }
    }

    private fun fallbackCoord(location: Location): String {
        return "%.5f, %.5f".format(location.latitude, location.longitude)
    }
}
