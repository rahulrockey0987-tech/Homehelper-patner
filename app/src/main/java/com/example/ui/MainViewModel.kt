package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HomeHelpDatabase
import com.example.data.local.JobBookingEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PayoutEntity
import com.example.data.local.ServiceZoneEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.WorkerEntity
import com.example.data.repository.HomeHelpRepository
import com.example.ui.theme.BrandThemeOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HomeHelpDatabase.getDatabase(application)
    private val repository = HomeHelpRepository(database)

    val workerProfile: StateFlow<WorkerEntity?> = repository.workerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJobs: StateFlow<List<JobBookingEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeJob: StateFlow<JobBookingEntity?> = repository.allJobs.map { list ->
        list.firstOrNull { it.status in listOf("ACCEPTED", "EN_ROUTE", "ARRIVED", "OTP_VERIFIED", "SERVICE_STARTED") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingRequests: StateFlow<List<JobBookingEntity>> = repository.allJobs.map { list ->
        list.filter { it.status == "ASSIGNED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayouts: StateFlow<List<PayoutEntity>> = repository.allPayouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicketEntity>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allZones: StateFlow<List<ServiceZoneEntity>> = repository.allZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Metrics
    val todayEarnings: StateFlow<Double> = repository.allJobs.map { jobs ->
        jobs.filter { it.status == "COMPLETED" && it.scheduledDate == "Today" }
            .sumOf { it.estimatedEarnings + if (it.additionalChargeApproved) it.additionalChargeAmount else 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 620.0)

    val weeklyEarnings: StateFlow<Double> = repository.allJobs.map { jobs ->
        val completedSum = jobs.filter { it.status == "COMPLETED" }
            .sumOf { it.estimatedEarnings + if (it.additionalChargeApproved) it.additionalChargeAmount else 0.0 }
        completedSum + 4230.0 // Add weekly previous completed ledger
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4850.0)

    val pendingPayoutBalance: StateFlow<Double> = repository.allJobs.map { jobs ->
        val completedEarned = jobs.filter { it.status == "COMPLETED" }
            .sumOf { it.estimatedEarnings + if (it.additionalChargeApproved) it.additionalChargeAmount else 0.0 }
        completedEarned + 1230.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1850.0)

    // UI Event Toast/Snackbar message bus
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    private val _brandTheme = MutableStateFlow(BrandThemeOption.INDIGO)
    val brandTheme: StateFlow<BrandThemeOption> = _brandTheme.asStateFlow()

    fun setBrandTheme(theme: BrandThemeOption) {
        _brandTheme.value = theme
        emitMessage("App brand color set to ${theme.title}")
    }

    fun getJobById(jobId: String): kotlinx.coroutines.flow.Flow<JobBookingEntity?> = repository.getJobById(jobId)

    fun emitMessage(msg: String) {
        viewModelScope.launch { _uiMessage.emit(msg) }
    }

    fun toggleOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.setOnlineStatus(isOnline)
            result.onSuccess {
                emitMessage(if (isOnline) "You are now ONLINE. Ready to receive requests." else "You are now OFFLINE.")
            }.onFailure {
                emitMessage("Error: ${it.message}")
            }
        }
    }

    fun updateKycStatus(status: String, reason: String = "") {
        viewModelScope.launch {
            repository.updateKycStatus(status, reason)
            emitMessage("KYC status updated to $status")
        }
    }

    fun sendPhoneOtp(phone: String, onSent: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPhoneOtp(phone)
            result.onSuccess { otp ->
                emitMessage("SMS OTP sent to $phone")
                onSent(true, otp)
            }.onFailure {
                emitMessage(it.message ?: "Failed to send SMS")
                onSent(false, it.message ?: "Error")
            }
        }
    }

    fun verifyPhoneOtp(phone: String, enteredOtp: String, expectedOtp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyPhoneOtp(phone, enteredOtp, expectedOtp)
            result.onSuccess {
                emitMessage("Mobile number verified successfully!")
                onResult(true, "Verified")
            }.onFailure {
                emitMessage(it.message ?: "Invalid OTP")
                onResult(false, it.message ?: "Invalid OTP")
            }
        }
    }

    fun sendAadhaarOtp(aadhaarRaw: String, onSent: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendAadhaarOtp(aadhaarRaw)
            result.onSuccess { otp ->
                emitMessage("UIDAI verification OTP sent to Aadhaar-linked mobile")
                onSent(true, otp)
            }.onFailure {
                emitMessage(it.message ?: "Failed to initiate Aadhaar OTP")
                onSent(false, it.message ?: "Error")
            }
        }
    }

    fun verifyAadhaarOtp(
        aadhaarRaw: String,
        enteredOtp: String,
        expectedOtp: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.verifyAadhaarOtp(aadhaarRaw, enteredOtp, expectedOtp)
            result.onSuccess {
                emitMessage("Aadhaar Card authenticated & verified with UIDAI!")
                onResult(true, "Aadhaar Verified")
            }.onFailure {
                emitMessage(it.message ?: "Aadhaar verification failed")
                onResult(false, it.message ?: "Invalid Aadhaar OTP")
            }
        }
    }

    fun verifyPanCard(panRaw: String, legalName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyPanCard(panRaw, legalName)
            result.onSuccess {
                emitMessage("PAN Card validated with Income Tax Department (NSDL)!")
                onResult(true, "PAN Verified")
            }.onFailure {
                emitMessage(it.message ?: "PAN validation failed")
                onResult(false, it.message ?: "Invalid PAN format")
            }
        }
    }

    fun acceptJob(jobId: String) {
        viewModelScope.launch {
            val result = repository.acceptJob(jobId)
            result.onSuccess {
                emitMessage("Job accepted! Customer has been notified.")
            }.onFailure {
                emitMessage(it.message ?: "Failed to accept job")
            }
        }
    }

    fun rejectJob(jobId: String) {
        viewModelScope.launch {
            val result = repository.rejectJob(jobId)
            result.onSuccess {
                emitMessage("Job request declined.")
            }.onFailure {
                emitMessage(it.message ?: "Failed to reject job")
            }
        }
    }

    fun startNavigation(jobId: String) {
        viewModelScope.launch {
            val result = repository.startNavigation(jobId)
            result.onSuccess {
                emitMessage("Navigation started. Status: EN_ROUTE.")
            }.onFailure {
                emitMessage(it.message ?: "Could not update status")
            }
        }
    }

    fun markArrived(jobId: String) {
        viewModelScope.launch {
            val result = repository.markArrived(jobId)
            result.onSuccess {
                emitMessage("Arrival confirmed. Please ask customer for arrival OTP.")
            }.onFailure {
                emitMessage(it.message ?: "Failed to mark arrival")
            }
        }
    }

    fun verifyArrivalOtp(jobId: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyArrivalOtp(jobId, otp)
            result.onSuccess {
                emitMessage("OTP verified successfully! You may now Start Service.")
                onResult(true, "OTP Verified")
            }.onFailure {
                emitMessage(it.message ?: "Invalid OTP")
                onResult(false, it.message ?: "Invalid OTP")
            }
        }
    }

    fun startService(jobId: String) {
        viewModelScope.launch {
            val result = repository.startService(jobId)
            result.onSuccess {
                emitMessage("Service timer started.")
            }.onFailure {
                emitMessage(it.message ?: "Could not start service")
            }
        }
    }

    fun requestAdditionalCharge(jobId: String, reason: String, amount: Double, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.requestAdditionalCharge(jobId, reason, amount)
            result.onSuccess {
                emitMessage("Additional charge of ₹${amount.toInt()} approved by customer!")
                onComplete(true)
            }.onFailure {
                emitMessage(it.message ?: "Failed to submit extra charge")
                onComplete(false)
            }
        }
    }

    fun completeService(
        jobId: String,
        beforeNotes: String,
        afterNotes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.completeService(jobId, beforeNotes, afterNotes)
            result.onSuccess {
                emitMessage("Service completed! Earnings credited.")
                onComplete(true)
            }.onFailure {
                emitMessage(it.message ?: "Failed to complete service")
                onComplete(false)
            }
        }
    }

    fun triggerEmergencySos(jobId: String?, emergencyType: String, description: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val result = repository.triggerEmergencySos(jobId, emergencyType, description)
            result.onSuccess {
                emitMessage("EMERGENCY BEACON SENT! Safety Dispatch desk alerted.")
                onComplete()
            }.onFailure {
                emitMessage("Emergency signal logged to local device.")
                onComplete()
            }
        }
    }

    fun requestPayout(amount: Double, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.requestPayout(amount)
            result.onSuccess {
                emitMessage("Payout of ₹${amount.toInt()} initiated to registered bank.")
                onResult(true, "Payout Initiated")
            }.onFailure {
                emitMessage(it.message ?: "Payout failed")
                onResult(false, it.message ?: "Payout failed")
            }
        }
    }

    fun submitSupportTicket(category: String, bookingId: String, subject: String, desc: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val result = repository.submitSupportTicket(category, bookingId, subject, desc)
            result.onSuccess {
                emitMessage("Support ticket submitted.")
                onComplete()
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }

    fun toggleZone(zoneId: String, isSelected: Boolean) {
        viewModelScope.launch { repository.toggleZoneSelection(zoneId, isSelected) }
    }

    fun toggleTemporaryLeave(leave: Boolean) {
        viewModelScope.launch {
            repository.updateTemporaryLeave(leave)
            emitMessage(if (leave) "Temporary leave enabled. You are set to offline." else "Temporary leave disabled.")
        }
    }

    fun completeOnboarding(
        name: String,
        phone: String,
        email: String,
        city: String,
        exp: Int,
        categories: String,
        skills: String,
        bankName: String,
        accountNum: String,
        ifsc: String
    ) {
        viewModelScope.launch {
            val maskedAcct = if (accountNum.length >= 4) "••••••••${accountNum.takeLast(4)}" else "••••••••1234"
            val updatedWorker = WorkerEntity(
                id = 1,
                fullName = name,
                phone = phone,
                email = email,
                city = city,
                experienceYears = exp,
                selectedCategories = categories,
                skills = skills,
                bankName = bankName,
                bankAccountHolder = name,
                bankAccountNumberMasked = maskedAcct,
                bankIfsc = ifsc,
                kycStatus = "UNDER_REVIEW", // Sent for review!
                isOnline = false // Cannot be online until verified!
            )
            repository.updateWorkerProfile(updatedWorker)
            repository.updateKycStatus("UNDER_REVIEW")
            emitMessage("Onboarding completed! Your profile and KYC are now Under Review.")
        }
    }
}
