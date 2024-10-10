package com.example.todoapp.Database

import android.media.session.MediaSession.Token
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TokenDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(tokenEntity: TokenEntity)
    @Update
    suspend fun updateToken(tokenEntity: TokenEntity)
    @Query("DELETE FROM TokenDatabase")
    suspend fun deleteAll()
    @Query("SELECT * FROM TokenDatabase")
    fun getToken() : LiveData<List<TokenEntity>>
}