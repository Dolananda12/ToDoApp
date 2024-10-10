package com.example.todoapp.Notificaiton

import com.example.NotificationDetail
import com.example.todoapp.Database.TaskStructure
import okhttp3.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Server {
    @Headers("Content-Type: application/json")
    @POST("/send")
    suspend fun sendTasks(@Body tasks: NotificationDetail): Response<Unit>
}