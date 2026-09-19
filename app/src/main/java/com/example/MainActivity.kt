package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.PaymentMethod
import com.example.model.Transaction
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BankAccountsScreen
import com.example.ui.screens.ChargeCalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PaymentFlowScreen
import com.example.ui.screens.ReceiveMoneyScreen
import com.example.ui.screens.ScanQrScreen
import com.example.ui.screens.SendMoneyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartPlannerScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.OneTheme
import com.example.ui.viewmodel.OneViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SendMoney : Screen("send_money")
    data object PaymentFlow : Screen("payment_flow")
    data object ScanQr : Screen("scan_qr")
    data object ReceiveMoney : Screen("receive_money")
    data object BankAccounts : Screen("bank_accounts")
    data object History : Screen("history")
    data object Calculator : Screen("calculator")
    data object Planner : Screen("planner")
    data object Settings : Screen("settings")
    data object Auth : Screen("auth")
}

class MainActivity : FragmentActivity() {

    private val viewModel: OneViewModel by viewModels {
        val app = application as OneApplication
        OneViewModel.factory(
            app.repository,
            app.paymentService,
            app.plannerService,
            app.calculatorService
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OneAppNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun OneAppNavHost(viewModel: OneViewModel) {
    val navController = rememberNavController()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScanQr = { navController.navigate(Screen.ScanQr.route) },
                onNavigateToSendMoney = { navController.navigate(Screen.SendMoney.route) },
                onNavigateToReceiveMoney = { navController.navigate(Screen.ReceiveMoney.route) },
                onNavigateToBankAccounts = { navController.navigate(Screen.BankAccounts.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToCalculator = { navController.navigate(Screen.Calculator.route) },
                onNavigateToPlanner = { navController.navigate(Screen.Planner.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onSelectTransaction = { _ ->
                    navController.navigate(Screen.History.route)
                },
                onQuickPay = { name, upiId ->
                    viewModel.setPaymentMethod(PaymentMethod.UPI_ID)
                    viewModel.setRecipientIdentifier(upiId)
                    viewModel.verifyRecipient()
                    navController.navigate(Screen.PaymentFlow.route)
                }
            )
        }

        composable(Screen.SendMoney.route) {
            SendMoneyScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectMethod = { method ->
                    if (method == PaymentMethod.QR) {
                        navController.navigate(Screen.ScanQr.route)
                    } else {
                        viewModel.setPaymentMethod(method)
                        navController.navigate(Screen.PaymentFlow.route)
                    }
                }
            )
        }

        composable(Screen.PaymentFlow.route) {
            PaymentFlowScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onFinishPayment = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ScanQr.route) {
            ScanQrScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onQrScanned = {
                    navController.navigate(Screen.PaymentFlow.route)
                }
            )
        }

        composable(Screen.ReceiveMoney.route) {
            ReceiveMoneyScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BankAccounts.route) {
            BankAccountsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            TransactionHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calculator.route) {
            ChargeCalculatorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Planner.route) {
            SmartPlannerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    viewModel.isAuthenticated.value = false
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
