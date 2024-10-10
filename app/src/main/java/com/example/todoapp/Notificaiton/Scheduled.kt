package com.example.todoapp.Notificaiton

import kotlinx.serialization.Serializable

@Serializable
data class Scheduled (
  val list : MutableList<SchedulerObject>
)