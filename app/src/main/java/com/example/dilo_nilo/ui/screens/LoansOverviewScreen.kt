package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.AppBottomNav
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansOverviewScreen(
    loanViewModel: LoanViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val loanState by loanViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { loanViewModel.loadLoans() }

    val borrowerLoans = loanState.loans.filter { it.borrowerId == userId }
    val lenderLoans = loanState.loans.filter { it.lenderId == userId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Loans", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) }
            )
        },
        bottomBar = { AppBottomNav(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Borrowing (${borrowerLoans.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Lending (${lenderLoans.size})") }
                )
            }

            val displayLoans = if (selectedTab == 0) borrowerLoans else lenderLoans

            if (loanState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (displayLoans.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (selectedTab == 0) "No borrowing activity" else "No lending activity",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedTab == 0) {
                            Button(
                                onClick = { navController.navigate(Routes.BORROW_REQUEST) },
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Request a Loan") }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayLoans) { loan ->
                        LoanSummaryCard(
                            loan = loan,
                            isLender = selectedTab == 1,
                            onClick = {
                                if (loan.status in listOf("pending", "counter_offered")) {
                                    navController.navigate(Routes.chat(loan.id))
                                } else {
                                    navController.navigate(Routes.loanDetail(loan.id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
