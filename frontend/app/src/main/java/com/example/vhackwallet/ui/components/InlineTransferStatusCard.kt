package com.example.vhackwallet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vhackwallet.R
import com.example.vhackwallet.ui.theme.CelestialBerry
import com.example.vhackwallet.ui.theme.CelestialDarkNavy
import com.example.vhackwallet.ui.theme.SoftGreyPurple
import kotlinx.coroutines.delay

/**
 * Inline cards showing transfer verification status
 * Displays on the payment detail page (not as popup)
 * Superapp-style design (like GoPay, OVO)
 */

@Composable
fun InlineTransferStatusCard(
    isVisible: Boolean = false,
    status: VerificationStatus = VerificationStatus.VERIFYING,
    onStatusComplete: () -> Unit = {}
) {
    if (!isVisible) return

    // Auto-dismiss after 3 seconds when verification completes
    LaunchedEffect(status) {
        if (status != VerificationStatus.VERIFYING) {
            delay(2000)  // Show success for 2 seconds before dismissing
            onStatusComplete()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                VerificationStatus.VERIFYING -> SoftGreyPurple
                VerificationStatus.VERIFIED -> Color(0xFFF0F8E8)  // Light green
                VerificationStatus.PENDING -> Color(0xFFF5F0E8)   // Light amber
                VerificationStatus.BLOCKED -> Color(0xFFFEEAE8)   // Light red
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status icon/animation
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                when (status) {
                    VerificationStatus.VERIFYING -> {
                        VerificationAnimationLottie(modifier = Modifier.size(48.dp))
                    }
                    VerificationStatus.VERIFIED -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    VerificationStatus.PENDING -> {
                        // Show warning-like indicator
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    VerificationStatus.BLOCKED -> {
                        // Show error indicator
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Status text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (status) {
                        VerificationStatus.VERIFYING -> "Verifying payment"
                        VerificationStatus.VERIFIED -> "Payment verified"
                        VerificationStatus.PENDING -> "Under review"
                        VerificationStatus.BLOCKED -> "Transaction blocked"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CelestialDarkNavy
                )
                Text(
                    text = when (status) {
                        VerificationStatus.VERIFYING -> "Please wait while we verify your transfer"
                        VerificationStatus.VERIFIED -> "Your transfer is ready to proceed"
                        VerificationStatus.PENDING -> "We're reviewing this new recipient"
                        VerificationStatus.BLOCKED -> "Transaction cannot proceed at this time"
                    },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun VerificationAnimationLottie(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.verification_animation)
    )

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

enum class VerificationStatus {
    VERIFYING, VERIFIED, PENDING, BLOCKED
}
