package com.example.vhackwallet.ui.transfer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.vhackwallet.ui.theme.*
import com.example.vhackwallet.ui.components.PendingVerificationOverlay
import java.util.Locale

/**
 * A custom shape with a slightly concave bottom edge for the celestial cherry theme.
 */
private val ConcaveHeaderShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    lineTo(width, 0f)
    lineTo(width, height)
    // Create a concave effect by curving the bottom edge upwards
    quadraticTo(width / 2f, height - 80f, 0f, height)
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferAmountScreen(
    name: String,
    id: String,
    isNewRecipient: Boolean,
    onBack: () -> Unit,
    onNext: (String, String) -> Unit
) {
    var amountInCents by remember { mutableLongStateOf(0L) }
    var details by remember { mutableStateOf("Fund Transfer") }

    // Sequence states: Biometrics -> ML Scan
    var showBiometricAuth by remember { mutableStateOf(false) }
    var showMLScan by remember { mutableStateOf(false) }

    val formattedAmount = remember(amountInCents) {
        String.format(Locale.getDefault(), "%.2f", amountInCents / 100.0)
    }

    /**
     * Visual transformation to highlight digits.
     * Use VerityBlue for digit emphasis to match app theme.
     */
    val numericHighlightTransformation = VisualTransformation { text ->
        val annotatedString = buildAnnotatedString {
            text.text.forEach { char ->
                if (char.isDigit()) {
                    withStyle(style = SpanStyle(color = VerityBlue, fontWeight = FontWeight.Bold)) {
                        append(char)
                    }
                } else {
                    append(char)
                }
            }
        }
        TransformedText(annotatedString, OffsetMapping.Identity)
    }

    /**
     * Currency text field that treats typed digits as cents (masked currency input).
     * Preserves selection by using TextFieldValue and updates `amountInCents` via callback.
     */
    @Composable
    fun CurrencyTextField(
        amountInCents: Long,
        onAmountChange: (Long) -> Unit,
        modifier: Modifier = Modifier,
        prefixContent: @Composable (() -> Unit)? = null
    ) {
        var textState by remember { mutableStateOf(TextFieldValue(if (amountInCents == 0L) "" else String.format(Locale.getDefault(), "%.2f", amountInCents / 100.0))) }

        // Keep textState in sync if amountInCents is updated externally
        LaunchedEffect(amountInCents) {
            val formatted = if (amountInCents == 0L) "" else String.format(Locale.getDefault(), "%.2f", amountInCents / 100.0)
            textState = textState.copy(text = formatted, selection = TextRange(formatted.length))
        }

        OutlinedTextField(
            value = textState,
            onValueChange = { newValue ->
                // Accept only digits from the typed input (digits map to cents)
                val cleanedDigits = newValue.text.filter { it.isDigit() }
                val newAmount = if (cleanedDigits.isEmpty()) 0L else cleanedDigits.toLong()
                onAmountChange(newAmount)

                val formatted = if (newAmount == 0L) "" else String.format(Locale.getDefault(), "%.2f", newAmount / 100.0)

                // Place cursor at end of formatted string (keeps behavior predictable)
                textState = TextFieldValue(formatted, selection = TextRange(formatted.length))
            },
            modifier = modifier,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VerityDark
            ),
            prefix = {
                if (prefixContent != null) prefixContent()
            },
            placeholder = {
                Text(
                    text = "0.00",
                    fontSize = 28.sp,
                    color = Color(0xFFCED4DA)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = numericHighlightTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerityBlue,
                unfocusedBorderColor = if (amountInCents > 0) VerityBlue.copy(alpha = 0.18f) else Color(0xFFE0E8F0),
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                cursorColor = VerityBlue
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money", fontWeight = FontWeight.Bold, color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
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
                                    // Match TransferSearch / Home BalanceCard gradient
                                    colors = listOf(VerityDark, VerityIndigo, VerityBlue)
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            // Top-right royal indigo mesh accent (match BalanceCard)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                                    center = center.copy(x = size.width * 0.82f, y = size.height * 0.18f),
                                    radius = size.minDimension
                                )
                            )

                            // Bottom-left cyan burst accent (subtle, match BalanceCard)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(VerityCyan.copy(alpha = 0.18f), Color.Transparent),
                                    center = center.copy(x = size.width * 0.18f, y = size.height * 0.78f),
                                    radius = size.minDimension
                                )
                            )

                            // Very subtle indigo hint near the upper center for depth
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(VerityIndigo.copy(alpha = 0.06f), Color.Transparent),
                                    center = center.copy(x = size.width * 0.52f, y = size.height * 0.32f),
                                    radius = size.minDimension * 0.6f
                                )
                            )

                            // More pronounced indigo hint on the left side for clearer transition
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(VerityIndigo.copy(alpha = 0.28f), Color.Transparent),
                                    center = center.copy(x = size.width * 0.10f, y = size.height * 0.28f),
                                    radius = size.minDimension * 0.9f
                                )
                            )

                            // Subtle soft-white overlay for depth (kept very light)
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // padding
                Spacer(modifier = Modifier.height(20.dp))
                // "Transferring to" text, sticking to left
                Text(
                    text = "Transferring to",
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    fontSize = 15.sp,
                    color = White,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Recipient Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(35.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(SurfaceSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase(),
                                color = VerityMuted.copy(alpha = 0.92f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = name, fontWeight = FontWeight.Bold, color = VerityDark)
                            Text(text = id, fontSize = 15.sp, color = VerityMuted.copy(alpha = 0.8f))
                        }
                    }
                }

                // Helpful reminder - Friendly superapp-style tip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF4FF)),
                    border = BorderStroke(1.dp, Color(0xFFD5E3F5))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Always verify recipient name before transferring",
                            fontSize = 12.sp,
                            color = Color(0xFF374151),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Amount Label
                Text(
                    text = "Amount",
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount Input Field - masked cents input (typed digits -> cents)
                CurrencyTextField(
                    amountInCents = amountInCents,
                    onAmountChange = { amountInCents = it },
                    modifier = Modifier.fillMaxWidth(),
                    prefixContent = {
                        Text(
                            text = "RM",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (amountInCents > 0) VerityBlue else Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )
                
                // Available Balance Text
                Text(
                    text = "Available: RM 12,450.80",
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
                    fontSize = 12.sp,
                    color = VerityMuted.copy(alpha = 0.8f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Details Label
                Text(
                    text = "What's the transfer for?",
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Details Input Field - Pre-filled, single line, with numeric highlighting and counter
                OutlinedTextField(
                    value = details,
                    onValueChange = { if (it.length <= 50) details = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = numericHighlightTransformation,
                    supportingText = {
                        Text(
                            text = "${details.length} / 50",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            fontSize = 11.sp,
                            color = if (details.length >= 50) VerityBlue else Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VerityBlue,
                        unfocusedBorderColor = if (details.isNotEmpty()) VerityBlue.copy(alpha = 0.18f) else Color(0xFFE0E8F0),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = VerityDark,
                        unfocusedTextColor = VerityDark,
                        cursorColor = VerityBlue
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (amountInCents > 0) {
                            showBiometricAuth = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerityBlue),
                    enabled = amountInCents > 0
                ) {
                    Text("Next", fontWeight = FontWeight.Bold)
                }
            }
            }
        }

        // overlays placed after Scaffold so they render above TopAppBar/title
        if (showBiometricAuth) {
            BiometricBottomPanel(
                onSuccess = {
                    showBiometricAuth = false
                    showMLScan = true
                },
                onUsePin = {
                    showBiometricAuth = false
                    showMLScan = true // Treat PIN fallback as success for this flow
                },
                onCancel = { showBiometricAuth = false }
            )
        }

        if (showMLScan) {
            PendingVerificationOverlay(
                isVisible = true,
                message = "Scanning for potential risks...",
                onVerificationComplete = {
                    showMLScan = false
                    onNext(formattedAmount, if (details.isEmpty()) "-" else details)
                }
            )
        }
    }
}
