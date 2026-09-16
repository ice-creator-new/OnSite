package com.suncheng.onsite.ui.letters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.suncheng.onsite.data.NoteListItem
import com.suncheng.onsite.data.NoteStatus
import com.suncheng.onsite.ui.glass.liquidSurface
import com.suncheng.onsite.ui.theme.IceDim
import com.suncheng.onsite.ui.theme.IceFg
import com.suncheng.onsite.ui.theme.IceMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LettersScreen(
    notes: List<NoteListItem>,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("信", color = IceFg, fontSize = 28.sp)
        Text("只看见地点和状态。正文要到场才开。", color = IceMuted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        if (notes.isEmpty()) {
            Text("还没有留下信。", color = IceDim, fontSize = 15.sp)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notes, key = { it.id }) { note ->
                    LetterRow(note, backdrop)
                }
            }
        }
    }
}

@Composable
private fun LetterRow(note: NoteListItem, backdrop: Backdrop) {
    Column(
        Modifier
            .fillMaxWidth()
            .liquidSurface(backdrop, shape = RoundedCornerShape(22.dp), refraction = 16.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(note.placeLabel, color = IceFg, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(note.status.label(), color = note.status.tint(), fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${formatTime(note.createdAt)} · ${note.radiusMeters.toInt()}m · ${if (note.hasImage) "有图" else "无图"}",
            color = IceDim,
            fontSize = 12.sp,
        )
    }
}

private fun NoteStatus.label(): String = when (this) {
    NoteStatus.Scheduled -> "等待到场"
    NoteStatus.Nearby -> "可拆"
    NoteStatus.Unlocked -> "已拆"
    NoteStatus.Expired -> "已过期"
}

private fun NoteStatus.tint() = when (this) {
    NoteStatus.Scheduled -> IceMuted
    NoteStatus.Nearby -> IceFg
    NoteStatus.Unlocked -> IceDim
    NoteStatus.Expired -> IceDim
}

private fun formatTime(millis: Long): String {
    return SimpleDateFormat("M/d HH:mm", Locale.getDefault()).format(Date(millis))
}
