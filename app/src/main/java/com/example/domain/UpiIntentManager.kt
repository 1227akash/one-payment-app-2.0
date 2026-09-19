package com.example.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import com.example.model.PaymentStatus
import java.net.URLEncoder
import java.util.Locale

data class InstalledUpiApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

sealed class UpiIntentResult {
    data class Success(
        val txnId: String?,
        val responseCode: String?,
        val approvalRefNo: String?,
        val rawResponse: String
    ) : UpiIntentResult()

    data class Submitted(
        val txnId: String?,
        val message: String,
        val rawResponse: String
    ) : UpiIntentResult()

    data class Failed(
        val reason: String,
        val responseCode: String?,
        val rawResponse: String?
    ) : UpiIntentResult()

    data object Cancelled : UpiIntentResult()
}

object UpiIntentManager {

    /**
     * Finds all applications installed on this Android device that support handling UPI payments.
     */
    fun getInstalledUpiApps(context: Context): List<InstalledUpiApp> {
        val packageManager = context.packageManager
        val testUri = Uri.parse("upi://pay?pa=test@upi&pn=Test&am=1.00&cu=INR")
        val intent = Intent(Intent.ACTION_VIEW, testUri)

        val resolveInfos = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
        } catch (e: Exception) {
            emptyList()
        }

        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            val appLabel = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            InstalledUpiApp(
                packageName = pkg,
                appName = appLabel,
                icon = icon
            )
        }.distinctBy { it.packageName }
    }

    /**
     * Builds the standard NPCI UPI Intent for real money payment.
     */
    fun createUpiPaymentIntent(
        payeeVpa: String,
        payeeName: String,
        amount: Double,
        transactionNote: String,
        transactionRefId: String,
        targetPackage: String? = null
    ): Intent {
        val encodedName = URLEncoder.encode(payeeName, "UTF-8")
        val encodedNote = URLEncoder.encode(transactionNote.ifBlank { "Payment via One UPI" }, "UTF-8")
        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        val uriString = buildString {
            append("upi://pay?")
            append("pa=").append(payeeVpa)
            append("&pn=").append(encodedName)
            append("&am=").append(formattedAmount)
            append("&cu=INR")
            append("&tn=").append(encodedNote)
            append("&tr=").append(transactionRefId)
        }

        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (!targetPackage.isNullOrBlank()) {
            intent.setPackage(targetPackage)
        }
        return intent
    }

    /**
     * Parses the UPI response data returned in onActivityResult / ActivityResultLauncher.
     */
    fun parseUpiResponse(data: Intent?): UpiIntentResult {
        if (data == null) {
            return UpiIntentResult.Cancelled
        }

        // Standard UPI apps pass response in "response" string extra or intent data
        val rawResponse = data.getStringExtra("response")
            ?: data.dataString
            ?: ""

        if (rawResponse.isBlank()) {
            // Check individual bundle extras
            val statusExtra = data.getStringExtra("Status") ?: data.getStringExtra("status")
            if (statusExtra.equals("SUCCESS", ignoreCase = true)) {
                return UpiIntentResult.Success(
                    txnId = data.getStringExtra("txnId"),
                    responseCode = data.getStringExtra("responseCode"),
                    approvalRefNo = data.getStringExtra("ApprovalRefNo"),
                    rawResponse = "Bundle status: $statusExtra"
                )
            } else if (statusExtra.equals("SUBMITTED", ignoreCase = true)) {
                return UpiIntentResult.Submitted(
                    txnId = data.getStringExtra("txnId"),
                    message = "Payment is processing with the recipient bank.",
                    rawResponse = "Bundle status: $statusExtra"
                )
            } else if (!statusExtra.isNullOrBlank()) {
                return UpiIntentResult.Failed(
                    reason = "Payment declined ($statusExtra)",
                    responseCode = data.getStringExtra("responseCode"),
                    rawResponse = "Bundle status: $statusExtra"
                )
            }
            return UpiIntentResult.Cancelled
        }

        // Parse key-value pairs formatted as "key=value&key2=value2"
        val map = HashMap<String, String>()
        rawResponse.split("&").forEach { pair ->
            val parts = pair.split("=", limit = 2)
            if (parts.size == 2) {
                map[parts[0].trim().lowercase(Locale.US)] = parts[1].trim()
            }
        }

        val status = map["status"]?.uppercase(Locale.US) ?: ""
        val txnId = map["txnid"]
        val responseCode = map["responsecode"]
        val approvalRefNo = map["approvalrefno"]

        return when {
            status == "SUCCESS" -> {
                UpiIntentResult.Success(
                    txnId = txnId,
                    responseCode = responseCode,
                    approvalRefNo = approvalRefNo,
                    rawResponse = rawResponse
                )
            }
            status == "SUBMITTED" || status == "PENDING" -> {
                UpiIntentResult.Submitted(
                    txnId = txnId,
                    message = "Transaction submitted to beneficiary bank.",
                    rawResponse = rawResponse
                )
            }
            status == "FAILURE" || status == "FAILED" -> {
                val reason = when (responseCode) {
                    "ZM" -> "Incorrect UPI PIN entered"
                    "Z6" -> "Insufficient funds in bank account"
                    "U30" -> "Transaction frequency limit exceeded"
                    "U16" -> "Risk threshold exceeded"
                    else -> "Payment declined by bank"
                }
                UpiIntentResult.Failed(
                    reason = reason,
                    responseCode = responseCode,
                    rawResponse = rawResponse
                )
            }
            else -> {
                UpiIntentResult.Cancelled
            }
        }
    }
}
