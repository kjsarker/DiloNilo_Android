package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.models.Profile
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.navigation.Routes
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.ConnectionViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowRequestScreen(
    connectionViewModel: ConnectionViewModel,
    loanViewModel: LoanViewModel,
    navController: NavController
) {
    val userId = supabase.auth.currentUserOrNull()?.id ?: return
    var acceptedContacts by remember { mutableStateOf<List<Pair<String, Profile?>>>(emptyList()) }
    var selectedLenderId by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }
    var termMonths by remember { mutableStateOf("") }
    val loanState by loanViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        connectionViewModel.getAcceptedConnections(userId) { result ->
            acceptedContacts = result.map {
                val otherId = if (it.first.requesterId == userId) it.first.receiverId else it.first.requesterId
                otherId to it.second
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request a Loan", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Select Lender",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = InterFontFamily
            )

            if (acceptedContacts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        "No accepted connections yet. Go to Search to find and connect with people.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            } else {
                acceptedContacts.forEach { (id, profile) ->
                    val name = profile?.fullName ?: "Unknown"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { selectedLenderId = id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLenderId == id)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLenderId == id,
                                onClick = { selectedLenderId = id },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Medium)
                                profile?.email?.let {
                                    Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Loan Amount ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = termMonths,
                onValueChange = { termMonths = it },
                label = { Text("Term (months)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            loanState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val lenderId = selectedLenderId ?: return@Button
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    val term = termMonths.toIntOrNull() ?: return@Button
                    loanViewModel.createLoan(lenderId, amt, term) { loanId ->
                        navController.navigate(Routes.borrowContract(loanId))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedLenderId != null && amount.isNotBlank() && termMonths.isNotBlank() && !loanState.isLoading
            ) {
                if (loanState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Continue to Contract", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
