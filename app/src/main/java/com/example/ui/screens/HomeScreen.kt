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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.BankAccount
import com.example.model.PaymentMethod
import com.example.model.Transaction
import com.example.ui.components.BankAvatar
import com.example.ui.components.StatusChip
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.OneViewModel
import java.util.Calendar

private fun getTimeBasedGreetingResId(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.good_morning
        in 12..16 -> R.string.good_afternoon
        in 17..21 -> R.string.good_evening
        else -> R.string.good_night
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OneViewModel,
    onNavigateToScanQr: () -> Unit,
    onNavigateToSendMoney: () -> Unit,
    onNavigateToReceiveMoney: () -> Unit,
    onNavigateToBankAccounts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSelectTransaction: (Transaction) -> Unit,
    onQuickPay: (name: String, upiId: String) -> Unit
) {
    val defaultAccount by viewModel.defaultAccount.collectAsStateWithLifecycle()
    val allAccounts by viewModel.bankAccounts.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()

    val displayAccount = defaultAccount ?: allAccounts.firstOrNull()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Greeting & Profile
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Text(
                            text = "${stringResource(getTimeBasedGreetingResId())}, ${userProfile?.displayName?.split(" ")?.firstOrNull() ?: "User"} 👋",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Fresh Installation & End-to-End Encryption Security Card
            if (userProfile?.isFreshInstall == true) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("fresh_install_banner")
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BrandPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.fresh_install_banner_title),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Hardware KeyStore Active • E2EE Armed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrandPrimary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.dismissFreshInstallBanner() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.fresh_install_banner_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandPrimary.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = BrandPrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "AES-256 GCM",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = BrandPrimary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusSuccess.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "NPCI / RBI Standards Verified",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = StatusSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Default Bank Account Card
            item {
                DefaultBankCard(
                    account = displayAccount,
                    totalAccountsCount = allAccounts.size,
                    onManageAccounts = onNavigateToBankAccounts,
                    onRefreshBalance = { displayAccount?.let { viewModel.refreshBalance(it.id) } }
                )
            }

            // Core Action Grid (Large, accessible touch targets >= 48dp)
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = stringResource(R.string.action_scan_pay),
                            subtitle = "Any UPI QR",
                            icon = Icons.Default.QrCodeScanner,
                            color = BrandPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "home_scan_pay_button",
                            onClick = onNavigateToScanQr
                        )
                        ActionCard(
                            title = stringResource(R.string.action_send_money),
                            subtitle = "UPI, Mobile, Bank",
                            icon = Icons.Default.Send,
                            color = BrandSecondary,
                            modifier = Modifier.weight(1f),
                            testTag = "home_send_money_button",
                            onClick = onNavigateToSendMoney
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = stringResource(R.string.action_request_money),
                            subtitle = "Personal QR / Link",
                            icon = Icons.Default.QrCode,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f),
                            testTag = "home_request_money_button",
                            onClick = onNavigateToReceiveMoney
                        )
                        ActionCard(
                            title = stringResource(R.string.action_my_accounts),
                            subtitle = "${allAccounts.size} Linked Banks",
                            icon = Icons.Default.AccountBalance,
                            color = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            testTag = "home_my_accounts_button",
                            onClick = onNavigateToBankAccounts
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = stringResource(R.string.action_history),
                            subtitle = "Statements & UTR",
                            icon = Icons.Default.History,
                            color = Color(0xFF059669),
                            modifier = Modifier.weight(1f),
                            testTag = "home_history_button",
                            onClick = onNavigateToHistory
                        )
                        ActionCard(
                            title = stringResource(R.string.action_calculator),
                            subtitle = "UPI MDR & Charges",
                            icon = Icons.Default.Calculate,
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f),
                            testTag = "home_calculator_button",
                            onClick = onNavigateToCalculator
                        )
                    }

                    ActionCard(
                        title = stringResource(R.string.action_planner),
                        subtitle = "Plan legitimate split schedules across accounts",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFF0D9488),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "home_planner_button",
                        onClick = onNavigateToPlanner
                    )
                }
            }

            // Quick Pay Payees
            item {
                Text(
                    text = "People & Merchants",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                val quickContacts = listOf(
                    Triple("Blue Tokai", "bluetokai@icici", "BT"),
                    Triple("Priya Verma", "priya@oksbi", "PV"),
                    Triple("Starbucks", "starbucks@icici", "SB"),
                    Triple("Rahul Sharma", "rahul@okhdfcbank", "RS"),
                    Triple("Zomato", "zomato@hdfcbank", "ZM")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(quickContacts) { (name, upiId, initials) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onQuickPay(name, upiId) }
                                .padding(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = initials,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = name.split(" ").first(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { onNavigateToHistory() }
                            .padding(4.dp)
                    )
                }
            }

            items(recentTransactions.take(3)) { transaction ->
                TransactionRowItem(
                    transaction = transaction,
                    onClick = { onSelectTransaction(transaction) }
                )
            }
        }
    }
}

@Composable
fun DefaultBankCard(
    account: BankAccount?,
    totalAccountsCount: Int,
    onManageAccounts: () -> Unit,
    onRefreshBalance: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0F766E))
                )
            )
            .testTag("default_bank_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    BankAvatar(bankCode = account?.bankCode ?: "HDFC", size = 38)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account?.bankName ?: "Link Bank Account",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = account?.maskedAccountNumber ?: "No bank account linked",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = BrandAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.default_account_badge),
                        color = BrandAccent,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = if (account != null) "₹%,.2f".format(account.balance) else "₹0.00",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefreshBalance,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("refresh_balance_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check Balance",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onManageAccounts,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("manage_accounts_button")
                    ) {
                        Text(
                            text = "$totalAccountsCount Banks",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("transaction_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                BankAvatar(bankCode = transaction.sourceBankName.take(4), size = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = transaction.recipientOrSenderName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.method.displayName} • ${transaction.sourceBankName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                val isReceived = transaction.type == "RECEIVED"
                val prefix = if (isReceived) "+ " else "- "
                val amountColor = if (isReceived) StatusSuccess else MaterialTheme.colorScheme.onSurface

                Text(
                    text = "$prefix₹%,.2f".format(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    ),
                    maxLines = 1
                )
                StatusChip(status = transaction.status)
            }
        }
    }
}
