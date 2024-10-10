package com.example.todoapp.Database
/*
import com.google.type.DateTime*/
import kotlinx.serialization.Serializable/*
import org.checkerframework.checker.units.qual.Time*/

@Serializable
data class TaskStructure (
    val heading : String="",
    val description : String="",
    var complete : Boolean=false,
    val photos : MutableList<String> = ArrayList(),
    val links : MutableList<String> = ArrayList(),
    val pay : MutableList<Pair<String,String>> = ArrayList(),
    val timeMin :Int = -1,
    val timeHour :Int= -1
)