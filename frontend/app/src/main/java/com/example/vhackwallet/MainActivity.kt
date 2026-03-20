package com.example.vhackwallet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vhackwallet.navigation.Screen
import com.example.vhackwallet.ui.activity.ActivityScreen
import com.example.vhackwallet.ui.components.LoadingScreen
import com.example.vhackwallet.ui.home.HomeScreen
import com.example.vhackwallet.ui.scanner.ScannerScreen
import com.example.vhackwallet.ui.theme.VhackWalletTheme
import com.example.vhackwallet.ui.transfer.TransferAmountScreen
import com.example.vhackwallet.ui.transfer.TransferConfirmScreen
import com.example.vhackwallet.ui.transfer.TransferSearchScreen
import com.example.vhackwallet.ui.transfer.TransferSuccessScreen

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            // Theme state managed at the top level
            var themeMode by remember { mutableStateOf(0) }
            
            VhackWalletTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                            onNavigateToActivity = { navController.navigate(Screen.Activity.route) },
                            onNavigateToTransfer = { navController.navigate(Screen.TransferSearch.route) },
                            onThemeToggle = {
                                // Cycles: 0 (Light) -> 1 (Dark) -> 2 (Blue-Purple)
                                themeMode = (themeMode + 1) % 3
                            }
                        )
                    }
                    composable(
                        route = Screen.Loading.route,
                        arguments = listOf(navArgument("nextRoute") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val nextRoute = backStackEntry.arguments?.getString("nextRoute")?.replace("|", "/") ?: ""
                        LoadingScreen(
                            nextRoute = nextRoute,
                            onFinished = { route ->
                                navController.navigate(route) {
                                    popUpTo(Screen.Loading.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Scanner.route) {
                        ScannerScreen(
                            onBack = { navController.popBackStack() },
                            onScanSuccess = { result ->
                                val simulatedName = "Wong Kah Lok"
                                val nextRoute = Screen.TransferAmount.createRoute(simulatedName, result)
                                navController.navigate(Screen.Loading.createRoute(nextRoute))
                            }
                        )
                    }
                    composable(Screen.Activity.route) {
                        ActivityScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.TransferSearch.route) {
                        TransferSearchScreen(
                            onBack = { navController.popBackStack() },
                            onContactSelected = { name, id, isNewRecipient ->
                                navController.navigate(Screen.TransferAmount.createRoute(name, id, isNewRecipient))
                            }
                        )
                    }
                    composable(
                        route = Screen.TransferAmount.route,
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType },
                            navArgument("isNewRecipient") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val isNewRecipient = backStackEntry.arguments?.getBoolean("isNewRecipient") ?: false
                        TransferAmountScreen(
                            name = name,
                            id = id,
                            isNewRecipient = isNewRecipient,
                            onBack = { navController.popBackStack() },
                            onNext = { amount, details ->
                                // Navigating to TransferConfirm as part of the new flow (Review Page)
                                navController.navigate(Screen.TransferConfirm.createRoute(name, id, amount, details, isNewRecipient))
                            }
                        )
                    }
                    composable(
                        route = Screen.TransferConfirm.route,
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType },
                            navArgument("amount") { type = NavType.StringType },
                            navArgument("details") { type = NavType.StringType },
                            navArgument("isNewRecipient") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val amount = backStackEntry.arguments?.getString("amount") ?: ""
                        val details = backStackEntry.arguments?.getString("details") ?: ""
                        val isNewRecipient = backStackEntry.arguments?.getBoolean("isNewRecipient") ?: false
                        TransferConfirmScreen(
                            name = name,
                            id = id,
                            amount = amount,
                            details = details,
                            isNewRecipient = isNewRecipient,
                            onBack = { navController.popBackStack() },
                            onConfirm = {
                                val nextRoute = Screen.TransferSuccess.createRoute(name, id, amount, details)
                                navController.navigate(nextRoute) {
                                    popUpTo(Screen.TransferConfirm.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        route = Screen.TransferSuccess.route,
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType },
                            navArgument("amount") { type = NavType.StringType },
                            navArgument("details") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        val amount = backStackEntry.arguments?.getString("amount") ?: ""
                        val details = backStackEntry.arguments?.getString("details") ?: ""
                        TransferSuccessScreen(
                            name = name,
                            id = id,
                            amount = amount,
                            details = details,
                            onDone = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
