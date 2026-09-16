package com.suncheng.onsite.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.suncheng.onsite.ui.theme.IceAccent
import com.suncheng.onsite.ui.theme.IceFg
import com.suncheng.onsite.ui.theme.IceMuted

@Composable
fun PermissionScreen(onReady: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    var step by remember { mutableStateOf(explainText(context)) }

    val notifyLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> step = explainText(context) }

    val fineLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ -> step = explainText(context) }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) onReady() else step = explainText(context) }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("到场", color = IceFg, fontSize = 34.sp)
        Spacer(Modifier.height(8.dp))
        Text("把一句话钉在某个地点。\n人到了才看得见，时间到了就消失。", color = IceMuted, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(28.dp))
        Text("只监测你钉过的地点。到了才提醒。不会记录你去过哪里。", color = IceFg, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(20.dp))
        Text(step, color = IceMuted, fontSize = 13.sp, lineHeight = 20.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = ctaLabel(context),
            color = IceAccent,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).clickable {
                when {
                    hasBackground(context) && hasFine(context) -> onReady()
                    !hasNotify(context) && Build.VERSION.SDK_INT >= 33 ->
                        notifyLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    !hasFine(context) -> fineLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                    !hasBackground(context) -> {
                        backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            )
                        ) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ),
                            )
                        }
                    }
                    else -> onReady()
                }
                step = explainText(context)
            },
        )
    }
}

fun hasFine(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

fun hasBackground(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

fun hasNotify(context: android.content.Context): Boolean {
    if (Build.VERSION.SDK_INT < 33) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

private fun explainText(context: android.content.Context): String = when {
    !hasNotify(context) -> "下一步：允许通知。标题只会写「你到了」，不会预告内容。"
    !hasFine(context) -> "下一步：允许定位。用来把信钉在你站的地方，拆信时再确认一次距离。"
    !hasBackground(context) -> "下一步：允许「始终允许」定位。只用地理围栏，不做持续追踪。"
    else -> "权限已齐。可以进去了。"
}

private fun ctaLabel(context: android.content.Context): String = when {
    hasFine(context) && hasBackground(context) -> "进入"
    !hasNotify(context) -> "允许通知"
    !hasFine(context) -> "允许定位"
    else -> "始终允许定位"
}
