package com.example.vhackwallet.ui.transfer

enum class RiskLevel {
    NONE, // No alert (Known recipients, low amount)
    LOW,  // Scam notice alert (Unknown recipients, low amount)
    MEDIUM, // Fraud alert (Unknown > 100, Known > 1000)
    HIGH  // Security Risk (Unknown > 5000)
}

enum class ProtectionMode {
    NONE, REVIEW, BLOCK
}

data class TransferRisk(
    val level: RiskLevel,
    val title: String,
    val message: String,
    val isNewRecipient: Boolean,
    val isHighValue: Boolean,
    val isSuspicious: Boolean,
    val protectionMode: ProtectionMode = ProtectionMode.NONE
)

object FraudProtection {
    private val knownRecipients = setOf("Sarah Jenkins", "Michael Chen", "Ahmad Razak", "Lim Wei Han", "Jessica Tan")
    
    fun evaluateRisk(recipientName: String, amountString: String): TransferRisk {
        val isKnown = knownRecipients.contains(recipientName)
        
        // Unified workflow: no amount-based separation
        return if (!isKnown) {
            // Unknown Recipient - Same flow regardless of amount
            TransferRisk(
                level = RiskLevel.LOW,
                title = "New Recipient",
                message = "This is your first transfer to this recipient. We'll verify this transaction for your protection.",
                isNewRecipient = true,
                isHighValue = false,
                isSuspicious = false,
                protectionMode = ProtectionMode.REVIEW
            )
        } else {
            // Known Recipient - Direct payment without verification
            TransferRisk(
                level = RiskLevel.NONE,
                title = "Trusted Recipient",
                message = "Regular transfer to a saved contact.",
                isNewRecipient = false,
                isHighValue = false,
                isSuspicious = false,
                protectionMode = ProtectionMode.NONE
            )
        }
    }
}
