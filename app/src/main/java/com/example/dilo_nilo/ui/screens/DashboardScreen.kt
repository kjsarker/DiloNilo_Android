package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.models.Loan
import com.example.dilo_nilo.data.models.LoanStatus
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.AppBottomNav
import com.example.dilo_nilo.ui.components.StatusPill
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    loanViewModel: LoanViewModel,
    navController: NavController
) {
    val uiState by authViewModel.uiState.collectAsState()
    val loanState by loanViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id

    LaunchedEffect(Unit) { loanViewModel.loadLoans() }

    val myLoansAsBorrower = loanState.loans.filter { it.borrowerId == userId }
    val myLoansAsLender = loanState.loans.filter { it.lenderId == userId }
    val activeLoans = loanState.loans.filter { it.status == LoanStatus.ACTIVE }
    val pendingLoans = loanState.loans.filter { it.status == LoanStatus.PENDING }

    Scaffold(
        bottomBar = { AppBottomNav(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Welcome back,",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = uiState.profile?.fullName ?: "Friend",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryChip("Active", activeLoans.size.toString(), Modifier.weight(1f))
                            SummaryChip("Pending", pendingLoans.size.toString(), Modifier.weight(1f))
                            SummaryChip("Total", loanState.loans.size.toString(), Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                // Quick actions
                Text(
                    "Quick Actions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "Borrow",
                        subtitle = "Request a loan",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.BORROW_REQUEST) }
                    )
                    ActionCard(
                        title = "Lend",
                        subtitle = "View requests",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.LENDER_DASHBOARD) }
                    )
                }
            }

            if (loanState.loans.isNotEmpty()) {
                item {
                    Text(
                        "Recent Loans",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontFamily = InterFontFamily
                    )
                }
                items(loanState.loans.take(5)) { loan ->
                    LoanSummaryCard(
                        loan = loan,
                        isLender = loan.lenderId == userId,
                        onClick = { navController.navigate(Routes.loanDetail(loan.id)) }
                    )
                }
            }

            if (loanState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = if (title == "Borrow") Icons.Default.Add else Icons.Default.AccountBalance,
                contentDescription = title,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSummaryCard(loan: Loan, isLender: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLender) "You lent" else "You borrowed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${"%.2f".format(loan.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${loan.termMonths} months",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(status = loan.status)
        }
    }
}
