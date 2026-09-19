package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.Transaction
import com.example.ui.components.OneTopAppBar
import com.example.ui.components.StatusChip
import com.example.ui.viewmodel.HistoryFilterTab
import com.example.ui.viewmodel.OneViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: OneViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val activeTab by viewModel.historyFilterTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()

    var selectedTransactionForReceipt by remember { mutableStateOf<Transaction?>(null) }

    fun shareReceipt(tx: Transaction) {
        val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
        val text = """
            --- ONE Payment Receipt ---
            Status: ${tx.status.name}
            Amount: ₹${tx.amount}
            Party: ${tx.recipientOrSenderName} (${tx.recipientOrSenderUpiId})
            Debited From: ${tx.sourceBankName} (${tx.sourceAccountMasked})
            UTR Ref: ${tx.utrReference}
            Date: $dateFormatted
            Mode: ${tx.method.displayName}
            Note: ${tx.note.ifEmpty { "N/A" }}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Payment Receipt"))
    }

    Scaffold(
        topBar = {
            OneTopAppBar(
                title = stringResource(R.string.history_title),
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.historySearchQuery.value = it },
                placeholder = { Text(stringResource(R.string.search_transactions)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.historySearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("history_search_field")
            )

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(HistoryFilterTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    val label = when (tab) {
                        HistoryFilterTab.ALL -> stringResource(R.string.tab_all)
                        HistoryFilterTab.SENT -> stringResource(R.string.tab_sent)
                        HistoryFilterTab.RECEIVED -> stringResource(R.string.tab_received)
                        HistoryFilterTab.PENDING -> stringResource(R.string.tab_pending)
                        HistoryFilterTab.FAILED -> stringResource(R.string.tab_failed)
                        HistoryFilterTab.REFUNDED -> stringResource(R.string.tab_refunded)
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.historyFilterTab.value = tab },
                        label = { Text(label) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("filter_tab_${tab.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction List
            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_transactions_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onClick = { selectedTransactionForReceipt = tx }
                        )
                    }
                }
            }
        }
    }

    // Receipt Bottom Sheet
    if (selectedTransactionForReceipt != null) {
        val tx = selectedTransactionForReceipt!!
        val dateFormatted = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

        ModalBottomSheet(
            onDismissRequest = { selectedTransactionForReceipt = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    StatusChip(status = tx.status)
                }

                Text(
                    text = "₹%,.2f".format(tx.amount),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.type == "RECEIVED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )

                ElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryRow(label = "Party", value = tx.recipientOrSenderName)
                        SummaryRow(label = "UPI ID / Acc", value = tx.recipientOrSenderUpiId)
                        SummaryRow(label = "Bank Account", value = "${tx.sourceBankName} (${tx.sourceAccountMasked})")
                        SummaryRow(label = "UPI Ref (UTR)", value = tx.utrReference)
                        SummaryRow(label = "Date & Time", value = dateFormatted)
                        SummaryRow(label = "Payment Type", value = "${tx.type} via ${tx.method.displayName}")
                        if (tx.note.isNotBlank()) {
                            SummaryRow(label = "Note", value = tx.note)
                        }
                    }
                }

                Button(
                    onClick = { shareReceipt(tx) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Official Receipt")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
