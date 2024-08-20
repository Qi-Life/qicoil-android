package com.Meditation.Sounds.frequencies.lemeor.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.Meditation.Sounds.frequencies.lemeor.data.model.Scalar

@Dao
interface ScalarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scalar: Scalar?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Scalar>?)

    @Delete
    suspend fun deleteListScalar(list: List<Scalar>?)

    @Query("SELECT * FROM scalar ORDER BY `id` ASC")
    fun getLiveDataScalars(): LiveData<List<Scalar>>

    @Query("SELECT * FROM scalar ORDER BY `id` ASC")
    suspend fun getData(): List<Scalar>

    @Query("SELECT * FROM scalar WHERE id=:id")
    fun getScalarById(id: Int) : LiveData<Scalar?>

    @Query("DELETE FROM scalar")
    suspend fun clear()
}
