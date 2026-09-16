package com.suncheng.onsite

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.suncheng.onsite.notify.ArriveNotifier
import com.suncheng.onsite.ui.AppRoot
import com.suncheng.onsite.ui.theme.OnSiteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: OnSiteViewModel by viewModels {
        OnSiteViewModel.factory(application)
    }

    private var openNoteId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        openNoteId = intent.getStringExtra(ArriveNotifier.EXTRA_NOTE_ID)
        setContent {
            OnSiteTheme {
                AppRoot(
                    viewModel = viewModel,
                    openNoteId = openNoteId,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openNoteId = intent.getStringExtra(ArriveNotifier.EXTRA_NOTE_ID)
    }
}
