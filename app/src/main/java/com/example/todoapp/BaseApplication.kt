package com.example.todoapp

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.NotificationDetail
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TokenDatabase
import com.example.todoapp.Database.TokenEntity
import com.example.todoapp.Database.TokenRepository
import com.example.todoapp.Notificaiton.Constansts12
import com.example.todoapp.Notificaiton.SendTaskWorker
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BaseApplication : Application(){
   private lateinit var tokenRepository: TokenRepository
   private lateinit var taskRepository: Repository
    override fun onCreate() {
        super.onCreate()
        val tokenDatabase= TokenDatabase.getInstance(this)
        val tokenDAO=tokenDatabase.dao
        tokenRepository=TokenRepository(tokenDAO)
        val taskDatabase =DayDatabse.getInstance(this)
        val dayDAO=taskDatabase.dao
        storeNotificationData(this, Json.encodeToString<NotificationDetail>(
            NotificationDetail(ArrayList(), "2", "2")
        ))
        taskRepository=Repository(dayDAO)
        val s = applicationContext.getSharedPreferences("app_preferences", MODE_PRIVATE)
        if(s.getString("permission",null)=="yes"){
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(notificationManager)
            val workRequest = PeriodicWorkRequestBuilder<IncompleteTaskWorker>(
                repeatInterval = 7,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(15)
            ).build()
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueue(workRequest).state.observeForever {
                println("worker:$it")
            }
        }
    }
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constansts12.PUSH_NOTIFICATION_CHANNEL_ID,
                Constansts12.PUSH_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        } else {
            return
        }
    }
    fun storeNotificationData(context: Context, notificationJson: String) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("notification_data", notificationJson)
        println("stored in RAM")
        editor.apply()
    }
}