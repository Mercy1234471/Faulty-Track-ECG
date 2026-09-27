package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FaultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FaultDao {
    @Query("SELECT * FROM faults ORDER BY reportedTimestamp DESC")
    fun getAllFaults(): Flow<List<FaultEntity>>

    @Query("SELECT * FROM faults ORDER BY reportedTimestamp DESC LIMIT 5")
    fun getRecentFaults(): Flow<List<FaultEntity>>

    @Query("SELECT * FROM faults WHERE status = :status ORDER BY reportedTimestamp DESC")
    fun getFaultsByStatus(status: String): Flow<List<FaultEntity>>

    @Query("SELECT * FROM faults WHERE id = :id")
    suspend fun getFaultById(id: Long): FaultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFault(fault: FaultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFaults(faults: List<FaultEntity>)

    @Update
    suspend fun updateFault(fault: FaultEntity)

    @Delete
    suspend fun deleteFault(fault: FaultEntity)

    @Query("DELETE FROM faults WHERE id = :id")
    suspend fun deleteFaultById(id: Long)

    @Query("SELECT COUNT(*) FROM faults")
    fun getTotalFaultsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM faults WHERE status = :status")
    fun getFaultsCountByStatus(status: String): Flow<Int>
}
