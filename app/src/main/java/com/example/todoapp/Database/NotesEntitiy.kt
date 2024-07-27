package com.example.todoapp.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("NotesDatabase")
data class  NotesEntitiy(
    @PrimaryKey val id:Int,
    @ColumnInfo("notes")
    var NoteStructure : NoteStructure
)