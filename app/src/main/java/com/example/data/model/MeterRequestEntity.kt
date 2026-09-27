package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meter_requests")
data class MeterRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestReference: String, // e.g. "ECG-MTR-2026-0001"
    val fullName: String,
    val phoneNumber: String,
    val gpsAddress: String,
    val location: String,
    val region: String,
    val category: String, // "Domestic", "Residential", "Non Residential"
    val ghanaCardFrontUri: String? = null,
    val ghanaCardBackUri: String? = null,
    val energyCommissionFormUri: String? = null,
    val sitePlanPhotoUri: String? = null,
    val status: String = "Submitted", // Submitted, Reviewed, Assigned, In Progress, Completed, Rejected
    val submittedDate: String,
    val submittedTime: String,
    val submittedTimestamp: Long = System.currentTimeMillis(),
    val assignedTechnician: String? = null,
    val adminRemarks: String? = null
)
