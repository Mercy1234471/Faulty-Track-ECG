package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "FAULT", "METER_REQUEST", "ASSIGNMENT", "SMS"
    val referenceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
