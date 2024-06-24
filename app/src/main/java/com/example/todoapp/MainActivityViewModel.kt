package com.example.todoapp

import android.content.Context
import android.content.Entity
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivityViewModel(private val repository: Repository) : ViewModel() {
    var dayEntitiy: DayEntitiy? = null
   var date_chaned=false
   var link : MutableList<String> = ArrayList()
   var photo : MutableList<String> = ArrayList()
   var pay : MutableList<Pair<String,String>> = ArrayList()
   var heading =""
   var description = ""
   val all_days = repository.tasks
   val _dateChanging = MutableLiveData<Int>().apply { value = LocalDateTime.now().dayOfMonth }
   var date_highlighted = MutableLiveData<Int>()
   fun set_entity(dayEntitiy: DayEntitiy?){
      this.dayEntitiy =dayEntitiy
      println("entity passed:"+dayEntitiy)
      if (dayEntitiy!=null){
          println("size")
      }
   }
    fun set_date_changed_1(yes : Boolean){
        date_chaned=yes
    }
    fun get_date_chaged1():Boolean{
        return this.date_chaned
    }
   fun get_entitiy():DayEntitiy?{
       return this.dayEntitiy
   }
   fun retrieveDayTasks(day : Int,month : Int) :LiveData<DayEntitiy> {
      val key = day + 100*month
      return repository.getDay(key)
  }
  fun set_link(list : MutableList<String>){
      link=list
  }
  fun set_pay(list : MutableList<Pair<String,String>>){
      pay=list
  }
  fun set_photo(list : MutableList<String>){
      photo=list
  }
  fun set_titile(list : String){
      heading=list
  }
  fun set_description(list : String){
      description=list
  }
  fun insertDayTasks(month: Int,day: Int) : LiveData<Boolean>{
      val task=TaskStructure(heading,description,false,photo,link,pay)
      val done= MutableLiveData<Boolean>(false)
        val key = day + 100*month
        val entitiy =  dayEntitiy
      println("insert:0")
      if(entitiy!=null){
          println("insert:1")
          val tasks = entitiy.tasksList
            tasks.add(0,task)
            viewModelScope.launch {
                if(repository.insertDay(DayEntitiy(key,tasks))){
                    done.value=true
                }
            }
        }else{
          println("insert:2")
          val tasks : MutableList<TaskStructure> = ArrayList()
            tasks.add(task)
            viewModelScope.launch {
                if(repository.insertDay(DayEntitiy(key,tasks))){
                    done.value=true
                }
            }
        }
      return done
  }

    fun reset(){
        link.clear()
        pay.clear()
        photo.clear()
        description=""
        heading=""
    }
    fun deleteallinsertions(){
        viewModelScope.launch {
            repository.deleteAllEntities()
        }
    }
    fun updateEntity(entitiy: DayEntitiy){
        viewModelScope.launch {
            repository.updateDay(entitiy)
        }
    }

     fun change(dayEntitiy: DayEntitiy, index:Int, change:Boolean){
        viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).async {
                sort(dayEntitiy,dayEntitiy.tasksList,index,change)
            }.await()
        }
    }
    fun sort(entitiy: DayEntitiy,tasklist: MutableList<TaskStructure>,index:Int,change:Boolean){
        entitiy.tasksList[index].complete=change
        var p=index
        val n=tasklist.size
        var h=index
        val task=tasklist[index]
        if(change) {
            Log.i("MYTAG","if block")
            while (p < n) {
                if (p < n - 1)
                    if (!tasklist[p + 1].complete) { //false
                        Log.i("MYTAG","1"+p.toString())
                        p = p + 1
                        h=p+1
                    }else{
                        Log.i("MYTAG","2")
                        break
                    }
                else
                    break
            }
            tasklist.add(h,tasklist[index])
            tasklist.removeAt(index)
        }else{
            var p1 = index
            Log.i("MYTAG","else block")
            while (p1>0){
                if(p1>=1){
                    if(tasklist[p1-1].complete){
                        p1=p1-1
                    }else{
                        break
                    }
                }else{
                    break
                }
            }
            tasklist.add(p1,task)
            tasklist.removeAt(index+1)
        }
        Log.i("MYTAG",tasklist.toString())
        entitiy.tasksList=tasklist
        updateEntity(entitiy)
    }
    fun onChangingDates(date : Int){
        _dateChanging.value=date
    }
    fun setHighlightValue(date: Int){
        date_highlighted.value=date
    }
    fun delete_task(dayEntitiy: DayEntitiy,index : Int){
        viewModelScope.launch {
            val tasks= dayEntitiy.tasksList
            tasks.removeAt(index)
            dayEntitiy.tasksList=tasks
            repository.insertDay(dayEntitiy)
        }
    }
}