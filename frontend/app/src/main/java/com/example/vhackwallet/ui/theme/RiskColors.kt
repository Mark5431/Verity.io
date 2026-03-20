package com.example.vhackwallet.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Extended color palette for risk assessment
 * Natural superapp-style colors (no artificial/harsh tones)
 */

// Risk Level Colors
val RiskAmber = Color(0xFFFFB84D)        // Verify needed - caution tone
val RiskGray = Color(0xFF999999)          // Pending review - neutral
val RiskRed = Color(0xFFFF5252)           // Blocked - only for critical

// Background tones - subtle, not overwhelming
val RiskAmberbg = Color(0xFFFFF8E1)       // Warm cream - feels safe
val RiskGraybg = Color(0xFFF5F5F5)        // Light gray - neutral
val RiskRedbg = Color(0xFFFFEBEE)         // Light red - warning only

// Superapp-style verification flow
object VerificationColors {
    val needsVerification = RiskAmber     // Amber for "verify needed"
    val underReview = RiskGray             // Gray for "reviewing"
    val blocked = RiskRed                  // Red only for truly blocked
    val verified = Color(0xFF4CAF50)       // Green for verified
}
