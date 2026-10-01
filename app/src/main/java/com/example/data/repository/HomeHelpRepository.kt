package com.example.data.repository

import com.example.data.local.HomeHelpDatabase
import com.example.data.local.JobBookingEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PayoutEntity
import com.example.data.local.ServiceZoneEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.WorkerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeHelpRepository(private val database: HomeHelpDatabase) {

    private val workerDao = database.workerDao()
    private val jobDao = database.jobDao()
    private val payoutDao = database.payoutDao()
    private val notificationDao = database.notificationDao()
    private val supportDao = database.supportDao()
    private val serviceZoneDao = database.serviceZoneDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    val workerProfile: Flow<WorkerEntity?> = workerDao.getWorkerProfile()
    val allJobs: Flow<List<JobBookingEntity>> = jobDao.getAllJobs()
    val allPayouts: Flow<List<PayoutEntity>> = payoutDao.getAllPayouts()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val allTickets: Flow<List<SupportTicketEntity>> = supportDao.getAllTickets()
    val allZones: Flow<List<ServiceZoneEntity>> = serviceZoneDao.getAllZones()

    fun getJobById(jobId: String): Flow<JobBookingEntity?> = jobDao.getJobById(jobId)

    suspend fun setOnlineStatus(isOnline: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val worker = workerDao.getWorkerProfileDirect()
            ?: return@withContext Result.failure(Exception("Worker profile not found"))

        if (isOnline && worker.kycStatus != "VERIFIED") {
            return@withContext Result.failure(
                Exception("Cannot go Online. Only fully VERIFIED professionals can receive service requests.")
            )
        }
        if (isOnline && worker.isSuspended) {
            return@withContext Result.failure(
                Exception("Account is temporarily suspended. Contact support for assistance.")
            )
        }

        workerDao.updateOnlineStatus(isOnline)
        Result.success(Unit)
    }

    suspend fun updateKycStatus(status: String, reason: String = ""): Unit = withContext(Dispatchers.IO) {
        workerDao.updateKycStatus(status, reason)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "KYC Verification Update",
                message = if (status == "VERIFIED") "Congratulations! Your profile documents have been verified. You can now go Online to receive service requests."
                          else if (status == "REJECTED") "KYC documents rejected: $reason. Please resubmit."
                          else "Your documents have been submitted and are currently Under Review by the operations team.",
                type = "KYC_UPDATE"
            )
        )
    }

    suspend fun updateWorkerProfile(worker: WorkerEntity) = withContext(Dispatchers.IO) {
        workerDao.insertOrUpdate(worker)
    }

    suspend fun updateTemporaryLeave(leave: Boolean) = withContext(Dispatchers.IO) {
        workerDao.updateTemporaryLeave(leave)
        if (leave) {
            workerDao.updateOnlineStatus(false)
        }
    }

    suspend fun toggleZoneSelection(zoneId: String, isSelected: Boolean) = withContext(Dispatchers.IO) {
        serviceZoneDao.updateZoneSelection(zoneId, isSelected)
    }

    // --- Phone OTP & KYC Verification (Aadhaar & PAN) ---

    suspend fun sendPhoneOtp(phone: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(Exception("Please enter a valid 10-digit mobile number"))
        }
        val otp = (100000..999999).random().toString()
        notificationDao.insertNotification(
            NotificationEntity(
                title = "HomeHelp SMS OTP",
                message = "Your 6-digit HomeHelp verification code is $otp. Do not share this with anyone.",
                type = "SYSTEM"
            )
        )
        Result.success(otp)
    }

    suspend fun verifyPhoneOtp(phone: String, enteredOtp: String, expectedOtp: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (enteredOtp.trim() != expectedOtp.trim()) {
            return@withContext Result.failure(Exception("Invalid SMS OTP code. Please check and try again."))
        }
        workerDao.updatePhoneVerified(true)
        Result.success(Unit)
    }

    suspend fun sendAadhaarOtp(aadhaarRaw: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanAadhaar = aadhaarRaw.replace(Regex("[^0-9]"), "")
        if (cleanAadhaar.length != 12) {
            return@withContext Result.failure(Exception("Aadhaar number must contain exactly 12 digits"))
        }
        // Simulated UIDAI Aadhaar OTP to registered mobile
        val uidaiOtp = (100000..999999).random().toString()
        notificationDao.insertNotification(
            NotificationEntity(
                title = "UIDAI Aadhaar OTP Sent",
                message = "OTP for Aadhaar eKYC ending in ${cleanAadhaar.takeLast(4)} is $uidaiOtp (Valid for 10 mins).",
                type = "KYC_UPDATE"
            )
        )
        Result.success(uidaiOtp)
    }

    suspend fun verifyAadhaarOtp(
        aadhaarRaw: String,
        enteredOtp: String,
        expectedOtp: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanAadhaar = aadhaarRaw.replace(Regex("[^0-9]"), "")
        if (enteredOtp.trim() != expectedOtp.trim()) {
            return@withContext Result.failure(Exception("Invalid UIDAI Aadhaar OTP. Please enter the 6-digit code received."))
        }
        val last4 = cleanAadhaar.takeLast(4)
        val masked = "•••• •••• $last4"
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        workerDao.updateAadhaarVerification(masked, true, todayStr)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Aadhaar e-KYC Verified Successfully",
                message = "Your Aadhaar ending with $last4 has been authenticated with UIDAI registry.",
                type = "KYC_UPDATE"
            )
        )
        Result.success(Unit)
    }

    suspend fun verifyPanCard(panRaw: String, legalName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val formattedPan = panRaw.trim().uppercase()
        val panPattern = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
        if (!panPattern.matches(formattedPan)) {
            return@withContext Result.failure(Exception("Invalid PAN format. Must be 10 characters (e.g. ABCDE1234F)."))
        }
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        workerDao.updatePanVerification(formattedPan, true, todayStr)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "PAN Card Verified (NSDL/ITD)",
                message = "PAN $formattedPan successfully verified and linked to $legalName for TDS compliance.",
                type = "KYC_UPDATE"
            )
        )
        Result.success(Unit)
    }

    // --- Job Lifecycle Methods (Enforcing Business Rules) ---

    suspend fun acceptJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val worker = workerDao.getWorkerProfileDirect()
            ?: return@withContext Result.failure(Exception("Worker not authenticated"))

        if (worker.kycStatus != "VERIFIED") {
            return@withContext Result.failure(Exception("Only VERIFIED workers can accept bookings."))
        }
        if (!worker.isOnline) {
            return@withContext Result.failure(Exception("You must be Online to accept new job requests."))
        }

        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "ASSIGNED") {
            return@withContext Result.failure(Exception("JOB_NO_LONGER_AVAILABLE: Job has already been assigned or accepted."))
        }

        jobDao.updateStatus(jobId, "ACCEPTED")
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Job Accepted: ${job.serviceTitle}",
                message = "You have confirmed booking #${job.id}. Scheduled at ${job.scheduledTime}.",
                type = "JOB_ALERT"
            )
        )
        Result.success(Unit)
    }

    suspend fun rejectJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        jobDao.updateStatus(jobId, "REJECTED")
        Result.success(Unit)
    }

    suspend fun startNavigation(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "ACCEPTED") {
            return@withContext Result.failure(Exception("Job is not in ACCEPTED state"))
        }

        jobDao.updateStatus(jobId, "EN_ROUTE")
        Result.success(Unit)
    }

    suspend fun markArrived(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "EN_ROUTE") {
            return@withContext Result.failure(Exception("Worker must be EN_ROUTE before marking ARRIVED"))
        }

        jobDao.updateStatus(jobId, "ARRIVED")
        Result.success(Unit)
    }

    suspend fun verifyArrivalOtp(jobId: String, enteredOtp: String): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "ARRIVED") {
            return@withContext Result.failure(Exception("Customer OTP can only be verified after worker has ARRIVED"))
        }

        if (job.arrivalOtp.trim() != enteredOtp.trim()) {
            return@withContext Result.failure(Exception("Invalid OTP code. Please ask customer for the 6-digit HomeHelp code."))
        }

        jobDao.updateStatus(jobId, "OTP_VERIFIED")
        Result.success(Unit)
    }

    suspend fun startService(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "OTP_VERIFIED") {
            return@withContext Result.failure(Exception("Cannot start service before customer arrival OTP is verified"))
        }

        jobDao.updateStatus(jobId, "SERVICE_STARTED")
        Result.success(Unit)
    }

    suspend fun requestAdditionalCharge(jobId: String, reason: String, amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0.0) {
            return@withContext Result.failure(Exception("Additional amount must be greater than zero"))
        }
        if (reason.isBlank()) {
            return@withContext Result.failure(Exception("Please provide a legitimate reason for extra work/parts"))
        }

        // Submits charge and simulates customer real-time approval
        jobDao.updateAdditionalCharge(jobId, reason, amount, isApproved = true)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Extra Charge Approved (₹$amount)",
                message = "Customer approved additional charge for: $reason",
                type = "JOB_ALERT"
            )
        )
        Result.success(Unit)
    }

    suspend fun completeService(
        jobId: String,
        beforeNotes: String,
        afterNotes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val job = jobDao.getJobByIdDirect(jobId)
            ?: return@withContext Result.failure(Exception("Job not found"))

        if (job.status != "SERVICE_STARTED") {
            return@withContext Result.failure(Exception("Job must be in SERVICE_STARTED state to complete"))
        }

        val totalEarnings = job.estimatedEarnings + if (job.additionalChargeApproved) job.additionalChargeAmount else 0.0

        val updatedJob = job.copy(
            status = "COMPLETED",
            beforeProofNotes = beforeNotes.ifBlank { "Completed initial pre-service diagnosis and setup checklist." },
            afterProofNotes = afterNotes.ifBlank { "Cleaned area, performed operational pressure check, customer demonstrated satisfaction." },
            completedAt = System.currentTimeMillis()
        )
        jobDao.updateJob(updatedJob)

        // Increment worker completed count
        val worker = workerDao.getWorkerProfileDirect()
        if (worker != null) {
            workerDao.insertOrUpdate(
                worker.copy(totalCompletedJobs = worker.totalCompletedJobs + 1)
            )
        }

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Service Completed! +₹${totalEarnings.toInt()}",
                message = "Service #${job.id} completed. Earnings added to your pending payout balance.",
                type = "PAYOUT"
            )
        )
        Result.success(Unit)
    }

    suspend fun triggerEmergencySos(jobId: String?, emergencyType: String, description: String): Result<Unit> = withContext(Dispatchers.IO) {
        val ticketId = "SOS-${(1000..9999).random()}"
        val ticket = SupportTicketEntity(
            id = ticketId,
            category = "SAFETY_EMERGENCY",
            relatedBookingId = jobId ?: "GENERAL",
            subject = "[CRITICAL SOS] $emergencyType",
            description = "Worker location beacon dispatched: $description. Platform Safety Dispatch notified.",
            status = "IN_PROGRESS",
            agentReply = "Safety Operations Lead has received your beacon. Dispatching supervisor & checking location coordinates."
        )
        supportDao.insertTicket(ticket)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Emergency Beacon Active (#$ticketId)",
                message = "Platform emergency desk has been alerted with your location and job reference.",
                type = "EMERGENCY"
            )
        )
        Result.success(Unit)
    }

    suspend fun submitSupportTicket(
        category: String,
        bookingId: String,
        subject: String,
        description: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val ticketId = "TCK-${(1000..9999).random()}"
        val ticket = SupportTicketEntity(
            id = ticketId,
            category = category,
            relatedBookingId = bookingId,
            subject = subject,
            description = description,
            status = "OPEN",
            agentReply = "Ticket received. Our support specialist typically responds within 15 minutes."
        )
        supportDao.insertTicket(ticket)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Support Ticket Created ($ticketId)",
                message = "Ticket for '$subject' has been logged. We will notify you when agent replies.",
                type = "SYSTEM"
            )
        )
        Result.success(Unit)
    }

    suspend fun requestPayout(amount: Double): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount < 500.0) {
            return@withContext Result.failure(Exception("Minimum payout withdrawal is ₹500"))
        }

        val worker = workerDao.getWorkerProfileDirect()
        val payoutId = "PAY-${(10000..99999).random()}"
        val refId = "TXN-${UUID.randomUUID().toString().take(10).uppercase()}"
        val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())

        val payout = PayoutEntity(
            id = payoutId,
            amount = amount,
            date = dateStr,
            status = "PROCESSING",
            referenceId = refId,
            bankMasked = worker?.bankAccountNumberMasked ?: "••••••••6742"
        )
        payoutDao.insertPayout(payout)
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Payout Request Initiated: ₹${amount.toInt()}",
                message = "Withdrawal of ₹${amount.toInt()} is being processed to your registered account ${payout.bankMasked}.",
                type = "PAYOUT"
            )
        )
        Result.success(Unit)
    }

    suspend fun markNotificationAsRead(id: Int) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // --- Seeding Baseline Production-Realistic Data ---

    private suspend fun seedInitialDataIfNeeded() {
        val existingWorker = workerDao.getWorkerProfileDirect()
        if (existingWorker == null) {
            workerDao.insertOrUpdate(
                WorkerEntity(
                    id = 1,
                    fullName = "Vikram Reddy",
                    phone = "+91 98490 12845",
                    email = "vikram.pro@homehelp.in",
                    city = "Hyderabad",
                    experienceYears = 5,
                    selectedCategories = "AC Service & Repair, Home Appliances, Electrical",
                    skills = "Inverter AC Servicing, Jet Pump Cleaning, Compressor Repair, Wiring",
                    languages = "English, Telugu, Hindi",
                    description = "Certified Urban HVAC & Electrical Technician with 5+ years experience. 100% on-time record.",
                    kycStatus = "VERIFIED",
                    kycGovtIdType = "Aadhaar Card",
                    kycGovtIdNumber = "XXXX-XXXX-8921",
                    isOnline = true,
                    isSuspended = false,
                    rating = 4.92,
                    reviewCount = 186,
                    totalCompletedJobs = 214,
                    bankAccountHolder = "Vikram Reddy",
                    bankAccountNumberMasked = "••••••••4819",
                    bankIfsc = "HDFC0002841",
                    bankName = "HDFC Bank",
                    isBankVerified = true
                )
            )
        }

        val existingJobs = jobDao.getAllJobs().firstOrNull()
        if (existingJobs.isNullOrEmpty()) {
            val jobs = listOf(
                // 1. Live Active Job in progress (Ready for operation walkthrough)
                JobBookingEntity(
                    id = "JOB-HYD-4821",
                    serviceCategory = "AC Service & Repair",
                    serviceTitle = "Deep Jet Foam AC Servicing (Split 1.5 Ton)",
                    customerName = "Ananya Sharma",
                    customerPhoneMasked = "+91 98•••• 1204",
                    customerAddressMasked = "Flat 402, Signature Towers, High Street",
                    customerLocality = "Madhapur, Hitec City",
                    scheduledDate = "Today",
                    scheduledTime = "11:30 AM",
                    estimatedDurationHours = 1.5,
                    estimatedEarnings = 780.0,
                    distanceKm = 2.4,
                    status = "EN_ROUTE",
                    arrivalOtp = "729143",
                    specialInstructions = "Please call upon arrival at the security gate for elevator passcode.",
                    additionalChargeReason = "",
                    additionalChargeAmount = 0.0,
                    additionalChargeApproved = false
                ),
                // 2. Pending New Request (Ready to Accept or Reject on Dashboard)
                JobBookingEntity(
                    id = "JOB-HYD-4822",
                    serviceCategory = "Electrical Repair",
                    serviceTitle = "Main Switchboard Tripping & MCB Replacement",
                    customerName = "Dr. K. Srinivas",
                    customerPhoneMasked = "+91 97•••• 8829",
                    customerAddressMasked = "Villa 18, Greenwood Meadows",
                    customerLocality = "Kondapur",
                    scheduledDate = "Today",
                    scheduledTime = "02:30 PM",
                    estimatedDurationHours = 1.0,
                    estimatedEarnings = 550.0,
                    distanceKm = 4.1,
                    status = "ASSIGNED",
                    arrivalOtp = "381902",
                    specialInstructions = "Electricity is fluctuating in the main hall. Need prompt troubleshooting."
                ),
                // 3. Upcoming Job for Tomorrow
                JobBookingEntity(
                    id = "JOB-HYD-4823",
                    serviceCategory = "AC Service & Repair",
                    serviceTitle = "AC Gas Leakage Check & Refill (R32)",
                    customerName = "Ramesh Verma",
                    customerPhoneMasked = "+91 99•••• 4410",
                    customerAddressMasked = "Road No 36, Jubilee Hills",
                    customerLocality = "Jubilee Hills",
                    scheduledDate = "Tomorrow",
                    scheduledTime = "10:00 AM",
                    estimatedDurationHours = 2.0,
                    estimatedEarnings = 1450.0,
                    distanceKm = 6.2,
                    status = "ACCEPTED",
                    arrivalOtp = "518392",
                    specialInstructions = "Outdoor unit is mounted on balcony wall with easy ladder access."
                ),
                // 4. Completed Job Today
                JobBookingEntity(
                    id = "JOB-HYD-4819",
                    serviceCategory = "Home Appliances",
                    serviceTitle = "Washing Machine Water Inlet Valve Repair",
                    customerName = "Pooja Hegde",
                    customerPhoneMasked = "+91 91•••• 3391",
                    customerAddressMasked = "Apt 201, Green Glen Woods",
                    customerLocality = "Gachibowli",
                    scheduledDate = "Today",
                    scheduledTime = "09:00 AM",
                    estimatedDurationHours = 1.0,
                    estimatedEarnings = 620.0,
                    distanceKm = 3.5,
                    status = "COMPLETED",
                    arrivalOtp = "192847",
                    specialInstructions = "Front load LG washer not drawing rinse water.",
                    beforeProofNotes = "Diagnosed blocked solenoid valve.",
                    afterProofNotes = "Replaced filter mesh and tested rapid spin cycle successfully.",
                    completedAt = System.currentTimeMillis() - 7200000L
                )
            )
            jobDao.insertAll(jobs)
        }

        val existingPayouts = payoutDao.getAllPayouts().firstOrNull()
        if (existingPayouts.isNullOrEmpty()) {
            payoutDao.insertAll(
                listOf(
                    PayoutEntity(
                        id = "PAY-99214",
                        amount = 4850.0,
                        date = "Yesterday, 06:30 PM",
                        status = "PAID",
                        referenceId = "UPI-REF-99201948",
                        bankMasked = "••••••••4819",
                        timestamp = System.currentTimeMillis() - 86400000L
                    ),
                    PayoutEntity(
                        id = "PAY-98102",
                        amount = 6200.0,
                        date = "28 Sep, 07:00 PM",
                        status = "PAID",
                        referenceId = "NEFT-HDFC-882194",
                        bankMasked = "••••••••4819",
                        timestamp = System.currentTimeMillis() - (86400000L * 3)
                    ),
                    PayoutEntity(
                        id = "PAY-97812",
                        amount = 5150.0,
                        date = "24 Sep, 05:45 PM",
                        status = "PAID",
                        referenceId = "UPI-REF-77182901",
                        bankMasked = "••••••••4819",
                        timestamp = System.currentTimeMillis() - (86400000L * 7)
                    )
                )
            )
        }

        val existingNotifications = notificationDao.getAllNotifications().firstOrNull()
        if (existingNotifications.isNullOrEmpty()) {
            notificationDao.insertAll(
                listOf(
                    NotificationEntity(
                        title = "New Job Assigned: Electrical Repair",
                        message = "New booking request in Kondapur (4.1 km away). Tap to view and accept.",
                        type = "JOB_ALERT",
                        timestamp = System.currentTimeMillis() - 1800000L,
                        isRead = false
                    ),
                    NotificationEntity(
                        title = "Weekly Payout Credited: ₹4,850",
                        message = "Your payout for week ending Sep 30 has been successfully deposited to HDFC Bank.",
                        type = "PAYOUT",
                        timestamp = System.currentTimeMillis() - 86400000L,
                        isRead = true
                    ),
                    NotificationEntity(
                        title = "5-Star Rating Received!",
                        message = "Customer rated you 5 stars: 'Super prompt and very clean AC service work!'",
                        type = "SYSTEM",
                        timestamp = System.currentTimeMillis() - 172800000L,
                        isRead = true
                    )
                )
            )
        }

        val existingZones = serviceZoneDao.getAllZones().firstOrNull()
        if (existingZones.isNullOrEmpty()) {
            serviceZoneDao.insertAll(
                listOf(
                    ServiceZoneEntity("zone_1", "Hyderabad", "Madhapur", true),
                    ServiceZoneEntity("zone_2", "Hyderabad", "Hitec City", true),
                    ServiceZoneEntity("zone_3", "Hyderabad", "Gachibowli", true),
                    ServiceZoneEntity("zone_4", "Hyderabad", "Kondapur", true),
                    ServiceZoneEntity("zone_5", "Hyderabad", "Kukatpally", true),
                    ServiceZoneEntity("zone_6", "Hyderabad", "Jubilee Hills", true),
                    ServiceZoneEntity("zone_7", "Hyderabad", "Banjara Hills", false),
                    ServiceZoneEntity("zone_8", "Hyderabad", "Financial District", true)
                )
            )
        }

        val existingTickets = supportDao.getAllTickets().firstOrNull()
        if (existingTickets.isNullOrEmpty()) {
            supportDao.insertAll(
                listOf(
                    SupportTicketEntity(
                        id = "TCK-4819",
                        category = "CUSTOMER_UNAVAILABLE",
                        relatedBookingId = "JOB-HYD-4819",
                        subject = "Customer delayed gate access by 10 minutes",
                        description = "Reported gate delay for records. Service proceeded normally afterwards.",
                        status = "RESOLVED",
                        createdAt = System.currentTimeMillis() - 90000000L,
                        agentReply = "Noted on file. No penalty applied to your on-time score."
                    )
                )
            )
        }
    }
}
