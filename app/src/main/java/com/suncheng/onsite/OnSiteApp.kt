package com.suncheng.onsite

import android.app.Application
import com.suncheng.onsite.data.AppDatabase
import com.suncheng.onsite.data.NoteRepository
import com.suncheng.onsite.geo.GeofenceManager
import com.suncheng.onsite.geo.LocationClient
import com.suncheng.onsite.geo.playServicesReady
import com.suncheng.onsite.notify.ArriveNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OnSiteApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
        runCatching { ArriveNotifier.ensureChannel(this) }
            .onFailure { CrashLog.write(this, it) }
        val db = AppDatabase.create(this)
        container = AppContainer(
            notes = NoteRepository(db.noteDao()),
            location = LocationClient(this),
            geofences = GeofenceManager(this),
        )
        if (playServicesReady(this)) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                runCatching {
                    container.geofences.replaceAll(container.notes.activeNotes())
                }.onFailure { CrashLog.write(this@OnSiteApp, it) }
            }
        }
    }
}

class AppContainer(
    val notes: NoteRepository,
    val location: LocationClient,
    val geofences: GeofenceManager,
)
