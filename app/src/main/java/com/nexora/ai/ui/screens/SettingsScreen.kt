package com.nexora.ai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.data.api.GeminiService
import com.nexora.ai.data.repository.ApiKeyItem
import com.nexora.ai.data.repository.SecureKeyRepository
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    secureKeyRepository: SecureKeyRepository,
    geminiService: GeminiService,
    isArabic: Boolean,
    onLanguageToggle: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var savedKeys by remember { mutableStateOf<List<ApiKeyItem>>(emptyList()) }
    var activeModel by remember { mutableStateOf("gemini-2.0-flash") }

    // Add key state
    var showAddDialog by remember { mutableStateOf(false) }
    var newKeyLabel by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    var newKeyModel by remember { mutableStateOf("gemini-2.0-flash") }

    // Test key state
    var testingKeyId by remember { mutableStateOf<String?>(null) }
    var testResultMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var testStatusMap by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    fun refreshKeys() {
        coroutineScope.launch {
            savedKeys = secureKeyRepository.getAllKeys()
            activeModel = secureKeyRepository.getActiveModel()
        }
    }

    LaunchedEffect(Unit) {
        refreshKeys()
    }

    val availableModels = listOf(
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-1.5-flash-latest",
        "gemini-2.5-flash",
        "gemini-1.5-pro"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexoraDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "إدارة المفاتيح والأمان" else "API Keys & Security",
                    color = NexoraElectricCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "تشفير عتادي كامل عبر Android Keystore" else "Hardware-backed Android Keystore Encryption",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onLanguageToggle() },
                color = NexoraSurfaceVariant
            ) {
                Text(
                    text = if (isArabic) "English" else "العربية",
                    color = NexoraPrimaryPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Security Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraSurface)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NexoraSuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isArabic) "التشفير المحلي مفعل (AES-256 GCM)" else "Hardware Encryption Active (AES-256 GCM)",
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic)
                            "تُحفظ كافة المفاتيح مشفرة داخل رقاقة الأمان بالهاتف ولا تخرج لأي جهة خارجية."
                        else
                            "All API keys are encrypted inside device secure hardware enclave.",
                        color = NexoraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Model Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NexoraElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "النموذج النشط (Active AI Model)" else "Active AI Model",
                        color = NexoraTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableModels.forEach { modelName ->
                        val isSelected = activeModel == modelName
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) NexoraElectricCyan else NexoraCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    activeModel = modelName
                                    coroutineScope.launch {
                                        secureKeyRepository.setActiveModel(modelName)
                                    }
                                },
                            color = if (isSelected) NexoraPrimaryPurple.copy(alpha = 0.25f) else NexoraSurface
                        ) {
                            Text(
                                text = modelName,
                                color = if (isSelected) NexoraElectricCyan else NexoraTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // API Keys Header & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "مفاتيح API المسجلة (${savedKeys.size})" else "Registered API Keys (${savedKeys.size})",
                color = NexoraTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = { showAddDialog = !showAddDialog },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_key_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "إضافة مفتاح جديد" else "Add New Key",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add Key Expandable Form
        AnimatedVisibility(visible = showAddDialog) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, NexoraElectricCyan, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NexoraSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "إضافة مفتاح API وتشفيره" else "Add and Encrypt API Key",
                        color = NexoraElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newKeyLabel,
                        onValueChange = { newKeyLabel = it },
                        label = { Text(if (isArabic) "اسم المفتاح (مثلاً: مفتاحي الشخصي)" else "Key Label") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary,
                            focusedBorderColor = NexoraElectricCyan,
                            unfocusedBorderColor = NexoraCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newKeyValue,
                        onValueChange = { newKeyValue = it },
                        label = { Text(if (isArabic) "الصق مفتاح API هنا" else "Paste API Key Here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_key_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NexoraTextPrimary,
                            unfocusedTextColor = NexoraTextPrimary,
                            focusedBorderColor = NexoraPrimaryPurple,
                            unfocusedBorderColor = NexoraCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant)
                        ) {
                            Text(if (isArabic) "إلغاء" else "Cancel", color = NexoraTextSecondary)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (newKeyValue.isNotBlank()) {
                                    coroutineScope.launch {
                                        secureKeyRepository.addOrUpdateKey(
                                            label = newKeyLabel.ifBlank { "Gemini Key" },
                                            apiKey = newKeyValue.trim(),
                                            preferredModel = newKeyModel,
                                            setAsActive = true
                                        )
                                        newKeyLabel = ""
                                        newKeyValue = ""
                                        showAddDialog = false
                                        refreshKeys()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraSuccessGreen),
                            enabled = newKeyValue.isNotBlank(),
                            modifier = Modifier.testTag("confirm_add_key_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "حفظ وتفعيل" else "Save & Activate", color = Color.White)
                        }
                    }
                }
            }
        }

        // List of Saved Keys
        if (savedKeys.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexoraCardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = NexoraTextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "لا توجد مفاتيح مسجلة بعد" else "No API keys registered yet",
                        color = NexoraTextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isArabic) "انقر على 'إضافة مفتاح جديد' للبدء" else "Click 'Add New Key' to begin",
                        color = NexoraElectricCyan,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            savedKeys.forEach { keyItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(
                            1.dp,
                            if (keyItem.isActive) NexoraElectricCyan else NexoraCardBorder,
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (keyItem.isActive) NexoraSurface else NexoraCardBackground
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = keyItem.isActive,
                                    onClick = {
                                        coroutineScope.launch {
                                            secureKeyRepository.setActiveKey(keyItem.id)
                                            refreshKeys()
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NexoraElectricCyan,
                                        unselectedColor = NexoraTextSecondary
                                    )
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = keyItem.label,
                                            color = NexoraTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        if (keyItem.isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                                color = NexoraSuccessGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (isArabic) "نشط" else "Active",
                                                    color = NexoraSuccessGreen,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = keyItem.getMaskedKey(),
                                        color = NexoraTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Test Key Button
                                IconButton(
                                    onClick = {
                                        testingKeyId = keyItem.id
                                        coroutineScope.launch {
                                            val result = geminiService.testApiKey(keyItem.apiKey)
                                            result.fold(
                                                onSuccess = { models ->
                                                    testStatusMap = testStatusMap + (keyItem.id to true)
                                                    val count = models.size
                                                    testResultMap = testResultMap + (
                                                        keyItem.id to (
                                                            if (isArabic)
                                                                "✅ صالح! عُثر على $count نماذج مدعومة."
                                                            else
                                                                "✅ Valid! Found $count supported models."
                                                        )
                                                    )
                                                    testingKeyId = null
                                                },
                                                onFailure = { err ->
                                                    testStatusMap = testStatusMap + (keyItem.id to false)
                                                    testResultMap = testResultMap + (
                                                        keyItem.id to "❌ ${err.localizedMessage ?: "Unknown error"}"
                                                    )
                                                    testingKeyId = null
                                                }
                                            )
                                        }
                                    }
                                ) {
                                    if (testingKeyId == keyItem.id) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = NexoraElectricCyan,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Test Key",
                                            tint = NexoraElectricCyan
                                        )
                                    }
                                }

                                // Delete Key Button
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            secureKeyRepository.deleteKey(keyItem.id)
                                            refreshKeys()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Key",
                                        tint = NexoraErrorRed.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Test Result Output Banner
                        testResultMap[keyItem.id]?.let { resultText ->
                            val isSuccess = testStatusMap[keyItem.id] == true
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                color = if (isSuccess) NexoraSuccessGreen.copy(alpha = 0.15f) else NexoraErrorRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = resultText,
                                    color = if (isSuccess) NexoraSuccessGreen else NexoraErrorRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
