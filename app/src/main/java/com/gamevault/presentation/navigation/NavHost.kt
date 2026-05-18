package com.gamevault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gamevault.presentation.game.GameScreen
import com.gamevault.presentation.vault.VaultScreen
import com.gamevault.presentation.vault.VaultViewModel
import com.gamevault.presentation.vault.PatternSetupScreen
import com.gamevault.presentation.vault.PinSetupScreen
import com.gamevault.presentation.vault.FileViewerScreen
import com.gamevault.presentation.vault.AppListScreen
import com.gamevault.presentation.vault.SettingsScreen
import com.gamevault.presentation.vault.BackupScreen

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
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val vaultViewModel: VaultViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = vaultViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
                }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreateBackup = {
                    // TODO: Trigger backup creation
                },
                onRestoreBackup = { path ->
                    // TODO: Restore from path
                },
                onDeleteBackup = { backupId ->
                    // TODO: Delete backup
                },
                onExportBackup = { path ->
                    // TODO: Export to path
                }
            )
        }
    }
}