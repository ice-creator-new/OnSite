package com.suncheng.onsite.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import com.suncheng.onsite.ui.theme.IceAccent
import com.suncheng.onsite.ui.theme.IceMuted
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class BottomTab(
    val label: String,
    val icon: ImageVector,
)

@Composable
fun OnSiteLiquidBottomTabs(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val capsule = Capsule()
    val mild = isXiaomiFamily()
    val containerColor = Color(0xFF1C1C1E).copy(alpha = if (mild) 0.16f else 0.28f)
    val tabCount = tabs.size.coerceAtLeast(1)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val droplet = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    var press by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val tabWidthPx = with(density) { maxWidth.toPx() } / tabCount
        val tabWidth = maxWidth / tabCount

        LaunchedEffect(selectedIndex, tabWidthPx) {
            if (!dragging) {
                droplet.animateTo(
                    selectedIndex * tabWidthPx,
                    spring(dampingRatio = 0.78f, stiffness = 380f),
                )
            }
        }

        val previewIndex =
            if (tabWidthPx <= 0f) selectedIndex
            else (droplet.value / tabWidthPx).roundToInt().coerceIn(0, tabCount - 1)

        Box(
            Modifier
                .fillMaxSize()
                .then(
                    if (canUseBackdropEngine()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsule },
                            effects = {
                                blur(4.dp.toPx())
                                if (!mild) vibrancy()
                                lens(
                                    refractionHeight = 14.dp.toPx(),
                                    refractionAmount = 18.dp.toPx(),
                                    chromaticAberration = !mild,
                                )
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.55f) },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.fallbackGlass(capsule)
                    },
                )
                .pointerInput(tabCount, tabWidthPx) {
                    detectTapGestures { offset ->
                        if (tabWidthPx <= 0f) return@detectTapGestures
                        val index = (offset.x / tabWidthPx).toInt().coerceIn(0, tabCount - 1)
                        onSelected(index)
                    }
                }
                .pointerInput(tabCount, tabWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragging = true
                            press = true
                        },
                        onDragEnd = {
                            press = false
                            dragging = false
                            if (tabWidthPx <= 0f) return@detectHorizontalDragGestures
                            val index = (droplet.value / tabWidthPx).roundToInt()
                                .coerceIn(0, tabCount - 1)
                            scope.launch {
                                droplet.animateTo(
                                    index * tabWidthPx,
                                    spring(dampingRatio = 0.72f, stiffness = 420f),
                                )
                            }
                            onSelected(index)
                        },
                        onDragCancel = {
                            press = false
                            dragging = false
                            scope.launch {
                                droplet.animateTo(
                                    selectedIndex * tabWidthPx,
                                    spring(dampingRatio = 0.8f, stiffness = 380f),
                                )
                            }
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            val max = (tabCount - 1) * tabWidthPx
                            scope.launch {
                                droplet.snapTo((droplet.value + amount).coerceIn(0f, max))
                            }
                        },
                    )
                },
        )

        val stretch = if (dragging || press) 1.16f else 1f
        Box(
            Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .offset { IntOffset(droplet.value.roundToInt(), 0) }
                .width(tabWidth - 10.dp)
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = stretch
                    scaleY = if (press) 1.06f else 1f
                }
                .then(
                    if (canUseBackdropEngine()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsule },
                            effects = {
                                blur(3.dp.toPx())
                                if (!mild) vibrancy()
                                lens(
                                    refractionHeight = if (dragging) 16.dp.toPx() else 10.dp.toPx(),
                                    refractionAmount = if (dragging) 22.dp.toPx() else 16.dp.toPx(),
                                    chromaticAberration = !mild,
                                )
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.78f) },
                            onDrawSurface = { drawRect(Color.White.copy(alpha = 0.10f)) },
                        )
                    } else {
                        Modifier.fallbackGlass(capsule)
                    },
                ),
        )

        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == previewIndex
                val tint by animateColorAsState(
                    targetValue = if (selected) IceAccent else IceMuted,
                    animationSpec = spring(stiffness = 500f),
                    label = "tabTint$index",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics {
                            role = Role.Tab
                            this.selected = selected
                        }
                        .scale(if (selected) 1.06f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(22.dp),
                        tint = tint,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = tint,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
        }
    }
}
