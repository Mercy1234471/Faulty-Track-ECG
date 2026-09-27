package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FaultDao
import com.example.data.dao.MeterRequestDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TechnicianDao
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TechnicianEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FaultEntity::class,
        MeterRequestEntity::class,
        TechnicianEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun faultDao(): FaultDao
    abstract fun meterRequestDao(): MeterRequestDao
    abstract fun technicianDao(): TechnicianDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecg_fault_tracker.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val techDao = database.technicianDao()
            val faultDao = database.faultDao()
            val meterDao = database.meterRequestDao()
            val notifDao = database.notificationDao()

            // Seed ECG Technicians across districts
            val sampleTechnicians = listOf(
                TechnicianEntity(
                    staffId = "ECG-TECH-0142",
                    name = "Kwame Mensah",
                    phoneNumber = "+233 24 456 7891",
                    location = "Makola Substation 4",
                    district = "Makola District",
                    region = "Accra East",
                    designation = "Senior Linesman",
                    activeFaultCount = 2,
                    isAvailable = true
                ),
                TechnicianEntity(
                    staffId = "ECG-TECH-0219",
                    name = "Kofi Boateng",
                    phoneNumber = "+233 20 891 2345",
                    location = "Legon Substation Bypass",
                    district = "Legon District",
                    region = "Accra East",
                    designation = "Transformer Specialist",
                    activeFaultCount = 1,
                    isAvailable = true
                ),
                TechnicianEntity(
                    staffId = "ECG-TECH-0388",
                    name = "Ama Osei",
                    phoneNumber = "+233 55 312 8764",
                    location = "Kaneshie Market Feeder Station",
                    district = "Kaneshie District",
                    region = "Accra West",
                    designation = "Metering Engineer",
                    activeFaultCount = 1,
                    isAvailable = true
                ),
                TechnicianEntity(
                    staffId = "ECG-TECH-0451",
                    name = "Emmanuel Darko",
                    phoneNumber = "+233 27 654 3210",
                    location = "Dansoman Control Post",
                    district = "Dansoman District",
                    region = "Accra West",
                    designation = "High Voltage Line Operator",
                    activeFaultCount = 0,
                    isAvailable = true
                ),
                TechnicianEntity(
                    staffId = "ECG-TECH-0520",
                    name = "Samuel Addo",
                    phoneNumber = "+233 24 112 9087",
                    location = "Adum Central Substation",
                    district = "Adum District",
                    region = "Ashanti",
                    designation = "Distribution Inspector",
                    activeFaultCount = 1,
                    isAvailable = true
                ),
                TechnicianEntity(
                    staffId = "ECG-TECH-0607",
                    name = "Grace Asare",
                    phoneNumber = "+233 50 774 1239",
                    location = "Tema Heavy Industrial Area",
                    district = "Tema District",
                    region = "Tema",
                    designation = "Substation Engineer",
                    activeFaultCount = 1,
                    isAvailable = true
                )
            )
            techDao.insertAllTechnicians(sampleTechnicians)

            // Seed Initial Faults
            val sampleFaults = listOf(
                FaultEntity(
                    faultReference = "POF-2026-00001",
                    faultType = "Transformer Outage",
                    region = "Accra East",
                    district = "Makola District",
                    locationAddress = "High Street, near Standard Chartered Bank",
                    gpsAddress = "GA-183-9022",
                    description = "Transformer tripped violently with loud blast and smoke emission. Entire block without electricity.",
                    reportedDate = "2026-09-24",
                    reportedTime = "02:15 PM",
                    reportedTimestamp = System.currentTimeMillis() - 86400000L,
                    status = "In Progress",
                    assignedTechnicianId = 1L,
                    assignedTechnicianName = "Kwame Mensah",
                    assignedTechnicianPhone = "+233 24 456 7891",
                    assignedTechnicianLocation = "Makola Substation 4",
                    resolutionNotes = "Replacement fuses mobilized. Safety perimeter secured."
                ),
                FaultEntity(
                    faultReference = "POF-2026-00002",
                    faultType = "Line Drop / Cable Cut",
                    region = "Accra West",
                    district = "Dansoman District",
                    locationAddress = "Keep Fit Junction, Sahara Road",
                    gpsAddress = "GA-452-1189",
                    description = "Heavy rain caused tree branch to snap 11kV overhead cable. Live line lying across the street.",
                    reportedDate = "2026-09-24",
                    reportedTime = "04:40 PM",
                    reportedTimestamp = System.currentTimeMillis() - 54000000L,
                    status = "Pending",
                    assignedTechnicianId = null,
                    assignedTechnicianName = null,
                    assignedTechnicianPhone = null,
                    assignedTechnicianLocation = null,
                    resolutionNotes = null
                ),
                FaultEntity(
                    faultReference = "POF-2026-00003",
                    faultType = "Low Voltage / Fluctuation",
                    region = "Ashanti",
                    district = "Adum District",
                    locationAddress = "Prempeh II Street, Commercial Bank Lane",
                    gpsAddress = "AK-029-4411",
                    description = "Severe low voltage around 130V. Freezers and air conditioners failing to turn on in commercial shops.",
                    reportedDate = "2026-09-24",
                    reportedTime = "06:10 PM",
                    reportedTimestamp = System.currentTimeMillis() - 36000000L,
                    status = "In Progress",
                    assignedTechnicianId = 5L,
                    assignedTechnicianName = "Samuel Addo",
                    assignedTechnicianPhone = "+233 24 112 9087",
                    assignedTechnicianLocation = "Adum Central Substation",
                    resolutionNotes = "Phase re-balancing in progress at local distribution feeder."
                ),
                FaultEntity(
                    faultReference = "POF-2026-00004",
                    faultType = "Burnt / Broken Pole",
                    region = "Tema",
                    district = "Tema District",
                    locationAddress = "Community 9, Light Industrial Area, Plot 14",
                    gpsAddress = "GT-048-7320",
                    description = "Wooden pole charred at base after refuse fire nearby. Leaning precariously towards warehouse roof.",
                    reportedDate = "2026-09-25",
                    reportedTime = "08:20 AM",
                    reportedTimestamp = System.currentTimeMillis() - 14400000L,
                    status = "Pending",
                    assignedTechnicianId = null,
                    assignedTechnicianName = null,
                    assignedTechnicianPhone = null,
                    assignedTechnicianLocation = null,
                    resolutionNotes = null
                ),
                FaultEntity(
                    faultReference = "POF-2026-00005",
                    faultType = "Meter Fault / Sparks",
                    region = "Accra East",
                    district = "Legon District",
                    locationAddress = "East Legon, Lagos Avenue, House 24",
                    gpsAddress = "GA-390-5512",
                    description = "Prepaid smart meter flashing red with continuous sparks from terminal lugs when load is applied.",
                    reportedDate = "2026-09-23",
                    reportedTime = "11:00 AM",
                    reportedTimestamp = System.currentTimeMillis() - 172800000L,
                    status = "Resolved",
                    assignedTechnicianId = 2L,
                    assignedTechnicianName = "Kofi Boateng",
                    assignedTechnicianPhone = "+233 20 891 2345",
                    assignedTechnicianLocation = "Legon Substation Bypass",
                    resolutionNotes = "Defective meter replaced with brand new smart card meter. Load tested normal."
                )
            )
            faultDao.insertAllFaults(sampleFaults)

            // Seed Initial Meter Requests
            val sampleRequests = listOf(
                MeterRequestEntity(
                    requestReference = "ECG-MTR-2026-0001",
                    fullName = "Nathaniel Kwakye Mensah",
                    phoneNumber = "+233 24 332 9901",
                    gpsAddress = "GA-821-3044",
                    location = "Spintex Road, Baatsona",
                    region = "Accra East",
                    category = "Residential",
                    status = "In Progress",
                    submittedDate = "2026-09-23",
                    submittedTime = "09:15 AM",
                    submittedTimestamp = System.currentTimeMillis() - 120000000L,
                    assignedTechnician = "Grace Asare",
                    adminRemarks = "Site inspection scheduled for transformer load tap."
                ),
                MeterRequestEntity(
                    requestReference = "ECG-MTR-2026-0002",
                    fullName = "Abena Serwaa Holdings",
                    phoneNumber = "+233 50 119 7800",
                    gpsAddress = "AK-190-2287",
                    location = "Ahodwo Commercial Zone",
                    region = "Ashanti",
                    category = "Non Residential",
                    status = "Submitted",
                    submittedDate = "2026-09-24",
                    submittedTime = "11:30 AM",
                    submittedTimestamp = System.currentTimeMillis() - 48000000L,
                    assignedTechnician = null,
                    adminRemarks = "Awaiting verification of Energy Commission certification."
                ),
                MeterRequestEntity(
                    requestReference = "ECG-MTR-2026-0003",
                    fullName = "Kwadwo Yeboah",
                    phoneNumber = "+233 27 554 1120",
                    gpsAddress = "GA-102-4491",
                    location = "Weija Barrier, New Site",
                    region = "Accra West",
                    category = "Domestic",
                    status = "Reviewed",
                    submittedDate = "2026-09-24",
                    submittedTime = "03:45 PM",
                    submittedTimestamp = System.currentTimeMillis() - 25000000L,
                    assignedTechnician = null,
                    adminRemarks = "Ghana card and site plan verified. Ready for technician dispatch."
                )
            )
            meterDao.insertAllMeterRequests(sampleRequests)

            // Seed notifications
            val sampleNotifs = listOf(
                NotificationEntity(
                    title = "New Fault Reported",
                    message = "High priority fault POF-2026-00002 (Line Drop) logged at Dansoman District.",
                    type = "FAULT",
                    referenceId = "POF-2026-00002",
                    timestamp = System.currentTimeMillis() - 54000000L,
                    isRead = false
                ),
                NotificationEntity(
                    title = "Technician Assigned via SMS",
                    message = "Fault POF-2026-00001 assigned to Kwame Mensah (+233 24 456 7891). SMS alert dispatched.",
                    type = "SMS",
                    referenceId = "POF-2026-00001",
                    timestamp = System.currentTimeMillis() - 86000000L,
                    isRead = false
                ),
                NotificationEntity(
                    title = "New Meter Request",
                    message = "Abena Serwaa Holdings submitted Non Residential meter request ECG-MTR-2026-0002.",
                    type = "METER_REQUEST",
                    referenceId = "ECG-MTR-2026-0002",
                    timestamp = System.currentTimeMillis() - 48000000L,
                    isRead = false
                )
            )
            for (n in sampleNotifs) {
                notifDao.insertNotification(n)
            }
        }
    }
}
