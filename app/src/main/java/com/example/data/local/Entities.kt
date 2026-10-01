package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worker_profile")
data class WorkerEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val phone: String,
    val email: String,
    val city: String,
    val experienceYears: Int = 4,
    val selectedCategories: String = "AC Repair, Plumbing",
    val skills: String = "Split AC, Inverter AC, Leak Detection, Pipe Fitting",
    val languages: String = "English, Hindi, Telugu",
    val description: String = "Certified technician with 4+ years of professional residential and commercial experience.",
    val kycStatus: String = "VERIFIED",
    val kycGovtIdType: String = "Aadhaar Card",
    val kycGovtIdNumber: String = "XXXX-XXXX-8912",
    val kycRejectionReason: String = "",
    val isOnline: Boolean = true,
    val isSuspended: Boolean = false,
    val rating: Double = 4.88,
    val reviewCount: Int = 142,
    val totalCompletedJobs: Int = 178,
    val bankAccountHolder: String = "Rajesh Sharma",
    val bankAccountNumberMasked: String = "••••••••6742",
    val bankIfsc: String = "HDFC0001234",
    val bankName: String = "HDFC Bank",
    val isBankVerified: Boolean = true,
    val temporaryLeave: Boolean = false,
    val isPhoneVerified: Boolean = true,
    val aadhaarNumberMasked: String = "•••• •••• 8921",
    val isAadhaarVerified: Boolean = true,
    val aadhaarVerifiedDate: String = "15 Sep 2026",
    val panNumber: String = "ABCDE1234F",
    val isPanVerified: Boolean = true,
    val panVerifiedDate: String = "16 Sep 2026"
)

@Entity(tableName = "jobs")
data class JobBookingEntity(
    @PrimaryKey val id: String,
    val serviceCategory: String,
    val serviceTitle: String,
    val customerName: String,
    val customerPhoneMasked: String,
    val customerAddressMasked: String,
    val customerLocality: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val estimatedDurationHours: Double,
    val estimatedEarnings: Double,
    val distanceKm: Double,
    val status: String, // ASSIGNED, ACCEPTED, EN_ROUTE, ARRIVED, OTP_VERIFIED, SERVICE_STARTED, SERVICE_COMPLETED, FINAL_BILL_PENDING, COMPLETED, REJECTED
    val arrivalOtp: String = "482910",
    val specialInstructions: String = "Please ring bell and wear shoe covers.",
    val additionalChargeReason: String = "",
    val additionalChargeAmount: Double = 0.0,
    val additionalChargeApproved: Boolean = false,
    val beforeProofNotes: String = "",
    val afterProofNotes: String = "",
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payouts")
data class PayoutEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val date: String,
    val status: String, // PENDING, PROCESSING, PAID, FAILED
    val referenceId: String,
    val bankMasked: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // JOB_ALERT, KYC_UPDATE, PAYOUT, EMERGENCY, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val category: String,
    val relatedBookingId: String,
    val subject: String,
    val description: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED
    val createdAt: Long = System.currentTimeMillis(),
    val agentReply: String = ""
)

@Entity(tableName = "service_zones")
data class ServiceZoneEntity(
    @PrimaryKey val id: String,
    val city: String,
    val areaName: String,
    val isSelected: Boolean = true
)
