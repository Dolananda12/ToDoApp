package com.example.todoapp

import android.content.Context
import android.content.Entity
import android.database.SQLException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.provider.ContactsContract.CommonDataKinds.Note
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
import com.example.todoapp.Database.NoteStructure
import com.example.todoapp.Database.NotesEntitiy
import com.example.todoapp.Database.Notes_Repository
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivityViewModel(private val repository: Repository,private val notesRepository: Notes_Repository) : ViewModel() {
    var dayEntitiy: DayEntitiy? = null
   var date_chaned=false
    var present_date_1=0
    var mode=false
    var present_date =0
    var present_month =0
    var present_month_1=0
    var date_higlighted=0
    var month_highlighted=0
    var updating = false
    var pop = false
    var link : MutableList<String> = ArrayList()
   var photo : MutableList<String> = ArrayList()
   var pay : MutableList<Pair<String,String>> = ArrayList()
   var heading =""
   var description = ""
    val notes = notesRepository.tasks
    var index=0
   val all_days = repository.tasks
   val _dateChanging = MutableLiveData<Int>().apply { value = LocalDateTime.now().dayOfMonth }
   var date_highlighted = MutableLiveData<Int>()
    var taskStructureForDisplay = TaskStructure("","",false,ArrayList(),ArrayList(),ArrayList())
    fun reset_tasks(){
        taskStructureForDisplay =TaskStructure("","",false,ArrayList(),ArrayList(),ArrayList())
    }
    fun init(){
        present_date = getTodayMonthDate().first
        present_month = getTodayMonthDate().second
        month_highlighted=present_month
        date_higlighted=present_date
    }
   fun set_entity(dayEntitiy: DayEntitiy?){
       if(dayEntitiy!=null) {
           if (dayEntitiy.id == (month_highlighted * 100 + date_higlighted))
               this.dayEntitiy = dayEntitiy
           println("entity passed:"+dayEntitiy)
       }else{
           this.dayEntitiy = dayEntitiy
           println("entity passed:"+dayEntitiy)
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
      Log.i("MYTAG","1:"+list.toString())
      link=list
  }
  fun set_pay(list : MutableList<Pair<String,String>>){
      Log.i("MYTAG","2"+list.toString())
      pay=list
  }
  fun set_photo(list : MutableList<String>){
      Log.i("MYTAG","3"+list.toString())
      photo=list
  }
  fun set_titile(list : String){
      Log.i("MYTAG","4"+list)
      heading=list
  }
  fun set_description(list : String){
      Log.i("MYTAG","5"+list)
      description=list
  }
  fun insertDayTasks(month: Int,day: Int) : LiveData<Boolean>{
      val task=TaskStructure(heading,description,false,photo,link,pay)
      val done= MutableLiveData<Boolean>(false)
        val key = day + 100*month
        val entitiy =  dayEntitiy
      println("insert:0"+entitiy)
      if(entitiy!=null){
          println("insert:1")
          val tasks = entitiy.tasksList
            viewModelScope.launch {
                 if(mode){
                     tasks[index]=task
                     entitiy.tasksList=tasks
                     index=0
                 updateEntity(entitiy).observeForever{
                     if(it){
                         done.value=true
                     }
                 }
                 }else {
                     tasks.add(0,task)
                     entitiy.tasksList=tasks
                     flow {
                         emit(repository.insertDay(entitiy))
                     }.onCompletion { cause ->
                         if (cause == null) {
                             Log.i("MYTAG","doneinsertion")
                             done.value=true
                         } else {
                             done.value=false
                             cause.printStackTrace()
                         }
                     }.catch { e ->
                         done.value=false
                         e.printStackTrace()
                     }.collect()
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
    fun databaseMigration(dayEntitiy: DayEntitiy,index: Int):LiveData<Boolean>{
        mode=false
        val task = dayEntitiy.tasksList.get(index)
        set_link(task.links)
        set_pay(task.pay)
        set_description(task.description)
        set_titile(task.heading)
        set_photo(task.photos)
        Log.i("MYTAG",dayEntitiy.toString())
        val a = MutableLiveData<Boolean>(false)
        viewModelScope.launch {
            flow {
                emit(insertDayTasks(month_highlighted,date_higlighted))
            }.
            onCompletion { cause ->
                if(cause==null){
                    a.value=true
                }
            }.collect()
        }
    return a
    }
    fun getTodayMonthDate(): Pair<Int, Int> {
        val date_month = LocalDateTime.now().dayOfMonth
        val month = LocalDateTime.now().monthValue
        println(month)
        return Pair(date_month, month)
    }
    fun reset(){
        link.clear()
        pay.clear()
        photo.clear()
        description=""
        heading=""
    }
    fun updateEntity(entitiy: DayEntitiy): LiveData<Boolean>{
        var a = MutableLiveData<Boolean>(false)
        viewModelScope.launch {
            flow {
                emit(repository.updateDay(entitiy))
            }.onCompletion { cause ->
                if (cause == null) {
                    Log.i("UPDATETAG","updated")
                    updating=false
                    a.value=true
                } else {
                    a.value=false
                    cause.printStackTrace()
                }
            }.catch { e ->
                a.value=false
                e.printStackTrace()
            }.collect()
        }
    return  a
    }
    fun insert_note(noteStructure: NoteStructure,mode : Boolean):LiveData<String>{
        var key=0
        val a =10
        var done = MutableLiveData<String>("false")
        for (i in 0..<noteStructure.file_name.length) {
            val c = noteStructure.file_name[i].code
            key += Math.pow(a.toDouble(), (i).toDouble()).toInt() * (c)
        }
        pop=false
        if(!mode) {
            try {
                viewModelScope.launch {
                    flow {
                        emit(notesRepository.insertDay(NotesEntitiy(key, noteStructure)))
                    }.onCompletion {
                        pop=true
                        done.value = "true"
                    }.catch {
                        e->
                        if(e is SQLiteConstraintException){
                            pop=false
                            done.value= "unique_key"
                        }
                    }
                    .collect()
                }
            } catch (e:SQLiteException) {
                Log.i("MYTAG","FFED")
                done.value= "unique_key"
            }
        }else{
            Log.i("MYTAG","key:$key "+noteStructure)
            try {
                viewModelScope.launch {
                    flow {
                        emit(notesRepository.updateDay(NotesEntitiy(key, noteStructure)))
                    }.onCompletion {
                        pop=true
                        done.value = "true"
                    }.catch {
                            e->
                        e.printStackTrace()
                        if(e is SQLiteConstraintException){
                            pop=false
                            done.value= "unique_key"
                        }
                    }.collect()
                }
            } catch (e: SQLException) {
                Log.i("MYTAG","FFED")
                done.value= "unique_key"
            }
        }
        return done
    }
     fun set_date_month(date: Int,month: Int){
         present_date_1=date
         present_month_1=month
     }
     fun change(dayEntitiy: DayEntitiy, index:Int, change:Boolean){
         set_entity(dayEntitiy)
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
        updating=true
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
    fun deleteNote(notesEntitiy: NotesEntitiy){
        viewModelScope.launch {
            notesRepository.deleteDay(notesEntitiy)
        }
    }
    fun delete_task(dayEntitiy: DayEntitiy,index : Int){
        println("command to delete task:"+dayEntitiy)
        viewModelScope.launch {
            val tasks = dayEntitiy.tasksList
            if (tasks.size > 0) {
                tasks.removeAt(index)
                dayEntitiy.tasksList = tasks
                repository.insertDay(dayEntitiy)
            }
        }
    }
}