package com.suncheng.onsite.ui.write

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.suncheng.onsite.ExpiryPreset
import com.suncheng.onsite.ui.glass.liquidSurface
import com.suncheng.onsite.ui.theme.IceAccent
import com.suncheng.onsite.ui.theme.IceDanger
import com.suncheng.onsite.ui.theme.IceDim
import com.suncheng.onsite.ui.theme.IceFg
import com.suncheng.onsite.ui.theme.IceMuted

@Composable
fun WriteScreen(
    placePreview: String,
    busy: Boolean,
    error: String?,
    backdrop: Backdrop,
    onClearError: () -> Unit,
    onRefreshPlace: () -> Unit,
    onSubmit: (body: String, place: String, radius: Double, expiry: ExpiryPreset, image: Uri?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var body by remember { mutableStateOf("") }
    var place by remember(placePreview) { mutableStateOf(placePreview) }
    var radius by remember { mutableFloatStateOf(50f) }
    var expiry by remember { mutableStateOf(ExpiryPreset.Days7) }
    var image by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        image = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 140.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("写", color = IceFg, fontSize = 28.sp)
        Text("钉在你现在站的地方。离开就看不见正文。", color = IceMuted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().liquidSurface(backdrop, RoundedCornerShape(24.dp)).padding(16.dp)) {
            Text("当前位置", color = IceDim, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.ifBlank { placePreview.ifBlank { "正在取定位…" } },
                color = IceFg,
                fontSize = 15.sp,
                modifier = Modifier.clickable { onRefreshPlace() },
            )
            TextField(
                value = place,
                onValueChange = { place = it },
                placeholder = { Text("改个地点名", color = IceDim) },
                singleLine = true,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().liquidSurface(backdrop, RoundedCornerShape(24.dp)).padding(16.dp)) {
            TextField(
                value = body,
                onValueChange = { body = it },
                placeholder = { Text("写一句，到了才看得见。", color = IceDim) },
                minLines = 4,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (image == null) "附一张图（可选）" else "已选图，再点可换",
                color = IceAccent,
                fontSize = 13.sp,
                modifier = Modifier.clickable { picker.launch("image/*") },
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().liquidSurface(backdrop, RoundedCornerShape(24.dp)).padding(16.dp)) {
            Text("半径  ${radius.toInt()} m", color = IceFg, fontSize = 14.sp)
            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 20f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = IceFg,
                    activeTrackColor = IceAccent,
                    inactiveTrackColor = IceDim,
                ),
            )
            Text("过期", color = IceFg, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpiryPreset.entries.forEach { preset ->
                    val selected = preset == expiry
                    Box(
                        Modifier
                            .liquidSurface(
                                backdrop,
                                RoundedCornerShape(50),
                                refraction = 10.dp,
                                fill = if (selected) Color(0x55FFFFFF) else Color(0x22FFFFFF),
                            )
                            .clickable { expiry = preset }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(preset.label(), color = if (selected) IceFg else IceMuted, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .liquidSurface(backdrop, RoundedCornerShape(50), refraction = 18.dp, chromatic = true)
                .clickable(enabled = !busy) {
                    onSubmit(body, place, radius.toDouble(), expiry, image)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(if (busy) "正在钉…" else "钉在这里", color = IceFg, fontSize = 16.sp)
        }
        if (error != null) {
            Spacer(Modifier.height(10.dp))
            Text(text = error, color = IceDanger, fontSize = 13.sp, modifier = Modifier.clickable { onClearError() })
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = IceAccent,
    focusedTextColor = IceFg,
    unfocusedTextColor = IceFg,
)

private fun ExpiryPreset.label(): String = when (this) {
    ExpiryPreset.Minute -> "1 分钟"
    ExpiryPreset.Hours2 -> "2 小时"
    ExpiryPreset.Today -> "今天"
    ExpiryPreset.Days7 -> "7 天"
}
