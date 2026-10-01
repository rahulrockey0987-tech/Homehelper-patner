package com.example.ui.screens.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusOnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    viewModel: MainViewModel,
    onNavigateToBankDetails: () -> Unit
) {
    val worker by viewModel.workerProfile.collectAsStateWithLifecycle()
    val todayEarnings by viewModel.todayEarnings.collectAsStateWithLifecycle()
    val weeklyEarnings by viewModel.weeklyEarnings.collectAsStateWithLifecycle()
    val pendingBalance by viewModel.pendingPayoutBalance.collectAsStateWithLifecycle()
    val payouts by viewModel.allPayouts.collectAsStateWithLifecycle()

    val grossEarningsThisMonth = weeklyEarnings * 3.8
    val platformFee = grossEarningsThisMonth * 0.15
    val netMonthlyEarnings = grossEarningsThisMonth - platformFee

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            TopAppBar(
                title = { Text("Earnings & Payouts", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }

        // Hero Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandBluePrimary, BrandBlueDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pending Payout Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            Text("Bank: ${worker?.bankAccountNumberMasked ?: "••••6742"}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${pendingBalance.toInt()}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Cleared & ready for automated weekly cycle or instant request",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.requestPayout(pendingBalance) { _, _ -> }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandAmberAccent),
                                shape = RoundedCornerShape(10.dp),
                                enabled = pendingBalance >= 500,
                                modifier = Modifier.weight(1f).height(44.dp).testTag("withdraw_payout_button")
                            ) {
                                Text("Withdraw Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onNavigateToBankDetails,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Bank Settings", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Accounting Breakdown: Gross vs Net
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Accounting Breakdown (This Month)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AccountingRow("Customer Booking Gross", "₹${grossEarningsThisMonth.toInt()}", isBold = false)
                        AccountingRow("Platform Marketplace Fee (15%)", "-₹${platformFee.toInt()}", isDeduction = true)
                        AccountingRow("Insurance & Safety Coverage", "FREE (HomeHelp Pro)", isBold = false)
                        AccountingRow("Incentives & Peak Surges", "+₹1,200", isBold = false, isSurge = true)
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                        AccountingRow("Net Professional Earnings", "₹${(netMonthlyEarnings + 1200).toInt()}", isBold = true, isHighlighted = true)
                    }
                }
            }
        }

        // Payout History Section
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payout History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Auto-transferred to IFSC", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(payouts) { payout ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (payout.status == "PAID") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (payout.status == "PAID") Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (payout.status == "PAID") StatusOnlineGreen else Color(0xFF92400E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Payout to ${payout.bankMasked}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(payout.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Ref: ${payout.referenceId}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${payout.amount.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = BrandBluePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        StatusBadge(status = payout.status)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AccountingRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isDeduction: Boolean = false,
    isSurge: Boolean = false,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) BrandBluePrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = when {
                isDeduction -> Color(0xFFB91C1C)
                isSurge -> StatusOnlineGreen
                isHighlighted -> BrandBluePrimary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
