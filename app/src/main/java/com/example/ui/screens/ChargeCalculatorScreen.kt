package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.MerchantCategory
import com.example.model.PaymentInstrument
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.OneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeCalculatorScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val amountInput by viewModel.calcAmount.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.calcCategory.collectAsStateWithLifecycle()
    val selectedInstrument by viewModel.calcInstrument.collectAsStateWithLifecycle()
    val calcResult by viewModel.calcResult.collectAsStateWithLifecycle()

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var instrumentDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.calculator_title),
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Regulatory Education Banner
            item {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Regulatory Guidelines",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.calculator_disclaimer),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF1E3A8A),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // Input Parameters Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Transaction Parameters",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        // Amount Input
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { viewModel.calcAmount.value = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Transaction Amount") },
                            prefix = { Text("₹ ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculator_amount_input")
                        )

                        // Merchant Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.select_merchant_category)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("calculator_category_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                MerchantCategory.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.label) },
                                        onClick = {
                                            viewModel.calcCategory.value = category
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Payment Instrument Dropdown
                        ExposedDropdownMenuBox(
                            expanded = instrumentDropdownExpanded,
                            onExpandedChange = { instrumentDropdownExpanded = !instrumentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedInstrument.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.select_payment_instrument)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instrumentDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("calculator_instrument_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = instrumentDropdownExpanded,
                                onDismissRequest = { instrumentDropdownExpanded = false }
                            ) {
                                PaymentInstrument.values().forEach { instrument ->
                                    DropdownMenuItem(
                                        text = { Text(instrument.label) },
                                        onClick = {
                                            viewModel.calcInstrument.value = instrument
                                            instrumentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calculation Breakdown Result Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_result_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Regulatory Charge Breakdown",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = BrandPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "GST 18% Applicable",
                                    color = BrandPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        SummaryRow(label = "Principal Amount", value = "₹%,.2f".format(calcResult.amount))
                        SummaryRow(label = "Applicable MDR Rate", value = "${calcResult.ratePercentage}%", isBold = true)
                        SummaryRow(label = "Base MDR / Interchange Fee", value = "₹%,.2f".format(calcResult.baseCharge))
                        SummaryRow(label = "Statutory GST Rate", value = "${calcResult.gstRatePercentage}% (CGST + SGST)", isBold = true)
                        SummaryRow(label = "GST on Processing Fee", value = "₹%,.2f".format(calcResult.gstAmount))
                        SummaryRow(
                            label = "Statutory Fee Cap",
                            value = calcResult.maxCap?.let { "₹%,.2f".format(it) } ?: "Not Applicable"
                        )
                        SummaryRow(
                            label = "Total Regulatory Deduction",
                            value = "₹%,.2f".format(calcResult.totalFee),
                            isBold = true
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        SummaryRow(
                            label = "Net Merchant Settlement",
                            value = "₹%,.2f".format(calcResult.netMerchantSettlement),
                            isBold = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.customer_payable),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "₹%,.2f".format(calcResult.customerPayableTotal),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandPrimary
                                )
                            )
                        }

                        if (calcResult.totalFee == 0.0) {
                            Surface(
                                color = BrandAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Zero Fee Mandate: Standard Bank-to-Bank UPI is 0% MDR under Section 10A of the PSS Act.",
                                    color = Color(0xFF065F46),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Benchmark Rates Table Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Official MDR & GST Benchmark Schedule",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Statutory rates prescribed by NPCI & RBI guidelines with 18% GST on processing fees:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        RateRow(instrument = "RuPay Credit Card on UPI", mdr = "1.40%", gst = "18%", cap = "₹150 (Txn > ₹2K)")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        RateRow(instrument = "Prepaid Wallets (PPI on UPI)", mdr = "1.10%", gst = "18%", cap = "₹250 (Txn > ₹2K)")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        RateRow(instrument = "Standard Merchant (P2M)", mdr = "0.90%", gst = "18%", cap = "₹100.00")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        RateRow(instrument = "Utility & Telecom Bills", mdr = "0.70%", gst = "18%", cap = "₹75.00")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        RateRow(instrument = "Fuel & Govt Payments", mdr = "0.50%", gst = "18%", cap = "₹50.00")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        RateRow(instrument = "Bank Account (P2P / UPI)", mdr = "0.00%", gst = "0%", cap = "Zero Fee")
                    }
                }
            }
        }
    }
}

@Composable
private fun RateRow(instrument: String, mdr: String, gst: String, cap: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instrument,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cap: $cap",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BrandPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "MDR: $mdr",
                    color = BrandPrimary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Surface(
                color = BrandAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "GST: $gst",
                    color = BrandAccent,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
