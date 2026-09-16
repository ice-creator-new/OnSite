package com.suncheng.onsite.ui.open

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kyant.backdrop.Backdrop
import com.suncheng.onsite.data.UnlockResult
import com.suncheng.onsite.data.UnlockedNote
import com.suncheng.onsite.ui.glass.liquidSurface
import com.suncheng.onsite.ui.theme.IceDim
import com.suncheng.onsite.ui.theme.IceFg
import com.suncheng.onsite.ui.theme.IceMuted
import java.io.File

@Composable
fun OpenScreen(
    unlocking: Boolean,
    result: UnlockResult?,
    backdrop: Backdrop,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("拆", color = IceFg, fontSize = 28.sp)
        Text("再取一次定位。不在半径里，打不开。", color = IceMuted, fontSize = 13.sp)
        Spacer(Modifier.height(28.dp))
        when {
            unlocking -> Text("正在确认你是否到场…", color = IceMuted)
            result is UnlockResult.Opened -> Envelope(result.note, backdrop)
            result is UnlockResult.EmptyHere -> EmptyState("这里没有信。", onRetry)
            result is UnlockResult.OutOfRange -> EmptyState("还没走进范围。", onRetry)
            result is UnlockResult.Expired -> EmptyState("已经过期，不能读了。", onRetry)
            result is UnlockResult.NoLocation -> EmptyState("没有定位，拆不了。", onRetry)
            else -> EmptyState("这里没有信。", onRetry)
        }
    }
}

@Composable
private fun Envelope(note: UnlockedNote, backdrop: Backdrop) {
    Column(
        Modifier
            .fillMaxWidth()
            .liquidSurface(backdrop, RoundedCornerShape(28.dp), refraction = 22.dp, chromatic = true)
            .padding(22.dp),
    ) {
        Text(note.placeLabel, color = IceDim, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Text(note.body, color = IceFg, fontSize = 20.sp, lineHeight = 28.sp)
        val path = note.imagePath
        if (!path.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun EmptyState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp).clickable(onClick = onRetry),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = IceMuted, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("点一下再试", color = IceDim, fontSize = 12.sp)
        }
    }
}
