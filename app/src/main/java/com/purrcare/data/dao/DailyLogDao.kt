package com.purrcare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.purrcare.data.entity.DailyLog
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

    @Query("SELECT * FROM daily_log WHERE cat_id = :catId ORDER BY timestamp DESC")
    fun getLogsForCat(catId: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE cat_id = :catId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogs(catId: Long, limit: Int): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE cat_id = :catId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsForCatInRange(catId: Long, startTime: Long, endTime: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE id = :logId")
    fun getLogById(logId: Long): Flow<DailyLog?>

    @Query("SELECT * FROM daily_log WHERE id = :logId")
    suspend fun getLogByIdOnce(logId: Long): DailyLog?

    @Query("DELETE FROM daily_log WHERE id = :logId")
    suspend fun deleteById(logId: Long)

    @Query("DELETE FROM daily_log WHERE cat_id = :catId")
    suspend fun deleteAllForCat(catId: Long)

    @Query("SELECT * FROM daily_log WHERE cat_id = :catId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecentLogForCat(catId: Long): DailyLog?

    @Query("SELECT * FROM daily_log WHERE cat_id = :catId AND timestamp BETWEEN :dayStart AND :dayEnd LIMIT 1")
    suspend fun getLogForCatOnDay(catId: Long, dayStart: Long, dayEnd: Long): DailyLog?
}
