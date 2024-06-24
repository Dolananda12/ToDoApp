package com.example.todoapp.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface DayDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(entitiy: DayEntitiy)
    @Update
    suspend fun updateDay(entitiy: DayEntitiy)
    @Delete
    suspend fun deleeteDay(entitiy: DayEntitiy)
    @Query("SELECT * FROM DAYDATABASE")
    fun getDayTasks() : LiveData<List<DayEntitiy>>
    @Query("SELECT * FROM DAYDATABASE WHERE id = :key")
     fun getEntityById(key: Int): LiveData<DayEntitiy>
    @Query("DELETE FROM DAYDATABASE")
    suspend fun deleteAllSubscribers()

}