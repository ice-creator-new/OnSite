package com.suncheng.onsite.ui.glass

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.suncheng.onsite.ui.theme.GlassFill
import com.suncheng.onsite.ui.theme.HighlightCold

fun canUseLens(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun Modifier.liquidSurface(
    backdrop: Backdrop,
    shape: Shape = RoundedCornerShape(24.dp),
    refraction: Dp = 20.dp,
    fill: Color = GlassFill,
    chromatic: Boolean = false,
): Modifier {
    return this.drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(8.dp.toPx())
            if (canUseLens()) {
                lens(
                    refractionHeight = refraction.toPx(),
                    refractionAmount = refraction.toPx(),
                    chromaticAberration = chromatic,
                )
            }
        },
        highlight = {
            Highlight.Default.copy(alpha = if (canUseLens()) 0.55f else 0.25f)
        },
        onDrawSurface = { drawRect(fill) },
    )
}

fun Modifier.fallbackGlass(shape: Shape = RoundedCornerShape(24.dp)): Modifier {
    return this
        .clip(shape)
        .drawWithContent {
            drawRoundRect(
                color = Color(0xCC121218),
                cornerRadius = CornerRadius(size.height / 4f, size.height / 4f),
            )
            drawContent()
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0f to HighlightCold,
                    0.18f to Color.Transparent,
                ),
                cornerRadius = CornerRadius(size.height / 4f, size.height / 4f),
            )
        }
        .border(1.dp, Color(0x33E8F4FF), shape)
}
