package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.StatusPill
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.ui.theme.Success
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    loanViewModel: LoanViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val loanState by loanViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id

    LaunchedEffect(loanId) { loanViewModel.loadLoan(loanId) }

    val loan = loanState.currentLoan
    val isLender = loan?.lenderId == userId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Details", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (loan == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$${"%.2f".format(loan.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    fontFamily = InterFontFamily
                )
                StatusPill(status = loan.status)
            }

            // Details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow("Term", "${loan.termMonths} months")
                    DetailRow("Role", if (isLender) "Lender" else "Borrower")
                    loan.paymentMethod?.let { DetailRow("Payment Method", it) }
                    loan.dueDate?.let { DetailRow("Due Date", it.take(10)) }
                    loan.disbursedAt?.let { DetailRow("Disbursed", it.take(10)) }
                }
            }

            // Verification status
            Text("Verification", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    VerificationItem(
                        label = "E-Contract Signed",
                        verified = loan.eContractSigned,
                        icon = Icons.Default.CheckCircle
                    )
                    VerificationItem(
                        label = "Video Verified",
                        verified = loan.videoVerified,
                        icon = Icons.Default.VideoFile
                    )
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.chat(loan.id)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat")
                }
                if (loan.status == "active" && !isLender) {
                    Button(
                        onClick = { /* mark as completed - future feature */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Mark Repaid") }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun VerificationItem(
    label: String,
    verified: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (verified) Success else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(label, fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            if (verified) "Verified" else "Pending",
            fontSize = 12.sp,
            color = if (verified) Success else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
