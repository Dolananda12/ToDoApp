package com.example.todoapp.Database

import androidx.lifecycle.LiveData
import androidx.room.Insert

class Repository (private val dao: DayDAO){
    val tasks = dao.getDayTasks()

    suspend fun insertDay(entitiy: DayEntitiy):Boolean{
           dao.insertDay(entitiy)
        println("inserted successfully")
        return true
    }
    suspend fun updateDay(entitiy: DayEntitiy){
        println("updating!")
        dao.updateDay(entitiy)
    }
    suspend fun deleteDay(entitiy: DayEntitiy){
        dao.deleeteDay(entitiy)
    }
     fun getDay(key : Int) :LiveData<DayEntitiy>{
         return dao.getEntityById(key)
    }
    suspend fun deleteAllEntities(){
        dao.deleteAllSubscribers()
    }
}