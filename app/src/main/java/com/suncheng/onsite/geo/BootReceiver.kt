package com.suncheng.onsite.geo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suncheng.onsite.OnSiteApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val app = context.applicationContext as? OnSiteApp
        if (app == null) {
            pending.finish()
            return
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val notes = app.container.notes.activeNotes()
                app.container.geofences.replaceAll(notes)
            } finally {
                pending.finish()
            }
        }
    }
}
