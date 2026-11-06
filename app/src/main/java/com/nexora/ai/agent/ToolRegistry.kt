package com.nexora.ai.agent

import com.nexora.ai.device.DeviceController
import com.nexora.ai.integrations.SocialMediaIntegrations
import com.nexora.ai.integrations.SocialPlatform
import com.nexora.ai.integrations.SocialPostRequest

interface AgentTool {
    val name: String
    val description: String
    val isSensitive: Boolean
    val impactExplanation: String
    suspend fun execute(parameters: Map<String, String>): Result<String>
}

class FlashlightTool(private val deviceController: DeviceController) : AgentTool {
    override val name: String = "toggle_flashlight"
    override val description: String = "تشغيل أو إطفاء فلاش الكاميرا للجهاز"
    override val isSensitive: Boolean = true
    override val impactExplanation: String = "سيتم تفعيل عتاد الفلاش الخاص بالكاميرا وقد يستهلك البطارية"

    override suspend fun execute(parameters: Map<String, String>): Result<String> {
        val state = parameters["state"]?.equals("on", ignoreCase = true)
        val success = deviceController.toggleFlashlight(state)
        return if (success) {
            Result.success("تم تغيير حالة الفلاش بنجاح")
        } else {
            Result.failure(Exception("تعذر تفعيل الفلاش أو الكاميرا غير متاحة"))
        }
    }
}

class VibrationTool(private val deviceController: DeviceController) : AgentTool {
    override val name: String = "haptic_pulse"
    override val description: String = "إرسال نبضة اهتزاز عبر محرك الهابتك للجهاز"
    override val isSensitive: Boolean = false
    override val impactExplanation: String = "نبضة اهتزاز تنبيهية قصيرة"

    override suspend fun execute(parameters: Map<String, String>): Result<String> {
        deviceController.triggerVibration(150)
        return Result.success("تم تشغيل نبضة الاهتزاز")
    }
}

class BatteryTelemetryTool(private val deviceController: DeviceController) : AgentTool {
    override val name: String = "get_battery_telemetry"
    override val description: String = "قراءة مستوى شحن البطارية وحالتها الفيزيائية"
    override val isSensitive: Boolean = false
    override val impactExplanation: String = "قراءة مستشعرات الطاقة للجهاز"

    override suspend fun execute(parameters: Map<String, String>): Result<String> {
        val battery = deviceController.getBatteryInfo()
        val chargingStatus = if (battery.isCharging) "قيد الشحن" else "على البطارية"
        return Result.success("نسبة الشحن: ${battery.levelPercentage}% ($chargingStatus) | الحرارة: ${battery.temperatureCelsius}°C")
    }
}

class SocialPublishTool(private val socialIntegrations: SocialMediaIntegrations) : AgentTool {
    override val name: String = "publish_social_post"
    override val description: String = "نشر أو إرسال منشور عبر منصات التواصل الاجتماعي (واتساب، فيسبوك، انستغرام، تيليجرام)"
    override val isSensitive: Boolean = true
    override val impactExplanation: String = "سيتم نقل المحتوى وفتح المنصة الاجتماعية المحددة لنشر المنشور بعد موافقتك الصريحة"

    override suspend fun execute(parameters: Map<String, String>): Result<String> {
        val platformStr = parameters["platform"]?.lowercase() ?: "general"
        val content = parameters["content"] ?: return Result.failure(IllegalArgumentException("محتوى المنشور فارغ"))

        val platform = when {
            platformStr.contains("whatsapp") || platformStr.contains("واتس") -> SocialPlatform.WHATSAPP
            platformStr.contains("facebook") || platformStr.contains("فيسبوك") -> SocialPlatform.FACEBOOK
            platformStr.contains("instagram") || platformStr.contains("انستا") -> SocialPlatform.INSTAGRAM
            platformStr.contains("telegram") || platformStr.contains("تيليجرام") -> SocialPlatform.TELEGRAM
            platformStr.contains("twitter") || platformStr.contains("تويتر") || platformStr.contains("x") -> SocialPlatform.TWITTER
            else -> SocialPlatform.GENERAL
        }

        val result = socialIntegrations.publishViaIntent(
            SocialPostRequest(
                platform = platform,
                content = content
            )
        )

        return if (result.success) {
            Result.success(result.message)
        } else {
            Result.failure(Exception(result.message))
        }
    }
}

class ToolRegistry(
    deviceController: DeviceController,
    socialIntegrations: SocialMediaIntegrations
) {
    private val tools = mutableMapOf<String, AgentTool>()

    init {
        register(FlashlightTool(deviceController))
        register(VibrationTool(deviceController))
        register(BatteryTelemetryTool(deviceController))
        register(SocialPublishTool(socialIntegrations))
    }

    fun register(tool: AgentTool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): AgentTool? = tools[name]

    fun getAllTools(): List<AgentTool> = tools.values.toList()
}
