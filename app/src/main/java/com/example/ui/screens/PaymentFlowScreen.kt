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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.PaymentExecutionResult
import com.example.model.BankAccount
import com.example.model.PaymentMethod
import com.example.model.Transaction
import com.example.ui.components.BankAvatar
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusUnknown
import com.example.ui.viewmodel.OneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFlowScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit,
    onFinishPayment: () -> Unit
) {
    val draft by viewModel.paymentDraft.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val defaultAccount by viewModel.defaultAccount.collectAsStateWithLifecycle()

    var showAuthSheet by remember { mutableStateOf(false) }
    var simulateFailure by remember { mutableStateOf(false) }
    var simulateUncertainty by remember { mutableStateOf(false) }

    val activeAccount = draft.selectedAccount ?: defaultAccount ?: bankAccounts.firstOrNull()

    // If an execution result is present, display Result Screen
    if (draft.executionResult != null) {
        PaymentResultScreen(
            result = draft.executionResult!!,
            onDone = {
                viewModel.resetPaymentDraft()
                onFinishPayment()
            },
            onRetry = {
                viewModel.resetPaymentDraft()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.review_payment),
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // STEP 1: Recipient Verification Card
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recipient_verification_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recipient Information",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!draft.isVerified) {
                        OutlinedTextField(
                            value = draft.recipientIdentifier,
                            onValueChange = { viewModel.setRecipientIdentifier(it) },
                            label = {
                                Text(
                                    when (draft.method) {
                                        PaymentMethod.UPI_ID -> "Enter UPI ID (e.g. name@okhdfcbank)"
                                        PaymentMethod.MOBILE -> "Enter 10-digit Mobile Number"
                                        PaymentMethod.BANK_IFSC -> "Account Number|IFSC Code"
                                        PaymentMethod.QR -> "UPI Payment Address"
                                    }
                                )
                            },
                            singleLine = true,
                            isError = draft.verificationError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipient_input_field")
                        )

                        if (draft.verificationError != null) {
                            Text(
                                text = draft.verificationError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.verifyRecipient() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verify_recipient_button")
                        ) {
                            Text("Verify Recipient")
                        }
                    } else {
                        // Verified recipient display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Recipient",
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = draft.recipientName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = draft.recipientIdentifier,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.setRecipientIdentifier("") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Change")
                            }
                        }
                    }
                }
            }

            // STEP 2: Amount & Note Input
            if (draft.isVerified) {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Amount",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = draft.amount,
                            onValueChange = { viewModel.setPaymentAmount(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                            prefix = {
                                Text(
                                    "₹ ",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_amount_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick increment chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(100, 500, 1000, 2000).forEach { inc ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        val current = draft.amount.toDoubleOrNull() ?: 0.0
                                        viewModel.setPaymentAmount((current + inc).toInt().toString())
                                    },
                                    label = { Text("+₹$inc") },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = draft.note,
                            onValueChange = { viewModel.setPaymentNote(it) },
                            label = { Text(stringResource(R.string.add_note)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_note_input")
                        )
                    }
                }

                // STEP 3: "PAY FROM" Bank Account Selection
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAY FROM (Select Bank Account)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${bankAccounts.size} Available",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        bankAccounts.forEach { account ->
                            val isSelected = activeAccount?.id == account.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.selectSourceAccount(account) }
                                    .testTag("select_account_${account.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        BankAvatar(bankCode = account.bankCode, size = 38)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = account.bankName,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                if (account.isDefault) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = BrandAccent.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Default",
                                                            color = BrandAccent,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${account.maskedAccountNumber} • Avail: ₹%,.2f".format(account.balance),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Selected",
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 4: Review Summary
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Payment Summary",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        SummaryRow(label = "Recipient", value = draft.recipientName)
                        SummaryRow(label = "Paying From", value = activeAccount?.bankName ?: "-")
                        SummaryRow(label = "Amount", value = "₹${draft.amount.ifEmpty { "0" }}")
                        SummaryRow(label = "UPI / Banking Fee", value = "₹0.00 (Standard UPI)")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        SummaryRow(
                            label = "Total Payable",
                            value = "₹${draft.amount.ifEmpty { "0" }}",
                            isBold = true
                        )
                    }
                }

                // Testing & Simulation Controls
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Simulator Controls (Resilience Verification)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Simulate Bank Authorization Failure", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = simulateFailure,
                                onCheckedChange = { simulateFailure = it; if (it) simulateUncertainty = false }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Simulate Status Uncertainty (Resilience test)", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = simulateUncertainty,
                                onCheckedChange = { simulateUncertainty = it; if (it) simulateFailure = false }
                            )
                        }
                    }
                }

                // Proceed to Authorize Button
                val canProceed = (draft.amount.toDoubleOrNull() ?: 0.0) > 0.0 && activeAccount != null
                Button(
                    onClick = { showAuthSheet = true },
                    enabled = canProceed,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("confirm_and_pay_button")
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay ₹${draft.amount.ifEmpty { "0" }}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Official UPI Authorization Bottom Sheet
    if (showAuthSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAuthSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Security",
                        tint = BrandPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.official_auth_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Authorize transfer of ₹${draft.amount} from ${activeAccount?.bankName} (${activeAccount?.maskedAccountNumber}) to ${draft.recipientName}.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.official_auth_dialog_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (draft.isAuthorizing) {
                    CircularProgressIndicator()
                    Text("Authorizing via NPCI gateway…", style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(
                        onClick = {
                            viewModel.executePayment(
                                forceSimulatedFailure = simulateFailure,
                                forceSimulatedUncertainty = simulateUncertainty
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("authorise_payment_button")
                    ) {
                        Text("Authorize with Screen Lock / PIN")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PaymentResultScreen(
    result: PaymentExecutionResult,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (result) {
                is PaymentExecutionResult.Success -> {
                    val tx = result.transaction
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StatusSuccess.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = StatusSuccess,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Payment Successful!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹%,.2f".format(tx.amount),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paid to ${tx.recipientOrSenderName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryRow(label = "UPI Ref / UTR", value = tx.utrReference)
                            SummaryRow(label = "Debited From", value = "${tx.sourceBankName} (${tx.sourceAccountMasked})")
                            SummaryRow(label = "Payment Mode", value = tx.method.displayName)
                            if (tx.note.isNotEmpty()) {
                                SummaryRow(label = "Note", value = tx.note)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("payment_result_done_button")
                    ) {
                        Text("Done")
                    }
                }

                is PaymentExecutionResult.Unknown -> {
                    // Safe status handling requirement
                    val tx = result.transaction
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StatusUnknown.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Unknown Status",
                            tint = StatusUnknown,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Verification Required",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusUnknown
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = StatusUnknown.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryRow(label = "Tracking UTR", value = tx.utrReference)
                            SummaryRow(label = "Amount", value = "₹${tx.amount}")
                            SummaryRow(label = "Advice", value = "Do NOT repay immediately")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Go to Transaction History")
                    }
                }

                is PaymentExecutionResult.Failed -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StatusFailed.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failed",
                            tint = StatusFailed,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Payment Failed",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusFailed
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.reason,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Try Again with Another Account")
                    }
                }

                is PaymentExecutionResult.DuplicateWarning -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StatusUnknown.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Duplicate Warning",
                            tint = StatusUnknown,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Duplicate Payment Protection",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.message,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("Acknowledge & Close")
                    }
                }

                is PaymentExecutionResult.RateLimitWarning -> {
                    Text(
                        text = "Rate Limit Exceeded",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result.message, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }

                else -> {}
            }
        }
    }
}
