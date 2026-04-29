package com.example.dilo_nilo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dilo_nilo.data.models.LoanStatus
import com.example.dilo_nilo.ui.theme.*

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bg, textColor, label) = when (status) {
        LoanStatus.ACTIVE -> Triple(StatusActiveBg, StatusActiveText, "Active")
        LoanStatus.PENDING -> Triple(StatusPendingBg, StatusPendingText, "Pending")
        LoanStatus.APPROVED -> Triple(StatusActiveBg, StatusActiveText, "Approved")
        LoanStatus.DUE_SOON -> Triple(StatusWarningBg, StatusWarningText, "Due Soon")
        LoanStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueText, "Overdue")
        LoanStatus.COMPLETED -> Triple(StatusCompletedBg, StatusCompletedText, "Completed")
        LoanStatus.REJECTED -> Triple(StatusRejectedBg, StatusRejectedText, "Rejected")
        LoanStatus.COUNTER_OFFERED -> Triple(StatusWarningBg, StatusWarningText, "Counter Offered")
        else -> Triple(StatusPendingBg, StatusPendingText, status.replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
