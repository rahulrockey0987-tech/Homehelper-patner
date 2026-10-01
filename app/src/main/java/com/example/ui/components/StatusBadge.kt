package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusPendingYellow
import com.example.ui.theme.StatusVerifiedBlue

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor, label) = when (status.uppercase()) {
        "VERIFIED" -> Quadruple(
            Color(0xFFE0F2FE),
            Color(0xFF0369A1),
            StatusVerifiedBlue,
            "VERIFIED PRO"
        )
        "UNDER_REVIEW" -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            StatusPendingYellow,
            "UNDER REVIEW"
        )
        "REJECTED" -> Quadruple(
            Color(0xFFFEE2E2),
            Color(0xFF991B1B),
            StatusErrorRed,
            "REJECTED"
        )
        "NOT_STARTED", "DRAFT" -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            Color(0xFF64748B),
            "KYC PENDING"
        )
        "ONLINE" -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            StatusOnlineGreen,
            "ONLINE"
        )
        "OFFLINE" -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            Color(0xFF64748B),
            "OFFLINE"
        )
        "ASSIGNED" -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            StatusPendingYellow,
            "NEW REQUEST"
        )
        "ACCEPTED" -> Quadruple(
            Color(0xFFE0E7FF),
            Color(0xFF3730A3),
            Color(0xFF4F46E5),
            "CONFIRMED"
        )
        "EN_ROUTE" -> Quadruple(
            Color(0xFFE0F2FE),
            Color(0xFF0369A1),
            Color(0xFF0284C7),
            "ON THE WAY"
        )
        "ARRIVED" -> Quadruple(
            Color(0xFFFCE7F3),
            Color(0xFF9D174D),
            Color(0xFFDB2777),
            "ARRIVED"
        )
        "OTP_VERIFIED" -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF15803D),
            StatusOnlineGreen,
            "OTP VERIFIED"
        )
        "SERVICE_STARTED" -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            StatusOnlineGreen,
            "IN PROGRESS"
        )
        "COMPLETED" -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            StatusOnlineGreen,
            "COMPLETED"
        )
        "PAID" -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            StatusOnlineGreen,
            "PAID"
        )
        "PROCESSING" -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            StatusPendingYellow,
            "PROCESSING"
        )
        else -> Quadruple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            Color(0xFF64748B),
            status
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(0.5.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = " $label",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
