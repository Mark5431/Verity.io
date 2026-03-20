package com.example.vhackwallet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vhackwallet.R
import com.example.vhackwallet.ui.theme.CelestialDarkNavy
import com.example.vhackwallet.ui.theme.VerityMuted
import kotlinx.coroutines.delay

/**
 * Simple pending verification overlay
 * Shows smooth animation while backend processes fraud detection silently
 * User sees normal verification process, no scary alerts
 * Modified to full page jump style for consistency
 */
@Composable
fun PendingVerificationOverlay(
    isVisible: Boolean = false,
    message: String = "Verifying payment...",
    onVerificationComplete: () -> Unit = {}
) {
    if (!isVisible) return

    // Auto-dismiss after 3 seconds (backend review time)
    LaunchedEffect(Unit) {
        delay(3000)
        onVerificationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Custom verification animation - Resized to fit the page better
            VerificationAnimationLottie()

            Spacer(modifier = Modifier.height(24.dp))

            // Simple message
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CelestialDarkNavy,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Please wait while we secure your transaction",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Powered by Verity.io",
                fontSize = 14.sp,
                color = VerityMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Custom verification animation from Lottie
 */
@Composable
private fun VerificationAnimationLottie() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.verification_animation)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(220.dp)
    )
}

/**
 * Silent background verification handler
 * Simulates backend fraud detection without UI interruption
 */
@Composable
fun SilentBackendVerification(
    isNewRecipient: Boolean,
    amount: String,
    onVerificationResult: (VerificationResult) -> Unit
) {
    LaunchedEffect(isNewRecipient, amount) {
        if (isNewRecipient) {
            // Simulate backend call (3 seconds)
            delay(3000)
            
            // Backend decides: allow/hold/block
            val result = when {
                amount.toDoubleOrNull() ?: 0.0 > 5000 -> VerificationResult.PENDING_REVIEW
                isNewRecipient -> VerificationResult.VERIFIED  // Low risk after check
                else -> VerificationResult.VERIFIED
            }
            
            onVerificationResult(result)
        }
    }
}

/**
 * Verification result from backend
 */
enum class VerificationResult {
    VERIFIED,        // Safe to proceed
    PENDING_REVIEW,  // Under manual review (show simple pending state)
    BLOCKED          // Fraudulent (show gentle message)
}
