package com.example.todoapp
import android.content.Context
import android.util.Log
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import com.example.todoapp.Notificaiton.PushNotificationWorker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.time.LocalDateTime

class IncompleteTaskWorker(context: Context,workerParameters: WorkerParameters):Worker(context,workerParameters) {
    private lateinit var taskRepository: Repository
    override fun doWork(): Result {
        println("entered Incomplete Task Worker")
        val dao = DayDatabse.getInstance(context = applicationContext).dao
        taskRepository = Repository(dao)
        val sb = StringBuilder("")
        CoroutineScope(Dispatchers.Main).launch {
           val op= async {
                getIncompleteTasks()
            }.await()
            println("incomplete tasks:${op}")
            if(op.size>0) {
                for (i in 0..<op.size) {
                    sb.append(op[i].heading)
                    sb.append("\n")
                }
                val data = workDataOf(
                    "title" to "Incomplete tasks",
                    "body" to sb.toString() ,
                    "deepLink" to "hi"
                )
                val notificationRequest =
                    OneTimeWorkRequestBuilder<PushNotificationWorker>()
                        .setConstraints(
                            androidx.work.Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .setInputData(data)
                        .build()
                WorkManager.getInstance(applicationContext).enqueue(notificationRequest)
            }else{
                println("no tasks to push")
            }
        }
        return Result.success()
    }
    suspend fun getIncompleteTasks() : MutableList<TaskStructure>{
        val h : MutableList<TaskStructure> = ArrayList()
        val k = CompletableDeferred<MutableList<TaskStructure>>()
        CoroutineScope(Dispatchers.Main).launch {
           val p = async {
               getTask()
           }.await().tasksList
            println("p is ${p.toString()}")
            for(i in 0..p.size-1){
               val l = p[i]
               if(!l.complete){
                   h.add(0,l)
               }
           }
           k.complete(h)
        }
    return k.await()
    }
    private suspend fun getTask() : DayEntitiy {
        val entityRetrieved = CompletableDeferred<DayEntitiy>()
        taskRepository.getDay(LocalDateTime.now().dayOfMonth+100*LocalDateTime.now().monthValue)
            .observeForever {
                if(it!=null){
                    entityRetrieved.complete(it)
                    Log.i("MYTAG", "Tasks are2: ${it.tasksList}")
                }
            }
        return entityRetrieved.await()
    }
}