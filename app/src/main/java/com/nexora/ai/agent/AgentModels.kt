package com.nexora.ai.agent

import java.util.UUID

enum class TaskStatus {
    PENDING,
    PLANNING,
    RUNNING,
    WAITING_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AgentSubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val requiresApproval: Boolean = false,
    val toolName: String? = null,
    val toolParameters: Map<String, String> = emptyMap(),
    val result: String? = null
)

data class ExecutionPlan(
    val id: String = UUID.randomUUID().toString(),
    val userGoal: String,
    val tasks: List<AgentSubTask>,
    val createdAt: Long = System.currentTimeMillis()
)

data class PendingApproval(
    val subTaskId: String,
    val actionName: String,
    val description: String,
    val impact: String,
    val toolParameters: Map<String, String>
)
