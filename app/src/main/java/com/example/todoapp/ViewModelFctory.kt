package com.example.todoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.Database.Notes_Repository
import com.example.todoapp.Database.Repository
import com.example.todoapp.MainActivityViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(private val repository:  Repository,private val repository2: Notes_Repository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(repository,repository2) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
