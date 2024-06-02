package com.example.todoapp
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import java.text.DateFormatSymbols
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private var present_date = getTodayMonthDate().first
    private var present_month = getTodayMonthDate().second
    var init = 0
    private lateinit var viewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = DayDatabse.getInstance(application).dao
        val repository = Repository(dao)
        val factory = ViewModelFactory(repository = repository)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]
        present_date= viewModel._dateChanging.value!!
        viewModel.setHighlightValue(present_date)
        setContent {
            MyApp()
        }
        show_count()
    }

    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Home") {
            composable("Home") {
                View1(navController)
            }
            composable("floating") {
                trial1_displayWindow(navController)
            }
        }
    }

    @Composable
    fun trial1_displayWindow(navHostController: NavHostController) {
        var heading by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var date_change by remember { mutableStateOf(viewModel._dateChanging.value) }
        viewModel._dateChanging.observe(this, Observer {
            date_change = it
        })
        var entity1 by remember {
            mutableStateOf(date_change?.let {
                viewModel.retrieveDayTasks(
                    it,
                    LocalDateTime.now().monthValue
                ).value
            })
        }
        viewModel.date_highlighted?.let {
            it.value?.let { it1 ->
                viewModel.retrieveDayTasks(it1, LocalDateTime.now().monthValue).observe(this@MainActivity, Observer {
                    entity1 = it
                    println("data of highlighted2:" + it.toString())
                })
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
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
            heading = information(label = "Title")
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            description = information(label = " ")
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                expandable_CardView(title = "Link", "link")    /*display other functions*/
                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth()
                )
                expandable_CardView(title = "Pay", "pay")
                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth()
                )
                expandable_CardView(title = "Photo", "photo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        val task = TaskStructure(
                            heading = heading,
                            complete = false,
                            description = description
                        )
                        if (entity1 == null) {
                            println("empty entity")
                            val tasks: MutableList<TaskStructure> = ArrayList()
                            tasks.add(task)
                            viewModel!!.date_highlighted?.let {
                                it.value?.let { it1 ->
                                    viewModel.insertDayTasks(tasks, LocalDateTime.now().monthValue,
                                        it1
                                    )
                                }
                            }
                        } else {
                            println("entered else")
                            val tasks: MutableList<TaskStructure> = entity1!!.tasksList
                            println(tasks)
                            tasks.add(task)
                            viewModel.date_highlighted?.let {
                                it.value?.let { it1 ->
                                    viewModel.insertDayTasks(tasks, LocalDateTime.now().monthValue,
                                        it1
                                    )
                                }
                            }
                        }
                        Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
                        navHostController.popBackStack()
                    }) {
                        Text(text = "Save", modifier = Modifier.wrapContentSize())
                    }
                }
            }
        }
    }

    @Composable
    fun information(label: String): String {
        var heading by remember { mutableStateOf("") }
        if (label == "Title") {
            OutlinedTextField(
                value = heading, onValueChange = {
                    heading = it
                },
                label = {
                    Text(text = label)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(100.dp)
            ) {
                TextField(value = heading, onValueChange = {
                    heading = it
                }, modifier = Modifier.fillMaxSize(), label = {
                    Text(text = "Task Description")
                })
            }
        }
        return heading
    }

    @Composable
    fun expandable_CardView(title: String, inputType: String) {
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
                .clickable {
                    expandable = !expandable
                    selected = !selected
                }
        ) {
            var color = Color.Black
            if (selected) {
                color = Color.Blue
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                when (inputType) {
                    "link" -> {
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
                    }

                    "photo" -> {
                        IconButton(
                            onClick = { selected = !selected },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                BitmapFactory.decodeResource(context.resources, R.drawable.gallery)
                                    .asImageBitmap(),
                                contentDescription = "Photos",
                                tint = color
                            )
                        }
                    }

                    "pay" -> {
                        IconButton(
                            onClick = { selected = !selected },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                BitmapFactory.decodeResource(context.resources, R.drawable.img_1)
                                    .asImageBitmap(),
                                contentDescription = "Pay",
                                tint = color
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(text = title, color = Color.Black, modifier = Modifier.wrapContentSize())
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (!expandable) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Arrow_down",
                                tint = color
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Arrow_up",
                                tint = color
                            )
                        }
                    }

                }
            }
            if (expandable) {
                Text(text = "coming", modifier = Modifier.fillMaxWidth())
            }
        }
    }

    @Composable
    fun View1(navHostController: NavHostController) {    /*displays the home screen doesn't get recomposed but its child functions do*/
        var date_change by remember { mutableStateOf(viewModel._dateChanging.value) }
        present_date= viewModel._dateChanging.value!!
        viewModel._dateChanging.observe(this, Observer {
            println("present date:" + it.toString())
            date_change = it
        })
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            Display_dates()   /*displays dates 5 circles*/
            finalDisplay()    /*displays previous tasks and add new task*/
            Column(
                modifier = Modifier    /*responsible for adding an Icon button at the bottom left*/
                    .fillMaxSize()
                    .padding(bottom = 50.dp), verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .wrapContentHeight(), horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Blue, CircleShape)
                            .border(1.dp, Color.Magenta, shape = CircleShape),
                        onClick = {
                            navHostController.navigate("floating")
                        }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "add",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Display_dates() {
        var present_date by remember { mutableStateOf(present_date) }
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
                        val present_date_1 = dates(present_date,present_month)[1].first
                        present_month=getMonth_name(dates(present_date,present_month)[1].second)
                        present_date=present_date_1
                        viewModel.onChangingDates(present_date)
                        viewModel.setHighlightValue(present_date)
                              },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
                Date_display(present = present_date,present_month)
                IconButton(
                    onClick = {
                        val present_date_1 = dates(present_date,present_month)[3].first
                        present_month=getMonth_name(dates(present_date,present_month)[3].second)
                        present_date=present_date_1
                        viewModel.onChangingDates(present_date)
                        viewModel.setHighlightValue(present_date)
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
        println("present month:"+month)
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
        println("month names:"+month_names+" "+"dates: "+dates)
        for(i in 0..4){
            info.add(Pair(dates.get(i),DateFormatSymbols.getInstance().months[month_names.get(i)-1]))
        }
        return info
    }
    @Composable
    fun Date_display(present: Int,present_month:Int) {
        println("recomposed-1")
        val dates = dates(present,present_month)
        val context = LocalContext.current
        var change_detected by remember { mutableStateOf(viewModel.date_highlighted.value) }
        viewModel.date_highlighted.observe(this, Observer {
                change_detected=it
        })
        println("date highlight:"+change_detected.toString())
        Box {
            Column {
                LazyRow(horizontalArrangement = Arrangement.SpaceEvenly) {
                    var count=0
                    items(items = dates, itemContent = {
                        Column {
                             spacing(index = count)
                             count++
                             change_detected?.let { it1 ->
                                 FirstDisplayItem(int = it.first, onClick = {
                                    Toast.makeText(context,"clicked on $it" , Toast.LENGTH_SHORT).show()
                                    change_detected=it.first
                                    viewModel.setHighlightValue(it.first)
                                    viewModel.onChangingDates(it.first)
                                }, it1,it.second)
                            }
                        }
                    })
                }
            }
        }
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
     fun TaskStructure1(task_description: String, check: Boolean, task_heading: String) {
        var complete by remember { mutableStateOf(check) }
        var editedDescription =task_description
        var editedHeading = task_heading
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp)) {
                Checkbox(
                    checked =complete,
                    onCheckedChange = {
                        complete=it
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.wrapContentSize()) {
                    BasicTextField(
                        value = editedHeading,
                        onValueChange = { editedHeading = it },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Start,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .wrapContentSize(align = Alignment.Center)
                            .padding(4.dp)
                    )
                }
            }
        }
     }
     @Composable
    fun finalDisplay(){
        var date_change by remember { mutableStateOf( viewModel._dateChanging.value)}
            viewModel._dateChanging.observe(this, Observer {
            date_change=it
        })
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(10.dp)){
            println("recmposedd")
            date_change?.let {
                displayTaskView(date = it, month =present_month )
            }
        }
    }
    @Composable
    fun displayTaskView(date :Int,month: Int){
        var entity1 by remember{ mutableStateOf(viewModel.retrieveDayTasks(date,month).value) }
        viewModel.retrieveDayTasks(date,month).observe(this, Observer {
             entity1=it
            println("data of highlighted:"+it.toString())
        })
        if(entity1==null){
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No tasks present!!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Blue,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "\n\nAdd tasks by clicking on the Add New Task button",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }
                }
        }
        entity1?.let { showDailyTask(entitiy = it) }
    }
    @Composable
    fun FirstDisplayItem(int: Int, onClick: () -> Unit,whomToHighlight : Int,month :String) {
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
                .clickable(onClick = onClick)
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
    fun showDailyTask(entitiy: DayEntitiy){
        val tasklist =entitiy.tasksList
        LazyColumn {
            items(items = tasklist, itemContent = {
                TaskStructure1(task_description = it.description, check = it.complete, task_heading = it.heading)
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp))
            })
        }
    }
    fun getTodayMonthDate(): Pair<Int, Int> {
        val date_month = LocalDateTime.now().dayOfMonth
        val month = LocalDateTime.now().monthValue
        println(month)
        return Pair(date_month, month)
    }
    fun show_count(){
        viewModel.all_days.observe(this, Observer {
            println("size of saved entities: "+it.size+" "+it.toString())
        })
    }
}
