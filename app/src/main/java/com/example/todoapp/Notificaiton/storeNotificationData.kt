package com.example.todoapp.Notificaiton

import android.content.Context

fun storeNotificationData(context: Context, notificationJson: String) {
    println("storing:$notificationJson")
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("Data", notificationJson)
    editor.apply()
}