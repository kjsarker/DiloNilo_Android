package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.data.models.Connection
import com.example.dilo_nilo.data.models.ConnectionStatus
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.ui.components.AppBottomNav
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.ui.theme.Success
import com.example.dilo_nilo.viewmodel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    connectionViewModel: ConnectionViewModel,
    navController: NavController
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    val userId = supabase.auth.currentUserOrNull()?.id

    LaunchedEffect(Unit) { connectionViewModel.loadConnections() }

    val pendingReceived = uiState.pendingReceived
    val accepted = uiState.connections.filter { it.status == ConnectionStatus.ACCEPTED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) }
            )
        },
        bottomBar = { AppBottomNav(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pendingReceived.isNotEmpty()) {
                item {
                    Text("Pending Requests", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
                }
                items(pendingReceived) { conn ->
                    val profile = uiState.profileCache[conn.requesterId]
                    PendingConnectionCard(
                        connection = conn,
                        name = profile?.fullName ?: "Unknown",
                        email = profile?.email,
                        onAccept = { connectionViewModel.respondToRequest(conn.id, true) },
                        onReject = { connectionViewModel.respondToRequest(conn.id, false) }
                    )
                }
                item { Divider() }
            }

            if (accepted.isNotEmpty()) {
                item {
                    Text("My Contacts", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = InterFontFamily)
                }
                items(accepted) { conn ->
                    val otherId = if (conn.requesterId == userId) conn.receiverId else conn.requesterId
                    val profile = uiState.profileCache[otherId]
                    AcceptedConnectionCard(
                        name = profile?.fullName ?: "Unknown",
                        email = profile?.email
                    )
                }
            }

            if (pendingReceived.isEmpty() && accepted.isEmpty() && !uiState.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            "No connections yet. Use Search to find and connect with people.",
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (uiState.isLoading) {
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
private fun PendingConnectionCard(
    connection: Connection,
    name: String,
    email: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                email?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("Wants to connect", fontSize = 12.sp, color = Primary)
            }
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, contentDescription = "Accept", tint = Success)
            }
            IconButton(onClick = onReject) {
                Icon(Icons.Default.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AcceptedConnectionCard(name: String, email: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                email?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(
                "Connected",
                color = Success,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
