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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.ui.components.PulsingAiCore
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

@Composable
fun HomeScreen(
    isArabic: Boolean,
    hasApiKey: Boolean,
    activeKeyLabel: String?,
    activeModel: String,
    onNavigateToTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title & Tagline
        Text(
            text = "NEXORA AI V2",
            color = NexoraElectricCyan,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (isArabic) "نظام التشغيل الذكي المستقل" else "Autonomous AI Operating System",
            color = NexoraTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Center Pulsing AI Core
        PulsingAiCore(
            size = 120.dp,
            isProcessing = false
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Greeting
        Text(
            text = if (isArabic) "أهلاً بك، أنا NEXORA" else "Welcome, I am NEXORA",
            color = NexoraTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Status Indicators
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (hasApiKey) NexoraSuccessGreen else NexoraErrorRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasApiKey) {
                    if (isArabic) "نظام Gemini متصل ($activeModel)" else "Gemini System Online ($activeModel)"
                } else {
                    if (isArabic) "مفتاح Gemini غير محدد" else "Gemini API Key Required"
                },
                color = if (hasApiKey) NexoraSuccessGreen else NexoraErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (activeKeyLabel != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                color = NexoraSurfaceVariant
            ) {
                Text(
                    text = if (isArabic) "المفتاح: $activeKeyLabel" else "Active: $activeKeyLabel",
                    color = NexoraElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Grid
        Text(
            text = if (isArabic) "الإجراءات السريعة" else "Quick Actions",
            color = NexoraTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                title = if (isArabic) "محادثة AI" else "AI Chat",
                subtitle = if (isArabic) "حوار متعدد الجولات" else "Multi-turn Chat",
                icon = Icons.Default.Chat,
                accentColor = NexoraElectricCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) } // Chat tab
            )
            QuickActionCard(
                title = if (isArabic) "وكيل المهام" else "Agent Engine",
                subtitle = if (isArabic) "تخطيط وتنفيذ مستقل" else "Plan & Execute",
                icon = Icons.Default.SmartToy,
                accentColor = NexoraPrimaryPurple,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(2) } // Agent tab
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                title = if (isArabic) "عتاد الجهاز" else "Device OS",
                subtitle = if (isArabic) "فلاش، نبض، وبطارية" else "Torch, Haptic & Power",
                icon = Icons.Default.PhoneAndroid,
                accentColor = NexoraSuccessGreen,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(3) } // Device tab
            )
            QuickActionCard(
                title = if (isArabic) "مفاتيح API" else "API Keys",
                subtitle = if (isArabic) "إضافة وفحص وتشفير" else "Manage & Test Keys",
                icon = Icons.Default.VpnKey,
                accentColor = NexoraElectricCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(4) } // Settings tab
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Capabilities Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NexoraElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "مميزات نظام NEXORA AI V2" else "NEXORA AI V2 Architecture",
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FeatureItem("🔒", if (isArabic) "حماية المفاتيح بتشفير عتادي Android Keystore" else "Hardware Keystore Encryption for all API keys")
                FeatureItem("⚡", if (isArabic) "محرك وكيل مستقل مع موافقة بشرية للعمليات الحساسة" else "Autonomous Agent Planner with Human-in-the-Loop approval")
                FeatureItem("📱", if (isArabic) "تكامل مباشر مع مستشعرات وعتاد هاتف أندرويد" else "Native hardware integration: Camera Flash, Haptic, Battery")
                FeatureItem("🌐", if (isArabic) "استرجاع تلقائي وتجربة نماذج متعددة (Gemini 2.0 / 1.5)" else "Multi-model auto-recovery across v1beta & v1 endpoints")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = NexoraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = NexoraTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun FeatureItem(emoji: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = NexoraTextSecondary, fontSize = 12.sp)
    }
}
