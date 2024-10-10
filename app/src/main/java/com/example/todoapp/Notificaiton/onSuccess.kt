package com.example.todoapp.Notificaiton
import android.content.Context
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

 suspend fun getTasks(taskRepository: Repository): MutableList<TaskStructure> {
    val taskRetrieved = CompletableDeferred<MutableList<TaskStructure>>(ArrayList())
    try{
        taskRepository.getDay(LocalDateTime.now().dayOfMonth+100* LocalDateTime.now().monthValue).observeForever { taskList ->
            if (taskList != null){
                taskRetrieved.complete(taskList.tasksList)
            }
        }
    }catch (e : Exception){
       e.printStackTrace()
    }
    return taskRetrieved.await()
}
fun onSuccess(taskRepository : Repository,context: Context){
    var s : MutableList<TaskStructure> = ArrayList()
    var index_2 = -1
    CoroutineScope(Dispatchers.Main).launch {
        s = async {
            getTasks(taskRepository)
        }.await()
    }
    if(s.size>0){
        if(s.size==1){
            if(LocalDateTime.now().hour*1000+LocalDateTime.now().minute<s[0].timeHour*1000+s[0].timeMin) {
                index_2=0
                val schedulerObject = SchedulerObject(s[0].timeHour, s[0].timeMin, s[0])
                storeNotificationData(
                    context,
                    Json.encodeToString(SchedulerObject.serializer(), schedulerObject)
                )
            }
        }else{
            val  tasklist = s
            var index = -1
            var t = -1
            val h = LocalDateTime.now().minute + 1000*LocalDateTime.now().hour
            for (i in 0..<tasklist.size) {
                val t_i = 1000 * tasklist[i].timeHour + tasklist[i].timeMin
                if (t_i > h) {
                    t = t_i
                    index = i
                }
            }
            for (i in 0..<tasklist.size) {
                val t_i = 1000 * tasklist[i].timeHour + tasklist[i].timeMin
                if (t_i > h && t > t_i) {
                    t = t_i
                    index = i
                }
            }
            if(index!=-1) {
                val schedulerObject = SchedulerObject(s[index].timeHour, s[index].timeMin, s[index])
                storeNotificationData(
                    context,
                    Json.encodeToString(SchedulerObject.serializer(), schedulerObject)
                )
                index_2=0
            }
        }
    }
    if(index_2==-1){
        storeNotificationData(context,Json.encodeToString(SchedulerObject.serializer(),
            SchedulerObject(-1,-1,TaskStructure())
        ))
        println("no replacement found")
    }
}