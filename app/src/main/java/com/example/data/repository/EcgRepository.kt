package com.example.data.repository

import com.example.data.dao.FaultDao
import com.example.data.dao.MeterRequestDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TechnicianDao
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TechnicianEntity
import kotlinx.coroutines.flow.Flow

class EcgRepository(
    private val faultDao: FaultDao,
    private val meterRequestDao: MeterRequestDao,
    private val technicianDao: TechnicianDao,
    private val notificationDao: NotificationDao
) {
    // Faults
    val allFaults: Flow<List<FaultEntity>> = faultDao.getAllFaults()
    val recentFaults: Flow<List<FaultEntity>> = faultDao.getRecentFaults()

    fun getFaultsByStatus(status: String): Flow<List<FaultEntity>> = faultDao.getFaultsByStatus(status)
    suspend fun getFaultById(id: Long): FaultEntity? = faultDao.getFaultById(id)
    suspend fun insertFault(fault: FaultEntity): Long = faultDao.insertFault(fault)
    suspend fun updateFault(fault: FaultEntity) = faultDao.updateFault(fault)
    suspend fun deleteFault(fault: FaultEntity) = faultDao.deleteFault(fault)
    suspend fun deleteFaultById(id: Long) = faultDao.deleteFaultById(id)

    // Meter Requests
    val allMeterRequests: Flow<List<MeterRequestEntity>> = meterRequestDao.getAllMeterRequests()
    fun getMeterRequestsByStatus(status: String): Flow<List<MeterRequestEntity>> = meterRequestDao.getMeterRequestsByStatus(status)
    suspend fun getMeterRequestById(id: Long): MeterRequestEntity? = meterRequestDao.getMeterRequestById(id)
    suspend fun insertMeterRequest(request: MeterRequestEntity): Long = meterRequestDao.insertMeterRequest(request)
    suspend fun updateMeterRequest(request: MeterRequestEntity) = meterRequestDao.updateMeterRequest(request)
    suspend fun updateMeterStatus(id: Long, status: String, technician: String?, remarks: String?) =
        meterRequestDao.updateStatusAndAssignment(id, status, technician, remarks)
    suspend fun deleteMeterRequest(request: MeterRequestEntity) = meterRequestDao.deleteMeterRequest(request)

    // Technicians
    val allTechnicians: Flow<List<TechnicianEntity>> = technicianDao.getAllTechnicians()
    val allDistricts: Flow<List<String>> = technicianDao.getAllDistricts()
    fun getTechniciansByDistrict(district: String): Flow<List<TechnicianEntity>> = technicianDao.getTechniciansByDistrict(district)
    suspend fun getTechnicianById(id: Long): TechnicianEntity? = technicianDao.getTechnicianById(id)
    suspend fun insertTechnician(technician: TechnicianEntity): Long = technicianDao.insertTechnician(technician)
    suspend fun incrementTechFaults(id: Long) = technicianDao.incrementFaultCount(id)

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()
    suspend fun insertNotification(notification: NotificationEntity): Long = notificationDao.insertNotification(notification)
    suspend fun markNotificationRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()
    suspend fun clearAllNotifications() = notificationDao.clearAll()
}
