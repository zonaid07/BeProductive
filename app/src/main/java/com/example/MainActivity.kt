package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.configureAppCheck
import com.example.prayer.PrayerAlarmScheduler
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.EnglishScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KnowledgeScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.PrayerScreen
import com.example.ui.screens.ReadingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksStatsScreen
import com.example.ui.screens.VocabularyScreen
import com.example.ui.theme.DailyPathTheme
import com.example.viewmodel.DailyPathViewModel

private data class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val primaryTabs = listOf(
    NavTab("home", "Home", Icons.Default.Home),
    NavTab("prayer", "Prayer", Icons.Default.AccessTime),
    NavTab("english", "English", Icons.Default.Translate),
    NavTab("vocabulary", "Vocab", Icons.Default.School),
    NavTab("reading", "Reading", Icons.AutoMirrored.Filled.MenuBook),
    NavTab("knowledge", "History", Icons.Default.HistoryEdu)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        PrayerAlarmScheduler.ensureNotificationChannel(this)
        setContent {
            val viewModel: DailyPathViewModel = viewModel()
            val profile by viewModel.profile.collectAsStateWithLifecycle()
            val darkTheme = when (profile.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            DailyPathTheme(darkTheme = darkTheme) {
                DailyPathApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun DailyPathApp(viewModel: DailyPathViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "home"
    val showBottomBar = primaryTabs.any { it.route == currentRoute }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    primaryTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = {
                                Text(
                                    text = tab.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = { fadeIn(animationSpec = tween(120)) },
                exitTransition = { fadeOut(animationSpec = tween(100)) },
                popEnterTransition = { fadeIn(animationSpec = tween(120)) },
                popExitTransition = { fadeOut(animationSpec = tween(100)) }
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { navigateToTab(it) },
                        onOpenNews = { navController.navigate("news") },
                        onOpenTasks = { navController.navigate("tasks") },
                        onOpenAiAssistant = { navController.navigate("ai_assistant") },
                        onOpenSettings = { navController.navigate("settings") }
                    )
                }
                composable("prayer") { PrayerScreen(viewModel = viewModel) }
                composable("english") {
                    EnglishScreen(
                        viewModel = viewModel,
                        onOpenAiAssistant = { navController.navigate("ai_assistant") }
                    )
                }
                composable("vocabulary") { VocabularyScreen(viewModel = viewModel) }
                composable("reading") { ReadingScreen(viewModel = viewModel) }
                composable("knowledge") { KnowledgeScreen(viewModel = viewModel) }
                composable("news") {
                    NewsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("tasks") {
                    TasksStatsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("ai_assistant") {
                    AiAssistantScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable("settings") {
                    SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
