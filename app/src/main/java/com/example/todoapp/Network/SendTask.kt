package com.example.todoapp.Network

import com.example.NotificationDetail
import okhttp3.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SendTask {
@POST("/sendTask")
suspend fun sendTask(@Body notificationDetail: NotificationDetail) : Response<String>
}