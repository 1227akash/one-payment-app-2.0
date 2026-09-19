package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.OneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlannerScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val amountInput by viewModel.plannerAmount.collectAsStateWithLifecycle()
    val planResult by viewModel.plannerResult.collectAsStateWithLifecycle()
    val planRecipient by viewModel.planRecipient.collectAsStateWithLifecycle()
    val planNote by viewModel.planNote.collectAsStateWithLifecycle()
    val planExecutionResult by viewModel.planExecutionResult.collectAsStateWithLifecycle()
    val isPlanExecuting by viewModel.isPlanExecuting.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.planner_title),
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
            // Regulatory Compliance Banner
            item {
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Statutory Disclaimer",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Regulatory Compliance Notice",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.planner_disclaimer),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF78350F),
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // Input Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Total Payment Amount",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { viewModel.updatePlannerAmount(it.filter { ch -> ch.isDigit() }) },
                            prefix = { Text("₹ ", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("planner_amount_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset quick buttons
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf("3000", "5000", "10000", "25000", "50000")) { preset ->
                                FilterChip(
                                    selected = amountInput == preset,
                                    onClick = { viewModel.updatePlannerAmount(preset) },
                                    label = { Text("₹$preset", fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Planned Splits Section
            if (planResult != null) {
                val plan = planResult!!

                // Execution Summary Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Execution Summary",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Scheduled Amount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹%,.2f".format(plan.totalAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recommended Tranches",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${plan.splits.size} Payments",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Average Tranche Size",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹%,.2f".format(plan.totalAmount / plan.splits.size.coerceAtLeast(1)),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // Target Beneficiary / Recipient Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Beneficiary Details",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedTextField(
                                value = planRecipient,
                                onValueChange = { viewModel.updatePlanRecipient(it) },
                                label = { Text(stringResource(R.string.plan_recipient_label)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("plan_recipient_input")
                            )
                            OutlinedTextField(
                                value = planNote,
                                onValueChange = { viewModel.updatePlanNote(it) },
                                label = { Text(stringResource(R.string.plan_note_label)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("plan_note_input")
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planned Transaction Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = BrandPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${plan.splits.size} Tranches",
                                color = BrandPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(plan.splits) { split ->
                    ElevatedCard(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_tranche_${split.trancheIndex}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text(
                                        text = "#${split.trancheIndex}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = split.bankAccount?.bankName ?: "Primary Bank Account",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = split.bankAccount?.let { "${it.maskedAccountNumber} • ${it.ifscPrefix}" } ?: "Active Account",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = split.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "₹%,.2f".format(split.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                Surface(
                                    color = BrandAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Tranche ${split.trancheIndex}",
                                        color = BrandAccent,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // In-One-Go Action Section
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Single-PIN Multi-Account Settlement",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandPrimary
                                )
                            }
                            Text(
                                text = "Authorize all ${plan.splits.size} tranches in one go. Enter your 6-digit Payment Security Code only once to execute and debit all transactions across your designated banks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                            Button(
                                onClick = {
                                    enteredPin = ""
                                    pinError = null
                                    showPinDialog = true
                                },
                                enabled = !isPlanExecuting && plan.splits.isNotEmpty() && plan.totalAmount > 0.0,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("execute_plan_button")
                            ) {
                                if (isPlanExecuting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Executing all parts in one go...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.pay_all_splits_button),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                // Summary Note
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = plan.providerNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Single PIN Authorization Dialog (PIN must be entered ONLY ONCE)
    if (showPinDialog && planResult != null) {
        val plan = planResult!!
        AlertDialog(
            onDismissRequest = {
                if (!isPlanExecuting) showPinDialog = false
            },
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Code",
                        tint = BrandPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.single_pin_auth_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.single_pin_auth_desc, plan.splits.size),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Settling ₹%,.2f across ${plan.splits.size} tranches".format(plan.totalAmount),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary
                            )
                            plan.splits.forEach { sp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${sp.trancheIndex} ${sp.bankAccount?.bankName ?: "Bank"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹%,.2f".format(sp.amount),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                enteredPin = it
                                pinError = null
                            }
                        },
                        label = { Text(stringResource(R.string.enter_payment_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError != null,
                        supportingText = {
                            if (pinError != null) {
                                Text(pinError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("PIN entered once for all transactions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_single_pin_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(12.dp))
                            Text(stringResource(R.string.pin_encrypted_tag), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin.length != 6) {
                            pinError = "PIN must be exactly 6 digits"
                            return@Button
                        }
                        viewModel.executeSmartPlanInOneGo(
                            enteredPin = enteredPin,
                            onSuccess = {
                                showPinDialog = false
                            },
                            onError = { err ->
                                pinError = err
                            }
                        )
                    },
                    enabled = enteredPin.length == 6 && !isPlanExecuting,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier.testTag("confirm_planner_pin_button")
                ) {
                    if (isPlanExecuting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Pay All Parts Now", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPinDialog = false },
                    enabled = !isPlanExecuting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Dialog when All Parts are Paid in One Go
    if (planExecutionResult != null) {
        val result = planExecutionResult!!
        AlertDialog(
            onDismissRequest = { viewModel.clearPlanExecutionResult() },
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(StatusSuccess.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = StatusSuccess,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.plan_paid_success_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.plan_paid_success_desc, result.tranches.size),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Paid in One Go", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "₹%,.2f".format(result.totalAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusSuccess)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Beneficiary", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(result.recipient, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            result.tranches.forEach { tranche ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Part #${tranche.trancheIndex} • ${tranche.bankName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            "₹%,.2f".format(tranche.amount),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "UTR: ${tranche.utr}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = StatusSuccess.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                stringResource(R.string.tranche_paid_status),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = StatusSuccess,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StatusSuccess.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                            Text("1 PIN Authorized • Hardware Encrypted", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = StatusSuccess)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearPlanExecutionResult() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("done_planner_success_button")
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
