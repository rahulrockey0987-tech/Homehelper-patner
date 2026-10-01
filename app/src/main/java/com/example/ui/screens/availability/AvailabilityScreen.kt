package com.example.ui.screens.availability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.StatusOnlineGreen

data class DayAvailability(
    val day: String,
    var isEnabled: Boolean,
    val timeSlot: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val schedule = remember {
        mutableStateListOf(
            DayAvailability("Monday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Tuesday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Wednesday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Thursday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Friday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Saturday", true, "09:00 AM – 07:00 PM"),
            DayAvailability("Sunday", false, "Weekly Rest Day (OFF)")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Working Hours & Availability", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "Weekly Work Schedule",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "The platform dispatch engine only assigns customer requests during your active time slots.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            schedule.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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
                        Column {
                            Text(
                                text = item.day,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (item.isEnabled) item.timeSlot else "OFF - Not Scheduled",
                                fontSize = 12.sp,
                                color = if (item.isEnabled) StatusOnlineGreen else Color.Gray
                            )
                        }

                        Switch(
                            checked = item.isEnabled,
                            onCheckedChange = { checked ->
                                schedule[index] = item.copy(isEnabled = checked)
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = BrandBluePrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.emitMessage("Working schedule updated successfully.")
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBluePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Save Availability Schedule", fontWeight = FontWeight.Bold)
            }
        }
    }
}
