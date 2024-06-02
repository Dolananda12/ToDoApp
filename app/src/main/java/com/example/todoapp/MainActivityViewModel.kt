package com.example.todoapp

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivityViewModel(private val repository: Repository) : ViewModel() {
  val all_days = repository.tasks
  val _dateChanging = MutableLiveData<Int>().apply { value = LocalDateTime.now().dayOfMonth }
  var date_highlighted = MutableLiveData<Int>().apply {_dateChanging!!.value}
  fun retrieveDayTasks(day : Int,month : Int) : LiveData<DayEntitiy> {
      val key = day + 100*month
      return repository.getDay(key)
  }
  fun insertDayTasks(tasks : MutableList<TaskStructure>,month: Int,day: Int){
      val key = day + 100*month
      viewModelScope.launch {
          repository.insertDay(DayEntitiy(key,tasks))
      }
  }
    fun deleteallinsertions(){
        viewModelScope.launch {
            repository.deleteAllEntities()
        }
    }
    fun onChangingDates(date : Int){
        _dateChanging.value=date
    }
    fun setHighlightValue(date: Int){
        date_highlighted.value=date
    }
}