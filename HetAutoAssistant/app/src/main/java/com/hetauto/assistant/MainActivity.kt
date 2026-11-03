package com.hetauto.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hetauto.assistant.ui.screens.DashboardScreen
import com.hetauto.assistant.ui.screens.DemoSandboxScreen
import com.hetauto.assistant.ui.screens.GalleryScreen
import com.hetauto.assistant.ui.screens.HistoryScreen
import com.hetauto.assistant.ui.screens.SettingsScreen
import com.hetauto.assistant.ui.theme.AccentCyan
import com.hetauto.assistant.ui.theme.CardBackground
import com.hetauto.assistant.ui.theme.DarkBackground
import com.hetauto.assistant.ui.theme.HetAutoAssistantTheme
import com.hetauto.assistant.ui.theme.TextMuted
import com.hetauto.assistant.ui.viewmodel.MainViewModel
import com.hetauto.assistant.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application as HetApplication)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            HetAutoAssistantTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

sealed class NavTab(val title: String, val icon: ImageVector) {
    object Dashboard : NavTab("Dashboard", Icons.Default.Dashboard)
    object Sandbox : NavTab("Sandbox", Icons.Default.Science)
    object History : NavTab("History", Icons.Default.History)
    object Gallery : NavTab("Captures", Icons.Default.PhotoLibrary)
    object Settings : NavTab("Settings", Icons.Default.Settings)
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        NavTab.Dashboard,
        NavTab.Sandbox,
        NavTab.History,
        NavTab.Gallery,
        NavTab.Settings
    )

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentCyan,
                            selectedTextColor = AccentCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CardBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> DemoSandboxScreen()
                2 -> HistoryScreen(viewModel = viewModel)
                3 -> GalleryScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
