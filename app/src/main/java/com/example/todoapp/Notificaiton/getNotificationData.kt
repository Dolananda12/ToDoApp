package com.example.todoapp.Notificaiton

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json

fun getNotficationData(context: Context): SchedulerObject?{
    val s =  context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    val h = s.getString("Data",null)
    var data : SchedulerObject? = null
    if(h!=null){
        Log.i("MYTAG","data stored is: $h")
        data  = Json.decodeFromString(SchedulerObject.serializer(),h)
    }else{
        println("nothing stored in RAM add tasks")
    }
    return data
}