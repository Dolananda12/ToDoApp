package com.example.todoapp.Notificaiton

import com.example.todoapp.Database.TaskStructure
import kotlinx.serialization.Serializable

@Serializable
data class SchedulerObject(
    val hour : Int,
    val min : Int,
    val task : TaskStructure
)
