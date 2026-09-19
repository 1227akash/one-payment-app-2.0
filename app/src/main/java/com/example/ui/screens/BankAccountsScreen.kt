package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BankAccount
import com.example.ui.components.BankAvatar
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.IfscLookupUiState
import com.example.ui.viewmodel.OneViewModel

data class AvailableBankOption(val name: String, val code: String, val ifscPrefix: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val ifscState by viewModel.ifscLookupState.collectAsStateWithLifecycle()
    var showAddAccountSheet by remember { mutableStateOf(false) }

    val popularBanks = listOf(
        AvailableBankOption("State Bank of India", "SBI", "SBIN0000456"),
        AvailableBankOption("HDFC Bank", "HDFC", "HDFC0000001"),
        AvailableBankOption("ICICI Bank", "ICICI", "ICIC0000001"),
        AvailableBankOption("Axis Bank", "AXIS", "UTIB0000001"),
        AvailableBankOption("Punjab National Bank", "PNB", "PUNB0000100"),
        AvailableBankOption("Bank of Baroda", "BOB", "BARB0000001"),
        AvailableBankOption("Kotak Mahindra Bank", "KOTAK", "KKBK0000001")
    )

    var inputAccountNumber by remember { mutableStateOf("") }
    var inputIfsc by remember { mutableStateOf("") }
    var inputBalance by remember { mutableStateOf("50000") }
    var isDefaultAccount by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.bank_accounts_title),
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { showAddAccountSheet = true },
                        modifier = Modifier.testTag("add_bank_account_top_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bank")
                    }
                }
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
            // Security Promise Banner
            item {
                Surface(
                    color = BrandPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security Promise",
                                tint = BrandPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Zero-Risk Bank Linking & Security Promise",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = BrandPrimary
                            )
                        }

                        Text(
                            text = stringResource(R.string.bank_security_promise),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandPrimary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(12.dp))
                                    Text("AES-256 Tokenized", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BrandPrimary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text("No Debit Card PINs Stored", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // Linked Accounts Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Linked Accounts (${bankAccounts.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedButton(
                        onClick = { showAddAccountSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_bank_account_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_bank_account))
                    }
                }
            }

            // Bank Account Cards
            items(bankAccounts) { account ->
                BankAccountCardItem(
                    account = account,
                    onSetDefault = { viewModel.setDefaultAccount(account.id) },
                    onRefreshBalance = { viewModel.refreshBalance(account.id) },
                    onUnlink = { viewModel.unlinkAccount(account.id) }
                )
            }
        }
    }

    // Add Bank Account Flow Sheet
    if (showAddAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddAccountSheet = false
                viewModel.resetIfscLookup()
                inputAccountNumber = ""
                inputIfsc = ""
            },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Link Real Bank Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = StatusSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live RBI API",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusSuccess
                            )
                        }
                    }
                }

                Text(
                    text = "Verify your branch via RBI IFSC directory in real-time and link your real bank account for UPI transfers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Select Popular Banks
                Column {
                    Text(
                        text = "Quick Select Popular Bank:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularBanks.take(4).forEach { bank ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (inputIfsc.equals(bank.ifscPrefix, ignoreCase = true)) BrandPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .clickable {
                                        inputIfsc = bank.ifscPrefix
                                        viewModel.lookupIfsc(bank.ifscPrefix)
                                    }
                            ) {
                                Text(
                                    text = bank.code,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (inputIfsc.equals(bank.ifscPrefix, ignoreCase = true)) BrandPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // IFSC Code Input with Live Lookup
                OutlinedTextField(
                    value = inputIfsc,
                    onValueChange = {
                        inputIfsc = it.uppercase()
                        if (inputIfsc.length == 11) {
                            viewModel.lookupIfsc(inputIfsc)
                        }
                    },
                    label = { Text("Bank IFSC Code (11 characters)") },
                    placeholder = { Text("e.g., SBIN0000456, HDFC0000001") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.lookupIfsc(inputIfsc) }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Verify IFSC")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Real-time IFSC Verification Feedback
                when (val state = ifscState) {
                    is IfscLookupUiState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying IFSC with centralized banking directory…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is IfscLookupUiState.Success -> {
                        val branch = state.info
                        ElevatedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = StatusSuccess.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = branch.bankName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSuccess
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Branch: ${branch.branch}, ${branch.city} (${branch.state})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = branch.address,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (branch.isUpiSupported) {
                                        Text(
                                            text = "UPI Supported",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = StatusSuccess,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Text(
                                        text = "• IMPS • RTGS • NEFT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    is IfscLookupUiState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IfscLookupUiState.Idle -> {
                        Text(
                            text = "Tip: Enter your 11-character IFSC or tap a bank above to auto-verify.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Account Number Input
                OutlinedTextField(
                    value = inputAccountNumber,
                    onValueChange = { inputAccountNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Bank Account Number") },
                    placeholder = { Text("e.g., 50100456789123") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Starting Balance (User's real balance)
                OutlinedTextField(
                    value = inputBalance,
                    onValueChange = { inputBalance = it },
                    label = { Text("Available Account Balance (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Default Account Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Set as Primary Payment Account",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Use this account for outgoing real UPI payments by default",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDefaultAccount,
                        onCheckedChange = { isDefaultAccount = it }
                    )
                }

                // Link Bank Account Action
                val canLink = inputAccountNumber.length >= 9 && inputIfsc.length == 11
                Button(
                    onClick = {
                        val verifiedBankName = (ifscState as? IfscLookupUiState.Success)?.info?.bankName
                            ?: popularBanks.find { it.ifscPrefix.equals(inputIfsc, ignoreCase = true) }?.name
                            ?: "Bank (${inputIfsc.take(4)})"
                        val bankCode = inputIfsc.take(4).uppercase()
                        val bal = inputBalance.toDoubleOrNull() ?: 50000.0

                        viewModel.linkRealBankAccount(
                            bankName = verifiedBankName,
                            bankCode = bankCode,
                            rawAccountNumber = inputAccountNumber,
                            ifsc = inputIfsc,
                            customBalance = bal,
                            setAsDefault = isDefaultAccount
                        )
                        showAddAccountSheet = false
                        viewModel.resetIfscLookup()
                        inputAccountNumber = ""
                        inputIfsc = ""
                    },
                    enabled = canLink,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("link_real_bank_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Link Real Bank Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BankAccountCardItem(
    account: BankAccount,
    onSetDefault: () -> Unit,
    onRefreshBalance: () -> Unit,
    onUnlink: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bank_account_card_${account.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BankAvatar(bankCode = account.bankCode, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (account.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = BrandAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.default_account_badge),
                                        color = BrandAccent,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${account.accountType} • ${account.maskedAccountNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "UPI: ${account.upiId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onUnlink) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Unlink Account",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balance: ₹%,.2f".format(account.balance),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Updated just now",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefreshBalance,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Balance",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!account.isDefault) {
                        OutlinedButton(
                            onClick = onSetDefault,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.set_as_default),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
