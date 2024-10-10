package com.example.todoapp.Notificaiton
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import kotlin.random.Random

fun sendNotification(
    title: String,
    body : String,
    deepLink: String,
    context: Context
){
    val intent = Intent(
        Intent.ACTION_VIEW,
        deepLink.toUri(),
        context,
        MainActivity::class.java
    )
    val pendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(1,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationBuilder = NotificationCompat.Builder(context,Constansts12.PUSH_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.baseline_notifications_24)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    notificationManager.notify(Random.nextInt(),notificationBuilder.build())
}