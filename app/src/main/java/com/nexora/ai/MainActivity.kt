package com.nexora.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.data.api.GeminiService
import com.nexora.ai.data.repository.SecureKeyRepository
import com.nexora.ai.data.repository.SecureKeyRepositoryImpl
import com.nexora.ai.ui.theme.NexoraCardBackground
import com.nexora.ai.ui.theme.NexoraCardBorder
import com.nexora.ai.ui.theme.NexoraDarkBackground
import com.nexora.ai.ui.theme.NexoraElectricCyan
import com.nexora.ai.ui.theme.NexoraErrorRed
import com.nexora.ai.ui.theme.NexoraPrimaryPurple
import com.nexora.ai.ui.theme.NexoraSuccessGreen
import com.nexora.ai.ui.theme.NexoraSurface
import com.nexora.ai.ui.theme.NexoraSurfaceVariant
import com.nexora.ai.ui.theme.NexoraTextPrimary
import com.nexora.ai.ui.theme.NexoraTextSecondary
import com.nexora.ai.ui.theme.NexoraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var secureKeyRepository: SecureKeyRepository
    private lateinit var geminiService: GeminiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        secureKeyRepository = SecureKeyRepositoryImpl(applicationContext)
        geminiService = GeminiService.create(applicationContext, secureKeyRepository)

        setContent {
            NexoraTheme {
                MainScreen(
                    geminiService = geminiService,
                    secureKeyRepository = secureKeyRepository
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    geminiService: GeminiService,
    secureKeyRepository: SecureKeyRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var isArabic by remember { mutableStateOf(true) }
    var hasKey by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showKeyEditor by remember { mutableStateOf(false) }
    var keySaveMessage by remember { mutableStateOf<String?>(null) }

    var promptInput by remember {
        mutableStateOf(if (isArabic) "ما هي قدراتك التشغيلية يا NEXORA؟" else "What are your core capabilities, NEXORA?")
    }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check key status on startup
    LaunchedEffect(Unit) {
        hasKey = secureKeyRepository.hasApiKey()
    }

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(NexoraDarkBackground),
            containerColor = NexoraDarkBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Language & Settings Switchers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEXORA AI V2",
                            color = NexoraElectricCyan,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("app_title")
                        )
                        Text(
                            text = if (isArabic) "نظام التشغيل الذكي المستقل" else "Autonomous AI Operating System",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isArabic = !isArabic
                                    promptInput = if (isArabic) {
                                        "ما هي قدراتك التشغيلية يا NEXORA؟"
                                    } else {
                                        "What are your core capabilities, NEXORA?"
                                    }
                                }
                                .testTag("language_toggle"),
                            color = NexoraSurfaceVariant
                        ) {
                            Text(
                                text = if (isArabic) "English" else "العربية",
                                color = NexoraPrimaryPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showKeyEditor = !showKeyEditor },
                            modifier = Modifier.testTag("key_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "API Key Settings",
                                tint = if (hasKey) NexoraElectricCyan else NexoraErrorRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated AI Core
                AnimatedAiCore(isProcessing = isLoading)

                Spacer(modifier = Modifier.height(16.dp))

                // Greeting & Status Card
                Text(
                    text = if (isArabic) "أهلاً بك، أنا NEXORA" else "Welcome, I am NEXORA",
                    color = NexoraTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("greeting_text")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (hasKey) NexoraSuccessGreen else NexoraErrorRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasKey) {
                            if (isArabic) "نظام Gemini متصل وجاهز (Android Keystore)" else "Gemini System Online (Android Keystore)"
                        } else {
                            if (isArabic) "مفتاح Gemini مفقود (يرجى إدخال المفتاح)" else "Gemini API Key Required (Click Key Icon)"
                        },
                        color = if (hasKey) NexoraSuccessGreen else NexoraErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Key Configuration Expandable Panel
                AnimatedVisibility(visible = showKeyEditor || !hasKey) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = NexoraElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "إعدادات الأمان (Android Keystore)" else "Keystore Security Configuration",
                                    color = NexoraTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isArabic)
                                    "يتم تشفير وتخزين مفتاح Gemini API في عتاد Android Keystore ولا يتم رفعه إلى أي خادم خارجي."
                                else
                                    "Your Gemini API key is encrypted hardware-side using Android Keystore and never exposed.",
                                color = NexoraTextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = {
                                    Text(
                                        text = if (isArabic) "الصق مفتاح Gemini API هنا..." else "Paste Gemini API Key here...",
                                        color = NexoraTextSecondary,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("api_key_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NexoraElectricCyan,
                                    unfocusedBorderColor = NexoraCardBorder,
                                    focusedTextColor = NexoraTextPrimary,
                                    unfocusedTextColor = NexoraTextPrimary,
                                    focusedContainerColor = NexoraSurface,
                                    unfocusedContainerColor = NexoraSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (hasKey) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                secureKeyRepository.clearGeminiApiKey()
                                                hasKey = secureKeyRepository.hasApiKey()
                                                keySaveMessage = if (isArabic) "تم حذف المفتاح" else "Key cleared"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                                        modifier = Modifier.testTag("clear_key_button")
                                    ) {
                                        Text(
                                            text = if (isArabic) "مسح" else "Clear",
                                            color = NexoraErrorRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Button(
                                    onClick = {
                                        if (apiKeyInput.isNotBlank()) {
                                            coroutineScope.launch {
                                                secureKeyRepository.saveGeminiApiKey(apiKeyInput.trim())
                                                hasKey = secureKeyRepository.hasApiKey()
                                                apiKeyInput = ""
                                                keySaveMessage = if (isArabic) "تم حفظ وتشفير المفتاح بنجاح!" else "Key encrypted & saved successfully!"
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimaryPurple),
                                    modifier = Modifier.testTag("save_key_button")
                                ) {
                                    Text(
                                        text = if (isArabic) "حفظ وتشفير" else "Encrypt & Save",
                                        color = Color.White
                                    )
                                }
                            }

                            keySaveMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg,
                                    color = NexoraSuccessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // AI Terminal Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexoraCardBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NexoraElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "محرك الذكاء الاصطناعي (Gemini Service)" else "AI Engine (Gemini Service)",
                                color = NexoraTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            label = {
                                Text(
                                    text = if (isArabic) "الأمر أو الرسالة" else "Command / Prompt",
                                    color = NexoraTextSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prompt_input_field"),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NexoraPrimaryPurple,
                                unfocusedBorderColor = NexoraCardBorder,
                                focusedTextColor = NexoraTextPrimary,
                                unfocusedTextColor = NexoraTextPrimary,
                                focusedContainerColor = NexoraSurface,
                                unfocusedContainerColor = NexoraSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (promptInput.isNotBlank() && !isLoading) {
                                    isLoading = true
                                    errorMessage = null
                                    aiResponse = null
                                    coroutineScope.launch {
                                        val systemInstruction = if (isArabic) {
                                            "أنت NEXORA AI، نظام تشغيل ذكي شخصي مستقل ومتقدم. أجب بدقة وذكاء وأسلوب تقني احترافي."
                                        } else {
                                            "You are NEXORA AI, an advanced autonomous AI personal operating system. Answer accurately, intelligently, and with high technical precision."
                                        }
                                        val result = geminiService.generateText(
                                            prompt = promptInput,
                                            systemInstruction = systemInstruction
                                        )
                                        result.fold(
                                            onSuccess = { responseText ->
                                                aiResponse = responseText
                                                isLoading = false
                                            },
                                            onFailure = { error ->
                                                errorMessage = error.localizedMessage ?: "Unknown error"
                                                isLoading = false
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("execute_ai_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimaryPurple),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic) "جاري المعالجة عبر Gemini..." else "Querying Gemini API...",
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "إرسال إلى NEXORA" else "Execute with NEXORA",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Response Display
                        aiResponse?.let { text ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NexoraElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = NexoraSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NexoraElectricCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isArabic) "استجابة NEXORA AI:" else "NEXORA AI Response:",
                                            color = NexoraElectricCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = text,
                                        color = NexoraTextPrimary,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.testTag("ai_response_text")
                                    )
                                }
                            }
                        }

                        // Error Display
                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, NexoraErrorRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = NexoraSurface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = NexoraErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = err,
                                        color = NexoraErrorRed,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.testTag("ai_error_text")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Animated glowing AI core with breathing pulsation.
 */
@Composable
fun AnimatedAiCore(isProcessing: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isProcessing) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isProcessing) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp)
            .scale(scale)
    ) {
        // Outer glowing ring
        Box(
            modifier = Modifier
                .size(105.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NexoraElectricCyan.copy(alpha = 0.35f),
                            NexoraPrimaryPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Middle ring
        Box(
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape)
                .border(2.dp, NexoraElectricCyan.copy(alpha = 0.8f), CircleShape)
                .background(NexoraSurfaceVariant)
        )
        // Center Core
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(NexoraPrimaryPurple, NexoraElectricCyan)
                    )
                )
        )
    }
}
