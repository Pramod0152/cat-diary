package com.petwell.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petwell.data.entity.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("DELETE FROM journal_entry WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)

    @Query("SELECT * FROM journal_entry WHERE pet_id = :petId ORDER BY date DESC")
    fun getEntriesForPet(petId: Long): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entry WHERE pet_id = :petId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesForPetInRange(petId: Long, startDate: Long, endDate: Long): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entry WHERE id = :entryId")
    suspend fun getEntryById(entryId: Long): JournalEntry?

    @Query("DELETE FROM journal_entry WHERE pet_id = :petId")
    suspend fun deleteAllForPet(petId: Long)
}
