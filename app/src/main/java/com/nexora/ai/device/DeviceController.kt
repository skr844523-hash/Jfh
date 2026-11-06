package com.nexora.ai.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryInfo(
    val levelPercentage: Int = 100,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 25.0f,
    val health: String = "Good"
)

data class DeviceTelemetry(
    val model: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    val board: String = Build.BOARD,
    val battery: BatteryInfo = BatteryInfo()
)

class DeviceController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraIdWithFlash: String? = null

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    init {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(
                    android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
                if (hasFlash && cameraIdWithFlash == null) {
                    cameraIdWithFlash = id
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Toggles the hardware camera flashlight.
     */
    fun toggleFlashlight(on: Boolean? = null): Boolean {
        val targetState = on ?: !_isFlashlightOn.value
        val camId = cameraIdWithFlash ?: return false

        return try {
            cameraManager?.setTorchMode(camId, targetState)
            _isFlashlightOn.value = targetState
            true
        } catch (e: CameraAccessException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Triggers hardware haptic vibration feedback.
     */
    fun triggerVibration(durationMillis: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Fetches live battery telemetry.
     */
    fun getBatteryInfo(): BatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter) ?: return BatteryInfo()

        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100

        val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
        val tempCelsius = tempTenths / 10.0f

        return BatteryInfo(
            levelPercentage = pct,
            isCharging = isCharging,
            temperatureCelsius = tempCelsius
        )
    }

    /**
     * Fetches complete device telemetry.
     */
    fun getDeviceTelemetry(): DeviceTelemetry {
        return DeviceTelemetry(
            battery = getBatteryInfo()
        )
    }

    /**
     * Opens system settings or an installed app.
     */
    fun openSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
