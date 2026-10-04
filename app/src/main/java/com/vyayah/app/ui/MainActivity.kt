package com.vyayah.app.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vyayah.app.ui.screens.cards.CardsScreen
import com.vyayah.app.ui.screens.ledger.LedgerScreen
import com.vyayah.app.ui.screens.onboarding.OnboardingScreen
import com.vyayah.app.ui.screens.save.SaveScreen
import com.vyayah.app.ui.screens.settings.SettingsScreen
import com.vyayah.app.ui.screens.today.TodayScreen
import com.vyayah.app.ui.theme.VyayahTheme

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.Dashboard)
    object Ledger : Screen("ledger", "Ledger", Icons.Default.ReceiptLong)
    object Cards : Screen("cards", "Cards", Icons.Default.CreditCard)
    object Save : Screen("save", "Save", Icons.Default.Savings)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Privacy: Prevent OS screenshots in recents overview
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            VyayahTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if onboarding is needed
    var isOnboardingComplete by remember { mutableStateOf(true) }

    val bottomNavItems = listOf(
        Screen.Today,
        Screen.Ledger,
        Screen.Cards,
        Screen.Save,
        Screen.Settings
    )

    if (!isOnboardingComplete) {
        OnboardingScreen(
            onComplete = { isOnboardingComplete = true }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Today.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Today.route) { TodayScreen() }
                composable(Screen.Ledger.route) { LedgerScreen() }
                composable(Screen.Cards.route) { CardsScreen() }
                composable(Screen.Save.route) { SaveScreen() }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}
