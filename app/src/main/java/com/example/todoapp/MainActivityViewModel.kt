package com.example.todoapp
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.NotificationDetail
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.NoteStructure
import com.example.todoapp.Database.NotesEntitiy
import com.example.todoapp.Database.Notes_Repository
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import com.example.todoapp.Database.TokenEntity
import com.example.todoapp.Database.TokenRepository
import com.example.todoapp.Notificaiton.ScheduleNotificationsWorker
import com.example.todoapp.Notificaiton.SchedulerObject
import com.example.todoapp.Notificaiton.SendTaskWorker
import com.example.todoapp.Notificaiton.getNotficationData
import com.example.todoapp.Notificaiton.storeNotificationData
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class MainActivityViewModel(private val repository: Repository,private val notesRepository: Notes_Repository,private val tokenRepository: TokenRepository) : ViewModel() {
    var dayEntitiy: DayEntitiy? = null
   var date_chaned=false
    var present_date_1=0
    var mode=false
    var present_date =0
    var present_month =0
    var list : MutableList<TaskStructure> = ArrayList()
    var present_month_1=0
    var selected_time_hour : Int? = null
    var selected_time_min   : Int? = null
    var date_higlighted=0
    var month_highlighted=0
    var updating = false
    var pop = false
    var calender_clicked =false
    var link : MutableList<String> = ArrayList()
   var photo : MutableList<String> = ArrayList()
   var pay : MutableList<Pair<String,String>> = ArrayList()
   var heading =""
    var dates : MutableList<Pair<Int,String>> =ArrayList()
   var description = ""
    val notes = notesRepository.tasks
    var index=0
   val all_days = repository.tasks
   val _dateChanging = MutableLiveData<Int>().apply { value = LocalDateTime.now().dayOfMonth }
   var date_highlighted = MutableLiveData<Int>()
    var taskStructureForDisplay = TaskStructure("","",false,ArrayList(),ArrayList(),ArrayList(),-1,-1)
    fun reset_tasks(){
        taskStructureForDisplay =TaskStructure("","",false,ArrayList(),ArrayList(),ArrayList(),-1,-1)
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
    var token : String = ""
    private suspend fun getToken(): String {
        val tokenRetrieved = CompletableDeferred<String>("")
        tokenRepository.token.observeForever { tokenList ->
            if (tokenList != null) {
                token = tokenList[0].token
                tokenRetrieved.complete(token)
                Log.i("MYTAG", "Token is12: $token")
            }
        }
        return tokenRetrieved.await()
    }
    fun insertToken(context: Context){
        viewModelScope.launch {
            try {
                val token= FirebaseMessaging.getInstance().token.await()
                println("token is $token")
                tokenRepository.insert(TokenEntity(0,token.toString()))
            }catch (e : Exception){
                e.printStackTrace()
                Toast.makeText(context, "$e", Toast.LENGTH_SHORT).show()
            }
        }
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
  fun setOFF_scheduler(entitiy: DayEntitiy,context: Context){
     viewModelScope.launch {
         val tasklist = entitiy.tasksList
         println("1:$tasklist")
         println("2:$list")
         if (tasklist.size > 0 && tasklist != list) {
             var index = -1
             var t = -1
             val h =  LocalDateTime.now().minute +1000*LocalDateTime.now().hour
             for (i in 0..<tasklist.size) {
                 val t_i = 1000 * tasklist[i].timeHour + tasklist[i].timeMin
                 println("value is 2:$h")
                 if (t_i >=h) {
                     t = t_i
                     index = i
                 }
             }
             for (i in 0..<tasklist.size) {
                 val t_i = 1000 * tasklist[i].timeHour + tasklist[i].timeMin
                 if (t_i >=h && t >=t_i) {
                     t = t_i
                     index = i
                 }
             }
             println("index is:$index")
             if (index != -1) {
                 if(tasklist[index]!= getNotficationData(context)!!.task) {
                     println("scheduling notification for:${tasklist[index]}")
                     val oneTimeWorkRequest =
                         OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>().setConstraints(
                             Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                 .build()
                         ).build()
                     WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
                     list = tasklist
                     storeNotificationData(
                         context, Json.encodeToString(
                             SchedulerObject.serializer(), SchedulerObject(
                                 tasklist[index].timeHour, tasklist[index].timeMin,
                                 tasklist[index]
                             )
                         )
                     )
                 }else{
                     println("duplicate 2")
                 }
             } else {
                 println("duplicate")
             }
         }else{
             println("schedulable tasks not present")
         }
     }
  }
  fun renderRequest(entitiy: DayEntitiy,context: Context){
     CoroutineScope(Dispatchers.Main).launch {
         val token_1 = async {
             getToken()
         }.await()
         val notification = NotificationDetail(entitiy.tasksList.toMutableList(),token,"Hi")
         val notification_Json = GsonBuilder().create().toJson(notification).toString()
         val taskdata = Data.Builder().putString("Notification",notification_Json)
             .build()
         val oneTimeRequest = OneTimeWorkRequestBuilder<SendTaskWorker>().setInputData(taskdata)
             .build()
         WorkManager.getInstance(context).enqueue(oneTimeRequest)
     }
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
      val task=TaskStructure(heading,description,false,photo,link,pay,selected_time_min!!,selected_time_hour!!)
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
                         delete_task_error(entitiy)
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
                delete_task_error(entitiy)
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
    fun delete_task_error(dayEntitiy: DayEntitiy){
        viewModelScope.launch {
            repository.deleteDay(entitiy = dayEntitiy)
        }
    }
}