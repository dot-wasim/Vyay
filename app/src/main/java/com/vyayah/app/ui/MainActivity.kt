package com.vyayah.app.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.vyayah.app.ui.theme.ForestGreen
import com.vyayah.app.ui.theme.VyayahTheme

sealed class Screen(val route: String, val title: String) {
    object Today : Screen("today", "TODAY")
    object Ledger : Screen("ledger", "LEDGER")
    object Cards : Screen("cards", "CARDS")
    object Trips : Screen("trips", "TRIPS")
    object Save : Screen("save", "SAVE")
    object Ask : Screen("ask", "ASK")
    object Settings : Screen("settings", "SETTINGS")
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
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

    var isOnboardingComplete by remember { mutableStateOf(true) }

    val bottomNavTabs = listOf(
        Screen.Today,
        Screen.Ledger,
        Screen.Cards,
        Screen.Trips,
        Screen.Save,
        Screen.Ask
    )

    if (!isOnboardingComplete) {
        OnboardingScreen(
            onComplete = { isOnboardingComplete = true }
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                val inkColor = MaterialTheme.colorScheme.onSurface
                val borderColor = inkColor.copy(alpha = 0.2f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Divider(color = borderColor, thickness = 1.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomNavTabs.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.8.sp,
                                        color = if (isSelected) inkColor else inkColor.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Active tab indicator bar
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(2.5.dp)
                                        .background(if (isSelected) ForestGreen else Color.Transparent)
                                )
                            }
                        }
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
                composable(Screen.Today.route) {
                    TodayScreen(
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Ledger.route) { LedgerScreen() }
                composable(Screen.Cards.route) { CardsScreen() }
                composable(Screen.Trips.route) { PlaceholderScreen("Trips", "Tag expenses to journeys and vacations (v1.1 feature).") }
                composable(Screen.Save.route) { SaveScreen() }
                composable(Screen.Ask.route) { PlaceholderScreen("Ask", "Natural language queries over your local ledger using offline AI (v1.1 feature).") }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
