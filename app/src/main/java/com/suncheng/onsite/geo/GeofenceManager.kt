package com.suncheng.onsite.geo

import android.content.Context
import android.content.Intent
import com.suncheng.onsite.data.NoteEntity

/**
 * 不依赖 GMS。围栏由 [FenceWatchService] 用系统 LocationManager 自己算距离。
 */
class GeofenceManager(private val context: Context) {

    fun register(note: NoteEntity) {
        if (note.expiresAt <= System.currentTimeMillis()) return
        FenceWatchService.start(context)
    }

    fun unregister(id: String) {
        FenceWatchService.start(
            context,
            Intent(context, FenceWatchService::class.java)
                .setAction(FenceWatchService.ACTION_DROP)
                .putExtra(EXTRA_NOTE_ID, id),
        )
    }

    fun replaceAll(notes: List<NoteEntity>) {
        val active = notes.filter { it.expiresAt > System.currentTimeMillis() }
        if (active.isEmpty()) {
            context.stopService(Intent(context, FenceWatchService::class.java))
        } else {
            FenceWatchService.start(context)
        }
    }

    companion object {
        const val MAX_GEOFENCES = 20
        const val EXTRA_NOTE_ID = "note_id"
    }
}
