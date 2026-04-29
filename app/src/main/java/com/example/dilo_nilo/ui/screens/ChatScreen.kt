package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.models.LoanMessage
import com.example.dilo_nilo.data.models.LoanStatus
import com.example.dilo_nilo.data.models.MessageType
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.StatusPill
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.AuthViewModel
import com.example.dilo_nilo.viewmodel.LoanViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    loanId: String,
    loanViewModel: LoanViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val loanState by loanViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id ?: return
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var showApproveDialog by remember { mutableStateOf(false) }
    var showCounterDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("cash") }
    var counterAmount by remember { mutableStateOf("") }
    var counterTerm by remember { mutableStateOf("") }

    LaunchedEffect(loanId) {
        loanViewModel.loadLoan(loanId)
        loanViewModel.subscribeToLoanChanges(loanId)
    }

    val loan = loanState.currentLoan
    val isLender = loan?.lenderId == userId
    val role = if (isLender) "lender" else "borrower"

    LaunchedEffect(loanState.messages.size) {
        if (loanState.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(loanState.messages.size - 1) }
        }
    }

    // Approve dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Loan", fontFamily = InterFontFamily) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select payment method:")
                    listOf("bkash", "nagad", "cash").forEach { method ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Text(method.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    loanViewModel.approveLoan(loanId, selectedPaymentMethod)
                    showApproveDialog = false
                }) { Text("Approve") }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Counter offer dialog
    if (showCounterDialog) {
        AlertDialog(
            onDismissRequest = { showCounterDialog = false },
            title = { Text("Counter Offer", fontFamily = InterFontFamily) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = counterAmount,
                        onValueChange = { counterAmount = it },
                        label = { Text("New Amount ($)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = counterTerm,
                        onValueChange = { counterTerm = it },
                        label = { Text("New Term (months)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = counterAmount.toDoubleOrNull() ?: return@Button
                    val term = counterTerm.toIntOrNull() ?: return@Button
                    loanViewModel.counterOffer(loanId, amt, term)
                    showCounterDialog = false
                }) { Text("Send Counter") }
            },
            dismissButton = {
                TextButton(onClick = { showCounterDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Loan Chat", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
                        loan?.let { StatusPill(status = it.status) }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Lender action buttons
                if (isLender && loan?.status in listOf(LoanStatus.PENDING, LoanStatus.COUNTER_OFFERED)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showApproveDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("Approve", fontSize = 13.sp) }
                        OutlinedButton(
                            onClick = { showCounterDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Counter", fontSize = 13.sp) }
                        OutlinedButton(
                            onClick = { loanViewModel.rejectLoan(loanId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Reject", fontSize = 13.sp) }
                    }
                }

                // Borrower: accept counter
                if (!isLender && loan?.status == LoanStatus.COUNTER_OFFERED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { loanViewModel.acceptCounter(loanId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Accept Counter Offer") }
                    }
                }

                // Message input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                loanViewModel.sendMessage(loanId, messageText.trim(), role)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.background(Primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(loanState.messages) { message ->
                MessageBubble(
                    message = message,
                    isOwn = message.senderId == userId
                )
            }
        }
    }
}

val CircleShape = RoundedCornerShape(50)

@Composable
private fun MessageBubble(message: LoanMessage, isOwn: Boolean) {
    val isSystem = message.type == MessageType.SYSTEM || message.senderRole == "system"

    if (isSystem) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isOwn) Primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwn) 16.dp else 4.dp,
                        bottomEnd = if (isOwn) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}
