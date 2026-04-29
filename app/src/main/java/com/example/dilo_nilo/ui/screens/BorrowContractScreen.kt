package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.ui.theme.Success
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowContractScreen(
    loanId: String,
    loanViewModel: LoanViewModel,
    navController: NavController
) {
    val loanState by loanViewModel.uiState.collectAsState()
    val profile = supabase.auth.currentUserOrNull()
    var signatureName by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

    LaunchedEffect(loanId) { loanViewModel.loadLoan(loanId) }

    val loan = loanState.currentLoan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Agreement", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) },
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
            // Contract card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "LOAN AGREEMENT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = InterFontFamily,
                        color = Primary
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    ContractRow("Loan Amount", "$${"%.2f".format(loan.amount)}")
                    ContractRow("Repayment Term", "${loan.termMonths} months")
                    ContractRow("Status", loan.status.replaceFirstChar { it.uppercase() })

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = buildString {
                            append("I, the borrower, agree to repay the full amount of ")
                            append("$${"%.2f".format(loan.amount)} ")
                            append("within ${loan.termMonths} months from the date of disbursement. ")
                            append("Failure to repay may result in legal action or other consequences as agreed by both parties. ")
                            append("This agreement is binding and made in good faith.")
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Text("Your Signature", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            OutlinedTextField(
                value = signatureName,
                onValueChange = { signatureName = it },
                label = { Text("Type your full name to sign") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "I have read and agree to the terms of this loan agreement.",
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            val userFullName = profile?.email ?: ""
            val isSignatureValid = signatureName.isNotBlank() && agreed

            Button(
                onClick = {
                    navController.navigate(Routes.borrowVideo(loanId))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isSignatureValid,
                colors = ButtonDefaults.buttonColors(containerColor = if (isSignatureValid) Primary else MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign & Proceed to Video", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ContractRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
