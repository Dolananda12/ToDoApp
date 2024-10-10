package com.example.todoapp.Notificaiton

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PushNotificationWorker (context: Context,parameters: WorkerParameters) : CoroutineWorker(context,parameters){
    override suspend fun doWork(): Result {
       val title = inputData.getString("title")
       val body = inputData.getString("body")
       val deepLink = inputData.getString("deepLink")
        println("title:$title+description:$body+deepLink:$deepLink")
      sendNotification(title!!,body!!,deepLink!!, context = applicationContext )
    return Result.success()
    }
}