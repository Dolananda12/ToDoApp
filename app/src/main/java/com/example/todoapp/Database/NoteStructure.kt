package com.example.todoapp.Database

import kotlinx.serialization.Serializable

@Serializable
data class NoteStructure(
    var file_name : String,
    var heading : String,
    var description : String
) : java.io.Serializable
