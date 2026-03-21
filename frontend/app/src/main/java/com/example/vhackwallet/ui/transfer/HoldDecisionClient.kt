package com.example.vhackwallet.ui.transfer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val USE_BACKEND_HOLD = true
private const val FORCE_HOLD_DEMO = true
private const val BACKEND_BASE_URL = "http://10.0.2.2:8000"

data class HoldDecisionUiModel(
    val reasons: List<String>,
    val explanation: String
)

object HoldDecisionClient {
    private val fallbackReasons = listOf(
        "First time sending to this recipient",
        "Higher amount than usual",
        "This device isn't recognized"
    )

    private const val fallbackExplanation =
        "We can’t confirm intent—this pattern can be risky, so we paused it to keep you safe."

    suspend fun resolveHoldDecision(
        recipientName: String,
        amountText: String,
        isNewRecipient: Boolean
    ): HoldDecisionUiModel = withContext(Dispatchers.IO) {
        if (!USE_BACKEND_HOLD) return@withContext HoldDecisionUiModel(fallbackReasons, fallbackExplanation)

        try {
            val response = fetchDecision(recipientName, amountText, isNewRecipient)
            val action = response.optString("action", "").lowercase(Locale.getDefault())

            if (action == "hold") {
                val reasonCodes = response.optJSONArray("reason_codes") ?: JSONArray()
                val reasons = (0 until reasonCodes.length())
                    .map { index -> reasonCodes.optString(index) }
                    .map { toUiReason(it) }
                    .filter { it.isNotBlank() }
                    .ifEmpty { fallbackReasons }

                val explanation = response.optString("explanation", fallbackExplanation)
                    .ifBlank { fallbackExplanation }

                HoldDecisionUiModel(reasons, explanation)
            } else {
                if (FORCE_HOLD_DEMO) {
                    HoldDecisionUiModel(fallbackReasons, fallbackExplanation)
                } else {
                    HoldDecisionUiModel(fallbackReasons, fallbackExplanation)
                }
            }
        } catch (_: Exception) {
            HoldDecisionUiModel(fallbackReasons, fallbackExplanation)
        }
    }

    private fun fetchDecision(
        recipientName: String,
        amountText: String,
        isNewRecipient: Boolean
    ): JSONObject {
        val url = URL("$BACKEND_BASE_URL/api/decision")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 2500
            readTimeout = 2500
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        val payload = buildRequestPayload(recipientName, amountText, isNewRecipient)
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payload.toString())
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        connection.disconnect()

        if (code !in 200..299) {
            throw IllegalStateException("Decision API failed with HTTP $code")
        }

        return JSONObject(body)
    }

    private fun buildRequestPayload(
        recipientName: String,
        amountText: String,
        isNewRecipient: Boolean
    ): JSONObject {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val amount = amountText
            .replace(",", "")
            .toDoubleOrNull()
            ?: 680.0

        return JSONObject().apply {
            put("transaction_id", "tx_demo_${System.currentTimeMillis()}")
            put("user", JSONObject().apply {
                put("user_id", "demo_user_001")
                put("account_age_days", 420)
                put("recent_tx_count", 14)
            })
            put("recipient", JSONObject().apply {
                put("payee_id", "demo_payee_001")
                put("payee_name", recipientName)
                put("is_new_payee", isNewRecipient)
                put("previous_transfer_count", if (isNewRecipient) 0 else 3)
            })
            put("device", JSONObject().apply {
                put("device_id", "android_demo_device")
                put("is_new_device", true)
                put("session_anomaly_count", 1)
            })
            put("transaction", JSONObject().apply {
                put("tx_type", "duitnow_transfer")
                put("amount", amount)
                put("currency", "MYR")
                put("timestamp", timestamp)
            })
            put("journey", JSONObject().apply {
                put("payee_added_this_session", isNewRecipient)
                put("otp_retry_count", 0)
                put("recent_support_contact", false)
            })
        }
    }

    private fun toUiReason(code: String): String {
        return when (code.uppercase(Locale.getDefault())) {
            "NEW_PAYEE" -> "First time sending to this recipient"
            "HIGH_AMOUNT" -> "Higher amount than usual"
            "NEW_DEVICE" -> "This device isn't recognized"
            else -> code.replace('_', ' ').lowercase(Locale.getDefault()).replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }
}
