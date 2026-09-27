package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MeterRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterRequestDao {
    @Query("SELECT * FROM meter_requests ORDER BY submittedTimestamp DESC")
    fun getAllMeterRequests(): Flow<List<MeterRequestEntity>>

    @Query("SELECT * FROM meter_requests WHERE status = :status ORDER BY submittedTimestamp DESC")
    fun getMeterRequestsByStatus(status: String): Flow<List<MeterRequestEntity>>

    @Query("SELECT * FROM meter_requests WHERE id = :id")
    suspend fun getMeterRequestById(id: Long): MeterRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeterRequest(request: MeterRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMeterRequests(requests: List<MeterRequestEntity>)

    @Update
    suspend fun updateMeterRequest(request: MeterRequestEntity)

    @Delete
    suspend fun deleteMeterRequest(request: MeterRequestEntity)

    @Query("UPDATE meter_requests SET status = :status, assignedTechnician = :technician, adminRemarks = :remarks WHERE id = :id")
    suspend fun updateStatusAndAssignment(id: Long, status: String, technician: String?, remarks: String?)

    @Query("SELECT COUNT(*) FROM meter_requests")
    fun getTotalRequestsCount(): Flow<Int>
}
