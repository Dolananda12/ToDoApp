package com.example.todoapp.Database

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotesInterface {
    @Insert
    suspend fun insertDay(entitiy:NotesEntitiy)
    @Update
    suspend fun updateDay(entitiy: NotesEntitiy)
    @Delete
    suspend fun deleeteDay(entitiy: NotesEntitiy)
    @Query("SELECT * FROM NotesDatabase")
    fun getDayTasks() : LiveData<MutableList<NotesEntitiy>>
    @Query("SELECT * FROM NotesDatabase WHERE id = :key")
    fun getEntityById(key: Int): LiveData<NotesEntitiy>
    @Query("DELETE FROM NotesDatabase")
    suspend fun deleteAllSubscribers()
}