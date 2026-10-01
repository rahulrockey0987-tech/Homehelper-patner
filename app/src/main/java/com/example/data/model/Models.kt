package com.example.data.model

enum class KycStatus {
    NOT_STARTED,
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    RESUBMISSION_REQUIRED
}

enum class WorkerLifecycle {
    REGISTERED,
    PROFILE_COMPLETED,
    KYC_SUBMITTED,
    UNDER_REVIEW,
    VERIFIED,
    ACTIVE,
    SUSPENDED
}

enum class JobStatus {
    ASSIGNED,
    ACCEPTED,
    EN_ROUTE,
    ARRIVED,
    OTP_VERIFIED,
    SERVICE_STARTED,
    SERVICE_COMPLETED,
    FINAL_BILL_PENDING,
    COMPLETED,
    REJECTED,
    CANCELLED
}

enum class PayoutStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    REVERSED
}

data class ServiceCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val isSelected: Boolean = false
)

data class DaySchedule(
    val dayName: String,
    val isEnabled: Boolean,
    val startTime: String,
    val endTime: String
)

data class AdditionalCharge(
    val reason: String,
    val amount: Double,
    val description: String,
    val isApproved: Boolean = false,
    val requestedAt: Long = System.currentTimeMillis()
)
