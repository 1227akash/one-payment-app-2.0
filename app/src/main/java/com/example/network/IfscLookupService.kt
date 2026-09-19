package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class BankBranchInfo(
    val bankName: String,
    val ifsc: String,
    val branch: String,
    val city: String,
    val state: String,
    val address: String,
    val micr: String? = null,
    val isUpiSupported: Boolean = true,
    val isNeftSupported: Boolean = true,
    val isImpsSupported: Boolean = true
)

object IfscLookupService {

    /**
     * Queries the RBI-backed IFSC directory over the internet to fetch real bank details.
     * Returns BankBranchInfo if found, or null/error if invalid IFSC or offline.
     */
    suspend fun lookupIfsc(ifscCode: String): Result<BankBranchInfo> = withContext(Dispatchers.IO) {
        val cleanIfsc = ifscCode.trim().uppercase()
        if (cleanIfsc.length != 11) {
            return@withContext Result.failure(IllegalArgumentException("IFSC code must be exactly 11 characters"))
        }

        try {
            val url = URL("https://ifsc.razorpay.com/$cleanIfsc")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; OneApp/1.0)")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseText = reader.use { it.readText() }
                connection.disconnect()

                val json = JSONObject(responseText)
                val bankName = json.optString("BANK", "Bank")
                val branch = json.optString("BRANCH", "")
                val city = json.optString("CENTRE", json.optString("CITY", ""))
                val state = json.optString("STATE", "")
                val address = json.optString("ADDRESS", "")
                val micr = json.optString("MICR", null)
                val upi = json.optBoolean("UPI", true)
                val neft = json.optBoolean("NEFT", true)
                val imps = json.optBoolean("IMPS", true)

                Result.success(
                    BankBranchInfo(
                        bankName = bankName,
                        ifsc = cleanIfsc,
                        branch = branch,
                        city = city,
                        state = state,
                        address = address,
                        micr = micr,
                        isUpiSupported = upi,
                        isNeftSupported = neft,
                        isImpsSupported = imps
                    )
                )
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                connection.disconnect()
                Result.failure(IllegalArgumentException("IFSC code not found in official RBI registry. Please check and re-enter."))
            } else {
                connection.disconnect()
                Result.failure(Exception("HTTP $responseCode received while verifying IFSC."))
            }
        } catch (e: Exception) {
            // Fallback for offline or connection issues
            Result.failure(e)
        }
    }
}
