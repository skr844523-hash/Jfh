package com.nexora.ai.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserTaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val projectId: String? = null,
    val isCompleted: Boolean = false,
    val priority: String = "Normal",
    val createdAt: Long = System.currentTimeMillis()
)

data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val category: String = "Preference",
    val createdAt: Long = System.currentTimeMillis()
)

class ProjectTaskRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexora_projects_prefs", Context.MODE_PRIVATE)

    private val _projects = MutableStateFlow<List<ProjectItem>>(emptyList())
    val projects: StateFlow<List<ProjectItem>> = _projects.asStateFlow()

    private val _tasks = MutableStateFlow<List<UserTaskItem>>(emptyList())
    val tasks: StateFlow<List<UserTaskItem>> = _tasks.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val projectsJson = prefs.getString("projects", null)
        if (projectsJson != null) {
            val list = mutableListOf<ProjectItem>()
            val arr = JSONArray(projectsJson)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    ProjectItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", "Project"),
                        description = o.optString("description", ""),
                        instructions = o.optString("instructions", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            _projects.value = list
        } else {
            // Seed initial sample project
            _projects.value = listOf(
                ProjectItem(
                    name = "تطوير NEXORA OS",
                    description = "بناء نظام تشغيل ذكي مستقل لإدارة المهام والأجهزة",
                    instructions = "التركيز على حماية الخصوصية ومصادقة المستخدم"
                )
            )
        }

        val tasksJson = prefs.getString("tasks", null)
        if (tasksJson != null) {
            val list = mutableListOf<UserTaskItem>()
            val arr = JSONArray(tasksJson)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    UserTaskItem(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "Task"),
                        projectId = if (o.has("projectId") && !o.isNull("projectId")) o.getString("projectId") else null,
                        isCompleted = o.optBoolean("isCompleted", false),
                        priority = o.optString("priority", "Normal"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            _tasks.value = list
        } else {
            _tasks.value = listOf(
                UserTaskItem(title = "فحص اتصال Gemini API", isCompleted = true),
                UserTaskItem(title = "تكوين مفاتيح الأمان في Android Keystore", isCompleted = true),
                UserTaskItem(title = "إعداد محرك المهام المستقلة", isCompleted = false)
            )
        }
    }

    suspend fun addProject(name: String, description: String, instructions: String = "") = withContext(Dispatchers.IO) {
        val newProject = ProjectItem(name = name, description = description, instructions = instructions)
        val updated = listOf(newProject) + _projects.value
        _projects.value = updated
        saveProjects(updated)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        val updated = _projects.value.filter { it.id != id }
        _projects.value = updated
        saveProjects(updated)
    }

    suspend fun addTask(title: String, priority: String = "Normal", projectId: String? = null) = withContext(Dispatchers.IO) {
        val newTask = UserTaskItem(title = title, priority = priority, projectId = projectId)
        val updated = listOf(newTask) + _tasks.value
        _tasks.value = updated
        saveTasks(updated)
    }

    suspend fun toggleTask(id: String) = withContext(Dispatchers.IO) {
        val updated = _tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _tasks.value = updated
        saveTasks(updated)
    }

    suspend fun deleteTask(id: String) = withContext(Dispatchers.IO) {
        val updated = _tasks.value.filter { it.id != id }
        _tasks.value = updated
        saveTasks(updated)
    }

    private fun saveProjects(list: List<ProjectItem>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("description", it.description)
                put("instructions", it.instructions)
                put("createdAt", it.createdAt)
            })
        }
        prefs.edit().putString("projects", arr.toString()).apply()
    }

    private fun saveTasks(list: List<UserTaskItem>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("projectId", it.projectId)
                put("isCompleted", it.isCompleted)
                put("priority", it.priority)
                put("createdAt", it.createdAt)
            })
        }
        prefs.edit().putString("tasks", arr.toString()).apply()
    }
}
