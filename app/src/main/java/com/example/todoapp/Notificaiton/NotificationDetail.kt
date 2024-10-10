package com.example

import com.example.todoapp.Database.TaskStructure
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDetail(
    val payload : MutableList<TaskStructure>,
    val token : String,
    val deeplink : String
)
