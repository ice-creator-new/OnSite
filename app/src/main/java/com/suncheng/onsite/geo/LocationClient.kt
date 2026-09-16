package com.suncheng.onsite.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationClient(private val context: Context) {
    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    private val manager by lazy {
        context.getSystemService(LocationManager::class.java)
    }

    @SuppressLint("MissingPermission")
    suspend fun current(): Location? {
        if (playServicesReady(context)) {
            val fresh = requestFused()
            if (fresh != null) return fresh
            val last = lastFused()
            if (last != null) return last
        }
        return systemLastKnown()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFused(): Location? = suspendCancellableCoroutine { cont ->
        val token = CancellationTokenSource()
        cont.invokeOnCancellation { token.cancel() }
        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } catch (_: Exception) {
            cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastFused(): Location? = suspendCancellableCoroutine { cont ->
        try {
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        } catch (_: Exception) {
            cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun systemLastKnown(): Location? {
        val gps = runCatching { manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
        val net = runCatching { manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        return listOfNotNull(gps, net).maxByOrNull { it.time }
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
