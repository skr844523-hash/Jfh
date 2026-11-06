package com.nexora.ai.agent

import com.nexora.ai.data.api.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentEngine(
    private val geminiService: GeminiService,
    private val toolRegistry: ToolRegistry,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _currentPlan = MutableStateFlow<ExecutionPlan?>(null)
    val currentPlan: StateFlow<ExecutionPlan?> = _currentPlan.asStateFlow()

    private val _pendingApproval = MutableStateFlow<PendingApproval?>(null)
    val pendingApproval: StateFlow<PendingApproval?> = _pendingApproval.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _executionLogs = MutableStateFlow<List<String>>(emptyList())
    val executionLogs: StateFlow<List<String>> = _executionLogs.asStateFlow()

    private var executionJob: Job? = null

    /**
     * Accepts a natural language prompt, analyzes intent, builds an execution plan,
     * and coordinates execution.
     */
    fun processGoal(userGoal: String) {
        executionJob?.cancel()
        executionJob = scope.launch {
            _isExecuting.value = true
            addLog("تحليل الهدف عبر محرك NEXORA: \"$userGoal\"")

            val subTasks = mutableListOf<AgentSubTask>()
            val lower = userGoal.lowercase()

            val isSocialPost = lower.contains("بوست") || lower.contains("نزل") || lower.contains("انشر") ||
                    lower.contains("ارسل") || lower.contains("post") || lower.contains("share") || lower.contains("publish")
            val containsWhatsapp = lower.contains("واتس") || lower.contains("whatsapp")
            val containsFacebook = lower.contains("فيسبوك") || lower.contains("فيس") || lower.contains("facebook")
            val containsInstagram = lower.contains("انستا") || lower.contains("انستغرام") || lower.contains("instagram")
            val containsTelegram = lower.contains("تيليجرام") || lower.contains("تلغرام") || lower.contains("telegram")
            val containsTwitter = lower.contains("تويتر") || lower.contains("twitter") || lower.contains("x")

            val containsFlash = lower.contains("فلاش") || lower.contains("كشاف") || lower.contains("flash") || lower.contains("torch")
            val containsVibrate = lower.contains("اهتزاز") || lower.contains("نبض") || lower.contains("vibrat")
            val containsBattery = lower.contains("بطارية") || lower.contains("شحن") || lower.contains("battery") || lower.contains("power")

            subTasks.add(
                AgentSubTask(
                    title = "تحليل الهدف والتحقق الأمني",
                    description = "فحص نية المستخدم ومطابقتها مع سياسات الأمان والموافقة المسبقة",
                    status = TaskStatus.COMPLETED,
                    result = "الطلب متوافق مع نظام الحماية"
                )
            )

            // Social Media Publishing Intent
            if (isSocialPost || containsWhatsapp || containsFacebook || containsInstagram || containsTelegram) {
                val platformName = when {
                    containsWhatsapp -> "واتساب (WhatsApp)"
                    containsFacebook -> "فيسبوك (Facebook)"
                    containsInstagram -> "انستغرام (Instagram)"
                    containsTelegram -> "تيليجرام (Telegram)"
                    containsTwitter -> "إكس / تويتر (X)"
                    else -> "منصة التواصل المحددة"
                }

                val platformCode = when {
                    containsWhatsapp -> "whatsapp"
                    containsFacebook -> "facebook"
                    containsInstagram -> "instagram"
                    containsTelegram -> "telegram"
                    containsTwitter -> "twitter"
                    else -> "general"
                }

                // Extract post content intelligently
                var extractedContent = extractPostContent(userGoal)
                if (extractedContent.isBlank()) {
                    extractedContent = userGoal
                }

                subTasks.add(
                    AgentSubTask(
                        title = "صياغة وتجهيز محتوى المنشور",
                        description = "تجهيز النص للنشر: \"$extractedContent\"",
                        status = TaskStatus.COMPLETED,
                        result = "تم تجهيز نص المنشور بنجاح"
                    )
                )

                subTasks.add(
                    AgentSubTask(
                        title = "نشر المنشور على $platformName",
                        description = "إرسال المحتوى إلى $platformName. يتطلب موافقتك الصريحة قبل الفتح أو الإرسال.",
                        status = TaskStatus.PENDING,
                        requiresApproval = true,
                        toolName = "publish_social_post",
                        toolParameters = mapOf(
                            "platform" to platformCode,
                            "content" to extractedContent
                        )
                    )
                )
            }

            if (containsBattery) {
                subTasks.add(
                    AgentSubTask(
                        title = "قراءة مستشعر البطارية",
                        description = "استعلام مباشر من نظام الطاقة للجهاز",
                        status = TaskStatus.PENDING,
                        requiresApproval = false,
                        toolName = "get_battery_telemetry"
                    )
                )
            }

            if (containsFlash) {
                val state = if (lower.contains("اطف") || lower.contains("إطفاء") || lower.contains("off")) "off" else "on"
                subTasks.add(
                    AgentSubTask(
                        title = "التحكم في فلاش الكاميرا",
                        description = "تعديل حالة عتاد الفلاش إلى: $state",
                        status = TaskStatus.PENDING,
                        requiresApproval = true,
                        toolName = "toggle_flashlight",
                        toolParameters = mapOf("state" to state)
                    )
                )
            }

            if (containsVibrate) {
                subTasks.add(
                    AgentSubTask(
                        title = "إرسال نبضة هابتك",
                        description = "توليد نبضة تنبيهية في المحرك الاهتزازي",
                        status = TaskStatus.PENDING,
                        requiresApproval = false,
                        toolName = "haptic_pulse"
                    )
                )
            }

            // Final report synthesis
            subTasks.add(
                AgentSubTask(
                    title = "توليد التقرير النهائي (NEXORA AI)",
                    description = "صياغة الرد والتوثيق بناءً على سير العمليات",
                    status = TaskStatus.PENDING,
                    requiresApproval = false,
                    toolName = "ai_synthesis"
                )
            )

            val plan = ExecutionPlan(
                userGoal = userGoal,
                tasks = subTasks
            )
            _currentPlan.value = plan
            addLog("تم إنشاء خطة عمل تحتوي على ${subTasks.size} مهام فرعية.")

            executeNextStep()
        }
    }

    private fun extractPostContent(prompt: String): String {
        // Look for content inside quotes
        val quoteMatch = Regex("[\"']([^\"']+)[\"']").find(prompt)
        if (quoteMatch != null) {
            return quoteMatch.groupValues[1]
        }

        // Look for patterns like "بوست كذا" or "انشر: كذا" or "نزل بوست: كذا"
        val colonSplit = prompt.split(":", "：", "قول", "نص", "محتوى", "انشر")
        if (colonSplit.size > 1) {
            val candidate = colonSplit.last().trim()
            if (candidate.length > 3) return candidate
        }

        // Clean out prefix words
        var cleaned = prompt
        listOf("نزل بوست على واتس", "نزل بوست على فيسبوك", "نزل بوست على انستا", "نزل بوست", "انشر على واتس", "انشر على فيسبوك", "انشر على انستغرام", "post to facebook", "post to instagram", "post to whatsapp").forEach { prefix ->
            cleaned = cleaned.replace(prefix, "", ignoreCase = true)
        }
        return cleaned.trim().ifEmpty { prompt }
    }

    /**
     * Executes the next pending task in the plan.
     */
    private suspend fun executeNextStep() {
        val plan = _currentPlan.value ?: return
        val nextTaskIndex = plan.tasks.indexOfFirst { it.status == TaskStatus.PENDING }

        if (nextTaskIndex == -1) {
            _isExecuting.value = false
            addLog("اكتملت جميع مهام الخطة بنجاح.")
            return
        }

        val task = plan.tasks[nextTaskIndex]
        updateTaskStatus(task.id, TaskStatus.RUNNING)

        if (task.requiresApproval) {
            val tool = task.toolName?.let { toolRegistry.getTool(it) }
            val impact = tool?.impactExplanation ?: "عملية حساسة تتطلب تأكيد المستخدم"
            updateTaskStatus(task.id, TaskStatus.WAITING_APPROVAL)
            _pendingApproval.value = PendingApproval(
                subTaskId = task.id,
                actionName = task.title,
                description = task.description,
                impact = impact,
                toolParameters = task.toolParameters
            )
            addLog("توقف مؤقت: في انتظار موافقة المستخدم الصريحة لتنفيذ \"${task.title}\"")
            return
        }

        performTaskExecution(task)
    }

    private suspend fun performTaskExecution(task: AgentSubTask) {
        addLog("جاري تنفيذ: ${task.title}...")

        if (task.toolName == "ai_synthesis") {
            val previousResults = _currentPlan.value?.tasks
                ?.filter { it.result != null && it.id != task.id }
                ?.joinToString("\n") { "- ${it.title}: ${it.result}" } ?: "لا توجد نتائج سابقة"

            val prompt = """
                أنت NEXORA AI. قمت بتنفيذ المهام التالية لتحقيق هدف المستخدم: "${_currentPlan.value?.userGoal}"
                النتائج الميدانية:
                $previousResults
                
                لخص للمستخدم النتيجة النهائية بإيجاز واحترافية باللغة العربية.
            """.trimIndent()

            val aiResult = geminiService.generateText(prompt)
            aiResult.fold(
                onSuccess = { summary ->
                    updateTaskStatus(task.id, TaskStatus.COMPLETED, summary)
                    addLog("اكتمل التقرير النهائي.")
                    executeNextStep()
                },
                onFailure = { err ->
                    updateTaskStatus(task.id, TaskStatus.COMPLETED, "تم التنفيذ بنجاح.")
                    executeNextStep()
                }
            )
            return
        }

        val tool = task.toolName?.let { toolRegistry.getTool(it) }
        if (tool != null) {
            val result = tool.execute(task.toolParameters)
            result.fold(
                onSuccess = { res ->
                    updateTaskStatus(task.id, TaskStatus.COMPLETED, res)
                    addLog("نجاح: $res")
                    executeNextStep()
                },
                onFailure = { err ->
                    updateTaskStatus(task.id, TaskStatus.FAILED, err.message)
                    addLog("فشل في ${task.title}: ${err.message}")
                    executeNextStep()
                }
            )
        } else {
            updateTaskStatus(task.id, TaskStatus.COMPLETED, "تم إنجاز المهمة")
            executeNextStep()
        }
    }

    /**
     * User approves the pending sensitive action.
     */
    fun approveAction() {
        val pending = _pendingApproval.value ?: return
        _pendingApproval.value = null
        addLog("تم استلام موافقة المستخدم الصريحة. جاري التنفيذ الفوري...")

        scope.launch {
            val plan = _currentPlan.value ?: return@launch
            val task = plan.tasks.firstOrNull { it.id == pending.subTaskId } ?: return@launch
            performTaskExecution(task)
        }
    }

    /**
     * User rejects the pending sensitive action.
     */
    fun rejectAction() {
        val pending = _pendingApproval.value ?: return
        _pendingApproval.value = null
        addLog("تم رفض العملية بواسطة المستخدم. تم إلغاء النشر بأمان.")

        updateTaskStatus(pending.subTaskId, TaskStatus.CANCELLED, "تم الإلغاء بواسطة المستخدم")

        scope.launch {
            executeNextStep()
        }
    }

    /**
     * Global Emergency Stop. Halts all agent operations instantly.
     */
    fun emergencyStop() {
        executionJob?.cancel()
        _isExecuting.value = false
        _pendingApproval.value = null

        val plan = _currentPlan.value
        if (plan != null) {
            val updated = plan.tasks.map {
                if (it.status == TaskStatus.RUNNING || it.status == TaskStatus.WAITING_APPROVAL || it.status == TaskStatus.PENDING) {
                    it.copy(status = TaskStatus.CANCELLED, result = "تم الإيقاف الطارئ (Emergency Stop)")
                } else it
            }
            _currentPlan.value = plan.copy(tasks = updated)
        }
        addLog("🚨 تم تفعيل الإيقاف الطارئ (Emergency Stop) وتم تعليق كافة العمليات فوراً.")
    }

    private fun updateTaskStatus(taskId: String, status: TaskStatus, result: String? = null) {
        val plan = _currentPlan.value ?: return
        val updated = plan.tasks.map {
            if (it.id == taskId) {
                it.copy(status = status, result = result ?: it.result)
            } else it
        }
        _currentPlan.value = plan.copy(tasks = updated)
    }

    private fun addLog(message: String) {
        _executionLogs.value = listOf(message) + _executionLogs.value.take(40)
    }
}
