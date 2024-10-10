package com.example.todoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.Database.Notes_Repository
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TokenRepository
import com.example.todoapp.MainActivityViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(private val repository:  Repository,private val repository2: Notes_Repository,private val tokenRepository: TokenRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(repository,repository2,tokenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
