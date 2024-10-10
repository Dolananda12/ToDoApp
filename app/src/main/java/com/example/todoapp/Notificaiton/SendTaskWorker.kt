package com.example.todoapp.Notificaiton

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.NotificationDetail
import com.example.todoapp.Network.SendTask
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json

class SendTaskWorker(context: Context,params: WorkerParameters) :CoroutineWorker(context,params){
    override suspend fun doWork(): Result {
        val taskData = inputData.getString("Notification")
        println("data to be sent:$taskData")
        taskData?.let {
            val apiService = RetrofitInstance.getRetrofitInstance().create(SendTask::class.java)
            val notificationDetail = GsonBuilder().create().fromJson(taskData,NotificationDetail::class.java)
            val cached_detail : NotificationDetail= Json.decodeFromString(getNotificationData(applicationContext)!!)  //converting string to class
            if (getNotificationData(applicationContext)?.isNotEmpty() == true) {
                if(cached_detail!=notificationDetail) {
                    storeNotificationData(applicationContext,Json.encodeToString(NotificationDetail.serializer(),notificationDetail))
                    try {
                        val response = apiService.sendTask(notificationDetail)
                        if (response.code()==200) {
                            println("success:${response.body()}")
                        } else {
                            println("failure:${response.errorBody()}")
                        }
                    } catch (e: Exception) {
                        println("sending failed")
                        e.printStackTrace()
                    }
                }else{
                    println("duplicate request with payload:$cached_detail")
                }
            } else {
                println("Error: Empty or null JSON string")
            }
        }
        return Result.success()
    }
    fun storeNotificationData(context: Context, notificationJson: String) {
        println("storing:$notificationJson")
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("notification_data", notificationJson)
        editor.apply()
    }
    suspend fun getNotificationData(context: Context): String? {
        val data = CompletableDeferred("")
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("notification_data", null)
    }
}