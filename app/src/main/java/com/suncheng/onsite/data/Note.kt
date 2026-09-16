package com.suncheng.onsite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteStatus {
    Scheduled,
    Nearby,
    Unlocked,
    Expired,
}

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val placeLabel: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val body: String,
    val imagePath: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val unlockedAt: Long?,
)

data class NoteListItem(
    val id: String,
    val placeLabel: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double,
    val hasImage: Boolean,
    val createdAt: Long,
    val expiresAt: Long,
    val unlockedAt: Long?,
    val status: NoteStatus,
    val coverUrl: String? = null,
)

data class UnlockedNote(
    val id: String,
    val placeLabel: String,
    val body: String,
    val imagePath: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val unlockedAt: Long?,
)

fun NoteEntity.toListItem(now: Long, distanceMeters: Double?): NoteListItem {
    return NoteListItem(
        id = id,
        placeLabel = placeLabel,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        hasImage = !imagePath.isNullOrBlank(),
        createdAt = createdAt,
        expiresAt = expiresAt,
        unlockedAt = unlockedAt,
        status = resolveStatus(now, distanceMeters),
    )
}

fun NoteEntity.resolveStatus(now: Long, distanceMeters: Double?): NoteStatus {
    if (now >= expiresAt) return NoteStatus.Expired
    if (unlockedAt != null) return NoteStatus.Unlocked
    if (distanceMeters != null && distanceMeters <= radiusMeters) return NoteStatus.Nearby
    return NoteStatus.Scheduled
}

fun NoteEntity.toUnlocked(): UnlockedNote = UnlockedNote(
    id = id,
    placeLabel = placeLabel,
    body = body,
    imagePath = imagePath,
    createdAt = createdAt,
    expiresAt = expiresAt,
    unlockedAt = unlockedAt,
)
