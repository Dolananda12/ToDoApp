package com.example.todoapp.Notificaiton

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.SystemClock
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.Schedulers
import androidx.work.workDataOf
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import com.example.todoapp.Database.TokenDatabase
import com.example.todoapp.Database.TokenRepository
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import kotlin.math.abs

class ScheduleNotificationsWorker(context: Context,workerParameters: WorkerParameters):Worker(context,workerParameters) {
    private lateinit var taskRepository: Repository
    override fun doWork(): Result {
        val b = getNotficationData(applicationContext)
        println("entered schedule notifications")
        if(b!=null) {
            if (b.hour != -1 && b.min != -1) {
                println("${b.hour + b.min + LocalDateTime.now().hour + LocalDateTime.now().minute}+dola")
                if (b.hour == LocalDateTime.now().hour && (abs(b.min - LocalDateTime.now().minute) < 5)) {
                    val data = workDataOf(
                        "title" to b.task.heading,
                        "body" to b.task.description,
                        "deepLink" to b.task.links.toString()
                    )
                    val notificationRequest =
                        OneTimeWorkRequestBuilder<PushNotificationWorker>()
                            .setConstraints(
                                androidx.work.Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                            .setInputData(data)
                            .build()
                    WorkManager.getInstance(applicationContext).enqueue(notificationRequest)
                    val taskDatabase = DayDatabse.getInstance(applicationContext)
                    val dayDAO = taskDatabase.dao
                    taskRepository = Repository(dayDAO)
                    onSuccess(taskRepository = taskRepository, context = applicationContext)
                } else {
                    println("time not matched")
                }
            }else{
                println("already sent notification")
            }
        }else {
            println("no data found for task scheduling!")
        }
    return Result.success()
    }
}