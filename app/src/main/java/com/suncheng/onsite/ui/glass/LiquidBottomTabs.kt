package com.suncheng.onsite.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import com.suncheng.onsite.ui.theme.IceFg
import com.suncheng.onsite.ui.theme.IceMuted

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
    val capsule = RoundedCornerShape(percent = 50)
    val containerColor = Color(0xFF121212).copy(alpha = 0.38f)
    val tabCount = tabs.size.coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(64.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val tabWidth = maxWidth / tabCount
        val dropletOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f),
            label = "droplet",
        )

        Box(
            Modifier.fillMaxSize().drawBackdrop(
                backdrop = backdrop,
                shape = { runCatching { Capsule() }.getOrDefault(capsule) },
                effects = {
                    vibrancy()
                    blur(8.dp.toPx())
                    if (canUseLens()) lens(24.dp.toPx(), 24.dp.toPx())
                },
                highlight = { Highlight.Default.copy(alpha = 0.45f) },
                onDrawSurface = { drawRect(containerColor) },
            )
        )

        Box(
            Modifier
                .padding(4.dp)
                .offset(x = dropletOffset)
                .width(tabWidth - 8.dp)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { runCatching { Capsule() }.getOrDefault(capsule) },
                    effects = {
                        vibrancy()
                        blur(6.dp.toPx())
                        if (canUseLens()) {
                            lens(
                                refractionHeight = 12.dp.toPx(),
                                refractionAmount = 16.dp.toPx(),
                                chromaticAberration = true,
                            )
                        }
                    },
                    highlight = { Highlight.Default.copy(alpha = 0.7f) },
                    onDrawSurface = { drawRect(Color(0xFF1A1A1A).copy(alpha = 0.22f)) },
                )
        )

        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "tabScale$index",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelected(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(18.dp).scale(scale),
                            tint = if (selected) IceFg else IceMuted,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            color = if (selected) IceFg else IceMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}
