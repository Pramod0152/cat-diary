package com.purrcare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.purrcare.data.entity.Medication
import com.purrcare.data.entity.MedicationLog
import kotlinx.coroutines.flow.Flow

data class MedicationWithLogs(
    val medication: Medication,
    val logs: List<MedicationLog>
)

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>): List<Long>

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medication WHERE cat_id = :catId ORDER BY med_name ASC")
    fun getMedicationsForCat(catId: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medication WHERE cat_id = :catId AND is_enabled = 1 ORDER BY med_name ASC")
    fun getEnabledMedicationsForCat(catId: Long): Flow<List<Medication>>

    @Query("SELECT * FROM medication WHERE id = :medicationId")
    fun getMedicationById(medicationId: Long): Flow<Medication?>

    @Query("SELECT * FROM medication WHERE id = :medicationId")
    suspend fun getMedicationByIdOnce(medicationId: Long): Medication?

    @Query("DELETE FROM medication WHERE id = :medicationId")
    suspend fun deleteById(medicationId: Long)

    @Query("DELETE FROM medication WHERE cat_id = :catId")
    suspend fun deleteAllForCat(catId: Long)

    @Transaction
    @Query("SELECT * FROM medication WHERE id = :medicationId")
    fun getMedicationWithLogs(medicationId: Long): Flow<MedicationWithLogs?>

    @Transaction
    @Query("SELECT * FROM medication WHERE cat_id = :catId ORDER BY med_name ASC")
    fun getMedicationsWithLogsForCat(catId: Long): Flow<List<MedicationWithLogs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<MedicationLog>): List<Long>

    @Update
    suspend fun updateLog(log: MedicationLog)

    @Delete
    suspend fun deleteLog(log: MedicationLog)

    @Query("SELECT * FROM medication_log WHERE medication_id = :medicationId ORDER BY timestamp DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_log WHERE medication_id = :medicationId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogsForMedication(medicationId: Long, limit: Int): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_log WHERE medication_id = :medicationId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsForMedicationInRange(medicationId: Long, startTime: Long, endTime: Long): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_log WHERE id = :logId")
    suspend fun getLogByIdOnce(logId: Long): MedicationLog?

    @Query("DELETE FROM medication_log WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("DELETE FROM medication_log WHERE medication_id = :medicationId")
    suspend fun deleteAllLogsForMedication(medicationId: Long)
}
