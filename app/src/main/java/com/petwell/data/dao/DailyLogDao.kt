package com.petwell.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petwell.data.entity.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DailyLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DailyLog>): List<Long>

    @Update
    suspend fun update(log: DailyLog)

    @Delete
    suspend fun delete(log: DailyLog)

    @Query("SELECT * FROM daily_log WHERE pet_id = :petId ORDER BY timestamp DESC")
    fun getLogsForPet(petId: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE pet_id = :petId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogs(petId: Long, limit: Int): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE pet_id = :petId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsForPetInRange(petId: Long, startTime: Long, endTime: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE id = :logId")
    fun getLogById(logId: Long): Flow<DailyLog?>

    @Query("SELECT * FROM daily_log WHERE id = :logId")
    suspend fun getLogByIdOnce(logId: Long): DailyLog?

    @Query("DELETE FROM daily_log WHERE id = :logId")
    suspend fun deleteById(logId: Long)

    @Query("DELETE FROM daily_log WHERE pet_id = :petId")
    suspend fun deleteAllForPet(petId: Long)

    @Query("SELECT * FROM daily_log WHERE pet_id = :petId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentLogForPet(petId: Long): DailyLog?

    @Query("SELECT * FROM daily_log WHERE pet_id = :petId AND timestamp BETWEEN :dayStart AND :dayEnd LIMIT 1")
    suspend fun getLogForPetOnDay(petId: Long, dayStart: Long, dayEnd: Long): DailyLog?
}
