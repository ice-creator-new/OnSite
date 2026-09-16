package com.suncheng.onsite

import android.app.Application
import android.location.Location
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suncheng.onsite.data.NoteEntity
import com.suncheng.onsite.data.NoteListItem
import com.suncheng.onsite.data.NoteStatus
import com.suncheng.onsite.data.UnlockResult
import com.suncheng.onsite.geo.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.UUID

data class OnSiteUiState(
    val notes: List<NoteListItem> = emptyList(),
    val lastLocation: Location? = null,
    val placePreview: String = "",
    val writeBusy: Boolean = false,
    val writeError: String? = null,
    val writeDoneTick: Long = 0L,
    val unlock: UnlockResult? = null,
    val unlocking: Boolean = false,
    val pendingOpenId: String? = null,
)

class OnSiteViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val container = (application as OnSiteApp).container

    private val _state = MutableStateFlow(OnSiteUiState())
    val state: StateFlow<OnSiteUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.notes.observeList { _state.value.lastLocation }.collect { items ->
                val here = _state.value.lastLocation
                val remapped = remap(items, here)
                _state.update { it.copy(notes = remapped) }
                pruneExpired(remapped)
            }
        }
        refreshLocation(alsoLabel = true)
    }

    fun refreshLocation(alsoLabel: Boolean = false) {
        viewModelScope.launch {
            val loc = container.location.current()
            _state.update { state ->
                state.copy(lastLocation = loc, notes = remap(state.notes, loc))
            }
            if (alsoLabel && loc != null) {
                val label = container.location.labelFor(loc)
                _state.update { it.copy(placePreview = label) }
            }
        }
    }

    fun consumeOpenIntent(noteId: String?) {
        if (noteId.isNullOrBlank()) return
        _state.update { it.copy(pendingOpenId = noteId) }
        tryOpen(noteId)
    }

    fun clearPendingOpen() {
        _state.update { it.copy(pendingOpenId = null) }
    }

    fun tryOpen(noteId: String? = _state.value.pendingOpenId) {
        viewModelScope.launch {
            _state.update { it.copy(unlocking = true, unlock = null) }
            val loc = container.location.current()
            _state.update { it.copy(lastLocation = loc) }
            val result = container.notes.unlockIfPresent(noteId, loc)
            if (result is UnlockResult.Opened) {
                container.geofences.unregister(result.note.id)
            }
            _state.update { it.copy(unlocking = false, unlock = result, lastLocation = loc) }
        }
    }

    fun createNote(
        body: String,
        placeLabel: String,
        radiusMeters: Double,
        expiry: ExpiryPreset,
        imageUri: Uri?,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(writeBusy = true, writeError = null) }
            val loc = container.location.current()
            if (loc == null) {
                _state.update { it.copy(writeBusy = false, writeError = "拿不到定位，信钉不下去。") }
                return@launch
            }
            val text = body.trim()
            if (text.isEmpty()) {
                _state.update { it.copy(writeBusy = false, writeError = "先写一句。") }
                return@launch
            }
            val active = container.notes.activeNotes()
            if (active.size >= GeofenceManager.MAX_GEOFENCES) {
                _state.update { it.copy(writeBusy = false, writeError = "活跃的信已满 20 封。") }
                return@launch
            }
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID().toString()
            val imagePath = imageUri?.let { persistImage(it, id) }
            val label = placeLabel.ifBlank {
                _state.value.placePreview.ifBlank { container.location.labelFor(loc) }
            }
            val note = NoteEntity(
                id = id,
                placeLabel = label,
                latitude = loc.latitude,
                longitude = loc.longitude,
                radiusMeters = radiusMeters,
                body = text,
                imagePath = imagePath,
                createdAt = now,
                expiresAt = expiry.expiresAt(now),
                unlockedAt = null,
            )
            container.notes.insert(note)
            container.geofences.register(note)
            _state.update {
                it.copy(
                    writeBusy = false,
                    writeError = null,
                    writeDoneTick = it.writeDoneTick + 1,
                    lastLocation = loc,
                    placePreview = label,
                )
            }
        }
    }

    private fun remap(items: List<NoteListItem>, location: Location?): List<NoteListItem> {
        if (location == null) return items
        val now = System.currentTimeMillis()
        val out = FloatArray(1)
        return items.map { item ->
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                item.latitude,
                item.longitude,
                out,
            )
            val status = when {
                now >= item.expiresAt -> NoteStatus.Expired
                item.unlockedAt != null -> NoteStatus.Unlocked
                out[0] <= item.radiusMeters -> NoteStatus.Nearby
                else -> NoteStatus.Scheduled
            }
            item.copy(status = status)
        }
    }

    fun clearWriteError() {
        _state.update { it.copy(writeError = null) }
    }

    private suspend fun pruneExpired(items: List<NoteListItem>) {
        items.filter { it.status == NoteStatus.Expired }.forEach { item ->
            container.geofences.unregister(item.id)
        }
    }

    private fun persistImage(uri: Uri, id: String): String? {
        val app = getApplication<Application>()
        return runCatching {
            val dir = File(app.filesDir, "images").apply { mkdirs() }
            val dest = File(dir, "$id.jpg")
            app.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            dest.absolutePath
        }.getOrNull()
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OnSiteViewModel(app) as T
                }
            }
        }
    }
}

enum class ExpiryPreset {
    Minute,
    Hours2,
    Today,
    Days7,
    ;

    fun expiresAt(now: Long): Long = when (this) {
        Minute -> now + 60_000L
        Hours2 -> now + 2 * 60 * 60_000L
        Today -> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 0)
            }
            val end = cal.timeInMillis
            if (end <= now) now + 60 * 60_000L else end
        }
        Days7 -> now + 7L * 24 * 60 * 60_000L
    }
}
