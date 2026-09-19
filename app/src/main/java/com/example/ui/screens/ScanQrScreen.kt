package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PaymentMethod
import com.example.security.SecurityManager
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.OneViewModel

data class TestQrPreset(
    val title: String,
    val upiUri: String,
    val payeeName: String,
    val amount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit,
    onQrScanned: () -> Unit
) {
    var manualUri by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }

    val testPresets = listOf(
        TestQrPreset(
            title = "Starbucks Cafe",
            payeeName = "Tata Starbucks Pvt Ltd",
            amount = "250",
            upiUri = "upi://pay?pa=starbucks@icici&pn=Tata%20Starbucks%20Pvt%20Ltd&am=250&cu=INR"
        ),
        TestQrPreset(
            title = "Blue Tokai Coffee",
            payeeName = "Blue Tokai Coffee Roasters",
            amount = "420",
            upiUri = "upi://pay?pa=bluetokai@icici&pn=Blue%20Tokai%20Roasters&am=420&cu=INR"
        ),
        TestQrPreset(
            title = "Metro Transit",
            payeeName = "Delhi Metro Express",
            amount = "60",
            upiUri = "upi://pay?pa=metro@paytm&pn=Delhi%20Metro&am=60&cu=INR"
        ),
        TestQrPreset(
            title = "Organic Supermarket",
            payeeName = "Nature Basket Mart",
            amount = "1450",
            upiUri = "upi://pay?pa=mart@axisbank&pn=Nature%20Basket&am=1450&cu=INR"
        )
    )

    fun handleScannedUri(uri: String) {
        val parsed = SecurityManager.parseUpiUri(uri)
        val pa = parsed["pa"] ?: ""
        val pn = parsed["pn"] ?: "Verified Merchant"
        val am = parsed["am"] ?: ""

        viewModel.setPaymentMethod(PaymentMethod.QR)
        viewModel.setRecipientIdentifier(pa)
        if (am.isNotEmpty()) viewModel.setPaymentAmount(am)
        viewModel.verifyRecipient()
        onQrScanned()
    }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.action_scan_pay),
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flashlight toggle",
                            tint = if (flashEnabled) BrandAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
            // Camera / Viewfinder Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, BrandPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .testTag("qr_viewfinder_box")
            ) {
                // Viewfinder scanning reticle
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .border(3.dp, BrandAccent, RoundedCornerShape(16.dp))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Viewfinder",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Align any UPI QR code within frame",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Quick Preset QR Codes for testing
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tap to Scan Test QR Presets",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(testPresets) { preset ->
                        ElevatedCard(
                            onClick = { handleScannedUri(preset.upiUri) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.width(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${preset.amount}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandAccent
                                    )
                                )
                                Text(
                                    text = preset.payeeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Manual UPI URI paste field
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Or Paste Official UPI Payment URI",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualUri,
                        onValueChange = { manualUri = it },
                        label = { Text("upi://pay?pa=...&pn=...&am=...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_upi_uri_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (manualUri.isNotBlank()) {
                                handleScannedUri(manualUri)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Process QR URI")
                    }
                }
            }
        }
    }
}
