package com.example.vhackwallet.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.vhackwallet.R
import com.example.vhackwallet.ui.theme.CelestialDarkNavy
import kotlinx.coroutines.delay

/**
 * Verification Screen
 * Shows verification animation for new recipients before proceeding to 2FA
 * After 3 seconds, shows biometric auth, then face auth, then completes
 */
@Composable
fun VerificationScreen(
    onVerificationComplete: () -> Unit,
    onCancelTransaction: () -> Unit = {}
) {
    var animationComplete by remember { mutableStateOf(false) }
    var showBiometricAuth by remember { mutableStateOf(false) }

    // Auto-complete animation after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        animationComplete = true
        showBiometricAuth = true  // Show unified 2FA panel after animation
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (!animationComplete) {
            // Show animation
            Surface(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(32.dp)
                    .wrapContentSize(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Verification animation
                    VerificationAnimationLottie(modifier = Modifier.size(100.dp))

                    // Status text
                    Text(
                        text = "Verifying payment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CelestialDarkNavy
                    )

                    Text(
                        text = "Please wait...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Unified 2FA Panel - Single page with both biometric and face stages
        if (showBiometricAuth) {
            UnifiedTwoFactorAuthPanel(
                onSuccess = { 
                    showBiometricAuth = false
                    onVerificationComplete()
                },
                onCancel = { 
                    showBiometricAuth = false
                    onCancelTransaction()
                }
            )
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
