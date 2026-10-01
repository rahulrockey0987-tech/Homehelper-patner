package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkerEntity::class,
        JobBookingEntity::class,
        PayoutEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        ServiceZoneEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HomeHelpDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun jobDao(): JobDao
    abstract fun payoutDao(): PayoutDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportDao(): SupportDao
    abstract fun serviceZoneDao(): ServiceZoneDao

    companion object {
        @Volatile
        private var INSTANCE: HomeHelpDatabase? = null

        fun getDatabase(context: Context): HomeHelpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeHelpDatabase::class.java,
                    "homehelp_pro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
