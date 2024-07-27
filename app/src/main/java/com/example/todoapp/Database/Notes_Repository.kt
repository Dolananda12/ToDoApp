package com.example.todoapp.Database

import androidx.lifecycle.LiveData

class Notes_Repository(private val dao: NotesInterface) {
    val tasks = dao.getDayTasks()

    suspend fun insertDay(entitiy: NotesEntitiy):Boolean{
        dao.insertDay(entitiy)
        println("inserted successfully")
        return true
    }
    suspend fun updateDay(entitiy: NotesEntitiy){
        println("updating!"+entitiy)
        dao.updateDay(entitiy)
    }
    suspend fun deleteDay(entitiy: NotesEntitiy){
        dao.deleeteDay(entitiy)
    }
    fun getDay(key : Int) : LiveData<NotesEntitiy> {
        return dao.getEntityById(key)
    }
    suspend fun deleteAllEntities(){
        dao.deleteAllSubscribers()
    }
}