package com.gamevault.presentation.navigation

sealed class Screen(val route: String) {
    data object Game : Screen("game")
    data object Vault : Screen("vault")
    data object PatternSetup : Screen("pattern_setup")
    data object PinSetup : Screen("pin_setup")
    data object FileViewer : Screen("file_viewer/{itemId}") {
        fun createRoute(itemId: Long) = "file_viewer/$itemId"
    }
    data object AppList : Screen("app_list")
    data object Settings : Screen("settings")
}