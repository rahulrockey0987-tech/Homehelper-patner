package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM worker_profile WHERE id = 1 LIMIT 1")
    fun getWorkerProfile(): Flow<WorkerEntity?>

    @Query("SELECT * FROM worker_profile WHERE id = 1 LIMIT 1")
    suspend fun getWorkerProfileDirect(): WorkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(worker: WorkerEntity)

    @Query("UPDATE worker_profile SET isOnline = :isOnline WHERE id = 1")
    suspend fun updateOnlineStatus(isOnline: Boolean)

    @Query("UPDATE worker_profile SET kycStatus = :status, kycRejectionReason = :reason WHERE id = 1")
    suspend fun updateKycStatus(status: String, reason: String = "")

    @Query("UPDATE worker_profile SET temporaryLeave = :leave WHERE id = 1")
    suspend fun updateTemporaryLeave(leave: Boolean)

    @Query("UPDATE worker_profile SET isPhoneVerified = :isVerified WHERE id = 1")
    suspend fun updatePhoneVerified(isVerified: Boolean)

    @Query("UPDATE worker_profile SET aadhaarNumberMasked = :masked, isAadhaarVerified = :isVerified, aadhaarVerifiedDate = :date WHERE id = 1")
    suspend fun updateAadhaarVerification(masked: String, isVerified: Boolean, date: String)

    @Query("UPDATE worker_profile SET panNumber = :pan, isPanVerified = :isVerified, panVerifiedDate = :date WHERE id = 1")
    suspend fun updatePanVerification(pan: String, isVerified: Boolean, date: String)
}

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobBookingEntity>>

    @Query("SELECT * FROM jobs WHERE status IN ('ASSIGNED', 'ACCEPTED', 'EN_ROUTE', 'ARRIVED', 'OTP_VERIFIED', 'SERVICE_STARTED') ORDER BY createdAt DESC")
    fun getActiveAndPendingJobs(): Flow<List<JobBookingEntity>>

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    fun getJobById(jobId: String): Flow<JobBookingEntity?>

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobByIdDirect(jobId: String): JobBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobBookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobBookingEntity>)

    @Update
    suspend fun updateJob(job: JobBookingEntity)

    @Query("UPDATE jobs SET status = :status WHERE id = :jobId")
    suspend fun updateStatus(jobId: String, status: String)

    @Query("UPDATE jobs SET additionalChargeReason = :reason, additionalChargeAmount = :amount, additionalChargeApproved = :isApproved WHERE id = :jobId")
    suspend fun updateAdditionalCharge(jobId: String, reason: String, amount: Double, isApproved: Boolean)

    @Query("UPDATE jobs SET beforeProofNotes = :beforeNotes, afterProofNotes = :afterNotes WHERE id = :jobId")
    suspend fun updateServiceProofs(jobId: String, beforeNotes: String, afterNotes: String)
}

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payouts ORDER BY timestamp DESC")
    fun getAllPayouts(): Flow<List<PayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: PayoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payouts: List<PayoutEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<SupportTicketEntity>)
}

@Dao
interface ServiceZoneDao {
    @Query("SELECT * FROM service_zones ORDER BY areaName ASC")
    fun getAllZones(): Flow<List<ServiceZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(zones: List<ServiceZoneEntity>)

    @Query("UPDATE service_zones SET isSelected = :isSelected WHERE id = :id")
    suspend fun updateZoneSelection(id: String, isSelected: Boolean)
}
