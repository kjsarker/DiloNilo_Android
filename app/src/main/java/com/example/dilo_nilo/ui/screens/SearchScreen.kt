package com.example.dilo_nilo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dilo_nilo.ui.components.AppBottomNav
import com.example.dilo_nilo.ui.theme.InterFontFamily
import com.example.dilo_nilo.ui.theme.Primary
import com.example.dilo_nilo.viewmodel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    connectionViewModel: ConnectionViewModel,
    navController: NavController
) {
    val uiState by connectionViewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var sentRequests by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find People", fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold) }
            )
        },
        bottomBar = { AppBottomNav(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.length >= 3) connectionViewModel.searchUsers(it)
                    else connectionViewModel.searchUsers("")
                },
                label = { Text("Search by email or phone") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            uiState.successMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2000)
                    connectionViewModel.clearMessage()
                }
            }

            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.searchResults) { user ->
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
                                Text(
                                    user.fullName ?: "Unknown",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                user.maskedEmail?.let {
                                    Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                user.maskedPhone?.let {
                                    Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (user.id in sentRequests) {
                                Text(
                                    "Sent",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        connectionViewModel.sendConnectionRequest(user.id)
                                        sentRequests = sentRequests + user.id
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = "Connect",
                                        tint = Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (query.length >= 3 && uiState.searchResults.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "No users found for \"$query\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
