package com.nexora.ai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.data.api.GeminiService
import com.nexora.ai.data.api.model.Content
import com.nexora.ai.data.api.model.Part
import com.nexora.ai.ui.theme.NexoraCardBackground
import com.nexora.ai.ui.theme.NexoraCardBorder
import com.nexora.ai.ui.theme.NexoraDarkBackground
import com.nexora.ai.ui.theme.NexoraElectricCyan
import com.nexora.ai.ui.theme.NexoraErrorRed
import com.nexora.ai.ui.theme.NexoraPrimaryPurple
import com.nexora.ai.ui.theme.NexoraSurface
import com.nexora.ai.ui.theme.NexoraSurfaceVariant
import com.nexora.ai.ui.theme.NexoraTextPrimary
import com.nexora.ai.ui.theme.NexoraTextSecondary
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "nexora"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatScreen(
    geminiService: GeminiService,
    activeModel: String,
    isArabic: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "nexora",
                text = if (isArabic)
                    "أهلاً بك! أنا NEXORA AI، نظام تشغيلك الذكي. كيف يمكنني مساعدتك اليوم؟"
                else
                    "Welcome! I am NEXORA AI, your autonomous operating system. How may I assist you today?"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val suggestedPrompts = if (isArabic) {
        listOf(
            "ما هي قدراتك التشغيلية؟",
            "افحص حالة مستشعرات الهاتف",
            "كيف يعمل التشفير في Keystore؟",
            "لخص مهامي الحالية"
        )
    } else {
        listOf(
            "What are your core capabilities?",
            "Inspect device telemetry",
            "Explain Keystore hardware security",
            "Summarize my tasks"
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || isSending) return
        val userMsg = prompt.trim()
        inputText = ""
        errorMessage = null
        messages.add(ChatMessage(sender = "user", text = userMsg))
        isSending = true

        coroutineScope.launch {
            // Build conversation history for multi-turn
            val history = messages.dropLast(1).map {
                Content(
                    role = if (it.sender == "user") "user" else "model",
                    parts = listOf(Part(text = it.text))
                )
            }

            val systemInstruction = if (isArabic) {
                "أنت NEXORA AI، نظام تشغيل شخصي ذكي مستقل فائق التطور. أجب بدقة وعمق وأسلوب تقني راقٍ."
            } else {
                "You are NEXORA AI, an advanced autonomous AI personal operating system. Answer accurately, intelligently, and with high technical precision."
            }

            val result = geminiService.generateChatResponse(
                history = history,
                userMessage = userMsg,
                systemInstruction = systemInstruction
            )

            result.fold(
                onSuccess = { responseText ->
                    messages.add(ChatMessage(sender = "nexora", text = responseText))
                    isSending = false
                },
                onFailure = { err ->
                    errorMessage = err.localizedMessage ?: "Unknown error"
                    isSending = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraDarkBackground)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexoraSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NexoraPrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NexoraElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NEXORA Assistant",
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NexoraElectricCyan)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeModel,
                            color = NexoraElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    messages.clear()
                    messages.add(
                        ChatMessage(
                            sender = "nexora",
                            text = if (isArabic) "تم مسح المحادثة. كيف يمكنني مساعدتك الآن؟" else "Chat cleared. How can I help?"
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = NexoraTextSecondary
                )
            }
        }

        // Error message if any
        errorMessage?.let { err ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = NexoraErrorRed.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NexoraErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = err,
                        color = NexoraErrorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .border(
                                1.dp,
                                if (isUser) NexoraPrimaryPurple.copy(alpha = 0.5f) else NexoraCardBorder,
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) NexoraPrimaryPurple.copy(alpha = 0.25f) else NexoraSurface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) (if (isArabic) "أنت" else "You") else "NEXORA AI",
                                color = if (isUser) NexoraPrimaryPurple else NexoraElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = NexoraTextPrimary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = NexoraTextSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("NEXORA Message", msg.text)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, if (isArabic) "تم النسخ" else "Copied", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NexoraElectricCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "NEXORA يفكر ويحلل..." else "NEXORA is analyzing...",
                            color = NexoraTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Suggested Prompts Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NexoraCardBorder, RoundedCornerShape(12.dp))
                        .clickable { sendMessage(prompt) },
                    color = NexoraSurface
                ) {
                    Text(
                        text = prompt,
                        color = NexoraTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexoraSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isArabic) "اكتب رسالتك لـ NEXORA..." else "Type a message to NEXORA...",
                        color = NexoraTextSecondary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NexoraTextPrimary,
                    unfocusedTextColor = NexoraTextPrimary,
                    focusedBorderColor = NexoraElectricCyan,
                    unfocusedBorderColor = NexoraCardBorder,
                    focusedContainerColor = NexoraDarkBackground,
                    unfocusedContainerColor = NexoraDarkBackground
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendMessage(inputText) },
                enabled = inputText.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) NexoraPrimaryPurple else NexoraSurfaceVariant)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else NexoraTextSecondary
                )
            }
        }
    }
}
