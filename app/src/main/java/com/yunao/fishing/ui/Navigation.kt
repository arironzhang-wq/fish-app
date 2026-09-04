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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yunao.fishing.ui.screens.CommunityScreen
import com.yunao.fishing.ui.screens.GearScreen
import com.yunao.fishing.ui.screens.HomeScreen
import com.yunao.fishing.ui.screens.LogScreen
import com.yunao.fishing.ui.screens.ProfileScreen
import com.yunao.fishing.ui.screens.SpotsScreen

private data class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val destinations = listOf(
    Dest("home", "出钓大脑", Icons.Filled.Home),
    Dest("log", "复盘日志", Icons.Filled.MenuBook),
    Dest("spots", "钓点", Icons.Filled.Place),
    Dest("community", "约钓", Icons.Filled.Groups),
    Dest("gear", "装备", Icons.Filled.Settings),
    Dest("profile", "我的", Icons.Filled.Person),
)

/**
 * 离线本机版：不需要注册/登录，启动直接进入主界面，
 * 所有数据保存在设备本地。
 */
@Composable
fun YuNaoApp() {
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
            composable("profile") { ProfileScreen() }
        }
    }
}
