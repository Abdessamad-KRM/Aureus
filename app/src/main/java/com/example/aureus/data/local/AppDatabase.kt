package com.example.aureus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aureus.data.local.dao.AccountDao
import com.example.aureus.data.local.dao.TransactionDao
import com.example.aureus.data.local.dao.UserDao
import com.example.aureus.data.local.entity.AccountEntity
import com.example.aureus.data.local.entity.TransactionEntity
import com.example.aureus.data.local.entity.UserEntity

/**
 * Room Database Configuration
 */
@Database(
    entities = [UserEntity::class, AccountEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
}