package com.suncheng.onsite.ui.glass

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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

private val Settle = spring<Float>(dampingRatio = 0.68f, stiffness = 260f)
private val PressSpring = spring<Float>(dampingRatio = 0.82f, stiffness = 500f)
private val TabMaxWidth = 64.dp
private val BarHeight = 58.dp

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
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val settled = remember { Animatable(0f) }
    val press = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var hoverIndex by remember { mutableIntStateOf(selectedIndex) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight),
        contentAlignment = Alignment.Center,
    ) {
        val itemWidth = minOf(TabMaxWidth, maxWidth / tabCount)
        val barWidth = itemWidth * tabCount
        val tabWidthPx = with(density) { itemWidth.toPx() }
        val maxTravel = (tabCount - 1) * tabWidthPx
        val dropletX = if (dragging) dragX else settled.value

        LaunchedEffect(selectedIndex, tabWidthPx) {
            if (!dragging && tabWidthPx > 0f) {
                hoverIndex = selectedIndex
                settled.animateTo(selectedIndex * tabWidthPx, Settle)
            }
        }

        fun indexOf(x: Float): Int {
            if (tabWidthPx <= 0f) return selectedIndex
            return (x / tabWidthPx).roundToInt().coerceIn(0, tabCount - 1)
        }

        Box(
            Modifier
                .width(barWidth)
                .height(BarHeight)
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
                    if (tabWidthPx <= 0f) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val tracker = VelocityTracker()
                        tracker.addPosition(down.uptimeMillis, down.position)
                        dragX = settled.value
                        dragging = true
                        var lastHover = indexOf(dragX)
                        scope.launch { press.animateTo(1f, PressSpring) }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.changedToUpIgnoreConsumed()) break
                            val delta = change.positionChange().x
                            if (delta == 0f) continue
                            tracker.addPosition(change.uptimeMillis, change.position)
                            change.consume()
                            dragX = (dragX + delta).coerceIn(0f, maxTravel)
                            val hover = indexOf(dragX)
                            if (hover != lastHover) {
                                lastHover = hover
                                hoverIndex = hover
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }

                        dragging = false
                        scope.launch { press.animateTo(0f, PressSpring) }
                        val projected = (dragX + tracker.calculateVelocity().x * 0.08f)
                            .coerceIn(0f, maxTravel)
                        val target = indexOf(projected)
                        hoverIndex = target
                        if (target != selectedIndex) onSelected(target)
                        val end = dragX
                        scope.launch {
                            settled.snapTo(end)
                            settled.animateTo(target * tabWidthPx, Settle)
                        }
                    }
                },
        ) {
            Box(
                Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .width(itemWidth - 8.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = dropletX
                        val p = press.value
                        scaleX = 1f + 0.14f * p
                        scaleY = 1f + 0.05f * p
                    }
                    .then(
                        if (canUseBackdropEngine()) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { capsule },
                                effects = {
                                    val p = press.value
                                    blur(3.dp.toPx())
                                    if (!mild) vibrancy()
                                    lens(
                                        refractionHeight = 10.dp.toPx() + 6.dp.toPx() * p,
                                        refractionAmount = 14.dp.toPx() + 8.dp.toPx() * p,
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
                    val selected = index == hoverIndex
                    val tint = if (selected) IceAccent else IceMuted
                    Column(
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .semantics {
                                role = Role.Tab
                                this.selected = selected
                            }
                            .graphicsLayer {
                                val s = if (selected) 1.04f else 1f
                                scaleX = s
                                scaleY = s
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp),
                            tint = tint,
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = tab.label,
                            color = tint,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            letterSpacing = 0.15.sp,
                        )
                    }
                }
            }
        }
    }
}
