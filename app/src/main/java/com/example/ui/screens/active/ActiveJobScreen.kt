package com.example.ui.screens.active

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.JobBookingEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AdditionalChargeDialog
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.OtpVerificationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveJobScreen(
    jobId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    val jobFlow = remember(jobId) { viewModel.getJobById(jobId) }
    val job: JobBookingEntity? by jobFlow.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

    var showOtpDialog by remember { mutableStateOf(false) }
    var showExtraChargeDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showCompletionModal by remember { mutableStateOf(false) }

    // Completion proof states
    var beforeProofNotes by remember { mutableStateOf("Pre-service initial inspection: Unit functional, slight copper valve leak observed.") }
    var afterProofNotes by remember { mutableStateOf("Replaced gasket, vacuum tested to 500 microns, high-pressure foam cleaned.") }
    var beforePhotoUploaded by remember { mutableStateOf(true) }
    var afterPhotoUploaded by remember { mutableStateOf(true) }

    // Live Service Timer (seconds elapsed)
    var elapsedSeconds by remember { mutableIntStateOf(1420) } // ~23 mins
    LaunchedEffect(job?.status) {
        if (job?.status == "SERVICE_STARTED") {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    if (showOtpDialog && job != null) {
        OtpVerificationDialog(
            expectedOtp = job?.arrivalOtp ?: "729143",
            onDismiss = { showOtpDialog = false },
            onOtpVerified = { otp ->
                viewModel.verifyArrivalOtp(jobId, otp) { success, _ ->
                    if (success) showOtpDialog = false
                }
            }
        )
    }

    if (showExtraChargeDialog) {
        AdditionalChargeDialog(
            onDismiss = { showExtraChargeDialog = false },
            onSubmitCharge = { reason, amount ->
                viewModel.requestAdditionalCharge(jobId, reason, amount) { success ->
                    if (success) showExtraChargeDialog = false
                }
            }
        )
    }

    if (showSosDialog) {
        EmergencySosDialog(
            jobId = jobId,
            onDismiss = { showSosDialog = false },
            onSendSosBeacon = { type, desc ->
                viewModel.triggerEmergencySos(jobId, type, desc) {
                    showSosDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Active Job Execution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Ref: $jobId",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Quick SOS button in active job
                    IconButton(
                        onClick = { showSosDialog = true },
                        modifier = Modifier
                            .background(StatusErrorRed.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                            .testTag("active_job_sos_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "SOS", tint = StatusErrorRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onNavigateToSupport) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = "Support", tint = BrandBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (job != null && job?.status != "COMPLETED") {
                SurfaceBottomActions(
                    status = job?.status ?: "ACCEPTED",
                    onStartNavigation = { viewModel.startNavigation(jobId) },
                    onMarkArrived = { viewModel.markArrived(jobId) },
                    onOpenOtpDialog = { showOtpDialog = true },
                    onStartService = { viewModel.startService(jobId) },
                    onRequestExtraCharge = { showExtraChargeDialog = true },
                    onOpenCompletion = { showCompletionModal = true }
                )
            }
        }
    ) { padding ->
        if (job == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading job details...")
            }
        } else {
            val j = job!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Status Stepper Visual
                LifecycleStepper(currentStatus = j.status)

                Spacer(modifier = Modifier.height(16.dp))

                // Service & Payout Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = j.status)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${(j.estimatedEarnings + if (j.additionalChargeApproved) j.additionalChargeAmount else 0.0).toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = BrandBluePrimary
                                )
                                Text(
                                    text = "Base ₹${j.estimatedEarnings.toInt()}${if (j.additionalChargeApproved) " + ₹${j.additionalChargeAmount.toInt()} Extra" else ""}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = j.serviceTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category: ${j.serviceCategory} • Duration: ~${j.estimatedDurationHours} hrs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Info & Privacy-Masked Contact
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(BrandBluePrimary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandBluePrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = j.customerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Customer (Verified Identity)",
                                        fontSize = 11.sp,
                                        color = StatusOnlineGreen
                                    )
                                }
                            }

                            // Privacy Contact Action (Virtual Number Masking)
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${j.customerPhoneMasked}")
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("call_customer_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Masked", fontSize = 12.sp, color = BrandBluePrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = j.customerAddressMasked,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${j.customerLocality} (Est. ${j.distanceKm} km)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (j.specialInstructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Customer Note: ${j.specialInstructions}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Map & Navigation Route Preview Simulation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Travel Route & Live ETA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("12 mins • 2.4 km", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusOnlineGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual navigation route diagram
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(BrandBluePrimary, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Current Pro Location (Hitec City Hub)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .width(2.dp)
                                        .height(30.dp)
                                        .background(BrandBluePrimary)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(StatusOnlineGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Customer: ${j.customerLocality}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(j.customerLocality + ", Hyderabad")}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open in External Maps", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Live Service Progress / Extra Charge Details
                if (j.status == "SERVICE_STARTED") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StatusOnlineGreen, BrandAmberAccent)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = StatusOnlineGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SERVICE IN PROGRESS", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = StatusOnlineGreen)
                                }
                                val mins = elapsedSeconds / 60
                                val secs = elapsedSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Live time tracking recorded for warranty and service audit.",
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )

                            if (j.additionalChargeApproved) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Extra Work: ${j.additionalChargeReason}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("+₹${j.additionalChargeAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBluePrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Completion Proof Dialog / Bottom Sheet
                if (showCompletionModal) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Service Completion Proof & Notes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Mandatory proof verification required to finalize bill & credit earnings.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = beforeProofNotes,
                                onValueChange = { beforeProofNotes = it },
                                label = { Text("Pre-Service / Diagnosis Notes") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = afterProofNotes,
                                onValueChange = { afterProofNotes = it },
                                label = { Text("Post-Service / Resolution Notes") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Before & After Photos: Attached", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusOnlineGreen, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.completeService(jobId, beforeProofNotes, afterProofNotes) { success ->
                                        if (success) {
                                            showCompletionModal = false
                                            onBack()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusOnlineGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("finalize_completion_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Finalize Service & Release Bill", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun LifecycleStepper(currentStatus: String) {
    val steps = listOf(
        "ACCEPTED" to "Confirmed",
        "EN_ROUTE" to "En Route",
        "ARRIVED" to "Arrived",
        "OTP_VERIFIED" to "OTP OK",
        "SERVICE_STARTED" to "Started",
        "COMPLETED" to "Done"
    )

    val currentIndex = when (currentStatus) {
        "ACCEPTED" -> 0
        "EN_ROUTE" -> 1
        "ARRIVED" -> 2
        "OTP_VERIFIED" -> 3
        "SERVICE_STARTED" -> 4
        "COMPLETED" -> 5
        else -> 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (key, label) ->
                val isDone = index <= currentIndex
                val isCurrent = index == currentIndex

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isDone) StatusOnlineGreen else Color.LightGray.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text("${index + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) BrandBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SurfaceBottomActions(
    status: String,
    onStartNavigation: () -> Unit,
    onMarkArrived: () -> Unit,
    onOpenOtpDialog: () -> Unit,
    onStartService: () -> Unit,
    onRequestExtraCharge: () -> Unit,
    onOpenCompletion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (status) {
                "ACCEPTED" -> {
                    Button(
                        onClick = onStartNavigation,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("action_start_navigation")
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Navigation (En Route)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                "EN_ROUTE" -> {
                    Button(
                        onClick = onMarkArrived,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAmberAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("action_mark_arrived")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I Have Arrived at Customer Location", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    }
                }
                "ARRIVED" -> {
                    Button(
                        onClick = onOpenOtpDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("action_enter_otp")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter Customer Arrival OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                "OTP_VERIFIED" -> {
                    Button(
                        onClick = onStartService,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusOnlineGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("action_start_service")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Service Timer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                "SERVICE_STARTED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestExtraCharge,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("action_extra_charge")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Extra Charge", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenCompletion,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusOnlineGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).height(48.dp).testTag("action_complete_service")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Service", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
