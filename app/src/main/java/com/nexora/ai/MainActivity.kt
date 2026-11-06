package com.nexora.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.ai.agent.AgentEngine
import com.nexora.ai.agent.ToolRegistry
import com.nexora.ai.data.api.GeminiService
import com.nexora.ai.data.repository.SecureKeyRepository
import com.nexora.ai.data.repository.SecureKeyRepositoryImpl
import com.nexora.ai.device.DeviceController
import com.nexora.ai.ui.screens.AgentScreen
import com.nexora.ai.ui.screens.ChatScreen
import com.nexora.ai.ui.screens.DeviceScreen
import com.nexora.ai.ui.screens.HomeScreen
import com.nexora.ai.ui.screens.SettingsScreen
import com.nexora.ai.ui.theme.NexoraDarkBackground
import com.nexora.ai.ui.theme.NexoraElectricCyan
import com.nexora.ai.ui.theme.NexoraPrimaryPurple
import com.nexora.ai.ui.theme.NexoraSurface
import com.nexora.ai.ui.theme.NexoraTextSecondary
import com.nexora.ai.ui.theme.NexoraTheme
import kotlinx.coroutines.launch

data class NavigationTab(
    val titleAr: String,
    val titleEn: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private lateinit var secureKeyRepository: SecureKeyRepository
    private lateinit var geminiService: GeminiService
    private lateinit var deviceController: DeviceController
    private lateinit var socialIntegrations: com.nexora.ai.integrations.SocialMediaIntegrations
    private lateinit var toolRegistry: ToolRegistry
    private lateinit var agentEngine: AgentEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        secureKeyRepository = SecureKeyRepositoryImpl(applicationContext)
        geminiService = GeminiService.create(applicationContext, secureKeyRepository)
        deviceController = DeviceController(applicationContext)
        socialIntegrations = com.nexora.ai.integrations.SocialMediaIntegrations(applicationContext)
        toolRegistry = ToolRegistry(deviceController, socialIntegrations)
        agentEngine = AgentEngine(geminiService, toolRegistry)

        setContent {
            NexoraTheme {
                MainAppScaffold(
                    secureKeyRepository = secureKeyRepository,
                    geminiService = geminiService,
                    deviceController = deviceController,
                    agentEngine = agentEngine
                )
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    secureKeyRepository: SecureKeyRepository,
    geminiService: GeminiService,
    deviceController: DeviceController,
    agentEngine: AgentEngine
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isArabic by remember { mutableStateOf(true) }

    var hasKey by remember { mutableStateOf(false) }
    var activeKeyLabel by remember { mutableStateOf<String?>(null) }
    var activeModel by remember { mutableStateOf("gemini-2.0-flash") }

    fun refreshKeyStatus() {
        coroutineScope.launch {
            hasKey = secureKeyRepository.hasApiKey()
            val activeItem = secureKeyRepository.getActiveKeyItem()
            activeKeyLabel = activeItem?.label
            activeModel = secureKeyRepository.getActiveModel()
        }
    }

    LaunchedEffect(Unit) {
        refreshKeyStatus()
    }

    val tabs = listOf(
        NavigationTab("الرئيسية", "Home", Icons.Default.Home, "tab_home"),
        NavigationTab("المساعد", "Assistant", Icons.Default.Chat, "tab_chat"),
        NavigationTab("الوكيل", "Agent", Icons.Default.SmartToy, "tab_agent"),
        NavigationTab("العتاد", "Device", Icons.Default.PhoneAndroid, "tab_device"),
        NavigationTab("المفاتيح", "Keys", Icons.Default.VpnKey, "tab_keys")
    )

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(NexoraDarkBackground),
            containerColor = NexoraDarkBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = NexoraSurface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = index
                                refreshKeyStatus()
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = if (isArabic) tab.titleAr else tab.titleEn,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (isArabic) tab.titleAr else tab.titleEn,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NexoraElectricCyan,
                                selectedTextColor = NexoraElectricCyan,
                                unselectedIconColor = NexoraTextSecondary,
                                unselectedTextColor = NexoraTextSecondary,
                                indicatorColor = NexoraPrimaryPurple.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        isArabic = isArabic,
                        hasApiKey = hasKey,
                        activeKeyLabel = activeKeyLabel,
                        activeModel = activeModel,
                        onNavigateToTab = {
                            selectedTab = it
                            refreshKeyStatus()
                        }
                    )
                    1 -> ChatScreen(
                        geminiService = geminiService,
                        activeModel = activeModel,
                        isArabic = isArabic
                    )
                    2 -> AgentScreen(
                        agentEngine = agentEngine,
                        isArabic = isArabic
                    )
                    3 -> DeviceScreen(
                        deviceController = deviceController,
                        isArabic = isArabic
                    )
                    4 -> SettingsScreen(
                        secureKeyRepository = secureKeyRepository,
                        geminiService = geminiService,
                        isArabic = isArabic,
                        onLanguageToggle = { isArabic = !isArabic }
                    )
                }
            }
        }
    }
}
