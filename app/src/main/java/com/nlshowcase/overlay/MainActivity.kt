package com.nlshowcase.overlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.nlshowcase.overlay.gl.CharacterScene
import com.nlshowcase.overlay.gl.GLTextureView
import com.nlshowcase.overlay.ui.Nl
import kotlin.math.roundToInt

/** Launcher screen: permissions, overlay timings and start / stop buttons. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherScreen(
                onHide = { moveTaskToBack(true) },
            )
        }
    }
}

@Composable
private fun LauncherScreen(onHide: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val heroScene = remember {
        CharacterScene().also {
            it.animation = "Idle"
            it.autoRotate = true
            it.modelScale = 1.05f
        }
    }

    var delay by remember { mutableStateOf(prefs.startDelaySec.toFloat()) }
    var autoHide by remember { mutableStateOf(prefs.autoHideSec.toFloat()) }
    var scale by remember { mutableStateOf(prefs.menuScale) }
    var openOnStart by remember { mutableStateOf(prefs.openMenuOnStart) }
    var blur by remember { mutableStateOf(prefs.blurEnabled) }
    var blurRadius by remember { mutableStateOf(prefs.blurRadius.toFloat()) }
    var homeLine by remember { mutableStateOf(prefs.homeLineVisible) }
    var onBoot by remember { mutableStateOf(prefs.startOnBoot) }
    var model3d by remember { mutableStateOf(prefs.model3dEnabled) }
    var animSpeed by remember { mutableStateOf(prefs.animationSpeed) }

    val notifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    fun save() {
        prefs.startDelaySec = delay.roundToInt()
        prefs.autoHideSec = autoHide.roundToInt()
        prefs.menuScale = scale
        prefs.openMenuOnStart = openOnStart
        prefs.blurEnabled = blur
        prefs.blurRadius = blurRadius.roundToInt()
        prefs.homeLineVisible = homeLine
        prefs.startOnBoot = onBoot
        prefs.model3dEnabled = model3d
        prefs.animationSpeed = animSpeed
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Nl.Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // ------------------------------------------------------------- hero
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(Nl.CardRadius))
                .background(Nl.Card)
                .border(1.dp, Nl.Stroke, RoundedCornerShape(Nl.CardRadius)),
        ) {
            AndroidView(
                factory = { ctx -> GLTextureView(ctx, heroScene) },
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Nl.Bg.copy(alpha = 0.92f)),
                        ),
                    ),
            )
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(
                    "Neverlose",
                    color = Nl.Text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Standoff 2", color = Nl.AccentSoft, fontSize = 13.sp)
                Text("t.me/NeverloseCome", color = Nl.TextFaint, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ------------------------------------------------------ permissions
        val canOverlay = Settings.canDrawOverlays(context)
        Card("Разрешения") {
            SettingRow(
                title = "Поверх других приложений",
                subtitle = if (canOverlay) "Выдано" else "Нужно выдать вручную",
            ) {
                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName),
                            ),
                        )
                    },
                ) {
                    Text(if (canOverlay) "Открыть" else "Выдать", fontSize = 12.sp)
                }
            }
            SettingRow(title = "Уведомления", subtitle = "Нужны для сервиса") {
                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifications.launch("android.permission.POST_NOTIFICATIONS")
                        }
                    },
                ) {
                    Text("Запросить", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --------------------------------------------------------- overlay
        Card("Оверлей") {
            SliderRow(
                title = "Задержка запуска",
                value = delay,
                range = 0f..30f,
                text = delay.roundToInt().toString() + " с",
            ) { delay = it }
            SliderRow(
                title = "Авто-скрытие",
                value = autoHide,
                range = 0f..120f,
                text = if (autoHide < 1f) "выкл" else autoHide.roundToInt().toString() + " с",
            ) { autoHide = it }
            SliderRow(
                title = "Масштаб меню",
                value = scale,
                range = 0.7f..1.3f,
                text = (scale * 100).roundToInt().toString() + "%",
            ) { scale = it }
            ToggleRow("Открывать меню автоматически", openOnStart) { openOnStart = it }
            ToggleRow("Белая линия снизу", homeLine) { homeLine = it }
            ToggleRow("Запуск при загрузке", onBoot) { onBoot = it }
        }

        Spacer(Modifier.height(12.dp))

        // --------------------------------------------------------- effects
        Card("Эффекты") {
            ToggleRow("Блюр фона", blur) { blur = it }
            SliderRow(
                title = "Сила блюра",
                value = blurRadius,
                range = 4f..60f,
                text = blurRadius.roundToInt().toString(),
            ) { blurRadius = it }
            ToggleRow("3D модель в Player", model3d) { model3d = it }
            SliderRow(
                title = "Скорость анимаций",
                value = animSpeed,
                range = 0.3f..2f,
                text = String.format("%.1fx", animSpeed),
            ) { animSpeed = it }
        }

        Spacer(Modifier.height(18.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    save()
                    if (Settings.canDrawOverlays(context)) {
                        OverlayService.start(context)
                        onHide()
                    } else {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.packageName),
                            ),
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Nl.Accent),
                modifier = Modifier.weight(1f),
            ) {
                Text("Запустить", fontSize = 14.sp)
            }
            OutlinedButton(
                onClick = {
                    save()
                    OverlayService.stop(context)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Остановить", fontSize = 14.sp, color = Nl.Text)
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Свайпни или нажми белую линию снизу экрана, чтобы открыть или спрятать меню.",
            color = Nl.TextFaint,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Nl.CardRadius))
            .background(Nl.Card)
            .border(1.dp, Nl.Stroke, RoundedCornerShape(Nl.CardRadius))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            title.uppercase(),
            color = Nl.TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Nl.Text, fontSize = 13.sp)
            if (subtitle != null) {
                Text(subtitle, color = Nl.TextFaint, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        trailing()
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    SettingRow(title = title) {
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Nl.Accent,
                uncheckedTrackColor = Nl.ToggleOff,
                uncheckedBorderColor = Color.Transparent,
            ),
            modifier = Modifier.size(width = 46.dp, height = 26.dp),
        )
    }
}

@Composable
private fun SliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    text: String,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Nl.Text, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(text, color = Nl.AccentSoft, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Nl.Accent,
                inactiveTrackColor = Nl.Field,
            ),
        )
    }
}
