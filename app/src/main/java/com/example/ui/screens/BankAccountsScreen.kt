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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BankAccount
import com.example.ui.components.BankAvatar
import com.example.ui.components.OneTopAppBar
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.viewmodel.OneViewModel

data class AvailableBankOption(val name: String, val code: String, val ifscPrefix: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val bankAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    var showAddAccountSheet by remember { mutableStateOf(false) }

    val popularBanks = listOf(
        AvailableBankOption("Axis Bank", "AXIS", "UTIB0001001"),
        AvailableBankOption("Kotak Mahindra Bank", "KOTAK", "KKBK0002002"),
        AvailableBankOption("Punjab National Bank", "PNB", "PUNB0003003"),
        AvailableBankOption("Bank of Baroda", "BOB", "BARB0004004"),
        AvailableBankOption("IndusInd Bank", "INDUS", "INDB0005005"),
        AvailableBankOption("Canara Bank", "CANARA", "CNRB0006006"),
        AvailableBankOption("Union Bank of India", "UNION", "UBIN0007007")
    )

    var selectedBankToAdd by remember { mutableStateOf<AvailableBankOption?>(null) }
    var inputAccountNumber by remember { mutableStateOf("") }
    var inputIfsc by remember { mutableStateOf("") }

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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Promise",
                            tint = BrandPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.bank_security_promise),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        )
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
                selectedBankToAdd = null
                inputAccountNumber = ""
                inputIfsc = ""
            },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Link a Bank Account",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Select your bank to discover and link accounts associated with your verified mobile number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (selectedBankToAdd == null) {
                    Text(
                        text = "Select Bank",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    popularBanks.forEach { bank ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBankToAdd = bank
                                    inputIfsc = bank.ifscPrefix
                                    inputAccountNumber = (1000000000..9999999999).random().toString()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BankAvatar(bankCode = bank.code, size = 36)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = bank.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "IFSC: ${bank.ifscPrefix}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val bank = selectedBankToAdd!!
                    ElevatedCard(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BankAvatar(bankCode = bank.code, size = 40)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = bank.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Automated discovery via PSP", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = inputAccountNumber,
                                onValueChange = { inputAccountNumber = it },
                                label = { Text("Account Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputIfsc,
                                onValueChange = { inputIfsc = it },
                                label = { Text("IFSC Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.linkNewBankAccount(bank.name, bank.code, inputAccountNumber, inputIfsc)
                                    showAddAccountSheet = false
                                    selectedBankToAdd = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Link Account Securely")
                            }
                        }
                    }
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
