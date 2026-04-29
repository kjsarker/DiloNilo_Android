package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.models.Loan
import com.example.dilo_nilo.data.models.LoanStatus
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.StatusPill
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LenderDashboardScreen(
    loanViewModel: LoanViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val loanState by loanViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id ?: return

    LaunchedEffect(Unit) { loanViewModel.loadLoans() }

    val lenderLoans = loanState.loans.filter { it.lenderId == userId }
    val pending = lenderLoans.filter { it.status == LoanStatus.PENDING }
    val active = lenderLoans.filter { it.status == LoanStatus.ACTIVE }
    val counterOffered = lenderLoans.filter { it.status == LoanStatus.COUNTER_OFFERED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lender Dashboard", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LenderStat("Pending", pending.size, Modifier.weight(1f))
                    LenderStat("Active", active.size, Modifier.weight(1f))
                    LenderStat("Counter", counterOffered.size, Modifier.weight(1f))
                }
            }

            if (pending.isNotEmpty()) {
                item {
                    Text("Pending Requests", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
                }
                items(pending) { loan ->
                    LenderLoanCard(loan = loan, onClick = { navController.navigate(Routes.chat(loan.id)) })
                }
            }

            if (counterOffered.isNotEmpty()) {
                item {
                    Text("Counter Offered", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
                }
                items(counterOffered) { loan ->
                    LenderLoanCard(loan = loan, onClick = { navController.navigate(Routes.chat(loan.id)) })
                }
            }

            if (active.isNotEmpty()) {
                item {
                    Text("Active Loans", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
                }
                items(active) { loan ->
                    LenderLoanCard(loan = loan, onClick = { navController.navigate(Routes.loanDetail(loan.id)) })
                }
            }

            if (lenderLoans.isEmpty() && !loanState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            "No lending activity yet. When someone sends you a loan request, it will appear here.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LenderStat(label: String, count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Primary)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LenderLoanCard(loan: Loan, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Loan Request",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$${"%.2f".format(loan.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "${loan.termMonths} months",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(status = loan.status)
        }
    }
}
