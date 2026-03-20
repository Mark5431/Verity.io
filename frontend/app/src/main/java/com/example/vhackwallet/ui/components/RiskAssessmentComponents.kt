package com.example.vhackwallet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vhackwallet.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Risk Levels for transfer verification
 */
enum class TransferRiskLevel {
    VERIFIED,        // Known recipient - no alert
    COMMON,          // Regular transfer - no alert
    VERIFY_NEEDED,   // New recipient - amber alert
    PENDING_REVIEW,  // Under system review - holding state
    BLOCKED          // Suspicious - blocked
}

/**
 * Represents risk assessment data
 */
data class TransferRiskData(
    val level: TransferRiskLevel = TransferRiskLevel.VERIFIED,
    val title: String = "",
    val subtitle: String = "",
    val reasons: List<String> = emptyList(),
    val isNewRecipient: Boolean = false,
    val requiresVerification: Boolean = false
)

/**
 * Natural, minimalist risk assessment modal - Superapp style
 * No time estimates, just shows action needed
 */
@Composable
fun RiskAssessmentModal(
    risk: TransferRiskData,
    onDismiss: () -> Unit,
    onVerify: () -> Unit,
    onCancel: () -> Unit
) {
    if (risk.level == TransferRiskLevel.VERIFIED || risk.level == TransferRiskLevel.COMMON) {
        return // No alert needed
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with subtle animation placeholder
                    RiskStatusIcon(level = risk.level)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = risk.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CelestialDarkNavy,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = risk.subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (risk.reasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        RiskReasonsList(reasons = risk.reasons, level = risk.level)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = onVerify,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (risk.level) {
                                    TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFB84D)  // Amber
                                    TransferRiskLevel.PENDING_REVIEW -> Color(0xFF999999)  // Gray
                                    TransferRiskLevel.BLOCKED -> Color(0xFF999999)  // Disabled gray
                                    else -> CelestialBerry
                                }
                            ),
                            enabled = risk.level != TransferRiskLevel.BLOCKED
                        ) {
                            Text(
                                text = when (risk.level) {
                                    TransferRiskLevel.VERIFY_NEEDED -> "Verify"
                                    TransferRiskLevel.PENDING_REVIEW -> "Reviewing..."
                                    TransferRiskLevel.BLOCKED -> "Blocked"
                                    else -> "Proceed"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Risk status icon with animation placeholder
 */
@Composable
private fun RiskStatusIcon(level: TransferRiskLevel) {
    val backgroundColor = when (level) {
        TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFF8E1)      // Warm cream
        TransferRiskLevel.PENDING_REVIEW -> Color(0xFFF5F5F5)     // Light gray
        TransferRiskLevel.BLOCKED -> Color(0xFFFFEBEE)            // Light red
        else -> Color(0xFFE8F5E9)                                  // Light green
    }

    val borderColor = when (level) {
        TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFB84D)      // Amber border
        TransferRiskLevel.PENDING_REVIEW -> Color(0xFFBDBDBD)     // Gray border
        TransferRiskLevel.BLOCKED -> Color(0xFFFF5252)            // Red border
        else -> Color(0xFF4CAF50)                                  // Green border
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .background(backgroundColor, CircleShape)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // TODO: Placeholder for your pending effect animation
        // Replace with your custom animation LottieAnimation when ready
        when (level) {
            TransferRiskLevel.VERIFY_NEEDED -> {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Verify Needed",
                    tint = Color(0xFFFFB84D),
                    modifier = Modifier.size(40.dp)
                )
            }
            TransferRiskLevel.PENDING_REVIEW -> {
                // Placeholder for animation - will use your custom LottieAnimation
                PendingAnimationPlaceholder()
            }
            TransferRiskLevel.BLOCKED -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Blocked",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(40.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Verified",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * Placeholder for your pending effect animation
 * Replace this with LottieAnimation(loading_files.json) when ready
 */
@Composable
private fun PendingAnimationPlaceholder() {
    var rotation by remember { mutableFloatStateOf(0f) }
    val infiniteRotation = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    Icon(
        imageVector = Icons.Default.Autorenew,
        contentDescription = "Checking",
        tint = Color(0xFF999999),
        modifier = Modifier
            .size(40.dp)
            .scale(1.2f)
            .rotate(rotationAngle)
    )
}

/**
 * List of reasons why verification is needed
 * Mimics superapp style - minimal, factual
 */
@Composable
private fun RiskReasonsList(
    reasons: List<String>,
    level: TransferRiskLevel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (level) {
                    TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFF8E1).copy(alpha = 0.6f)
                    TransferRiskLevel.PENDING_REVIEW -> Color(0xFFF5F5F5)
                    else -> Color(0xFFFFEBEE).copy(alpha = 0.6f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        reasons.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = reason,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Inline card for showing verification status
 * Use this in TransferConfirmScreen as a strip instead of modal
 */
@Composable
fun VerificationStatusStrip(
    risk: TransferRiskData,
    onVerify: () -> Unit
) {
    if (risk.level == TransferRiskLevel.VERIFIED || risk.level == TransferRiskLevel.COMMON) {
        return // No alert needed
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = when (risk.level) {
            TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFF8E1)    // Warm cream
            TransferRiskLevel.PENDING_REVIEW -> Color(0xFFF5F5F5)   // Light gray
            else -> Color(0xFFFFEBEE)                                // Light red
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (risk.level) {
                        TransferRiskLevel.VERIFY_NEEDED -> Icons.Default.Info
                        TransferRiskLevel.PENDING_REVIEW -> Icons.Default.Schedule
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when (risk.level) {
                        TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFB84D)
                        TransferRiskLevel.PENDING_REVIEW -> Color(0xFF999999)
                        else -> Color(0xFFFF5252)
                    },
                    modifier = Modifier.size(20.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = risk.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CelestialDarkNavy
                    )
                    if (risk.subtitle.isNotEmpty()) {
                        Text(
                            text = risk.subtitle,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (risk.level != TransferRiskLevel.BLOCKED) {
                Button(
                    onClick = onVerify,
                    modifier = Modifier
                        .height(32.dp)
                        .widthIn(min = 60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (risk.level) {
                            TransferRiskLevel.VERIFY_NEEDED -> Color(0xFFFFB84D)
                            TransferRiskLevel.PENDING_REVIEW -> Color(0xFF999999)
                            else -> CelestialBerry
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    enabled = risk.level != TransferRiskLevel.PENDING_REVIEW
                ) {
                    Text(
                        text = when (risk.level) {
                            TransferRiskLevel.VERIFY_NEEDED -> "Verify"
                            TransferRiskLevel.PENDING_REVIEW -> "..."
                            else -> "OK"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Helper to create natural-looking risk data
 */
object RiskAssessmentDefaults {

    fun verifyNeededRisk(recipientName: String, reasons: List<String> = emptyList()): TransferRiskData {
        return TransferRiskData(
            level = TransferRiskLevel.VERIFY_NEEDED,
            title = "Verify recipient",
            subtitle = "New recipient requires verification",
            reasons = if (reasons.isEmpty()) {
                listOf(
                    "First time sending to this number",
                    "Amount differs from your usual transfer"
                )
            } else reasons,
            isNewRecipient = true,
            requiresVerification = true
        )
    }

    fun pendingReviewRisk(): TransferRiskData {
        return TransferRiskData(
            level = TransferRiskLevel.PENDING_REVIEW,
            title = "Verifying recipient",
            subtitle = "We're checking this recipient",
            reasons = emptyList(),
            requiresVerification = false
        )
    }

    fun blockedRisk(): TransferRiskData {
        return TransferRiskData(
            level = TransferRiskLevel.BLOCKED,
            title = "Cannot complete transfer",
            subtitle = "This recipient has been blocked",
            reasons = listOf("Transaction cannot proceed at this time"),
            requiresVerification = false
        )
    }

    fun verifiedRisk(): TransferRiskData {
        return TransferRiskData(
            level = TransferRiskLevel.VERIFIED,
            title = "Recipient verified",
            subtitle = "Safe to proceed"
        )
    }
}
