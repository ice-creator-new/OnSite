package com.suncheng.onsite.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.suncheng.onsite.OnSiteViewModel
import com.suncheng.onsite.data.Prefs
import com.suncheng.onsite.ui.glass.BottomTab
import com.suncheng.onsite.ui.glass.OnSiteLiquidBottomTabs
import com.suncheng.onsite.ui.letters.LettersScreen
import com.suncheng.onsite.ui.open.OpenScreen
import com.suncheng.onsite.ui.permission.PermissionScreen
import com.suncheng.onsite.ui.permission.hasBackground
import com.suncheng.onsite.ui.permission.hasFine
import com.suncheng.onsite.ui.theme.IceBg
import com.suncheng.onsite.ui.write.WriteScreen

@Composable
fun AppRoot(
    viewModel: OnSiteViewModel,
    openNoteId: String?,
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var showOnboarding by remember {
        mutableStateOf(!prefs.onboardingDone || !hasFine(context) || !hasBackground(context))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(openNoteId) {
        if (!openNoteId.isNullOrBlank()) {
            viewModel.consumeOpenIntent(openNoteId)
            tab = 2
        }
    }
    LaunchedEffect(state.writeDoneTick) {
        if (state.writeDoneTick > 0) tab = 0
    }

    if (showOnboarding) {
        Box(Modifier.fillMaxSize().background(IceBg)) {
            IcyBackdrop()
            PermissionScreen(
                onReady = {
                    prefs.onboardingDone = true
                    showOnboarding = false
                    viewModel.refreshLocation(alsoLabel = true)
                },
            )
        }
        return
    }

    val backdrop = rememberLayerBackdrop {
        drawRect(IceBg)
        drawContent()
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            IcyBackdrop()
            when (tab) {
                0 -> LettersScreen(notes = state.notes, backdrop = backdrop, modifier = Modifier.fillMaxSize())
                1 -> WriteScreen(
                    placePreview = state.placePreview,
                    busy = state.writeBusy,
                    error = state.writeError,
                    backdrop = backdrop,
                    onClearError = viewModel::clearWriteError,
                    onRefreshPlace = { viewModel.refreshLocation(alsoLabel = true) },
                    onSubmit = { body, place, radius, expiry, image ->
                        viewModel.createNote(body, place, radius, expiry, image)
                    },
                )
                else -> OpenScreen(
                    unlocking = state.unlocking,
                    result = state.unlock,
                    backdrop = backdrop,
                    onRetry = { viewModel.tryOpen() },
                )
            }
        }

        OnSiteLiquidBottomTabs(
            tabs = listOf(
                BottomTab("信", Icons.Outlined.MailOutline),
                BottomTab("写", Icons.Outlined.Create),
                BottomTab("拆", Icons.Outlined.MarkEmailUnread),
            ),
            selectedIndex = tab,
            onSelected = { index ->
                tab = index
                when (index) {
                    1 -> viewModel.refreshLocation(alsoLabel = true)
                    2 -> viewModel.tryOpen()
                }
            },
            backdrop = backdrop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun IcyBackdrop() {
    Canvas(Modifier.fillMaxSize().background(IceBg)) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF182033), IceBg),
                center = Offset(size.width * 0.55f, size.height * 0.18f),
                radius = size.maxDimension * 0.7f,
            ),
        )
        val step = 48.dp.toPx()
        val line = Color(0x10D7E4F5)
        var x = 0f
        while (x < size.width) {
            drawLine(line, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}
