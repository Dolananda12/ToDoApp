package com.example.todoapp.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("TokenDatabase")
data class TokenEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    @ColumnInfo("token")
    val token :String
)
