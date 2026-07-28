package com.petwell.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petwell.data.entity.PetProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PetProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<PetProfile>): List<Long>

    @Update
    suspend fun update(profile: PetProfile)

    @Delete
    suspend fun delete(profile: PetProfile)

    @Query("SELECT * FROM pet_profile WHERE id = :petId")
    fun getPetById(petId: Long): Flow<PetProfile?>

    @Query("SELECT * FROM pet_profile ORDER BY name ASC")
    fun getAllPets(): Flow<List<PetProfile>>

    @Query("SELECT * FROM pet_profile WHERE id = :petId")
    suspend fun getPetByIdOnce(petId: Long): PetProfile?

    @Query("DELETE FROM pet_profile WHERE id = :petId")
    suspend fun deleteById(petId: Long)
}
