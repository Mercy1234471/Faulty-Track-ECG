package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faults")
data class FaultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val faultReference: String, // e.g. "POF-2026-00001"
    val faultType: String,      // e.g. "Transformer Outage", "Line Drop", etc.
    val region: String,         // e.g. "Accra East", "Accra West", "Ashanti", etc.
    val district: String,       // e.g. "Makola", "Adum", etc.
    val locationAddress: String,
    val gpsAddress: String,     // e.g. "GA-183-9022"
    val description: String,
    val reportedDate: String,   // "2026-09-25"
    val reportedTime: String,   // "09:30 AM"
    val reportedTimestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending", "In Progress", "Resolved"
    val assignedTechnicianId: Long? = null,
    val assignedTechnicianName: String? = null,
    val assignedTechnicianPhone: String? = null,
    val assignedTechnicianLocation: String? = null,
    val resolutionNotes: String? = null
)
