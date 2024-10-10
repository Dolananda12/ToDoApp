package com.example.todoapp.Notificaiton

import android.content.Intent
import android.hardware.BatteryState
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.Exception

class PushNotificationService : FirebaseMessagingService() {
    override fun getStartCommandIntent(originalIntent: Intent?): Intent {
        return super.getStartCommandIntent(originalIntent)
    }

    override fun handleIntent(intent: Intent?) {
        super.handleIntent(intent)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(message.data.isNotEmpty()) {
            val title = message.data["title"]
            val description = message.data["description"]
            val deeplink = message.data["deepLink"] ?: ""
            val workerData = workDataOf(
                "title" to title,
                "body" to  description,
                "deeplink" to deeplink
            )
            val notificationRequest = OneTimeWorkRequestBuilder<PushNotificationWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                )
                .setConstraints(Constraints.Builder().setRequiresCharging(true).build())
                .setInputData(workerData)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(notificationRequest)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
    }

    override fun onSendError(msgId: String, exception: Exception) {
        super.onSendError(msgId, exception)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}