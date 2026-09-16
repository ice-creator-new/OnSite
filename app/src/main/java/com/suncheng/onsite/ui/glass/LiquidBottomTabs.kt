package com.suncheng.onsite.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
    val capsule = Capsule()
    val containerColor = Color(0xFF1C1C1E).copy(alpha = 0.42f)
    val tabCount = tabs.size.coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val tabWidth = maxWidth / tabCount
        val dropletOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.86f, stiffness = 420f),
            label = "droplet",
        )

        Box(
            Modifier
                .fillMaxSize()
                .then(
                    if (canUseBackdropEngine()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsule },
                            effects = {
                                blur(if (isXiaomiFamily()) 20.dp.toPx() else 12.dp.toPx())
                                if (canUseLens()) {
                                    vibrancy()
                                    lens(20.dp.toPx(), 20.dp.toPx())
                                }
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.38f) },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.fallbackGlass(capsule)
                    },
                ),
        )

        Box(
            Modifier
                .padding(horizontal = 6.dp, vertical = 5.dp)
                .offset(x = dropletOffset)
                .width(tabWidth - 12.dp)
                .fillMaxHeight()
                .padding(vertical = 1.dp)
                .then(
                    if (canUseBackdropEngine()) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { capsule },
                            effects = {
                                blur(if (isXiaomiFamily()) 16.dp.toPx() else 6.dp.toPx())
                                if (canUseLens()) {
                                    vibrancy()
                                    lens(
                                        refractionHeight = 10.dp.toPx(),
                                        refractionAmount = 14.dp.toPx(),
                                        chromaticAberration = true,
                                    )
                                }
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.62f) },
                            onDrawSurface = { drawRect(Color.White.copy(alpha = 0.10f)) },
                        )
                    } else {
                        Modifier.fallbackGlass(capsule)
                    },
                ),
        )

        Row(Modifier.fillMaxSize()) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.06f else 1f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f),
                    label = "tabScale$index",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelected(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(scale),
                        tint = if (selected) IceFg else IceMuted,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        color = if (selected) IceFg else IceMuted,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
        }
    }
}
