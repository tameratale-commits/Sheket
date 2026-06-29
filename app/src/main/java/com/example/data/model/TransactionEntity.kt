package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "SALE" (ገቢ), "EXPENSE" (ወጪ), "DEBT" (የክሬዲት እዳ)
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Int? = null, // linked customer if related to debt
    val customerName: String? = null // cached customer name for quick list views
)
