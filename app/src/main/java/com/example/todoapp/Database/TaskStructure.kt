package com.example.todoapp.Database

data class TaskStructure (
    val heading : String,
    val description : String,
    var complete : Boolean,
    val photos : MutableList<String>,
    val links : MutableList<String>,
    val pay : MutableList<Pair<String,String>>
)