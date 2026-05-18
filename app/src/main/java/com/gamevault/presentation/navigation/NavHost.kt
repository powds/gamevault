package com.gamevault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamevault.presentation.game.GameScreen
import com.gamevault.presentation.vault.VaultScreen
import com.gamevault.presentation.vault.PatternSetupScreen
import com.gamevault.presentation.vault.PinSetupScreen
import com.gamevault.presentation.vault.FileViewerScreen
import com.gamevault.presentation.vault.AppListScreen
import com.gamevault.presentation.vault.SettingsScreen

@Composable
fun GameVaultNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Game.route
    ) {
        composable(Screen.Game.route) {
            GameScreen(
                onUnlockVault = {
                    navController.navigate(Screen.Vault.route)
                },
                onSetupPattern = {
                    navController.navigate(Screen.PatternSetup.route)
                },
                onSetupPin = {
                    navController.navigate(Screen.PinSetup.route)
                }
            )
        }

        composable(Screen.Vault.route) {
            VaultScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenFile = { itemId ->
                    navController.navigate(Screen.FileViewer.createRoute(itemId))
                },
                onOpenAppList = {
                    navController.navigate(Screen.AppList.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.PatternSetup.route) {
            PatternSetupScreen(
                onPatternSet = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PinSetup.route) {
            PinSetupScreen(
                onPinSet = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.FileViewer.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            FileViewerScreen(
                itemId = itemId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AppList.route) {
            AppListScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}