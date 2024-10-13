package com.example.todoapp
import BottomNavigationItem
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Entity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.NoteStructure
import com.example.todoapp.Database.NotesDatabase
import com.example.todoapp.Database.NotesEntitiy
import com.example.todoapp.Database.Notes_Repository
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import com.example.todoapp.Database.TokenDatabase
import com.example.todoapp.Database.TokenRepository
import com.example.todoapp.Notificaiton.Constansts12
import com.example.todoapp.Notificaiton.SendTaskWorker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    var index=2
    private lateinit var viewModel: MainActivityViewModel
    var updating = false
    var entities :MutableList<DayEntitiy> = ArrayList()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = DayDatabse.getInstance(application).dao
        val repository = Repository(dao)
        val dao2 = NotesDatabase.getInstance(application).dao
        val repository2 = Notes_Repository(dao2)
        val dao3= TokenDatabase.getInstance(application).dao
        val repository3= TokenRepository(dao3)
        val factory = ViewModelFactory(repository = repository, repository2,repository3)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]
        viewModel.init()
        viewModel.set_date_changed_1(true)
        setContent {

            /*viewModel.insertToken(this)
          */BottomNavigationBar()
            /* viewModel.delete_task_error(DayEntitiy(viewModel.date_higlighted+100*viewModel.month_highlighted,ArrayList()))*/
            show_count()
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {
                }
            )
            SideEffect {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            LaunchedEffect(key1 = true) {
                val workRequest = PeriodicWorkRequestBuilder<IncompleteTaskWorker>(
                    repeatInterval = 5,
                    repeatIntervalTimeUnit = TimeUnit.HOURS
                ).setBackoffCriteria(
                    backoffPolicy = BackoffPolicy.LINEAR,
                    duration = Duration.ofSeconds(15)
                ).build()
                val workManager = WorkManager.getInstance(applicationContext)
                /*workManager.enqueueUniquePeriodicWork(
                    "Unique",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )*/
                workManager.enqueue(workRequest)
            }
        }
        val notificationManager  = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
        createNotificationChannel(notificationManager as NotificationManager)
    }
    fun createNotificationChannel(notificationManager: NotificationManager){
      val channel  = NotificationChannel(Constansts12.PUSH_NOTIFICATION_CHANNEL_ID,Constansts12
          .PUSH_NOTIFICATION_CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH)
          .apply {
             enableLights(true)
          }
      notificationManager.createNotificationChannel(channel)
    }
    @Composable
    fun BottomNavigationBar() {
        var navigationSelectedItem by remember {
            mutableStateOf(0)
        }
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(modifier = Modifier.height(50.dp)) {
                    //getting the list of bottom navigation items for our data class
                    BottomNavigationItem().bottomNavigationItems().forEachIndexed {index,navigationItem ->
                        //iterating all items with their respective indexes
                        NavigationBarItem(
                            selected = index == navigationSelectedItem,
                            label = {
                                Text(navigationItem.label)
                            },
                            icon = {
                                Icon(
                                    navigationItem.icon,
                                    contentDescription = navigationItem.label
                                )
                            },
                            onClick = {
                                navigationSelectedItem = index
                                navController.navigate(navigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
            navController = navController,
            startDestination = Screens.Home.route,
            modifier = Modifier.padding(paddingValues = paddingValues)) {
                composable(Screens.Home.route) {
                    View1(navController)
                }
                composable("home"){
                    View1(navHostController = navController)
                }
                composable(Screens.Plan.route) {
                    View_Notes(navController)
                }
                composable("floating/{mode}") {
                    var mode : Boolean? = false
                    try {
                        mode = it.arguments?.getBoolean("mode")
                    }catch (e : ClassCastException){
                        println("mg")
                    }
                    if (mode != null) {
                        trial1_displayWindow(navController,mode)
                    }
                }
                composable("view_notes") {
                    View_Notes(navController)
                }
                composable(
                    route = "write_notes/{note}"
                ) { backStackEntry ->
                    val noteJson = backStackEntry.arguments?.getString("note")
                    val noteStructure = Gson().fromJson(noteJson, NoteStructure::class.java)
                    Write_Notes(navController, noteStructure)
                }
            }
        }
    }
    @Composable
    fun View_Notes(navHostController: NavHostController){
        val n : MutableList<NotesEntitiy> = ArrayList()
        var notes by remember {
            mutableStateOf(n)
        }
        var height by remember {
            mutableStateOf(0)
        }
        var change by remember {
            mutableStateOf(false)
        }
        viewModel.notes.observe(this@MainActivity, Observer {
            if(it!=null){
                notes=it
            }
        })
        println("recomposed at viewNotes"+notes)
        Column(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                height = it.size.height
            }){
           Row{
               Text(text = "Notes", modifier = Modifier.padding(10.dp),Color.Blue, fontWeight = FontWeight.Bold, fontSize = 25.sp)
           }
            Log.i("MYTAG", "3: " + notes.toString())
            if(notes.size>0) {
               LazyColumn(modifier = Modifier.heightIn(0.dp, (0.3 * height).dp)) {
                   var count = 0
                   items(notes) {
                     change=Display_notes(notes,navHostController,count)
                     count++
                   }
               }
           }else{
               Card {
                   Text(text = "Add notes by clicking on the Icon below", modifier = Modifier.padding(10.dp),Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
               }
            }
           Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom){
               Row(modifier = Modifier
                   .fillMaxWidth()
                   .padding(10.dp), horizontalArrangement = Arrangement.End){
                   IconButton(onClick = {
                       val noteStructure = NoteStructure("","","")
                       val noteJson = Gson().toJson(noteStructure)
                       navHostController.navigate("write_notes/$noteJson") }, modifier = Modifier.border(2.dp,Color.Blue)) {
                      Icon(Icons.Filled.Add,"Add_Notes")
                   }
               }
           }
       }
    }
    @Composable
    fun Display_notes(notesEntitiy: MutableList<NotesEntitiy>,navHostController: NavHostController,index: Int):Boolean{
        val note=notesEntitiy[index].NoteStructure
        var delete = false
        Card(modifier = Modifier
            .padding(10.dp)
            .clickable {
                val noteJson = Gson().toJson(note)
                navHostController.navigate("write_notes/$noteJson")
            }){
            Column(modifier = Modifier
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                Text(text = "File name: ${note.file_name}", modifier = Modifier.padding(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Text(text ="Title:${note.heading}", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text ="Description:${note.description}", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(10.dp))
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween){
                    IconButton(onClick = {
                        viewModel.deleteNote(notesEntitiy[index])
                        delete=true
                    }) {
                        Icon(Icons.Filled.Delete,"delete_note")
                    }
                    TextButton(onClick = {
                    }) {
                        Text(text = "Print", modifier =Modifier.padding(10.dp))
                    }
                }
            }
        }
    return delete
    }
    @Composable
    fun Write_Notes(navHostController: NavHostController, noteStructure: NoteStructure?) {
        Log.i("MYTAG","noteStructure: "+noteStructure)
        val context = LocalContext.current
        var heading by remember {
            mutableStateOf("")
        }
        var description by remember {
            mutableStateOf("")
        }
        var height by remember {
            mutableStateOf(0)
        }
        var clicked by remember {
            mutableStateOf(false)
        }
        var s by remember {
            mutableStateOf(false)
        }
        var filename by remember {
            mutableStateOf("")
        }
        if(!s) {
            if (noteStructure != null) {
                heading = noteStructure.heading
                description = noteStructure.description
                s = true
                filename=noteStructure.file_name
            }
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                height = it.size.height
            }, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter Note:",
                    modifier = Modifier.padding(10.dp),
                    fontSize = 20.sp,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = heading, onValueChange = {
                heading = it
            }, label = {
                Text(text = "Type in your Title")
            }, modifier = Modifier
                .heightIn(0.dp, 100.dp)
                .fillMaxWidth()
                .padding(10.dp))
            Spacer(modifier = Modifier.height(35.dp))
            OutlinedTextField(value = description, onValueChange = {
                description = it
            }, modifier = Modifier
                .heightIn(300.dp, (0.22f * height).dp)
                .fillMaxWidth()
                .padding(10.dp))
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        clicked = !clicked
                    }) {
                        Text(text = "Save", modifier = Modifier.padding(10.dp), fontSize = 10.sp)
                    }
                }
            }
            if (clicked) {
               var mode=false
               if(filename!=""){
                   mode=true
               }
               if(!mode) {
                   Save_action(onDismissRequest = {
                       clicked = false
                   }) {
                       try {
                           viewModel.insert_note(NoteStructure(it, heading, description), mode)
                               .observe(this@MainActivity,
                                   Observer {
                                       println("observing4" + it + " " + viewModel.pop.toString())
                                       if (it == "true") {
                                           clicked = false
                                           if (viewModel.pop) {
                                               navHostController.popBackStack()
                                           }
                                       } else if (it == "unique_key") {
                                           Toast.makeText(
                                               context,
                                               "This name already exists!",
                                               Toast.LENGTH_SHORT
                                           ).show()
                                           clicked = false
                                       }
                                   })
                       }catch (e : Exception){
                           Toast.makeText( this@MainActivity,"unable to save", Toast.LENGTH_SHORT).show()
                       }
                   }

               }else{
                   if (noteStructure != null) {
                       viewModel.insert_note(NoteStructure(filename,heading, description), mode).observe(this@MainActivity,
                               Observer {
                                   println("observing4"+it+" "+viewModel.pop.toString())
                                   if (it == "true") {
                                       clicked = false
                                       navHostController.popBackStack()
                                   } else if (it == "unique_key") {
                                       Toast.makeText(
                                           context,
                                           "This name already exists!",
                                           Toast.LENGTH_SHORT
                                       ).show()
                                       clicked = false
                                   }
                               })
                   }
               }
            }
        }
    }
    @Composable
    fun Save_action(
        onDismissRequest: () -> Unit,
        onConfirmation: (name : String) -> Unit
    ){
       Dialog(onDismissRequest = onDismissRequest) {
           var name by remember {
               mutableStateOf("")
           }
           Card(
               modifier = Modifier
                   .fillMaxWidth()
                   .heightIn(150.dp)
                   .padding(16.dp),
               shape = RoundedCornerShape(16.dp)
           ){
               OutlinedTextField(value = name, onValueChange = {
                   name=it
               }, label = {
                   Text(text = "File Name:")
               }, modifier = Modifier.padding(10.dp))
               Row(modifier = Modifier
                   .fillMaxWidth()
                   .padding(10.dp), horizontalArrangement = Arrangement.End) {
                 TextButton(onClick = { onConfirmation(name) }) {
                     Text(text = "Save")
                 }
               }
           }
       }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun showCalender(closeSelection: UseCaseState.() -> Unit,navHostController: NavHostController){
        val localDate=LocalDate.of(2025,viewModel.month_highlighted,viewModel.date_higlighted)
        CalendarDialog(
            state = rememberUseCaseState(visible = true, onCloseRequest = {
                closeSelection() }),
            config = CalendarConfig(
                yearSelection = true,
                monthSelection = true,
                style = CalendarStyle.MONTH,
                disabledDates = listOf(localDate)
            ),
            selection = CalendarSelection.Date { newDate ->
                viewModel.calender_clicked=true
                viewModel.date_highlighted.value=newDate.dayOfMonth
                viewModel.month_highlighted=newDate.monthValue
                viewModel.date_higlighted=newDate.dayOfMonth
                println("date calendar:"+viewModel.month_highlighted)
                viewModel.dates=dates(viewModel.date_higlighted,viewModel.month_highlighted)
                navHostController.navigate("home")
            }
        )
    }
    @Composable
    fun LinkCardView(title: String,mode : Boolean,link3 : MutableList<String>){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        val link1: MutableList<String> = ArrayList()
        var link by remember { mutableStateOf(link1) }
        var m1 by remember{
            mutableStateOf(mode)
        }
        if(m1) {
            m1 = false
            link = link3
            if (link3.size > 0) {
                expandable = true
                viewModel.set_link(link3)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
        ) {
            val color = if (selected) Color.Blue else Color.Black
            println("recomposed-8"+link)
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { selected = !selected },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            BitmapFactory.decodeResource(context.resources, R.drawable.link)
                                .asImageBitmap(),
                            contentDescription = "link",
                            tint = color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = title,
                            color = Color.Black,
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { expandable = !expandable },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (expandable) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                                contentDescription = if (expandable) "Arrow_up" else "Arrow_down",
                                tint = color
                            )
                        }
                    }
                }
                if (expandable) {  /*recomposed every time I press add*/
                    var clicked by remember { mutableStateOf(false) }
                    var s by remember { mutableStateOf("") }
                    Column {
                        val scrollState = rememberScrollState()
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 100.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                            ) {
                                for (i in 0..<link.size) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Text(text = link[i])
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                    )
                                }
                            }
                        }
                        on_add {
                            clicked = true
                            s=it
                        }
                        if (clicked) {
                            link.add(s)
                            viewModel.set_link(link)
                           clicked = false
                        }
                        s = ""
                    }
                }
            }
        }
    }
    @Composable
    fun on_add(onClick: (String) -> Unit): String{
        var single_link by remember{ mutableStateOf("") }
        Row {
            OutlinedTextField(
                value = single_link, onValueChange = {
                    single_link = it
                },
                label = {
                    Text(text = "Link.")
                },
                modifier = Modifier
                    .wrapContentSize()
                    .padding(4.dp)
            )
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
                Button(onClick = {
                    println("clicked-2")
                    onClick(single_link)
                    single_link=""
                }) {
                    Text(text = "save", fontSize = 9.sp)
                }
            }
        }
        return single_link
    }
    @Composable
    fun on_add_1(onClick: (Pair<String,String>) -> Unit){
        var name by remember{ mutableStateOf("") }
        var amount by remember{ mutableStateOf("") }
        Row {
            OutlinedTextField(
                value = name, onValueChange = {
                    name = it
                },
                label = {
                    Text(text = "Name")
                },
                modifier = Modifier
                    .width(150.dp)
                    .padding(4.dp)
            )
            OutlinedTextField(
                value = amount, onValueChange = {
                    amount=it
                },
                label = {
                    Text(text = "Amount")
                },
                modifier = Modifier
                    .width(150.dp)
                    .padding(4.dp)
            )
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
                Button(onClick = {
                    if(name!=""&&amount!="") {
                        println("clicked-2")
                        onClick(Pair(amount,name))
                        name=""
                        amount=""
                    }else{
                        Toast.makeText(this@MainActivity, "please enter all fields!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "pay", fontSize = 9.sp)
                }
            }
        }
    }
    @Composable
    fun PayCardView(title: String,mode : Boolean,link2 : MutableList<Pair<String,String>>){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        var clicked by remember { mutableStateOf(false) }
        var link by remember {
            mutableStateOf<MutableList<Pair<String,String>>>(ArrayList())  //amount,name
        }
        var m1 by remember{
            mutableStateOf(mode)
        }
        if(m1){
            link=link2
            m1=false
            if(link2.size>0){
                viewModel.set_pay(link2)
                expandable=true
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
        ) {
            var color = if (selected) Color.Blue else Color.Black

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        selected = !selected
                        expandable=!expandable
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        BitmapFactory.decodeResource(context.resources, R.drawable.img_1).asImageBitmap(),
                        contentDescription = "Pay",
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(text = title, color = Color.Black, modifier = Modifier.wrapContentSize())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = { expandable = !expandable },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (expandable) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (expandable) "Arrow_up" else "Arrow_down",
                            tint = color
                        )
                    }
                }
            }
            if (expandable) {
                Column {
                    val scrollState = rememberScrollState()
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(0.dp, 100.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            for (i in 0..<link.size) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                                        Row {
                                            Text(text = "Name: "+link[i].second, modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(5.dp))
                                            Spacer(modifier = Modifier.width(20.dp))
                                            Text(text = "Amount: "+link[i].first,modifier= Modifier
                                                .fillMaxHeight()
                                                .padding(5.dp))
                                        }
                                    }
                                }
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                            }
                        }
                    }
                    on_add_1 {
                        Toast.makeText(this@MainActivity, it.first +" " +it.second, Toast.LENGTH_SHORT).show()
                        link.add(it)
                        viewModel.set_pay(link)
                        clicked=true
                    }
                    if(clicked){
                        clicked=false
                    }
                }
            }
        }
    }
    @Composable
    fun PhotoCardView(title: String,mode : Boolean,images : MutableList<String>){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        var list_images by remember {
            mutableStateOf<MutableList<String>>(ArrayList())
        }
        var checkedItemsCount by remember{
            mutableStateOf("")
        }
        var m1 by remember {
            mutableStateOf(mode)
        }
        if(m1) {
            list_images = images
            if (images.size > 0){
                viewModel.set_photo(images)
            expandable = true
        }
            m1=false
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
        ) {
            val color = if (selected) Color.Blue else Color.Black
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        selected = !selected
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        BitmapFactory.decodeResource(context.resources, R.drawable.gallery).asImageBitmap(),
                        contentDescription = "Photos",
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(text = title, color = Color.Black, modifier = Modifier.wrapContentSize())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            expandable = !expandable
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (expandable) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                            contentDescription = if (expandable) "Arrow_up" else "Arrow_down",
                            tint = color
                        )
                    }
                }
            }
            if(expandable){
                checkedItemsCount="open"
            }else{
                checkedItemsCount="closed"
            }
            if (checkedItemsCount!="closed") {
                if(list_images.size>0){
                    displayImages(base64String = list_images)
                }
                gallery_launcher().observe(this@MainActivity, Observer {
                    println("observed"+it)
                    if(it!=null) {
                        list_images.add(it)
                        viewModel.set_photo(list_images)
                        println("size of photo list"+list_images.size)
                        checkedItemsCount="open_closed"
                    }
                })

            }
        }
    }
    @Composable
    fun displayImages(base64String : MutableList<String>){
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .heightIn(0.dp, 100.dp)
                .horizontalScroll(scrollState)
        ) {
            println("recomposd photocard"+base64String.size)
            for (i in 0..<base64String.size) {
                display_photo(base64 = base64String[i])
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
    }
    @Composable
    fun gallery_launcher() : MutableLiveData<String?>{
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val live = MutableLiveData<String?>(null)
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        )  { uri: Uri? ->
            selectedImageUri = uri
            uri?.let {
                live.value=uriToBase64(context,it)
            }
        }
        Column {
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                Button(
                    onClick = {
                        launcher.launch("image/*")
                    }
                ) {
                    Text(text = "Select Image")
                }
            }
        }
        return live
    }
    @OptIn(ExperimentalEncodingApi::class)//
    fun uriToBase64(context: Context, uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return kotlin.io.encoding.Base64.encode(byteArray)
    }
    @Composable
    fun display_photo(base64: String){  //output is an Image composable for that we need an imaebitmap
        var decodeString : ByteArray? = null
        try{
            decodeString=Base64.getDecoder().decode(base64)
        }catch (e :Exception){
            e.printStackTrace()
        }
        if(decodeString!=null){
            val bitmap = BitmapFactory.decodeByteArray(decodeString,0,decodeString.size)
            Image(bitmap.asImageBitmap(), contentDescription = "selected photo", modifier = Modifier.size(100.dp), contentScale = ContentScale.Crop)
        }else{
            Text(text = "not able to display")
        }
    }
    @Composable
    fun trial1_displayWindow(navHostController: NavHostController,mode : Boolean) {
        val context= LocalContext.current
        var heading =""
        var description = ""
        var select_timepicker by remember {
            mutableStateOf(false)
        }
        var time_selected_d by remember {
            mutableStateOf("")
        }
        var link1 : MutableList<String> = ArrayList()
        var photo2 : MutableList<String> = ArrayList()
        var pay3 : MutableList<Pair<String,String>> = ArrayList()
        val taskStructure = viewModel.taskStructureForDisplay
        if(viewModel.mode) {
            heading = taskStructure.heading
            description = taskStructure.description
            link1 = taskStructure.links
            pay3 = taskStructure.pay
            photo2 = taskStructure.photos
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Text(
                "New Task:",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            information(label = "Title",heading)
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            information(label = " ",description)
            var firstColumnHeight by remember {
                mutableStateOf(0)
            }
            Button(onClick = {
               select_timepicker=true
            }, modifier = Modifier.padding(10.dp)) {
                Text(text = "Select Time", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
            }
            if(select_timepicker){
                  FullScreenDialogExample(onDismissRequest = { select_timepicker=false}) {
                      Card(modifier = Modifier.fillMaxWidth(),RoundedCornerShape(30.dp)){
                      DisplayTimeDate(modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp)) {
                              Toast.makeText(this@MainActivity, "saved",Toast.LENGTH_SHORT).show()
                              select_timepicker=false
                          }
                      }
                  }
            }
            Column(modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    firstColumnHeight = coordinates.size.height
                }, verticalArrangement = Arrangement.Bottom,
            ) {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier
                    .padding(5.dp)
                    .heightIn(0.dp, (0.3f * firstColumnHeight).dp)
                    .verticalScroll(scrollState)
                    .border(5.dp, Color.Black)
                ) {
                    LinkCardView(title = "link",true,link1)
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                    PhotoCardView(title = "attach photos!!",true,photo2)
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                    PayCardView(title = "Name,Amount",true,pay3)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        println("month highlighted:"+viewModel.month_highlighted+" "+"date:"+viewModel.date_higlighted)
                        if(viewModel.selected_time_hour!=null&&viewModel.selected_time_min!=null) {
                            try {
                                viewModel.insertDayTasks(
                                    viewModel.month_highlighted,
                                    viewModel.date_higlighted
                                ).observe(this@MainActivity, Observer {
                                    if (it == true) {
                                        Toast.makeText(context, "saved", Toast.LENGTH_SHORT).show()
                                        navHostController.navigate("home")
                                        viewModel.reset_tasks()
                                        viewModel.set_date_changed_1(true)
                                        viewModel.reset()

                                    } else {
                                        Toast.makeText(context, "saving..", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                })
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "exception while saving!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.delete_task_error(
                                    DayEntitiy(
                                        viewModel.date_higlighted + 100 * viewModel.month_highlighted,
                                        ArrayList()
                                    )
                                )
                            }
                        }else{
                            Toast.makeText(this@MainActivity,"please select time for task scheduling", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(text = "Save", modifier = Modifier.wrapContentSize())
                    }
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DisplayTimeDate(
        modifier: Modifier,
        onSelect: () -> Unit
    ):String{
        val d: MutableLiveData<String>
        val currentTime = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = true,
        )
        Column {
            TimePicker(
                state = timePickerState,
                modifier=modifier
            )
            Button(onClick = {
                onSelect()
                viewModel.selected_time_hour=timePickerState.hour
                viewModel.selected_time_min=timePickerState.minute
            }, modifier = Modifier.padding(5.dp)) {
                Text("Select", fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
        }
    return StringBuilder("Selected Time:"+viewModel.selected_time_hour.toString()+":"+viewModel.selected_time_min.toString()).toString()
    }
    @Composable
    fun information(label: String,start : String){
        var heading by remember { mutableStateOf(start) }
        if (label == "Title") {
            if(start!="")
                viewModel.set_titile(heading)
            OutlinedTextField(
                value = heading, onValueChange = {
                    heading=it
                    viewModel.set_titile(heading)
                },
                label = {
                    Text(text = label)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        } else {
            if(start!="")
                viewModel.set_description(heading)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(100.dp)
            ) {
                TextField(value = heading, onValueChange = {
                    heading = it
                    viewModel.set_description(heading)
                }, modifier = Modifier.fillMaxSize(), label = {
                    Text(text = "Task Description")
                })
            }
        }
    }

    @Composable
    fun View1(navHostController: NavHostController) {    /*displays the home screen doesn't get recomposed but its child functions do*/
        var date_change by rememberSaveable { mutableStateOf(viewModel._dateChanging.value) }
        viewModel.present_date= viewModel._dateChanging.value!!
        viewModel.date_highlighted.observe(this, Observer {
            println("present date:" + it.toString())
            date_change = it
        })
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            Display_dates()  /*displays dates 5 circles*/
            finalDisplay(navHostController)    /*displays previous tasks and add new task*/
        }
    }

    @Composable
    fun Display_dates() {
        val context = LocalContext.current
        var change_detected by rememberSaveable { mutableStateOf(viewModel.date_higlighted) }
        println("change_detected"+change_detected)
        var list_dates by rememberSaveable {
            mutableStateOf(dates(viewModel.date_higlighted,viewModel.month_highlighted))
        }
        viewModel.date_highlighted.observe(this,Observer{
            println("display dates:"+it)
            if(viewModel.dates.size>0){
                change_detected = it
                list_dates = viewModel.dates
                println("dates:" + list_dates)
                viewModel.calender_clicked = false
                viewModel.dates=ArrayList()
            }
        })
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        index=index-1
                        var present_date_1=0
                        if(index>=0) {
                            present_date_1 = list_dates[index].first
                            viewModel.date_highlighted.value=present_date_1
                            viewModel.present_month=getMonth_number(list_dates[index].second)
                            change_detected=present_date_1
                        }
                        if(index==-1){
                            var proxy=list_dates
                           viewModel.present_date=dates(proxy[0].first,getMonth_name(proxy[0].second))[0].first
                            viewModel.present_month=getMonth_name(dates(proxy[0].first,getMonth_name(proxy[0].second))[0].second)
                            proxy=dates(viewModel.present_date,viewModel.present_month)
                            viewModel.present_date=proxy[1].first
                            viewModel.present_month=getMonth_number(proxy[1].second)
                            list_dates=dates(viewModel.present_date,viewModel.present_month)
                            viewModel._dateChanging.value=viewModel.present_date
                            viewModel.date_highlighted.value=viewModel.present_date
                            Log.i("MYTAG","dates1:"+list_dates)
                            viewModel.date_highlighted.value=viewModel.present_date
                            change_detected=viewModel.present_date
                            index=2
                        }
                        viewModel.month_highlighted=viewModel.present_month
                              },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
                Box {
                    Column {
                        LazyRow(horizontalArrangement = Arrangement.SpaceEvenly) {
                            var count=0
                            items(items = list_dates, itemContent = {
                                Column {
                                    spacing(index = count)
                                    FirstDisplayItem(int = it.first, onClick = {
                                        Toast.makeText(context,"clicked on $it" , Toast.LENGTH_SHORT).show()
                                        viewModel.date_highlighted.value=it.first
                                        change_detected=it.first
                                        viewModel.month_highlighted=getMonth_number(it.second)
                                        viewModel.date_higlighted=it.first
                                        viewModel.set_date_changed_1(true)
                                        Log.i("MYTAG","highlighted:"+viewModel.month_highlighted+" "+viewModel.date_higlighted)
                                    }, change_detected,it.second,count)
                                    count++
                                }
                            })
                        }
                    }
                }
                IconButton(
                    onClick = {
                        index=index+1
                        var present_date_1=0
                        if(index<=4) {
                            present_date_1 = list_dates[index].first
                            viewModel.present_month=getMonth_number(list_dates[index].second)
                            viewModel.date_highlighted.value=present_date_1
                            change_detected=present_date_1
                        }
                        if(index==5){
                            var proxy=list_dates
                            viewModel.present_date=dates(proxy[4].first,getMonth_name(proxy[4].second))[4].first
                            viewModel.present_month=getMonth_name(dates(proxy[4].first,getMonth_name(proxy[4].second))[4].second)
                            proxy=dates(viewModel.present_date,viewModel.present_month)
                            viewModel.present_date=proxy[3].first
                            viewModel.present_month=getMonth_number(proxy[3].second)
                            list_dates=dates(viewModel.present_date,viewModel.present_month)
                            viewModel._dateChanging.value=viewModel.present_date
                            viewModel.date_highlighted.value=viewModel.present_date
                            Log.i("MYTAG","dates2:"+list_dates)
                            change_detected=viewModel.present_date
                            index=2
                        }
                       viewModel.month_highlighted=viewModel.present_month
                    },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Forward"
                    )
                }
            }
        }
    }
    fun getMonth_number(s: String): Int {
        return when (s) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
            else -> throw IllegalArgumentException("Invalid month name: $s")
        }
    }
    fun getMonthName(n: Int): String {
        return when (n) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> throw IllegalArgumentException("Invalid month number: $n")
        }
    }

    fun getMonth_name(index: String) : Int{
        if(index=="January"){
            return 1
        }else if(index=="February"){
            return 2
        }else if(index=="March"){
            return 3
        }else if(index=="April"){
            return 4
        }else if(index=="May"){
            return 5
        }else if(index=="June"){
            return 6
        }else if(index=="July"){
            return 7
        }else if(index=="August"){
            return 8
        }else if(index=="September"){
            return 9
        }else if(index=="October"){
            return 10
        }else if(index=="November"){
            return 11
        }
        return 12
    }
    fun dates(date: Int,month:Int): MutableList<Pair<Int,String>> {    /*Weak Fix*/
        println("generating dates for:"+date+" "+month)
        var dates: List<Int> = ArrayList()
        var month_names: List<Int> = ArrayList()
        var info : MutableList<Pair<Int,String>> = ArrayList()
        if(date > 2&&date<=28){
            month_names=listOf(month, month, month, month, month)
        }
        if (date > 2&&date<=28) {
            dates=   listOf(date - 2, date - 1, date, date + 1, date + 2)
        } else {
            if(date<=2) {
                val last_month = month-1
                var last_date = 0
                var penultimate_day = 0
                if (last_month == 2) {
                    if (LocalDateTime.now().year % 4 == 0) {
                        last_date = 29
                        penultimate_day = last_date - 1
                    } else {
                        last_date = 28
                        penultimate_day = last_date - 1
                    }
                } else if (last_month == 4 || last_month == 6 || last_month == 9 || last_month == 11) {
                    last_date = 30
                    penultimate_day = 29
                } else {
                    last_date = 31
                    penultimate_day = 30
                }
                if (date == 1) {
                    dates = listOf(penultimate_day, last_date, date, date + 1, date + 2)
                    month_names = listOf(month - 1, month - 1, month, month, month)
                } else {
                    dates = listOf(last_date, 1, date, date + 1, date + 2)
                    month_names = listOf(month - 1, month, month, month, month)
                }
            }else{
                val next_month = month+1
                var last_date = 0
                var penultimate_date = 0
                if(date==29) {
                    if (next_month == 2) {
                        penultimate_date=1
                        last_date=2
                    } else if (next_month == 4 || next_month == 6 || next_month == 9 || next_month == 11) {
                        last_date = 31
                        penultimate_date=30
                    } else {
                        last_date = 1
                        penultimate_date=30
                    }
                }else if(date==30){
                    if (next_month == 4 || next_month == 6 || next_month == 9 || next_month == 11) {
                        penultimate_date = 31
                        last_date=1
                    } else {
                        penultimate_date = 1
                        last_date=2
                    }
                }else if(date==31){
                    penultimate_date=1
                    last_date=2
                }
                dates=listOf(date - 2, date - 1, date, penultimate_date,last_date)
                if(last_date==1){
                    month_names=listOf(month, month, month, month, month+1)
                }
                else if(penultimate_date==1){
                    month_names=listOf(month, month, month, month+1, month+1)
                }else{
                    month_names=listOf(month, month, month, month, month)
                }
            }
        }
        for(i in 0..4){
            info.add(Pair(dates.get(i),DateFormatSymbols.getInstance().months[month_names.get(i)-1]))
        }
        return info
    }
    @Composable
    fun spacing(index: Int){
        println("spacing")
        if(index==2){
            Spacer(modifier = Modifier.height((10 * 5).dp))
        }else{
            var space=0
            if(index==0){
                space=0
            }else if(index==1){
                space=3
            }else if(index==3){
                space=3
            }else{
                space=0
            }
            Spacer(modifier = Modifier.height((10 * space).dp))
        }

    }
    @Composable
    fun TaskStructure1(
        task_description: String, check: Boolean, task_heading: String,
        dayEntitiy: DayEntitiy,
        index: Int,
        navHostController: NavHostController,
        onClick: () -> Unit
    ){
        val editedDescription =task_description
        val editedHeading = task_heading
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(10.dp)
                .clickable {
                    val taskStructure = dayEntitiy.tasksList[index]
                    viewModel.taskStructureForDisplay = taskStructure
                    Log.i("MYTAG", "index:" + index)
                    val mode = true
                    viewModel.mode = true
                    viewModel.index = index
                    navHostController.navigate("floating/$mode")
                }
        ) {
            Row(modifier = Modifier.padding(10.dp)) {
                Checkbox(
                    checked =check,
                    onCheckedChange = {
                        viewModel.change(dayEntitiy,index,it)
                        viewModel.set_date_month(viewModel.present_date,viewModel.present_month)
                        onClick()
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.wrapContentSize()) {
                    Text(
                        text = editedHeading,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = editedDescription,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = {
                            onClick()
                            viewModel.delete_task(dayEntitiy, index)
                        },
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
    @Composable
    fun finalDisplay(navHostController: NavHostController){
        var date_change by rememberSaveable { mutableStateOf(viewModel.date_higlighted)}
        viewModel.date_highlighted.observe(this, Observer {
            if(it!=date_change){
                date_change=it
                viewModel.set_date_changed_1(true)
            }
            viewModel.date_higlighted=it
            println("date ki data:"+it)
        })
        Column(modifier = Modifier
            .fillMaxWidth()){
            println("date changed")
            displayTaskView(navHostController,date_change)
        }
    }
    @Composable
    fun displayTaskView(navHostController: NavHostController,date: Int){
        var entity1 by remember{ mutableStateOf(viewModel.get_entitiy()) }
        var mode by remember {
            mutableStateOf(false)
        }
        val c : Boolean? = false
        var change by remember { mutableStateOf(c) } /* aimed to recompose if any Task gets deleted*/
        var clicked by remember {
            mutableStateOf(false)
        }
        var showCalender by remember {
            mutableStateOf(false)
        }
        Column (modifier = Modifier
            .fillMaxWidth()){
            println("recomposed at taskview:"+entity1)
            Row(horizontalArrangement = Arrangement.SpaceBetween){
                PartialCircularBorderBox(area = 70.dp,entity1)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.End){
                    IconButton(onClick = {
                        showCalender=true
                    }) {
                        Icon(imageVector = Icons.Filled.DateRange, contentDescription = "calendar")
                    }
                }
            }
            if(showCalender){
                showCalender({
                    showCalender=false
                },navHostController)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Work To-Dos", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 25.sp, modifier = Modifier.padding(10.dp))
                Button(onClick = {
                    val mode= false
                    viewModel.mode=mode
                    navHostController.navigate("floating/$mode") }) {
                    Text(text = "Add Task")
                }
            }
            showDailyTask(navHostController) {
                clicked = true
            }
            if(clicked){
                FullScreenDialogExample({
                    clicked=false
                },{ UndoneTasks() })
            }
        }
    }
    @Composable
    fun UndoneTasks(){
        val a:MutableList<DayEntitiy> = entities
        val c :MutableList<DayEntitiy> = ArrayList()
        a.reverse()
        println("Recomposed at UndoneTasks"+"ALL TASKS:"+a)
        for(i in 0..a.size-1) {
            var tasks: MutableList<TaskStructure> = ArrayList()
            val id = a[i].id
            if (id<100*viewModel.month_highlighted+viewModel.date_higlighted){
                for (j in 0..a[i].tasksList.size - 1) {
                    if (!a[i].tasksList.get(j).complete) {
                        tasks.add(a[i].tasksList.get(j))
                    }
                }
            if (tasks.size > 0) {
                c.add(DayEntitiy(id, tasks))
            }
            }
        }
        Column(modifier = Modifier.fillMaxWidth()){
            println("Recomposed at UndoneTasks1")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                var d by remember {
                    mutableStateOf(false)
                }
                if(d){

                }
                println("Recomposed at UndoneTasks2")
                if (c.size > 0) {
                  for(i in 0..c.size-1){
                      val id = c[i].id
                      val month=id/100
                      val date =id%100
                      val tasks = c[i].tasksList
                      for(j in 0..c[i].tasksList.size-1) {
                          Row(
                              modifier = Modifier
                                  .fillMaxWidth(),
                              horizontalArrangement = Arrangement.SpaceBetween
                          ) {
                              Column(modifier = Modifier.padding(10.dp)) {
                                  Text(text = "Date:$date")
                                  Text(text = "Month:${getMonthName(month)}")
                              }
                              Column(
                                  modifier = Modifier
                                      .padding(10.dp)
                                      .widthIn(0.dp, 60.dp)
                              ) {
                                  Text(
                                      text = tasks[j].heading,
                                      fontWeight = FontWeight.Bold
                                  )
                                  Text(
                                      text = tasks[j].description,
                                      minLines = 1,
                                      maxLines = 2,
                                      overflow = TextOverflow.Ellipsis
                                  )
                              }
                              Checkbox(checked = tasks[j].complete, onCheckedChange = {
                                  if (it) {
                                      viewModel.databaseMigration(c[i], j)
                                          .observe(this@MainActivity,
                                              Observer {
                                              println("Observing at CheckBo:  "+it)
                                                  if (it) {
                                                      println("Recomposed at UndoneTasks420")
                                                      viewModel.delete_task(c[i], j)
                                                      if(c[i].tasksList.size==0){
                                                          c.removeAt(i)
                                                      }
                                                      d = !d
                                                  }
                                              })
                                  }
                              })
                          }
                      }
                  }
                } else {
                    Text(text = "Hurray!! No incomplete tasks!!", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
    @Composable
    fun FullScreenDialogExample(onDismissRequest: () ->Unit,content: @Composable ()->Unit) {
        Dialog(
            onDismissRequest = {
                onDismissRequest()
            }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            // Custom layout for the dialog
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    content()
                }
            }
        }
    }
    @Composable
    fun FirstDisplayItem(int: Int, onClick: () -> Unit,whomToHighlight : Int,month :String,index1: Int) {
        var borderColour = Color.Black
        var borderMargin = 0.dp
        var area = 50.dp
        var weight = FontWeight.Normal
        if(int == whomToHighlight){
            borderColour= Color.Blue
            borderMargin = 4.dp
            area = 75.dp
            weight= FontWeight.Black
        }
        Box(
            modifier = Modifier
                .size(area)
                .background(Color.White, CircleShape)
                .border(borderMargin, borderColour, shape = CircleShape)
                .clickable {
                    onClick()
                    index = index1
                }
            ,
        ) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxSize()
                .padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = int.toString(),
                    fontWeight = weight,
                    color = Color.Black
                )
                Text(
                    text = month,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    fontSize =10.sp
                )
            }
        }
    }
    @Composable
    fun PartialCircularBorderBox(area: Dp,entitiy: DayEntitiy?) {
        var color = Color.Red
        var borderPercentage = 0f
        if(entitiy!=null){
          val taskslist = entitiy.tasksList
          var j= taskslist.size-1
          val n=j+1
          var count=0
          while(j>=0){
              if(taskslist[j].complete){
                  count++
              }else{
                  break
              }
              j--
          }
          if(n>0)
              borderPercentage= (count.toFloat()/n.toFloat())*100
              Log.i("MYTAG",count.toString()+" "+n.toString()+" "+borderPercentage.toString())
          }
        if(borderPercentage>=0&&borderPercentage<20){
              color=Color(resources.getColor(R.color.red_2))
        }
        else if(borderPercentage>=20&&borderPercentage<50){
            color=Color(resources.getColor(R.color.traffic_light1))
        }
        else if(borderPercentage>=50&&borderPercentage<80){
            color=Color(resources.getColor(R.color.traffic_light2))
        }
        else if(borderPercentage>=80&&borderPercentage<100){
            color=Color(resources.getColor(R.color.green_1))
        }else{
            color=Color(resources.getColor(R.color.green_2))
        }
        Box(
            modifier = Modifier
                .size(area)
                .background(Color.White, CircleShape)
                .padding(10.dp)
                .border(1.dp, Color.Black, CircleShape)
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(text = borderPercentage.toInt().toString(),color=color)
            Canvas(modifier = Modifier.size(area)) {
                val borderThickness = 4.dp.toPx() // Example border thickness
                val sweepAngle = 360 * borderPercentage / 100
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = borderThickness)
                )
            }
        }
    }
    @Composable
    fun showDailyTask(navHostController: NavHostController,onClick: () -> Unit){
        var change by remember { mutableStateOf(false) }
        var height by remember {
            mutableStateOf(0)
        }
        var entity by remember {
            mutableStateOf<DayEntitiy?>(viewModel.dayEntitiy)
        }
        println("composed:$change")
        println("showing:" + entity)
        LaunchedEffect(key1 = viewModel.date_higlighted) {
            println("key changed:${viewModel.date_higlighted}")
            try {
                entity = async {
                    viewModel.retrieveDayTasks(viewModel.date_higlighted,viewModel.month_highlighted)
                }.await()
                viewModel.dayEntitiy=entity
                println("waited for:$entity")
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        if (entity != null) {
            val taskList = entity!!.tasksList
            if (taskList.size > 0) {
                println("entered if for content rendering :$entity")
                Column(modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        height = it.size.height
                    }) {
                    if (elements_presenet(taskList, false)) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(0.dp, (0.7f * height).dp)
                        ) {
                            var count = 0
                            items(items = taskList) {
                                if (!it.complete) {
                                    TaskStructure1(
                                        task_description = it.description,
                                        check = it.complete,
                                        task_heading = it.heading,
                                        entity!!,
                                        count,
                                        navHostController
                                    ) {
                                        change = !change
                                    }
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(15.dp)
                                    )
                                }
                                count++
                            }
                        }
                    }
                    var expandable by remember {
                        mutableStateOf(true)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Column(modifier = Modifier.heightIn(0.dp, (0.25f * height).dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Completed Tasks",
                                    color = Color.Blue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                                IconButton(onClick = {
                                    expandable = !expandable
                                }) {
                                    if (!expandable)
                                        Icon(Icons.Filled.KeyboardArrowUp, "Up")
                                    else
                                        Icon(Icons.Filled.KeyboardArrowDown, "Down")
                                }
                            }
                            if (expandable) {
                                if (elements_presenet(taskList, true)) {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(
                                            0.dp,
                                            (0.3f * height).dp
                                        )
                                    ) {
                                        var count = 0
                                        items(items = taskList) {
                                            if (it.complete) {
                                                TaskStructure1(
                                                    task_description = it.description,
                                                    check = it.complete,
                                                    task_heading = it.heading,
                                                    entity!!,
                                                    count,
                                                    navHostController
                                                ) {
                                                    change = !change
                                                }
                                                Spacer(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(15.dp)
                                                )
                                            }
                                            count++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "No tasks present!!",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = "View Undone Tasks",
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable {
                                    onClick()
                                }
                        )
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "No tasks present!!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "View Undone Tasks",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(10.dp)
                            .clickable {
                                onClick()
                            }
                    )
                }
            }
        }
    }

    fun show_count(){
        viewModel.all_days.observe(this) {
            entities= it.toMutableList()
            println("size of saved entities: " + it.size + " " + it.toString())
        }
    }
    fun elements_presenet(taskList: MutableList<TaskStructure>,complete : Boolean):Boolean{
        println("tasks to be present:$taskList")
        for(i in 0..taskList.size-1){
             if(complete==taskList[i].complete)
                 return true
         }
        return false
    }
}