package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val currentDebt: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
