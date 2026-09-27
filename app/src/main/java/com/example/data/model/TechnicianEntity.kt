package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "technicians")
data class TechnicianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val staffId: String,       // e.g. "ECG-TECH-101"
    val name: String,          // e.g. "Kwame Mensah"
    val phoneNumber: String,   // e.g. "+233 24 555 0192"
    val location: String,      // e.g. "Accra East Substation 3"
    val district: String,      // e.g. "Makola District"
    val region: String,        // e.g. "Accra East"
    val designation: String = "Field Linesman",
    val activeFaultCount: Int = 0,
    val isAvailable: Boolean = true
)
