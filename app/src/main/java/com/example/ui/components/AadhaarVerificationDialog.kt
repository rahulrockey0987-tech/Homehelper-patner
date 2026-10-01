package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAmberAccent
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import kotlinx.coroutines.delay

@Composable
fun AadhaarVerificationDialog(
    initialAadhaar: String = "",
    onDismiss: () -> Unit,
    onSendOtp: (aadhaarNumber: String, onSent: (String) -> Unit) -> Unit,
    onVerifyOtp: (aadhaarNumber: String, enteredOtp: String, expectedOtp: String, onDone: (Boolean) -> Unit) -> Unit
) {
    var rawNumber by remember { mutableStateOf(initialAadhaar.replace(Regex("[^0-9]"), "")) }
    var step by remember { mutableIntStateOf(1) } // 1 = Enter number, 2 = Enter UIDAI OTP, 3 = Verified
    var sentOtpCode by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(step, canResend) {
        if (step == 2 && !canResend) {
            countdown = 30
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            canResend = true
        }
    }

    // Formatted Aadhaar text "XXXX XXXX XXXX"
    val formattedAadhaar = buildString {
        for (i in rawNumber.indices) {
            append(rawNumber[i])
            if ((i == 3 || i == 7) && i != rawNumber.lastIndex) {
                append(" ")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(BrandBluePrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Aadhaar e-KYC",
                        tint = BrandBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Aadhaar Card Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "UIDAI Digital e-KYC Authentication",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column {
                if (step == 1) {
                    Text(
                        text = "Enter your 12-digit Aadhaar number. A secure 6-digit OTP will be sent to your UIDAI registered mobile number.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = formattedAadhaar,
                        onValueChange = { input ->
                            val digitsOnly = input.replace(" ", "")
                            if (digitsOnly.length <= 12 && digitsOnly.all { it.isDigit() }) {
                                rawNumber = digitsOnly
                                errorMessage = null
                            }
                        },
                        label = { Text("12-Digit Aadhaar Number") },
                        placeholder = { Text("XXXX XXXX XXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("aadhaar_number_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrandBluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Aadhaar Card Front & Back Photo", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Document attached: aadhaar_card.pdf", fontSize = 10.sp, color = StatusOnlineGreen)
                            }
                        }
                    }
                } else if (step == 2) {
                    Text(
                        text = "Enter the 6-digit UIDAI verification code sent to mobile linked with Aadhaar (${formattedAadhaar.takeLast(4)}).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sandbox test helper banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "UIDAI SMS OTP: $sentOtpCode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF92400E)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                enteredOtp = it
                                errorMessage = null
                            }
                        },
                        label = { Text("6-Digit Aadhaar OTP") },
                        placeholder = { Text("• • • • • •") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("aadhaar_otp_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!canResend) "Resend in ${countdown}s" else "Didn't receive OTP?",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (canResend) {
                            TextButton(onClick = {
                                canResend = false
                                onSendOtp(rawNumber) { newCode ->
                                    sentOtpCode = newCode
                                }
                            }) {
                                Text("Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBluePrimary)
                            }
                        }
                    }
                } else {
                    // Success state
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFDCFCE7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusOnlineGreen, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Aadhaar Card Verified!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("UIDAI e-KYC completed successfully for •••• •••• ${rawNumber.takeLast(4)}.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = StatusErrorRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (step == 1) {
                Button(
                    onClick = {
                        if (rawNumber.length != 12) {
                            errorMessage = "Please enter complete 12-digit Aadhaar number"
                            return@Button
                        }
                        onSendOtp(rawNumber) { generatedCode ->
                            sentOtpCode = generatedCode
                            step = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    enabled = rawNumber.length == 12,
                    modifier = Modifier.testTag("send_aadhaar_otp_button")
                ) {
                    Text("Get Aadhaar OTP")
                }
            } else if (step == 2) {
                Button(
                    onClick = {
                        if (enteredOtp.length != 6) {
                            errorMessage = "Please enter all 6 digits"
                            return@Button
                        }
                        onVerifyOtp(rawNumber, enteredOtp, sentOtpCode) { success ->
                            if (success) {
                                step = 3
                            } else {
                                errorMessage = "Incorrect Aadhaar OTP code"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    enabled = enteredOtp.length == 6,
                    modifier = Modifier.testTag("confirm_aadhaar_otp_button")
                ) {
                    Text("Verify & Authenticate")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOnlineGreen)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (step != 3) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
