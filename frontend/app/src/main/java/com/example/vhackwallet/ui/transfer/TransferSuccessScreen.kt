package com.example.vhackwallet.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vhackwallet.ui.components.GoPaySuccessAnimation
import com.example.vhackwallet.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransferSuccessScreen(
    name: String,
    id: String,
    amount: String,
    details: String,
    onDone: () -> Unit
) {
    val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    val refNo = "TRX" + System.currentTimeMillis().toString().takeLast(10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftGreyPurple)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VerityIndigo.copy(alpha = 0.12f), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.85f
                    )
                )
            }
            GoPaySuccessAnimation()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment Successful",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = VerityDark
        )

        Text(
            text = "RM $amount",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = VerityIndigo,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Receipt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Transaction Details", fontWeight = FontWeight.Bold, color = VerityDark, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                ReceiptRow(label = "Recipient Name", value = name)
                ReceiptRow(label = "Account/ID", value = id)
                ReceiptRow(label = "Ref No.", value = refNo)
                ReceiptRow(label = "Date & Time", value = currentDate)
                ReceiptRow(label = "Note", value = details)
                ReceiptRow(label = "Status", value = "Completed", valueColor = SuccessGreen)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SoftGreyPurple)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = VerityIndigo, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Share Receipt", color = VerityIndigo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerityBlue)
        ) {
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color = VerityDark) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = VerityMuted.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
