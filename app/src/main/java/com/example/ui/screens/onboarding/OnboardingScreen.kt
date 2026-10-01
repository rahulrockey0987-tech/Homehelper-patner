package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AadhaarVerificationDialog
import com.example.ui.components.PanVerificationDialog
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusOnlineGreen

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    initialFullName: String = "Rajesh Verma",
    initialPhone: String = "+91 98491 55210",
    initialEmail: String = "rajesh.v@homehelp.in",
    initialCity: String = "Hyderabad",
    onOnboardingComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 7

    // Step 1: Basic
    var fullName by remember { mutableStateOf(initialFullName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }
    var dob by remember { mutableStateOf("15/08/1992") }

    // Step 2: Pro
    var categories by remember { mutableStateOf("AC Service & Repair, Electrical") }
    var skills by remember { mutableStateOf("Split AC, Inverter AC, Circuit Breaker, Wiring") }
    var experienceYears by remember { mutableIntStateOf(5) }
    var languages by remember { mutableStateOf("English, Hindi, Telugu") }
    var description by remember { mutableStateOf("Experienced certified AC technician with residential and apartment complex expertise.") }

    // Step 3: Areas
    var city by remember { mutableStateOf(initialCity) }
    val selectedAreas = remember { mutableStateListOf("Madhapur", "Hitec City", "Gachibowli", "Kondapur") }

    // Step 4: Documents (Aadhaar & PAN)
    var govtIdType by remember { mutableStateOf("Aadhaar Card") }
    var govtIdNumber by remember { mutableStateOf("5829-4412-8819") }
    var documentUploaded by remember { mutableStateOf(true) }

    var aadhaarNumber by remember { mutableStateOf("582944128921") }
    var isAadhaarVerified by remember { mutableStateOf(false) }
    var showAadhaarDialog by remember { mutableStateOf(false) }

    var panNumber by remember { mutableStateOf("ABCDE1234F") }
    var isPanVerified by remember { mutableStateOf(false) }
    var showPanDialog by remember { mutableStateOf(false) }

    if (showAadhaarDialog) {
        AadhaarVerificationDialog(
            initialAadhaar = aadhaarNumber,
            onDismiss = { showAadhaarDialog = false },
            onSendOtp = { num, onSent ->
                viewModel.sendAadhaarOtp(num) { success, otp ->
                    if (success) onSent(otp)
                }
            },
            onVerifyOtp = { num, entered, expected, onDone ->
                viewModel.verifyAadhaarOtp(num, entered, expected) { success, _ ->
                    if (success) {
                        isAadhaarVerified = true
                        aadhaarNumber = num
                    }
                    onDone(success)
                }
            }
        )
    }

    if (showPanDialog) {
        PanVerificationDialog(
            initialPan = panNumber,
            registeredName = fullName,
            onDismiss = { showPanDialog = false },
            onVerifyPan = { pan, onDone ->
                viewModel.verifyPanCard(pan, fullName) { success, _ ->
                    if (success) {
                        isPanVerified = true
                        panNumber = pan
                    }
                    onDone(success)
                }
            }
        )
    }

    // Step 5: Bank
    var bankName by remember { mutableStateOf("HDFC Bank") }
    var accountHolder by remember { mutableStateOf(initialFullName) }
    var accountNumber by remember { mutableStateOf("50100492815412") }
    var ifscCode by remember { mutableStateOf("HDFC0001234") }

    // Step 6: Availability
    var workingHours by remember { mutableStateOf("09:00 AM – 07:00 PM") }
    var workingDays by remember { mutableStateOf("Mon, Tue, Wed, Thu, Fri, Sat") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = "Pro Onboarding (Step $step of $totalSteps)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { step.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = BrandBluePrimary,
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Previous")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            viewModel.completeOnboarding(
                                name = fullName,
                                phone = phone,
                                email = email,
                                city = city,
                                exp = experienceYears,
                                categories = categories,
                                skills = skills,
                                bankName = bankName,
                                accountNum = accountNumber,
                                ifsc = ifscCode
                            )
                            onOnboardingComplete()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (step == totalSteps) BrandAmberAccent else BrandBluePrimary
                    ),
                    modifier = Modifier.weight(if (step > 1) 1.5f else 1f).height(48.dp).testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (step == totalSteps) "Submit for KYC Review" else "Continue",
                        fontWeight = FontWeight.Bold,
                        color = if (step == totalSteps) Color.Black else Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                1 -> {
                    // Step 1: Basic Info
                    StepHeader(icon = Icons.Default.Person, title = "Step 1: Personal Details", desc = "Legal identity required for background checks.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Legal Name (as on Govt ID)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Primary Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth (DD/MM/YYYY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                2 -> {
                    // Step 2: Professional Info
                    StepHeader(icon = Icons.Default.Work, title = "Step 2: Skills & Trade Experience", desc = "Choose your service categories and certifications.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = categories,
                        onValueChange = { categories = it },
                        label = { Text("Service Categories") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Specific Technical Skills") },
                        placeholder = { Text("e.g. Jet Pump Cleaning, Gas Leak Detection") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = languages,
                        onValueChange = { languages = it },
                        label = { Text("Languages Spoken") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Professional Bio") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                3 -> {
                    // Step 3: Service Area
                    StepHeader(icon = Icons.Default.LocationOn, title = "Step 3: Service Zones", desc = "Select localities where you can reliably travel to jobs.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Selected Operating Zones:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val zoneOptions = listOf("Madhapur", "Hitec City", "Gachibowli", "Kondapur", "Kukatpally", "Jubilee Hills", "Banjara Hills")
                    zoneOptions.forEach { zone ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (selectedAreas.contains(zone)) selectedAreas.remove(zone)
                                    else selectedAreas.add(zone)
                                }
                        ) {
                            Checkbox(
                                checked = selectedAreas.contains(zone),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedAreas.add(zone)
                                    else selectedAreas.remove(zone)
                                }
                            )
                            Text(zone, fontSize = 14.sp)
                        }
                    }
                }
                4 -> {
                    // Step 4: Documents & KYC
                    StepHeader(icon = Icons.Default.Description, title = "Step 4: Identity & KYC Documents", desc = "Mandatory for platform background verification.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = govtIdType,
                        onValueChange = { govtIdType = it },
                        label = { Text("Government ID Document Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = govtIdNumber,
                        onValueChange = { govtIdNumber = it },
                        label = { Text("Govt ID Number (Aadhaar / Voter / PAN)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Govt ID Front & Back Photo Attached", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("aadhaar_card_scan.pdf (1.8 MB - Verified format)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusOnlineGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ready for admin review", fontSize = 12.sp, color = StatusOnlineGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                5 -> {
                    // Step 5: Bank Details
                    StepHeader(icon = Icons.Default.AccountBalance, title = "Step 5: Bank Details for Payouts", desc = "All weekly earnings are deposited into this account.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        label = { Text("Account Holder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Bank Account Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { ifscCode = it },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                6 -> {
                    // Step 6: Availability
                    StepHeader(icon = Icons.Default.CalendarMonth, title = "Step 6: Working Hours & Days", desc = "Specify your regular schedule when you are available.")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = workingDays,
                        onValueChange = { workingDays = it },
                        label = { Text("Active Working Days") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = { workingHours = it },
                        label = { Text("Standard Working Hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                7 -> {
                    // Step 7: Review & Submit
                    StepHeader(icon = Icons.Default.Send, title = "Step 7: Verification Agreement", desc = "Submit your profile for HomeHelp Operations Review.")
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Summary Before Submission", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            SummaryRow("Applicant:", fullName)
                            SummaryRow("Phone:", phone)
                            SummaryRow("Trade Category:", categories)
                            SummaryRow("City & Zones:", "$city (${selectedAreas.size} zones)")
                            SummaryRow("ID Type:", govtIdType)
                            SummaryRow("Bank Account:", "••••••••${accountNumber.takeLast(4)}")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "CRITICAL BUSINESS RULE: Upon submitting, your status will become UNDER_REVIEW. You cannot go online or accept jobs until the HomeHelp admin operations team approves your background check.",
                                fontSize = 11.sp,
                                color = BrandBluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(BrandBluePrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
