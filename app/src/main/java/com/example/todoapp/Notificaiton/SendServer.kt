package com.example.todoapp.Notificaiton

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class SendServer(context: Context,workerParameters: WorkerParameters) : Worker(context,workerParameters) {
    override fun doWork(): Result {//deferrable tasks
        try {
            for(i in 0..6000){
                println("dola:$i")
            }
        }catch (e : Exception){
            return Result.failure()
        }
    return Result.success()
    }
}