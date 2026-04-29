package com.example.dilo_nilo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dilo_nilo.ui.screens.AuthScreen
import com.example.dilo_nilo.ui.screens.BorrowContractScreen
import com.example.dilo_nilo.ui.screens.BorrowRequestScreen
import com.example.dilo_nilo.ui.screens.BorrowVideoScreen
import com.example.dilo_nilo.ui.screens.ChatScreen
import com.example.dilo_nilo.ui.screens.ConnectionsScreen
import com.example.dilo_nilo.ui.screens.DashboardScreen
import com.example.dilo_nilo.ui.screens.LenderDashboardScreen
import com.example.dilo_nilo.ui.screens.LoanDetailScreen
import com.example.dilo_nilo.ui.screens.LoansOverviewScreen
import com.example.dilo_nilo.ui.screens.ProfileScreen
import com.example.dilo_nilo.ui.screens.SearchScreen
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.ConnectionViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel
import io.github.jan.tennert.supabase.auth.SessionStatus

object Routes {
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val BORROW_REQUEST = "borrow_request"
    const val BORROW_CONTRACT = "borrow_contract/{loanId}"
    const val BORROW_VIDEO = "borrow_video/{loanId}"
    const val CHAT = "chat/{loanId}"
    const val LENDER_DASHBOARD = "lender_dashboard"
    const val LOANS_OVERVIEW = "loans_overview"
    const val LOAN_DETAIL = "loan_detail/{loanId}"
    const val SEARCH = "search"
    const val CONNECTIONS = "connections"
    const val PROFILE = "profile"

    fun borrowContract(loanId: String) = "borrow_contract/$loanId"
    fun borrowVideo(loanId: String) = "borrow_video/$loanId"
    fun chat(loanId: String) = "chat/$loanId"
    fun loanDetail(loanId: String) = "loan_detail/$loanId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    loanViewModel: LoanViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel = viewModel()
) {
    val sessionStatus by authViewModel.sessionStatus.collectAsState(initial = SessionStatus.LoadingFromStorage)
    val startDestination = when (sessionStatus) {
        is SessionStatus.Authenticated -> Routes.DASHBOARD
        is SessionStatus.NotAuthenticated -> Routes.AUTH
        else -> Routes.AUTH
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                authViewModel = authViewModel,
                loanViewModel = loanViewModel,
                navController = navController
            )
        }

        composable(Routes.BORROW_REQUEST) {
            BorrowRequestScreen(
                connectionViewModel = connectionViewModel,
                loanViewModel = loanViewModel,
                navController = navController
            )
        }

        composable(
            Routes.BORROW_CONTRACT,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStack ->
            val loanId = backStack.arguments?.getString("loanId") ?: return@composable
            BorrowContractScreen(
                loanId = loanId,
                loanViewModel = loanViewModel,
                navController = navController
            )
        }

        composable(
            Routes.BORROW_VIDEO,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStack ->
            val loanId = backStack.arguments?.getString("loanId") ?: return@composable
            BorrowVideoScreen(
                loanId = loanId,
                loanViewModel = loanViewModel,
                navController = navController
            )
        }

        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStack ->
            val loanId = backStack.arguments?.getString("loanId") ?: return@composable
            ChatScreen(
                loanId = loanId,
                loanViewModel = loanViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Routes.LENDER_DASHBOARD) {
            LenderDashboardScreen(
                loanViewModel = loanViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Routes.LOANS_OVERVIEW) {
            LoansOverviewScreen(
                loanViewModel = loanViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(
            Routes.LOAN_DETAIL,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStack ->
            val loanId = backStack.arguments?.getString("loanId") ?: return@composable
            LoanDetailScreen(
                loanId = loanId,
                loanViewModel = loanViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                connectionViewModel = connectionViewModel,
                navController = navController
            )
        }

        composable(Routes.CONNECTIONS) {
            ConnectionsScreen(
                connectionViewModel = connectionViewModel,
                navController = navController
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
