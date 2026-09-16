package com.suncheng.onsite.ui.letters

import com.suncheng.onsite.data.NoteListItem
import com.suncheng.onsite.data.NoteStatus

private const val MIN = 60_000L
private const val HOUR = 60 * MIN
private const val DAY = 24 * HOUR

fun mockLetters(now: Long = System.currentTimeMillis()): List<NoteListItem> {
    val rows = listOf(
        Mock("葰涌广场地下", NoteStatus.Nearby, 40.0, true, 20 * MIN, 2 * HOUR, "night-market"),
        Mock("葰芳地铁站 A 出口", NoteStatus.Nearby, 60.0, false, 55 * MIN, 3 * HOUR, "metro"),
        Mock("荃湾西如心广场", NoteStatus.Scheduled, 80.0, true, 2 * HOUR, DAY, "mall"),
        Mock("大帽山观景台", NoteStatus.Scheduled, 120.0, true, 5 * HOUR, 3 * DAY, "mountain"),
        Mock("中环天星码头", NoteStatus.Scheduled, 50.0, false, 8 * HOUR, 2 * DAY, "ferry"),
        Mock("尖沙咀星光大道", NoteStatus.Unlocked, 70.0, true, 1 * DAY, 6 * DAY, "harbour"),
        Mock("旺角朗豪坊天桥", NoteStatus.Unlocked, 45.0, false, 2 * DAY, 5 * DAY, "neon"),
        Mock("铜锣湾时代广场", NoteStatus.Scheduled, 90.0, true, 3 * DAY, 7 * DAY, "city"),
        Mock("沙田城门河畔", NoteStatus.Scheduled, 100.0, false, 4 * DAY, 7 * DAY, "river"),
        Mock("西贡海鲜街", NoteStatus.Expired, 55.0, true, 10 * DAY, -2 * HOUR, "seafood"),
        Mock("大屿山昂坪广场", NoteStatus.Scheduled, 150.0, true, 6 * DAY, 10 * DAY, "ngongping"),
        Mock("屯门码头巴士站", NoteStatus.Nearby, 35.0, false, 12 * MIN, HOUR, "bus"),
        Mock("元朗大马路", NoteStatus.Scheduled, 80.0, false, 18 * HOUR, 4 * DAY, "street"),
        Mock("粉岭联和墟", NoteStatus.Expired, 60.0, false, 12 * DAY, -DAY, "market"),
        Mock("赤柱美利楼", NoteStatus.Unlocked, 50.0, true, 5 * DAY, 8 * DAY, "stanley"),
        Mock("南丫岛榕树湾", NoteStatus.Scheduled, 200.0, true, 7 * DAY, 14 * DAY, "island"),
        Mock("将军澳调景岭海滨", NoteStatus.Scheduled, 75.0, false, 9 * HOUR, 2 * DAY, "waterfront"),
        Mock("青衣长发广场", NoteStatus.Nearby, 30.0, true, 8 * MIN, 90 * MIN, "plaza"),
        Mock("深水埗黄金商场", NoteStatus.Unlocked, 40.0, false, 3 * DAY, 6 * DAY, "gold"),
        Mock("香港大学本部大楼", NoteStatus.Scheduled, 65.0, true, 26 * HOUR, 5 * DAY, "campus"),
        Mock("跑马地马场看台", NoteStatus.Expired, 90.0, true, 20 * DAY, -3 * DAY, "racecourse"),
        Mock("太平山顶缆车上站", NoteStatus.Scheduled, 110.0, true, 11 * HOUR, 3 * DAY, "peak"),
        Mock("湾仔金紫荆广场", NoteStatus.Unlocked, 55.0, false, 6 * DAY, 9 * DAY, "bauhinia"),
        Mock("荔枝角丽阁邸球场", NoteStatus.Scheduled, 70.0, false, 15 * HOUR, 2 * DAY, "court"),
    )
    return rows.mapIndexed { index, mock ->
        val created = now - mock.ago
        val unlocked = if (mock.status == NoteStatus.Unlocked) created + HOUR else null
        NoteListItem(
            id = "mock-$index",
            placeLabel = mock.place,
            latitude = 22.3668 + index * 0.002,
            longitude = 114.1390 + index * 0.001,
            radiusMeters = mock.radius,
            hasImage = mock.hasImage,
            createdAt = created,
            expiresAt = now + mock.ttl,
            unlockedAt = unlocked,
            status = mock.status,
            coverUrl = "https://picsum.photos/seed/${mock.seed}/960/540",
        )
    }
}

private data class Mock(
    val place: String,
    val status: NoteStatus,
    val radius: Double,
    val hasImage: Boolean,
    val ago: Long,
    val ttl: Long,
    val seed: String,
)
