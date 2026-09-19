package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.BankAvatar
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.OneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveMoneyScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val defaultAccount by viewModel.defaultAccount.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var customAmount by remember { mutableStateOf("") }
    val upiId = defaultAccount?.upiId ?: userProfile?.upiHandle ?: "akash@okhdfcbank"
    val userName = userProfile?.displayName ?: "Akash Sharma"

    val paymentUri = if (customAmount.isNotBlank()) {
        "upi://pay?pa=$upiId&pn=${userName.replace(" ", "%20")}&am=$customAmount&cu=INR"
    } else {
        "upi://pay?pa=$upiId&pn=${userName.replace(" ", "%20")}&cu=INR"
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", text))
        Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun sharePaymentRequest() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Pay $userName via UPI: $upiId \nDirect Link: $paymentUri")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share UPI Payment Link")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.receive_money_title),
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code Card
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("receive_qr_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Scan to pay using any UPI app",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stylized Matrix QR Visual Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.size(170.dp)) {
                            val moduleCount = 19
                            val cellSize = size.width / moduleCount
                            val hashSeed = (paymentUri.hashCode().toLong() and 0xFFFFFF)

                            for (r in 0 until moduleCount) {
                                for (c in 0 until moduleCount) {
                                    // Draw Finder patterns at top-left, top-right, bottom-left
                                    val isFinderTL = r < 7 && c < 7
                                    val isFinderTR = r < 7 && c >= (moduleCount - 7)
                                    val isFinderBL = r >= (moduleCount - 7) && c < 7

                                    val isDark = when {
                                        isFinderTL -> (r == 0 || r == 6 || c == 0 || c == 6) || (r in 2..4 && c in 2..4)
                                        isFinderTR -> (r == 0 || r == 6 || c == (moduleCount - 7) || c == (moduleCount - 1)) || (r in 2..4 && c in (moduleCount - 5)..(moduleCount - 3))
                                        isFinderBL -> (r == (moduleCount - 7) || r == (moduleCount - 1) || c == 0 || c == 6) || (r in (moduleCount - 5)..(moduleCount - 3) && c in 2..4)
                                        else -> ((r * 31 + c * 17 + hashSeed) % 3 == 0L)
                                    }

                                    if (isDark) {
                                        drawRoundRect(
                                            color = Color(0xFF0F172A),
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize * 0.95f, cellSize * 0.95f),
                                            cornerRadius = CornerRadius(2f, 2f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (customAmount.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = BrandAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Requested: ₹$customAmount",
                                color = Color(0xFF065F46),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // UPI ID display box with copy button
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "UPI ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = upiId,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                            }
                            IconButton(onClick = { copyToClipboard(upiId) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy UPI ID",
                                    tint = BrandPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Crediting Bank info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BankAvatar(bankCode = defaultAccount?.bankCode ?: "HDFC", size = 22)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credits to ${defaultAccount?.bankName ?: "HDFC Bank"} (${defaultAccount?.maskedAccountNumber ?: "XXXX 4582"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Set Custom Amount
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.set_custom_amount),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it.filter { ch -> ch.isDigit() } },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Optional amount (e.g. 500)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_amount_input")
                    )
                }
            }

            // Share Button
            Button(
                onClick = { sharePaymentRequest() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("share_payment_request_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.share_payment_request),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
