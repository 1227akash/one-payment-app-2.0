package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentStatus
import com.example.ui.theme.StatusFailed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRefunded
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusUnknown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun StatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        PaymentStatus.SUCCESS -> Triple(StatusSuccess.copy(alpha = 0.15f), StatusSuccess, Icons.Default.CheckCircle)
        PaymentStatus.PENDING -> Triple(StatusPending.copy(alpha = 0.15f), StatusPending, Icons.Default.HourglassEmpty)
        PaymentStatus.FAILED -> Triple(StatusFailed.copy(alpha = 0.15f), StatusFailed, Icons.Default.Error)
        PaymentStatus.CANCELLED -> Triple(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, Icons.Default.Error)
        PaymentStatus.REFUNDED -> Triple(StatusRefunded.copy(alpha = 0.15f), StatusRefunded, Icons.Default.Refresh)
        PaymentStatus.UNKNOWN -> Triple(StatusUnknown.copy(alpha = 0.15f), StatusUnknown, Icons.Default.Help)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.label,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.label,
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun BankAvatar(bankCode: String, modifier: Modifier = Modifier, size: Int = 44) {
    val color = when (bankCode.uppercase()) {
        "HDFC" -> Color(0xFF004C8F)
        "SBI" -> Color(0xFF280071)
        "ICICI" -> Color(0xFFB8281F)
        "AXIS" -> Color(0xFF861F41)
        "KOTAK" -> Color(0xFFED1C24)
        "PNB" -> Color(0xFFA20C32)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "Bank logo for $bankCode",
            tint = Color.White,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}
