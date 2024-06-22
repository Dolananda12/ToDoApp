package com.example.todoapp.Database

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("DayDatabase")
data class DayEntitiy(
    @PrimaryKey val id:Int,
    @ColumnInfo("tasks")
    var tasksList: MutableList<TaskStructure> = ArrayList()
)