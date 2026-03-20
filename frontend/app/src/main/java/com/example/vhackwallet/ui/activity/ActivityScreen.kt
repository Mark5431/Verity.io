package com.example.vhackwallet.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vhackwallet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Period Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = true, label = "All")
                FilterChip(selected = false, label = "Income")
                FilterChip(selected = false, label = "Expense")
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelLarge,
                        color = CelestialBerry.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(todayTransactions) { transaction ->
                    ActivityCard(transaction)
                }
                item {
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.labelLarge,
                        color = CelestialBerry.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(yesterdayTransactions) { transaction ->
                    ActivityCard(transaction)
                }
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String) {
    Surface(
        color = if (selected) CelestialBerry else White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) White else CelestialBerry.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ActivityCard(transaction: TransactionData) {
    Surface(
        color = White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftGreyPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = transaction.icon, contentDescription = null, tint = CelestialBerry)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.name, fontWeight = FontWeight.Bold, color = CelestialDarkNavy)
                Text(text = transaction.time, style = MaterialTheme.typography.bodySmall, color = CelestialBerry.copy(alpha = 0.7f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount.startsWith("+")) Color(0xFF2E7D32) else CelestialDarkNavy
                )
            }
        }
    }
}

data class TransactionData(
    val name: String,
    val amount: String,
    val time: String,
    val icon: ImageVector,
    val isSuspicious: Boolean = false
)

val todayTransactions = listOf(
    TransactionData("Apple Store", "- RM 999.00", "10:24 AM", Icons.Default.Laptop),
    TransactionData("Sarah Jenkins", "+ RM 150.00", "09:15 AM", Icons.Default.Person),
    TransactionData("Starbucks", "- RM 12.50", "08:30 AM", Icons.Default.Coffee, isSuspicious = true)
)

val yesterdayTransactions = listOf(
    TransactionData("Netflix", "- RM 15.99", "07:00 PM", Icons.Default.PlayCircle),
    TransactionData("Amazon.com", "- RM 124.00", "02:30 PM", Icons.Default.ShoppingCart),
    TransactionData("Grocery Store", "- RM 54.20", "11:00 AM", Icons.Default.Store)
)
