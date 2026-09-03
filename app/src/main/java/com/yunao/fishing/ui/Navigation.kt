package com.yunao.fishing.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yunao.fishing.data.FirebaseRepository
import com.yunao.fishing.ui.auth.LoginScreen
import com.yunao.fishing.ui.auth.RegisterScreen
import com.yunao.fishing.ui.screens.CommunityScreen
import com.yunao.fishing.ui.screens.GearScreen
import com.yunao.fishing.ui.screens.HomeScreen
import com.yunao.fishing.ui.screens.LogScreen
import com.yunao.fishing.ui.screens.ProfileScreen
import com.yunao.fishing.ui.screens.SpotsScreen
import kotlinx.coroutines.flow.collectLatest

private data class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val destinations = listOf(
    Dest("home", "出钓大脑", Icons.Filled.Home),
    Dest("log", "复盘日志", Icons.Filled.MenuBook),
    Dest("spots", "钓点", Icons.Filled.Place),
    Dest("community", "约钓", Icons.Filled.Groups),
    Dest("gear", "装备", Icons.Filled.Settings),
    Dest("profile", "我的", Icons.Filled.Person),
)
@Composable
fun YuNaoApp() {
    val rootNavController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // 根据登录状态实时决定起始页面；登出后自动回到登录页
    LaunchedEffect(Unit) {
        FirebaseRepository.authStateFlow().collectLatest { user ->
            if (startDestination == null) {
                startDestination = if (user != null) "main" else "login"
            } else if (user == null) {
                rootNavController.navigate("login") {
                    popUpTo(0)
                }
            }
        }
    }

    val start = startDestination ?: return
    NavHost(navController = rootNavController, startDestination = start) {
        composable("login") {
            LoginScreen(
                onLoggedIn = {
                    rootNavController.navigate("main") { popUpTo(0) }
                },
                onGoRegister = { rootNavController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegistered = {
                    rootNavController.navigate("main") { popUpTo(0) }
                },
                onGoLogin = { rootNavController.popBackStack() }
            )
        }
        composable("main") {
            MainScreen(onSignOut = { FirebaseRepository.signOut() })
        }
    }
}
@Composable
private fun MainScreen(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                destinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("log") { LogScreen() }
            composable("spots") { SpotsScreen() }
            composable("community") { CommunityScreen() }
            composable("gear") { GearScreen() }
            composable("profile") { ProfileScreen(onSignOut = onSignOut) }
        }
    }
}
