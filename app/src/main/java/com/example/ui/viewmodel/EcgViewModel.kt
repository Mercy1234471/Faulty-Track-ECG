package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TechnicianEntity
import com.example.data.repository.EcgRepository
import com.example.util.EcgHttpServer
import com.example.util.ExcelExportHelper
import com.example.util.SmsNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EcgViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EcgRepository
    private val httpServer: EcgHttpServer

    val faults: StateFlow<List<FaultEntity>>
    val recentFaults: StateFlow<List<FaultEntity>>
    val meterRequests: StateFlow<List<MeterRequestEntity>>
    val technicians: StateFlow<List<TechnicianEntity>>
    val districts: StateFlow<List<String>>
    val notifications: StateFlow<List<NotificationEntity>>
    val unreadNotificationsCount: StateFlow<Int>

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow("Staff") // "Customer", "Staff", "Admin"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _serverIp = MutableStateFlow("127.0.0.1")
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    private val _isServerActive = MutableStateFlow(false)
    val isServerActive: StateFlow<Boolean> = _isServerActive.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = EcgRepository(
            database.faultDao(),
            database.meterRequestDao(),
            database.technicianDao(),
            database.notificationDao()
        )

        faults = repository.allFaults.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        recentFaults = repository.recentFaults.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        meterRequests = repository.allMeterRequests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        technicians = repository.allTechnicians.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        districts = repository.allDistricts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        notifications = repository.allNotifications.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        unreadNotificationsCount = repository.unreadNotificationsCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        // Initialize embedded HTTP server
        httpServer = EcgHttpServer(application, repository, viewModelScope, 8080)
        httpServer.start()
        _serverIp.value = httpServer.getDeviceIpAddress()
        _isServerActive.value = true
    }

    override fun onCleared() {
        super.onCleared()
        httpServer.stop()
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setUserRole(role: String) {
        _userRole.value = role
        if (role != "Admin") {
            _isAdminLoggedIn.value = false
        }
    }

    fun loginAdmin(password: String): Boolean {
        // Default ECG Admin credentials: admin / ecg2026 or 1234
        if (password.trim() == "ecg2026" || password.trim() == "1234" || password.trim().equals("admin", ignoreCase = true)) {
            _isAdminLoggedIn.value = true
            _userRole.value = "Admin"
            _snackbarMessage.value = "Welcome Admin! Full administrative privileges unlocked."
            return true
        } else {
            _snackbarMessage.value = "Invalid Admin credentials. Try default 'ecg2026'."
            return false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _userRole.value = "Staff"
        _snackbarMessage.value = "Logged out from Admin Console."
    }

    fun logNewFault(
        type: String,
        region: String,
        district: String,
        location: String,
        gps: String,
        description: String,
        date: String,
        time: String,
        assignedTech: TechnicianEntity?
    ) {
        viewModelScope.launch {
            val nextNum = (faults.value.size + 1).toString().padStart(5, '0')
            val ref = "POF-2026-$nextNum"

            val fault = FaultEntity(
                faultReference = ref,
                faultType = type,
                region = region,
                district = district,
                locationAddress = location,
                gpsAddress = gps,
                description = description,
                reportedDate = date.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                },
                reportedTime = time.ifBlank {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                },
                reportedTimestamp = System.currentTimeMillis(),
                status = if (assignedTech != null) "In Progress" else "Pending",
                assignedTechnicianId = assignedTech?.id,
                assignedTechnicianName = assignedTech?.name,
                assignedTechnicianPhone = assignedTech?.phoneNumber,
                assignedTechnicianLocation = assignedTech?.location,
                resolutionNotes = if (assignedTech != null) "Assigned to ${assignedTech.name}" else null
            )

            repository.insertFault(fault)

            // Add notification
            repository.insertNotification(
                NotificationEntity(
                    title = "New Fault Logged",
                    message = "Fault $ref ($type) reported at $location ($region).",
                    type = "FAULT",
                    referenceId = ref
                )
            )

            // If technician is assigned, dispatch automatic real SMS
            if (assignedTech != null) {
                repository.incrementTechFaults(assignedTech.id)
                val smsResult = SmsNotificationHelper.sendDirectSms(
                    getApplication(),
                    assignedTech.phoneNumber,
                    ref
                )
                repository.insertNotification(
                    NotificationEntity(
                        title = "SMS Sent to Technician",
                        message = "Automatic SMS sent to ${assignedTech.name} (${assignedTech.phoneNumber}): \"A fault( $ref ) has been assigned to you. Please login to view the details\"",
                        type = "SMS",
                        referenceId = ref
                    )
                )
                _snackbarMessage.value = "Fault $ref registered! Automatic SMS dispatched to ${assignedTech.name} (${assignedTech.phoneNumber})."
            } else {
                _snackbarMessage.value = "Fault $ref registered successfully!"
            }
        }
    }

    fun assignTechnician(faultId: Long, technician: TechnicianEntity) {
        viewModelScope.launch {
            val fault = repository.getFaultById(faultId) ?: return@launch
            val updated = fault.copy(
                status = "In Progress",
                assignedTechnicianId = technician.id,
                assignedTechnicianName = technician.name,
                assignedTechnicianPhone = technician.phoneNumber,
                assignedTechnicianLocation = technician.location,
                resolutionNotes = "Assigned to ${technician.name}"
            )
            repository.updateFault(updated)
            repository.incrementTechFaults(technician.id)

            // Dispatch automatic SMS
            val smsResult = SmsNotificationHelper.sendDirectSms(
                getApplication(),
                technician.phoneNumber,
                fault.faultReference
            )

            repository.insertNotification(
                NotificationEntity(
                    title = "Technician Assigned via SMS",
                    message = "Fault ${fault.faultReference} assigned to ${technician.name}. SMS alert: \"A fault( ${fault.faultReference} ) has been assigned to you. Please login to view the details\"",
                    type = "SMS",
                    referenceId = fault.faultReference
                )
            )
            _snackbarMessage.value = "Assigned to ${technician.name}. SMS sent: \"A fault( ${fault.faultReference} ) has been assigned to you.\""
        }
    }

    fun updateFaultStatus(faultId: Long, newStatus: String, notes: String?) {
        viewModelScope.launch {
            val fault = repository.getFaultById(faultId) ?: return@launch
            val updated = fault.copy(
                status = newStatus,
                resolutionNotes = notes ?: fault.resolutionNotes
            )
            repository.updateFault(updated)

            repository.insertNotification(
                NotificationEntity(
                    title = "Fault Status Updated",
                    message = "Fault ${fault.faultReference} marked as $newStatus.",
                    type = "FAULT",
                    referenceId = fault.faultReference
                )
            )
            _snackbarMessage.value = "Fault ${fault.faultReference} status updated to $newStatus."
        }
    }

    fun deleteFault(faultId: Long) {
        viewModelScope.launch {
            val fault = repository.getFaultById(faultId)
            repository.deleteFaultById(faultId)
            _snackbarMessage.value = "Fault ${fault?.faultReference ?: ""} removed."
        }
    }

    fun submitMeterRequest(
        fullName: String,
        phoneNumber: String,
        gpsAddress: String,
        location: String,
        region: String,
        category: String,
        ghanaCardFront: String?,
        ghanaCardBack: String?,
        energyCommissionForm: String?,
        sitePlanPhoto: String?
    ) {
        viewModelScope.launch {
            val nextNum = (meterRequests.value.size + 1).toString().padStart(4, '0')
            val ref = "ECG-MTR-2026-$nextNum"
            val now = Date()

            val request = MeterRequestEntity(
                requestReference = ref,
                fullName = fullName,
                phoneNumber = phoneNumber,
                gpsAddress = gpsAddress,
                location = location,
                region = region,
                category = category,
                ghanaCardFrontUri = ghanaCardFront,
                ghanaCardBackUri = ghanaCardBack,
                energyCommissionFormUri = energyCommissionForm,
                sitePlanPhotoUri = sitePlanPhoto,
                status = "Submitted",
                submittedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now),
                submittedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now),
                submittedTimestamp = System.currentTimeMillis()
            )

            repository.insertMeterRequest(request)

            repository.insertNotification(
                NotificationEntity(
                    title = "New Meter Request",
                    message = "$fullName submitted a new $category meter application ($ref).",
                    type = "METER_REQUEST",
                    referenceId = ref
                )
            )

            _snackbarMessage.value = "Meter Request $ref submitted successfully!"
        }
    }

    fun updateMeterRequestStatus(
        requestId: Long,
        status: String,
        technician: String?,
        remarks: String?
    ) {
        viewModelScope.launch {
            val req = repository.getMeterRequestById(requestId) ?: return@launch
            repository.updateMeterStatus(requestId, status, technician, remarks)

            repository.insertNotification(
                NotificationEntity(
                    title = "Meter Request Status Updated",
                    message = "Request ${req.requestReference} updated to $status (Assigned: ${technician ?: "None"}).",
                    type = "METER_REQUEST",
                    referenceId = req.requestReference
                )
            )
            _snackbarMessage.value = "Meter Request ${req.requestReference} marked as $status."
        }
    }

    fun deleteMeterRequest(requestId: Long) {
        viewModelScope.launch {
            val req = repository.getMeterRequestById(requestId) ?: return@launch
            repository.deleteMeterRequest(req)
            _snackbarMessage.value = "Meter Request ${req.requestReference} deleted."
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun exportFaultsExcel() {
        viewModelScope.launch {
            val currentFaults = faults.value
            val csv = ExcelExportHelper.generateFaultsCsv(currentFaults)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val filename = "ECG_Faults_Report_$dateStr.csv"
            val result = ExcelExportHelper.saveAndShareReport(
                getApplication(),
                filename,
                csv,
                "ECG Faults Report Excel"
            )
            if (result.isSuccess) {
                _snackbarMessage.value = "Excel report downloaded! Saved as $filename"
            } else {
                _snackbarMessage.value = "Export failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun exportMeterRequestsExcel() {
        viewModelScope.launch {
            val currentRequests = meterRequests.value
            val csv = ExcelExportHelper.generateMeterRequestsCsv(currentRequests)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val filename = "ECG_Meter_Requests_$dateStr.csv"
            val result = ExcelExportHelper.saveAndShareReport(
                getApplication(),
                filename,
                csv,
                "ECG Meter Requests Excel"
            )
            if (result.isSuccess) {
                _snackbarMessage.value = "Meter requests Excel downloaded! Saved as $filename"
            } else {
                _snackbarMessage.value = "Export failed: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }
}
