package com.example.todoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
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
import com.example.todoapp.Notificaiton.ScheduleNotificationsWorker
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
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BaseApplication : Application(){
   private lateinit var tokenRepository: TokenRepository
   private lateinit var taskRepository: Repository
   var token =""
    private lateinit var tasks : DayEntitiy
    override fun onCreate() {
        super.onCreate()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        val tokenDatabase= TokenDatabase.getInstance(this)
        val tokenDAO=tokenDatabase.dao
        tokenRepository=TokenRepository(tokenDAO)
        val taskDatabase =DayDatabse.getInstance(this)
        val dayDAO=taskDatabase.dao
        storeNotificationData(this, Json.encodeToString<NotificationDetail>(
            NotificationDetail(ArrayList(), "2", "2")
        ))
        taskRepository=Repository(dayDAO)
        //ScheduleTask(this)
        //scheduleTasks(applicationContext)
        /*val dailyWorkRequest = OneTimeWorkRequestBuilder<IncompleteTaskWorker>()
            .build()
        WorkManager.getInstance(this).enqueue(dailyWorkRequest).state.observeForever {
            println("state of worker: $it")
        }*/
    }
    private fun scheduleTasks(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val periodicWorkRequest = PeriodicWorkRequestBuilder<ScheduleNotificationsWorker>(
            2, TimeUnit.SECONDS
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "ScheduleNotifications",
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
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
    private suspend fun getToken(): String {
        val tokenRetrieved = CompletableDeferred<String>("")
        try{
            tokenRepository.token.observeForever { tokenList ->
                if (tokenList != null&&tokenList.size>0) {
                    token = tokenList[0].token
                    tokenRetrieved.complete(token)
                    Log.i("MYTAG", "Token is: $token")
                }else{
                    insertToken(applicationContext)
                }
            }
        }catch (e : Exception){
            insertToken(applicationContext)
        }
        return tokenRetrieved.await()
    }
    fun insertToken(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token= FirebaseMessaging.getInstance().token.await()
                println("token is $token")
                tokenRepository.insert(TokenEntity(0,token.toString()))
            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(context, "$e", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private suspend fun getTask() : DayEntitiy{
        val entityRetrieved = CompletableDeferred<DayEntitiy>()
        taskRepository.getDay(LocalDateTime.now().dayOfMonth+100*LocalDateTime.now().monthValue)
            .observeForever {
                println("getting:$it")
                if(it!=null){
                    entityRetrieved.complete(it)
                    Log.i("MYTAG", "Tasks are: ${it.tasksList}")
                }
            }
        return entityRetrieved.await()
    }
    fun storeNotificationData(context: Context, notificationJson: String) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("notification_data", notificationJson)
        println("stored in RAM")
        editor.apply()
    }
    private fun ScheduleTask(context: Context){
        CoroutineScope(Dispatchers.Main).launch {
            token = async {
                getToken()
            }.await()
            val h =async {
                getTask()
            }.await()
            val notification = NotificationDetail(h.tasksList, token,"")
            Log.i("MYTAG","sending data:${notification}")
            val notificationJson = GsonBuilder().create().toJson(notification)
            val taskData = Data.Builder()
                .putString("Notification", notificationJson)
                .build()
            /*try {
                WorkManager.getInstance(context).enqueue(oneTimerequest)
            }catch (e : Exception){
                e.printStackTrace()
            }*/
        }
    }
}