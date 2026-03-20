package com.example.vhackwallet.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vhackwallet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Phone Number", "Account Number", "DuitNow ID")
    
    var recipientValue by remember { mutableStateOf("") }
    var amountValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftGreyPurple)
            )
        },
        containerColor = SoftGreyPurple
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CelestialBerry
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            recipientValue = "" // Reset on tab switch
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) CelestialBerry else CelestialBerry.copy(alpha = 0.6f),
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val label = when(selectedTab) {
                        0 -> "Recipient Phone Number"
                        1 -> "Bank Account Number"
                        else -> "DuitNow ID / NRIC"
                    }
                    val icon = when(selectedTab) {
                        0 -> Icons.Default.ContactPhone
                        1 -> Icons.Default.Numbers
                        else -> Icons.Default.QrCode
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = CelestialBerry.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = recipientValue,
                        onValueChange = { recipientValue = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        leadingIcon = { Icon(icon, contentDescription = null, tint = CelestialBerry) },
                        placeholder = { Text("Enter $label") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedTab == 1) KeyboardType.Number else KeyboardType.Text
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelestialBerry,
                            unfocusedBorderColor = SoftGreyPurple
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Amount (RM)",
                        style = MaterialTheme.typography.labelLarge,
                        color = CelestialBerry.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = amountValue,
                        onValueChange = { amountValue = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        leadingIcon = {
                            Text("RM", fontWeight = FontWeight.Bold, color = CelestialBerry, modifier = Modifier.padding(start = 12.dp))
                        },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelestialBerry,
                            unfocusedBorderColor = SoftGreyPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (recipientValue.isNotEmpty() && amountValue.isNotEmpty()) {
                        onSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelestialBerry)
            ) {
                Text("Confirm Transfer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
