package com.nexora.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.device.DeviceController
import com.nexora.ai.ui.theme.NexoraCardBackground
import com.nexora.ai.ui.theme.NexoraCardBorder
import com.nexora.ai.ui.theme.NexoraDarkBackground
import com.nexora.ai.ui.theme.NexoraElectricCyan
import com.nexora.ai.ui.theme.NexoraPrimaryPurple
import com.nexora.ai.ui.theme.NexoraSuccessGreen
import com.nexora.ai.ui.theme.NexoraSurface
import com.nexora.ai.ui.theme.NexoraSurfaceVariant
import com.nexora.ai.ui.theme.NexoraTextPrimary
import com.nexora.ai.ui.theme.NexoraTextSecondary
import com.nexora.ai.ui.theme.NexoraWarningAmber

@Composable
fun DeviceScreen(
    deviceController: DeviceController,
    isArabic: Boolean
) {
    val isFlashlightOn by deviceController.isFlashlightOn.collectAsState()
    var batteryInfo by remember { mutableStateOf(deviceController.getBatteryInfo()) }
    var telemetry by remember { mutableStateOf(deviceController.getDeviceTelemetry()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "عتاد الجهاز ونظام التشغيل" else "Device OS & Hardware",
                    color = NexoraElectricCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "تكامل مباشر مع مستشعرات هاتف أندرويد" else "Native Android Hardware Telemetry",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    batteryInfo = deviceController.getBatteryInfo()
                    telemetry = deviceController.getDeviceTelemetry()
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NexoraElectricCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Flashlight Controller Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isFlashlightOn) NexoraElectricCyan else NexoraCardBorder,
                    RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlashlightOn) NexoraSurface else NexoraCardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFlashlightOn) NexoraElectricCyan.copy(alpha = 0.25f)
                                else NexoraSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = null,
                            tint = if (isFlashlightOn) NexoraElectricCyan else NexoraTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isArabic) "فلاش الكاميرا (Torch)" else "Camera Flashlight",
                            color = NexoraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isFlashlightOn) {
                                if (isArabic) "مضاء حالياً" else "Currently Active"
                            } else {
                                if (isArabic) "مطفأ" else "Turned Off"
                            },
                            color = if (isFlashlightOn) NexoraElectricCyan else NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = isFlashlightOn,
                    onCheckedChange = { deviceController.toggleFlashlight(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NexoraElectricCyan,
                        checkedTrackColor = NexoraPrimaryPurple.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("flashlight_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Haptic Vibration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = null,
                        tint = NexoraPrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "محرك الاهتزاز الهابتك (Haptics)" else "Haptic Feedback Engine",
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "اختبار إرسال نبضات لمسية تنبيهية عبر عتاد الهاتف." else "Trigger physical haptic pulses on Android device.",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { deviceController.triggerVibration(100) },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimaryPurple),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vibrate_short_button")
                    ) {
                        Text(if (isArabic) "نبضة قصيرة (100ms)" else "Short (100ms)", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { deviceController.triggerVibration(300) },
                        colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vibrate_long_button")
                    ) {
                        Text(if (isArabic) "نبضة تأكيدية (300ms)" else "Long (300ms)", fontSize = 11.sp, color = NexoraElectricCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Battery Telemetry Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (batteryInfo.levelPercentage > 20) NexoraSuccessGreen else NexoraWarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "حالة الطاقة والبطارية" else "Battery & Power Telemetry",
                            color = NexoraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = if (batteryInfo.isCharging) NexoraSuccessGreen.copy(alpha = 0.2f) else NexoraSurfaceVariant
                    ) {
                        Text(
                            text = if (batteryInfo.isCharging) {
                                if (isArabic) "قيد الشحن ⚡" else "Charging ⚡"
                            } else {
                                if (isArabic) "على البطارية" else "On Battery"
                            },
                            color = if (batteryInfo.isCharging) NexoraSuccessGreen else NexoraTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${batteryInfo.levelPercentage}%",
                        color = NexoraTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "حرارة البطارية: ${batteryInfo.temperatureCelsius}°C",
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { batteryInfo.levelPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (batteryInfo.levelPercentage > 20) NexoraSuccessGreen else NexoraWarningAmber,
                    trackColor = NexoraSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Specs Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = NexoraElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "مواصفات الجهاز" else "Device Hardware Specifications",
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SpecRow(if (isArabic) "الهاتف" else "Model", telemetry.model)
                SpecRow(if (isArabic) "نظام التشغيل" else "OS Version", telemetry.androidVersion)
                SpecRow(if (isArabic) "اللوحة الأم" else "Board", telemetry.board)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { deviceController.openSettings() },
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = NexoraTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isArabic) "فتح إعدادات النظام" else "Open Android Settings", color = NexoraTextPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = NexoraTextSecondary, fontSize = 12.sp)
        Text(text = value, color = NexoraTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
