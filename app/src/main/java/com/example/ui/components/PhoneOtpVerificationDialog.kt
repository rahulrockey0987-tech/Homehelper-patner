package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusOnlineGreen
import kotlinx.coroutines.delay

@Composable
fun PhoneOtpVerificationDialog(
    initialPhone: String,
    onDismiss: () -> Unit,
    onSendOtp: (phone: String, onSent: (String) -> Unit) -> Unit,
    onVerifyOtp: (phone: String, enteredOtp: String, expectedOtp: String, onDone: (Boolean) -> Unit) -> Unit
) {
    var phoneNumber by remember { mutableStateOf(initialPhone) }
    var step by remember { mutableIntStateOf(1) } // 1: Send OTP, 2: Enter OTP, 3: Verified
    var sentOtp by remember { mutableStateOf("") }
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
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone OTP",
                        tint = BrandBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Mobile Number Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Instant 2-Factor SMS Authentication",
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
                        text = "We will send a 6-digit SMS verification code to verify your professional account.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; errorMessage = null },
                        label = { Text("10-Digit Mobile Number") },
                        placeholder = { Text("+91 98490 12845") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("phone_otp_input_number")
                    )
                } else if (step == 2) {
                    Text(
                        text = "Enter the 6-digit SMS code sent to $phoneNumber.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = "SMS OTP Received: $sentOtp",
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
                        label = { Text("6-Digit SMS OTP") },
                        placeholder = { Text("• • • • • •") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("phone_otp_input_code")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!canResend) "Resend in ${countdown}s" else "Didn't receive code?",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (canResend) {
                            TextButton(onClick = {
                                canResend = false
                                onSendOtp(phoneNumber) { newOtp ->
                                    sentOtp = newOtp
                                }
                            }) {
                                Text("Resend SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBluePrimary)
                            }
                        }
                    }
                } else {
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
                        Text("Mobile Number Verified!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("$phoneNumber successfully authenticated via SMS.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        val clean = phoneNumber.replace(Regex("[^0-9]"), "")
                        if (clean.length < 10) {
                            errorMessage = "Please enter valid 10-digit phone number"
                            return@Button
                        }
                        onSendOtp(phoneNumber) { code ->
                            sentOtp = code
                            step = 2
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    modifier = Modifier.testTag("send_phone_otp_button")
                ) {
                    Text("Send SMS OTP")
                }
            } else if (step == 2) {
                Button(
                    onClick = {
                        if (enteredOtp.length != 6) {
                            errorMessage = "Please enter all 6 digits"
                            return@Button
                        }
                        onVerifyOtp(phoneNumber, enteredOtp, sentOtp) { success ->
                            if (success) {
                                step = 3
                            } else {
                                errorMessage = "Incorrect SMS OTP code"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                    enabled = enteredOtp.length == 6,
                    modifier = Modifier.testTag("confirm_phone_otp_button")
                ) {
                    Text("Verify Phone")
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
