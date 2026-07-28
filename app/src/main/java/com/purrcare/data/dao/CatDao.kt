package com.purrcare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.purrcare.data.entity.CatProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: CatProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<CatProfile>): List<Long>

    @Update
    suspend fun update(profile: CatProfile)

    @Delete
    suspend fun delete(profile: CatProfile)

    @Query("SELECT * FROM cat_profile WHERE id = :catId")
    fun getCatById(catId: Long): Flow<CatProfile?>

    @Query("SELECT * FROM cat_profile ORDER BY name ASC")
    fun getAllCats(): Flow<List<CatProfile>>

    @Query("SELECT * FROM cat_profile WHERE id = :catId")
    suspend fun getCatByIdOnce(catId: Long): CatProfile?

    @Query("DELETE FROM cat_profile WHERE id = :catId")
    suspend fun deleteById(catId: Long)
}
