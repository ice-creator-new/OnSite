package com.suncheng.onsite.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class LocationClient(private val context: Context) {
    private val manager = context.getSystemService(LocationManager::class.java)

    @SuppressLint("MissingPermission")
    suspend fun current(): Location? {
        val live = requestCurrent()
        if (live != null) return live
        return lastKnown()
    }

    @SuppressLint("MissingPermission")
    fun lastKnown(): Location? {
        val gps = runCatching { manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()
        val net = runCatching { manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        return listOfNotNull(gps, net).maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrent(): Location? = suspendCancellableCoroutine { cont ->
        val provider = bestProvider()
        if (provider == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val finished = AtomicBoolean(false)
        fun done(location: Location?) {
            if (finished.compareAndSet(false, true) && cont.isActive) {
                cont.resume(location)
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(
                    provider,
                    signal,
                    context.mainExecutor,
                ) { location -> done(location) }
            } else {
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        done(location)
                    }
                }
                cont.invokeOnCancellation { manager.removeUpdates(listener) }
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            done(null)
        } catch (_: IllegalArgumentException) {
            done(null)
        }
    }

    fun bestProvider(): String? {
        return when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) -> LocationManager.PASSIVE_PROVIDER
            else -> null
        }
    }

    fun enabledProviders(): List<String> {
        return buildList {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
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
