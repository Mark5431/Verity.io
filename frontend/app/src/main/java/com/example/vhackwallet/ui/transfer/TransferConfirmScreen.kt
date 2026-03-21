package com.example.vhackwallet.ui.transfer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.vhackwallet.ui.components.PendingVerificationOverlay
import com.example.vhackwallet.ui.components.SilentBackendVerification
import com.example.vhackwallet.ui.components.VerificationResult
import com.example.vhackwallet.ui.notifications.LocalHoldNotification
import com.example.vhackwallet.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * A custom shape with a slightly concave bottom edge for the celestial cherry theme.
 */
private val ConcaveHeaderShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    lineTo(width, 0f)
    lineTo(width, height)
    // Create a deeper convex effect to match TransferSearch/Amount headers
    quadraticTo(width / 2f, height + 160f, 0f, height)
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferConfirmScreen(
    name: String,
    id: String,
    amount: String,
    details: String,
    isNewRecipient: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    var showPinAuth by remember { mutableStateOf(false) }
    var showTransferAnimation by remember { mutableStateOf(false) }
    var hasNotifiedHold by remember { mutableStateOf(false) }
    var holdReasons by remember {
        mutableStateOf(
            listOf(
                "First time sending to this recipient",
                "Higher amount than usual",
                "This device isn't recognized"
            )
        )
    }
    var holdExplanation by remember {
        mutableStateOf("We can’t confirm intent—this pattern can be risky, so we paused it to keep you safe.")
    }
    val context = LocalContext.current
    
    val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

    LaunchedEffect(name, amount, hasNotifiedHold) {
        if (!hasNotifiedHold) {
            delay(1200)
            LocalHoldNotification.show(
                context = context,
                amount = amount,
                recipientName = name
            )
            hasNotifiedHold = true
        }
    }

    LaunchedEffect(name, amount, isNewRecipient) {
        val decision = HoldDecisionClient.resolveHoldDecision(
            recipientName = name,
            amountText = amount,
            isNewRecipient = isNewRecipient
        )
        holdReasons = decision.reasons
        holdExplanation = decision.explanation
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Paused", fontWeight = FontWeight.Bold, color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { /* Help action */ },
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "Help",
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Help",
                                color = White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = SoftGreyPurple
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
            // Header matching BalanceCard style (Verity gradient + accents)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.22f)
                    .clip(ConcaveHeaderShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(VerityDark, VerityIndigo, VerityBlue)
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    // Top-right royal indigo mesh accent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                            center = center.copy(x = size.width * 0.82f, y = size.height * 0.18f),
                            radius = size.minDimension
                        )
                    )

                    // Bottom-left cyan burst accent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityCyan.copy(alpha = 0.18f), Color.Transparent),
                            center = center.copy(x = size.width * 0.18f, y = size.height * 0.78f),
                            radius = size.minDimension
                        )
                    )

                    // Subtle indigo hints for depth and transition
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.06f), Color.Transparent),
                            center = center.copy(x = size.width * 0.52f, y = size.height * 0.32f),
                            radius = size.minDimension * 0.6f
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                            center = center.copy(x = size.width * 0.10f, y = size.height * 0.28f),
                            radius = size.minDimension * 0.9f
                        )
                    )

                    // Very light soft-white overlay for subtle depth
                    drawRect(
                        color = White.copy(alpha = 0.015f),
                        topLeft = Offset.Zero,
                        size = Size(size.width * 0.6f, size.height * 0.36f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Card 1: Recipient Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo with first letter of recipient
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SurfaceSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerityMuted.copy(alpha = 0.92f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerityDark
                            )
                            Text(
                                text = id,
                                color = VerityMuted.copy(alpha = 0.8f),
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Card 2: Transfer Details - Slightly narrower and slightly darker background
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    // Card Header (Transfer Details)
                    Text(
                        text = "Transfer Details",
                        fontSize = 16.sp,
                        color = VerityMuted,
                        modifier = Modifier.padding(top = 16.dp, start = 24.dp, bottom = 8.dp)
                    )
                    // horizontal dark gray bar - shrunk from both sides
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(1.dp)
                            .background(Color(0xFFE0E8F0))
                    )
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        ConfirmRow(label = "Amount", value = "RM $amount")
                        ConfirmRow(label = "Date & Time", value = currentDate)
                        ConfirmRow(label = "Remarks", value = details)
                        ConfirmRow(label = "Fee", value = "RM 0.00")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SoftGreyPurple)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Payable", fontWeight = FontWeight.Bold, color = CelestialDarkNavy)
                            Text(text = "RM $amount", fontWeight = FontWeight.Bold, color = CelestialDarkNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Card 3 which internally displays why the transaction was paused
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = VerityCyan.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, VerityBlue.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                        Text(
                            text = "Why we paused this transaction:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerityBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        holdReasons.forEach { reason ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(VerityBlue)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = reason,
                                    fontSize = 14.sp,
                                    color = CelestialDarkNavy.copy(alpha = 0.8f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = holdExplanation,
                            fontSize = 13.sp,
                            lineHeight = 14.sp,
                            color = VerityBlue.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Secure end-to-end encrypted transaction", fontSize = 12.sp, color = VerityMuted.copy(alpha = 0.7f))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        showPinAuth = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerityBlue)
                ) {
                    Text(
                        text = "Proceed to Transfer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }

                // Vertical padding between buttons
                Spacer(modifier = Modifier.height(4.dp))

                // Button for "Take Me Back"
                OutlinedButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerityBlue)
                ) {
                    Text(
                        text = "Take Me Back",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerityBlue
                    )
                }
            }

            }
        }

        // overlays after Scaffold so they render above TopAppBar/title
        if (showPinAuth) {
            PinBottomPanel(
                onSuccess = {
                    showPinAuth = false
                    showTransferAnimation = true
                },
                onCancel = { showPinAuth = false }
            )
        }

        if (showTransferAnimation) {
            CashTransferAnimationOverlay(
                onComplete = {
                    showTransferAnimation = false
                    onConfirm()
                }
            )
        }
    }
}

@Composable
fun ConfirmRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = VerityMuted.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = if (highlight) Color(0xFFD32F2F) else VerityDark, fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 14.sp)
    }
}
