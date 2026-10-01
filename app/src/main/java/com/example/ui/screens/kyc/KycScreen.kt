package com.example.ui.screens.kyc

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AadhaarVerificationDialog
import com.example.ui.components.AdminKycSimulatorDialog
import com.example.ui.components.PanVerificationDialog
import com.example.ui.components.PhoneOtpVerificationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusPendingYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val worker by viewModel.workerProfile.collectAsStateWithLifecycle()
    val kycStatus = worker?.kycStatus ?: "VERIFIED"

    var showAdminDialog by remember { mutableStateOf(false) }
    var showAadhaarDialog by remember { mutableStateOf(false) }
    var showPanDialog by remember { mutableStateOf(false) }
    var showPhoneOtpDialog by remember { mutableStateOf(false) }

    if (showAdminDialog) {
        AdminKycSimulatorDialog(
            currentStatus = kycStatus,
            onDismiss = { showAdminDialog = false },
            onApplyStatus = { newStatus, reason ->
                viewModel.updateKycStatus(newStatus, reason)
                showAdminDialog = false
            }
        )
    }

    if (showAadhaarDialog) {
        AadhaarVerificationDialog(
            initialAadhaar = worker?.aadhaarNumberMasked ?: "582944128921",
            onDismiss = { showAadhaarDialog = false },
            onSendOtp = { num, onSent ->
                viewModel.sendAadhaarOtp(num) { success, otp ->
                    if (success) onSent(otp)
                }
            },
            onVerifyOtp = { num, entered, expected, onDone ->
                viewModel.verifyAadhaarOtp(num, entered, expected) { success, _ ->
                    onDone(success)
                }
            }
        )
    }

    if (showPanDialog) {
        PanVerificationDialog(
            initialPan = worker?.panNumber ?: "ABCDE1234F",
            registeredName = worker?.fullName ?: "Rajesh Sharma",
            onDismiss = { showPanDialog = false },
            onVerifyPan = { pan, onDone ->
                viewModel.verifyPanCard(pan, worker?.fullName ?: "Rajesh Sharma") { success, _ ->
                    onDone(success)
                }
            }
        )
    }

    if (showPhoneOtpDialog) {
        PhoneOtpVerificationDialog(
            initialPhone = worker?.phone ?: "+91 98490 12845",
            onDismiss = { showPhoneOtpDialog = false },
            onSendOtp = { ph, onSent ->
                viewModel.sendPhoneOtp(ph) { success, otp ->
                    if (success) onSent(otp)
                }
            },
            onVerifyOtp = { ph, entered, expected, onDone ->
                viewModel.verifyPhoneOtp(ph, entered, expected) { success, _ ->
                    onDone(success)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KYC & Document Verification", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdminDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Simulate Admin Verification", tint = BrandBluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Overall Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (kycStatus) {
                        "VERIFIED" -> Color(0xFFF0FDF4)
                        "REJECTED" -> Color(0xFFFEF2F2)
                        "UNDER_REVIEW" -> Color(0xFFFFFBEB)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
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
                                    .size(36.dp)
                                    .background(
                                        when (kycStatus) {
                                            "VERIFIED" -> StatusOnlineGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> StatusErrorRed.copy(alpha = 0.2f)
                                            else -> StatusPendingYellow.copy(alpha = 0.2f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (kycStatus) {
                                        "VERIFIED" -> Icons.Default.CheckCircle
                                        "REJECTED" -> Icons.Default.Warning
                                        else -> Icons.Default.Shield
                                    },
                                    contentDescription = null,
                                    tint = when (kycStatus) {
                                        "VERIFIED" -> StatusOnlineGreen
                                        "REJECTED" -> StatusErrorRed
                                        else -> StatusPendingYellow
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Compliance & Verification", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = when (kycStatus) {
                                        "VERIFIED" -> "Verified Professional"
                                        "REJECTED" -> "Verification Rejected"
                                        "UNDER_REVIEW" -> "Pending Review"
                                        else -> "Not Submitted"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        StatusBadge(status = kycStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (kycStatus) {
                            "VERIFIED" -> "Your Aadhaar, PAN card, and mobile number are authenticated. You are authorized to receive live customer service jobs."
                            "REJECTED" -> "Notice: ${worker?.kycRejectionReason?.ifBlank { "Document clarity issue." } ?: "Document clarity issue."} Please resubmit your Aadhaar or PAN card."
                            "UNDER_REVIEW" -> "Your documents have been submitted to compliance. You will be notified once active."
                            else -> "Please verify your Aadhaar card and PAN card below to activate your account."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Aadhaar Card Verification Section
            Text("1. Aadhaar Card Verification (UIDAI e-KYC)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandBluePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = worker?.aadhaarNumberMasked ?: "•••• •••• 8921",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (worker?.isAadhaarVerified == true) "UIDAI Authenticated on ${worker?.aadhaarVerifiedDate}" else "Verification Pending",
                                    fontSize = 11.sp,
                                    color = if (worker?.isAadhaarVerified == true) StatusOnlineGreen else StatusPendingYellow
                                )
                            }
                        }

                        if (worker?.isAadhaarVerified == true) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = StatusOnlineGreen, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Proof Attached: Front & Back PDF",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showAadhaarDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("verify_aadhaar_action_button")
                        ) {
                            Text(
                                text = if (worker?.isAadhaarVerified == true) "Re-Verify via OTP" else "Verify via OTP",
                                fontSize = 12.sp,
                                color = BrandBluePrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. PAN Card Verification Section
            Text("2. PAN Card Verification (NSDL / Income Tax)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandAmberAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = BrandAmberAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = worker?.panNumber ?: "ABCDE1234F",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (worker?.isPanVerified == true) "TDS Tax Linked (${worker?.panVerifiedDate})" else "PAN Not Verified",
                                    fontSize = 11.sp,
                                    color = if (worker?.isPanVerified == true) StatusOnlineGreen else StatusPendingYellow
                                )
                            }
                        }

                        if (worker?.isPanVerified == true) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = StatusOnlineGreen, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Name on PAN: ${worker?.fullName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showPanDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("verify_pan_action_button")
                        ) {
                            Text(
                                text = if (worker?.isPanVerified == true) "Update PAN" else "Verify PAN Card",
                                fontSize = 12.sp,
                                color = BrandBluePrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Mobile Number with SMS OTP Section
            Text("3. Mobile Number & 2FA Security", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(StatusOnlineGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = StatusOnlineGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = worker?.phone ?: "+91 98490 12845",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (worker?.isPhoneVerified == true) "SMS 2-Factor OTP Verified" else "Phone Unverified",
                                    fontSize = 11.sp,
                                    color = if (worker?.isPhoneVerified == true) StatusOnlineGreen else StatusErrorRed
                                )
                            }
                        }

                        if (worker?.isPhoneVerified == true) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = StatusOnlineGreen, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Primary dispatch phone",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { showPhoneOtpDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("verify_phone_action_button")
                        ) {
                            Text(
                                text = "Verify with SMS OTP",
                                fontSize = 12.sp,
                                color = BrandBluePrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Simulator note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compliance Standard: Aadhaar e-KYC and PAN matching are cross-referenced before worker activation to prevent marketplace fraud.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
