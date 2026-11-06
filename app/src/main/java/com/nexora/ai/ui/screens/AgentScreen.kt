package com.nexora.ai.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.nexora.ai.agent.AgentEngine
import com.nexora.ai.agent.AgentSubTask
import com.nexora.ai.agent.TaskStatus
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
import com.nexora.ai.ui.theme.NexoraWarningAmber

@Composable
fun AgentScreen(
    agentEngine: AgentEngine,
    isArabic: Boolean
) {
    val currentPlan by agentEngine.currentPlan.collectAsState()
    val pendingApproval by agentEngine.pendingApproval.collectAsState()
    val isExecuting by agentEngine.isExecuting.collectAsState()
    val logs by agentEngine.executionLogs.collectAsState()

    var goalInput by remember {
        mutableStateOf(if (isArabic) "افحص حالة البطارية وأرسل نبضة اهتزاز تنبيهية" else "Check battery status and send haptic pulse")
    }

    val sampleGoals = if (isArabic) {
        listOf(
            "نزل بوست على واتس: أهلاً بكم من نظام NEXORA AI V2 ⚡",
            "نزل بوست على فيسبوك: تم تفعيل نظام الذكاء الاصطناعي بنجاح",
            "نزل بوست على انستا: تجربة النشر الذكي المستقل",
            "افحص حالة البطارية وأرسل نبضة اهتزاز",
            "قم بتشغيل فلاش الكاميرا"
        )
    } else {
        listOf(
            "Post to WhatsApp: Welcome to NEXORA AI V2 ⚡",
            "Post to Facebook: Autonomous AI OS online",
            "Post to Instagram: Next-gen AI operating system",
            "Check battery status and send haptic pulse",
            "Toggle camera flashlight on"
        )
    }

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
                    text = if (isArabic) "محرك الوكيل المستقل" else "Autonomous Agent Engine",
                    color = NexoraElectricCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isArabic) "تخطيط ذكي • أدوات عتادية • موافقة بشرية" else "Smart Planner • Native Tools • Human Gate",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp
                )
            }

            // Global Emergency Stop Button
            Button(
                onClick = { agentEngine.emergencyStop() },
                colors = ButtonDefaults.buttonColors(containerColor = NexoraErrorRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("emergency_stop_button")
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "إيقاف طارئ" else "EMERGENCY STOP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goal Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isArabic) "حدد هدف أو مهمة الوكيل:" else "Specify Agent Goal / Mission:",
                    color = NexoraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agent_goal_input"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NexoraTextPrimary,
                        unfocusedTextColor = NexoraTextPrimary,
                        focusedBorderColor = NexoraElectricCyan,
                        unfocusedBorderColor = NexoraCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (goalInput.isNotBlank()) {
                            agentEngine.processGoal(goalInput.trim())
                        }
                    },
                    enabled = goalInput.isNotBlank() && !isExecuting,
                    colors = ButtonDefaults.buttonColors(containerColor = NexoraPrimaryPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_agent_button")
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "جاري التخطيط والتنفيذ..." else "Planning & Executing...", color = Color.White)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "تفعيل الوكيل وتوليد الخطة" else "Launch Agent Planner", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Goals Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleGoals) { preset ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, NexoraCardBorder, RoundedCornerShape(10.dp))
                        .clickable { goalInput = preset },
                    color = NexoraSurface
                ) {
                    Text(
                        text = preset,
                        color = NexoraTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Human Approval Gate Modal Card
        pendingApproval?.let { approval ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(2.dp, NexoraWarningAmber, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NexoraSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NexoraWarningAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NexoraWarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "موافقة بشرية مطلوبة (Human Approval)" else "Human Approval Required",
                                color = NexoraWarningAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = approval.actionName,
                                color = NexoraTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = approval.description,
                        color = NexoraTextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    approval.toolParameters["content"]?.let { content ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NexoraElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            color = NexoraDarkBackground
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isArabic) "المحتوى الدقيق للمنشور:" else "Exact Post Content:",
                                    color = NexoraElectricCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"$content\"",
                                    color = NexoraTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = NexoraSurfaceVariant
                    ) {
                        Text(
                            text = "الأثر: ${approval.impact}",
                            color = NexoraTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { agentEngine.rejectAction() },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraSurfaceVariant),
                            modifier = Modifier.testTag("reject_approval_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = NexoraErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "رفض" else "Reject", color = NexoraErrorRed)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { agentEngine.approveAction() },
                            colors = ButtonDefaults.buttonColors(containerColor = NexoraSuccessGreen),
                            modifier = Modifier.testTag("grant_approval_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "موافقة وتمرير" else "Approve & Execute", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Execution Plan Display
        currentPlan?.let { plan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexoraElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = NexoraCardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "خطة التنفيذ الحالية:" else "Current Execution Plan:",
                        color = NexoraElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = plan.userGoal,
                        color = NexoraTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    plan.tasks.forEachIndexed { index, task ->
                        TaskItemRow(index = index + 1, task = task, isArabic = isArabic)
                        if (index < plan.tasks.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Real-time Execution Logs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexoraCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NexoraSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isArabic) "سجل عمليات الوكيل (Execution Telemetry)" else "Execution Telemetry Logs",
                    color = NexoraTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (logs.isEmpty()) {
                    Text(
                        text = if (isArabic) "بانتظار إطلاق مهمة..." else "Awaiting mission start...",
                        color = NexoraTextSecondary.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                } else {
                    logs.take(6).forEach { log ->
                        Text(
                            text = "> $log",
                            color = NexoraTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TaskItemRow(index: Int, task: AgentSubTask, isArabic: Boolean) {
    val (statusColor, statusLabel) = when (task.status) {
        TaskStatus.PENDING -> NexoraTextSecondary to (if (isArabic) "قيد الانتظار" else "Pending")
        TaskStatus.PLANNING -> NexoraElectricCyan to (if (isArabic) "تخطيط" else "Planning")
        TaskStatus.RUNNING -> NexoraElectricCyan to (if (isArabic) "قيد التشغيل" else "Running")
        TaskStatus.WAITING_APPROVAL -> NexoraWarningAmber to (if (isArabic) "بانتظار الموافقة" else "Waiting Approval")
        TaskStatus.COMPLETED -> NexoraSuccessGreen to (if (isArabic) "مكتمل" else "Completed")
        TaskStatus.FAILED -> NexoraErrorRed to (if (isArabic) "فشل" else "Failed")
        TaskStatus.CANCELLED -> NexoraTextSecondary to (if (isArabic) "ملغي" else "Cancelled")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = NexoraSurfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index. ${task.title}",
                    color = NexoraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = task.description, color = NexoraTextSecondary, fontSize = 11.sp)

            task.result?.let { res ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp)),
                    color = NexoraDarkBackground
                ) {
                    Text(
                        text = res,
                        color = NexoraElectricCyan,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}
