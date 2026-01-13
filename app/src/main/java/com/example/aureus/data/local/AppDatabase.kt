package com.example.aureus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.CardDao
import com.example.aureus.data.local.dao.ContactDao
import com.example.aureus.data.local.dao.StatisticsCacheDao
import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.local.entity.AccountEntity
import com.example.aureus.data.local.entity.BankCardEntity
import com.example.aureus.data.local.entity.ContactEntity
import com.example.aureus.data.local.entity.StatisticsCacheEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.local.entity.UserEntity

/**
 * Room Database Configuration
 * Enhanced for Offline-First Phase 7:
 * - Added BankCard and Contact entities
 * - Added sync tracking fields (lastSyncedAt, isSynced, isPendingUpload)
 * - Increased version to 5 for schema migration
 * - Added userId index to TransactionEntity
 */
@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        BankCardEntity::class,
        ContactEntity::class,
        StatisticsCacheEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao
    abstract fun contactDao(): ContactDao
    abstract fun statisticsCacheDao(): StatisticsCacheDao
}