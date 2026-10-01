package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.JobBookingEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AdminKycSimulatorDialog
import com.example.ui.components.BrandThemeSelectorDialog
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusPendingYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToActiveJob: (jobId: String) -> Unit,
    onNavigateToJobDetail: (jobId: String) -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToKyc: () -> Unit,
    onNavigateToAvailability: () -> Unit,
    onNavigateToServiceAreas: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val worker by viewModel.workerProfile.collectAsStateWithLifecycle()
    val activeJob by viewModel.activeJob.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val todayEarnings by viewModel.todayEarnings.collectAsStateWithLifecycle()
    val weeklyEarnings by viewModel.weeklyEarnings.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotifications = notifications.count { !it.isRead }

    var showSosDialog by remember { mutableStateOf(false) }
    var showKycSimulator by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    val currentBrandTheme by viewModel.brandTheme.collectAsStateWithLifecycle()

    if (showThemeSelector) {
        BrandThemeSelectorDialog(
            currentTheme = currentBrandTheme,
            onDismiss = { showThemeSelector = false },
            onSelectTheme = { chosenTheme ->
                viewModel.setBrandTheme(chosenTheme)
            }
        )
    }

    if (showSosDialog) {
        EmergencySosDialog(
            jobId = activeJob?.id,
            onDismiss = { showSosDialog = false },
            onSendSosBeacon = { type, desc ->
                viewModel.triggerEmergencySos(activeJob?.id, type, desc) {
                    showSosDialog = false
                }
            }
        )
    }

    if (showKycSimulator && worker != null) {
        AdminKycSimulatorDialog(
            currentStatus = worker?.kycStatus ?: "VERIFIED",
            onDismiss = { showKycSimulator = false },
            onApplyStatus = { newStatus, reason ->
                viewModel.updateKycStatus(newStatus, reason)
                showKycSimulator = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BrandBluePrimary, BrandBlueDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = worker?.fullName?.take(2)?.uppercase() ?: "VR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hello, ${worker?.fullName?.split(" ")?.firstOrNull() ?: "Vikram"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BrandAmberAccent, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = " ${worker?.rating ?: 4.9}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${worker?.city ?: "Hyderabad"} • ${worker?.selectedCategories?.split(",")?.firstOrNull() ?: "AC Service"}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Brand Color Theme Switcher
                        IconButton(
                            onClick = { showThemeSelector = true },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                                .testTag("dashboard_palette_button")
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Theme Color", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Admin KYC tester shortcut
                        IconButton(
                            onClick = { showKycSimulator = true },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Admin Tester", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // SOS Emergency Button
                        IconButton(
                            onClick = { showSosDialog = true },
                            modifier = Modifier
                                .background(StatusErrorRed, CircleShape)
                                .size(36.dp)
                                .testTag("dashboard_sos_button")
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Notifications Icon with badge
                        Box {
                            IconButton(
                                onClick = onNavigateToNotifications,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .size(36.dp)
                                    .testTag("dashboard_notifications_button")
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(BrandAmberAccent, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$unreadNotifications", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Online/Offline & Verification Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (worker?.isOnline == true) StatusOnlineGreen else Color.LightGray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (worker?.isOnline == true) "ONLINE & AVAILABLE" else "OFFLINE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = if (worker?.isOnline == true) "Eligible to receive nearby job requests" else "Switch online to receive bookings",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = worker?.isOnline == true,
                            onCheckedChange = { checked ->
                                viewModel.toggleOnlineStatus(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusOnlineGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("online_offline_switch")
                        )
                    }
                }
            }
        }

        // KYC Warning / Banner if NOT VERIFIED
        item {
            val kyc = worker?.kycStatus ?: "VERIFIED"
            if (kyc != "VERIFIED") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable { onNavigateToKyc() },
                    colors = CardDefaults.cardColors(
                        containerColor = if (kyc == "REJECTED") Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (kyc == "REJECTED") Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (kyc == "REJECTED") StatusErrorRed else StatusPendingYellow,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (kyc == "REJECTED") "KYC Verification Rejected" else "KYC Verification Pending Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (kyc == "REJECTED") StatusErrorRed else Color(0xFF92400E)
                            )
                            Text(
                                text = if (kyc == "REJECTED") "Reason: ${worker?.kycRejectionReason}. Tap to resubmit documents."
                                      else "Your documents are under review by HomeHelp admin. You will be notified once active.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Earnings Metric Cards
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Earnings Overview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "View Details",
                        color = BrandBluePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToEarnings() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Today's Earnings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "₹${todayEarnings.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = BrandBluePrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Net take-home", fontSize = 10.sp, color = StatusOnlineGreen)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Weekly Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "₹${weeklyEarnings.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Next payout: Friday", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Jobs Done", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${worker?.totalCompletedJobs ?: 214}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("100% On-time", fontSize = 10.sp, color = StatusOnlineGreen)
                        }
                    }
                }
            }
        }

        // Active Job Banner (If exists)
        if (activeJob != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(StatusOnlineGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE ON-SITE JOB",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = StatusOnlineGreen,
                                letterSpacing = 1.sp
                            )
                        }
                        StatusBadge(status = activeJob?.status ?: "ACCEPTED")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToActiveJob(activeJob?.id ?: "") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = activeJob?.serviceTitle ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeJob?.customerLocality} (${activeJob?.distanceKm} km away)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled: ${activeJob?.scheduledDate} at ${activeJob?.scheduledTime}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "₹${activeJob?.estimatedEarnings?.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandBluePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onNavigateToActiveJob(activeJob?.id ?: "") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("active_job_action_button")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (activeJob?.status) {
                                        "ACCEPTED" -> "Start Travel Navigation"
                                        "EN_ROUTE" -> "I Have Arrived (Verify OTP)"
                                        "ARRIVED" -> "Enter Customer OTP"
                                        "OTP_VERIFIED" -> "Start Service Timer"
                                        "SERVICE_STARTED" -> "Complete Service & Proof"
                                        else -> "Open Active Job"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pending Job Requests (New Broadcasts)
        if (pendingRequests.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Service Requests (${pendingRequests.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(BrandAmberAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Fast Response Required", fontSize = 11.sp, color = BrandAmberAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(pendingRequests) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BrandBluePrimary.copy(alpha = 0.5f), BrandAmberAccent.copy(alpha = 0.5f))))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusBadge(status = "ASSIGNED")
                            Text("Payout: ₹${job.estimatedEarnings.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BrandBluePrimary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(job.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${job.customerLocality} • ${job.distanceKm} km away", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Time: ${job.scheduledDate}, ${job.scheduledTime} (~${job.estimatedDurationHours} hrs)", fontSize = 12.sp, color = Color.Gray)

                        if (job.specialInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Note: ${job.specialInstructions}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.rejectJob(job.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("reject_job_${job.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = StatusErrorRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Decline", color = StatusErrorRed)
                            }

                            Button(
                                onClick = { viewModel.acceptJob(job.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                                modifier = Modifier.weight(1.5f).height(44.dp).testTag("accept_job_${job.id}")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept Job", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Professional Tools",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Service Areas",
                        subtitle = "8 zones active",
                        icon = Icons.Default.Map,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToServiceAreas
                    )
                    QuickActionCard(
                        title = "Availability",
                        subtitle = "Mon–Sat 9AM-7PM",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAvailability
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "KYC Hub",
                        subtitle = worker?.kycStatus ?: "VERIFIED",
                        icon = Icons.Default.Shield,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToKyc
                    )
                    QuickActionCard(
                        title = "Help & Tickets",
                        subtitle = "24/7 Operations",
                        icon = Icons.Default.HeadsetMic,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSupport
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(BrandBluePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
