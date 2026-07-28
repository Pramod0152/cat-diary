package com.petwell.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.PetReminderLog
import kotlinx.coroutines.flow.Flow

data class ReminderWithLogs(
    @Embedded val reminder: PetReminder,
    @Relation(
        parentColumn = "id",
        entityColumn = "reminder_id"
    )
    val logs: List<PetReminderLog>
)

@Dao
interface PetReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: PetReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<PetReminder>): List<Long>

    @Update
    suspend fun update(reminder: PetReminder)

    @Delete
    suspend fun delete(reminder: PetReminder)

    @Query("SELECT * FROM pet_reminder WHERE pet_id = :petId ORDER BY title ASC")
    fun getRemindersForPet(petId: Long): Flow<List<PetReminder>>

    @Query("SELECT * FROM pet_reminder WHERE pet_id = :petId AND is_enabled = 1 ORDER BY title ASC")
    fun getEnabledRemindersForPet(petId: Long): Flow<List<PetReminder>>

    @Query("SELECT * FROM pet_reminder WHERE id = :reminderId")
    fun getReminderById(reminderId: Long): Flow<PetReminder?>

    @Query("SELECT * FROM pet_reminder WHERE id = :reminderId")
    suspend fun getReminderByIdOnce(reminderId: Long): PetReminder?

    @Query("DELETE FROM pet_reminder WHERE id = :reminderId")
    suspend fun deleteById(reminderId: Long)

    @Query("DELETE FROM pet_reminder WHERE pet_id = :petId")
    suspend fun deleteAllForPet(petId: Long)

    @Transaction
    @Query("SELECT * FROM pet_reminder WHERE id = :reminderId")
    fun getReminderWithLogs(reminderId: Long): Flow<ReminderWithLogs?>

    @Transaction
    @Query("SELECT * FROM pet_reminder WHERE pet_id = :petId ORDER BY title ASC")
    fun getRemindersWithLogsForPet(petId: Long): Flow<List<ReminderWithLogs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PetReminderLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<PetReminderLog>): List<Long>

    @Update
    suspend fun updateLog(log: PetReminderLog)

    @Delete
    suspend fun deleteLog(log: PetReminderLog)

    @Query("SELECT * FROM pet_reminder_log WHERE reminder_id = :reminderId ORDER BY timestamp DESC")
    fun getLogsForReminder(reminderId: Long): Flow<List<PetReminderLog>>

    @Query("SELECT * FROM pet_reminder_log WHERE reminder_id = :reminderId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestLogsForReminder(reminderId: Long, limit: Int): Flow<List<PetReminderLog>>

    @Query("SELECT * FROM pet_reminder_log WHERE reminder_id = :reminderId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsForReminderInRange(reminderId: Long, startTime: Long, endTime: Long): Flow<List<PetReminderLog>>

    @Query("SELECT * FROM pet_reminder_log WHERE id = :logId")
    suspend fun getLogByIdOnce(logId: Long): PetReminderLog?

    @Query("DELETE FROM pet_reminder_log WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("DELETE FROM pet_reminder_log WHERE reminder_id = :reminderId")
    suspend fun deleteAllLogsForReminder(reminderId: Long)

    @Query("""
        SELECT rl.* FROM pet_reminder_log rl
        INNER JOIN pet_reminder r ON rl.reminder_id = r.id
        WHERE r.pet_id = :petId AND rl.timestamp BETWEEN :startTime AND :endTime
        ORDER BY rl.timestamp DESC
    """)
    suspend fun getLogsForPetInRange(petId: Long, startTime: Long, endTime: Long): List<PetReminderLog>
}
