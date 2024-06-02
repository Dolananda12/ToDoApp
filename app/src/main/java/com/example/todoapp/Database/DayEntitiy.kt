package com.example.todoapp.Database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("DayDatabase")
data class DayEntitiy (
    @PrimaryKey val id:Int,
    @ColumnInfo("tasks")
    val tasksList : MutableList<TaskStructure>
)