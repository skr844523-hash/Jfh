package com.nexora.ai.integrations

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

enum class SocialPlatform(val displayNameAr: String, val displayNameEn: String, val packageName: String) {
    WHATSAPP("واتساب", "WhatsApp", "com.whatsapp"),
    FACEBOOK("فيسبوك", "Facebook", "com.facebook.katana"),
    INSTAGRAM("انستغرام", "Instagram", "com.instagram.android"),
    TELEGRAM("تيليجرام", "Telegram", "org.telegram.messenger"),
    TWITTER("إكس (تويتر)", "X / Twitter", "com.twitter.android"),
    GENERAL("مشاركة عامة", "General Share", "")
}

data class SocialPostRequest(
    val platform: SocialPlatform,
    val content: String,
    val mediaUri: Uri? = null
)

data class SocialPublishResult(
    val success: Boolean,
    val platform: SocialPlatform,
    val message: String,
    val usedFallback: Boolean = false
)

class SocialMediaIntegrations(private val context: Context) {

    /**
     * Checks if the native app for the requested platform is installed.
     */
    fun isAppInstalled(platform: SocialPlatform): Boolean {
        if (platform == SocialPlatform.GENERAL || platform.packageName.isEmpty()) return true
        return try {
            context.packageManager.getPackageInfo(platform.packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Dispatches content to the selected social platform via native Android Intent.
     */
    suspend fun publishViaIntent(request: SocialPostRequest): SocialPublishResult = withContext(Dispatchers.Main) {
        val platform = request.platform
        val content = request.content.trim()

        try {
            when (platform) {
                SocialPlatform.WHATSAPP -> {
                    // Send to WhatsApp text dispatcher
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                        `package` = "com.whatsapp"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (isAppInstalled(SocialPlatform.WHATSAPP)) {
                        context.startActivity(intent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تم فتح تطبيق واتساب وتجهيز المنشور للإرسال بموافقتك."
                        )
                    } else {
                        // Fallback to web link or general share
                        val encoded = URLEncoder.encode(content, "UTF-8")
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encoded")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تطبيق واتساب غير مثبت، تم الفتح عبر المتصفح.",
                            usedFallback = true
                        )
                    }
                }

                SocialPlatform.TELEGRAM -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                        `package` = "org.telegram.messenger"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (isAppInstalled(SocialPlatform.TELEGRAM)) {
                        context.startActivity(intent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تم توجيه المحتوى إلى تيليجرام بنجاح."
                        )
                    } else {
                        val encoded = URLEncoder.encode(content, "UTF-8")
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/share/url?url=$encoded")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تم الفتح عبر بوابة تيليجرام على الويب.",
                            usedFallback = true
                        )
                    }
                }

                SocialPlatform.FACEBOOK -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                        `package` = "com.facebook.katana"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (isAppInstalled(SocialPlatform.FACEBOOK)) {
                        context.startActivity(intent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تم فتح فيسبوك وتجهيز المنشور للنشر."
                        )
                    } else {
                        return@withContext launchGeneralShare(content, platform)
                    }
                }

                SocialPlatform.INSTAGRAM -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, content)
                        `package` = "com.instagram.android"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (isAppInstalled(SocialPlatform.INSTAGRAM)) {
                        context.startActivity(intent)
                        return@withContext SocialPublishResult(
                            success = true,
                            platform = platform,
                            message = "تم توجيه المنشور إلى انستغرام."
                        )
                    } else {
                        return@withContext launchGeneralShare(content, platform)
                    }
                }

                SocialPlatform.TWITTER -> {
                    val encoded = URLEncoder.encode(content, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=$encoded")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return@withContext SocialPublishResult(
                        success = true,
                        platform = platform,
                        message = "تم توجيه التغريدة إلى منصة X (تويتر)."
                    )
                }

                SocialPlatform.GENERAL -> {
                    return@withContext launchGeneralShare(content, platform)
                }
            }
        } catch (e: Exception) {
            return@withContext SocialPublishResult(
                success = false,
                platform = platform,
                message = "فشل النشر: ${e.localizedMessage ?: "حدث خطأ غير متوقع"}"
            )
        }
    }

    private fun launchGeneralShare(content: String, platform: SocialPlatform): SocialPublishResult {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "نشر عبر NEXORA AI").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            SocialPublishResult(
                success = true,
                platform = platform,
                message = "تم فتح قائمة مشاركة النظام لاختيار المنصة المطلوبة.",
                usedFallback = true
            )
        } catch (e: Exception) {
            SocialPublishResult(
                success = false,
                platform = platform,
                message = "تعذر فتح قائمة المشاركة: ${e.message}"
            )
        }
    }
}
