package com.suncheng.onsite.data

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(
    private val dao: NoteDao,
) {
    fun observeList(location: () -> Location?): Flow<List<NoteListItem>> {
        return dao.observeAll().map { rows ->
            val now = System.currentTimeMillis()
            val here = location()
            rows.map { entity ->
                entity.toListItem(now, here?.distanceToNote(entity))
            }
        }
    }

    suspend fun listItems(location: Location?): List<NoteListItem> {
        val now = System.currentTimeMillis()
        return dao.getAll().map { it.toListItem(now, location?.distanceToNote(it)) }
    }

    suspend fun getEntity(id: String): NoteEntity? = dao.getById(id)

    suspend fun insert(note: NoteEntity) = dao.upsert(note)

    suspend fun markUnlocked(id: String) {
        dao.markUnlocked(id, System.currentTimeMillis())
    }

    suspend fun activeNotes(): List<NoteEntity> = dao.getActive(System.currentTimeMillis())

    suspend fun unlockIfPresent(
        id: String?,
        location: Location?,
    ): UnlockResult {
        val now = System.currentTimeMillis()
        val candidates = if (id != null) {
            listOfNotNull(dao.getById(id))
        } else {
            dao.getActive(now)
        }
        if (location == null) return UnlockResult.NoLocation
        val readable = candidates
            .asSequence()
            .filter { now < it.expiresAt }
            .map { it to location.distanceToNote(it) }
            .filter { it.second <= it.first.radiusMeters }
            .minByOrNull { it.second }
            ?.first
            ?: return if (id != null) UnlockResult.OutOfRange else UnlockResult.EmptyHere

        if (readable.unlockedAt == null) {
            dao.markUnlocked(readable.id, now)
        }
        val fresh = dao.getById(readable.id) ?: readable
        return UnlockResult.Opened(fresh.toUnlocked())
    }

    suspend fun delete(id: String) = dao.delete(id)
}

sealed interface UnlockResult {
    data class Opened(val note: UnlockedNote) : UnlockResult
    data object EmptyHere : UnlockResult
    data object OutOfRange : UnlockResult
    data object Expired : UnlockResult
    data object NoLocation : UnlockResult
}

fun Location.distanceToNote(note: NoteEntity): Double {
    val out = FloatArray(1)
    Location.distanceBetween(
        latitude,
        longitude,
        note.latitude,
        note.longitude,
        out,
    )
    return out[0].toDouble()
}
