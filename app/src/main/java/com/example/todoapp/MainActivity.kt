package com.example.todoapp
import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.SQLException
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontLoadingStrategy.Companion.Async
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todoapp.Database.DayDatabse
import com.example.todoapp.Database.DayEntitiy
import com.example.todoapp.Database.Repository
import com.example.todoapp.Database.TaskStructure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Async
import java.io.ByteArrayOutputStream
import java.text.DateFormatSymbols
import java.time.LocalDateTime
import java.util.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.exp

class MainActivity : ComponentActivity() {
    private var present_date = getTodayMonthDate().first
    private var present_month = getTodayMonthDate().second
    var index=2
    private lateinit var viewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = DayDatabse.getInstance(application).dao
        val repository = Repository(dao)
        val factory = ViewModelFactory(repository = repository)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]
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
            composable("view_notes") {
                trial1_displayWindow(navController)
            }
        }
    }
    @Composable
    fun LinkCardView(title: String){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        val link1: MutableList<String> = ArrayList()
        val link by remember { mutableStateOf(link1) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
        ) {
            val color = if (selected) Color.Blue else Color.Black
            println("recomposed-8")
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
    fun PayCardView(title: String){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        var clicked by remember { mutableStateOf(false) }
        var link by remember {
            mutableStateOf<MutableList<Pair<String,String>>>(ArrayList())  //amount,name
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
    fun PhotoCardView(title: String){
        var expandable by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var selected by remember { mutableStateOf(false) }
        var list_images by remember {
            mutableStateOf<MutableList<String>>(ArrayList())
        }
        var checkedItemsCount by remember{
            mutableStateOf("")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.dp, Color.Black)
                .padding(10.dp)
                .border(2.dp, Color.Blue)
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
    fun add_on(){
        var width by remember {
            mutableStateOf(0)
        }
        println("recomposed cosof width"+width)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .onGloballyPositioned {
                width = it.size.width
            }, horizontalArrangement = Arrangement.SpaceEvenly){
            Card(modifier = Modifier
                .width((width).dp)
                .height(150.dp)){
                Text(text = "Personal To-Dos", color = Color.Blue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                Text(text = "Click to Add..", modifier = Modifier.padding(5.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
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
    fun trial1_displayWindow(navHostController: NavHostController) {
        val context= LocalContext.current
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
           information(label = "Title")
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            information(label = " ")
            var firstColumnHeight by remember {
                mutableStateOf(0)
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
                    .border(2.dp, Color.Black)
                ) {
                    LinkCardView(title = "link")
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                    PhotoCardView(title = "attach photos!!")
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                    PayCardView(title = "Name,Amount")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        viewModel.insertDayTasks(present_month,present_date).observe(this@MainActivity,Observer{
                            if(it==true){
                                Toast.makeText(context, "saved", Toast.LENGTH_SHORT).show()
                                viewModel.reset()
                                navHostController.popBackStack()
                            }else{
                                Toast.makeText(context, "saving..", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }) {
                        Text(text = "Save", modifier = Modifier.wrapContentSize())
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    fun information(label: String){
        var heading by remember { mutableStateOf("") }
        if (label == "Title") {
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
        var date_change by remember { mutableStateOf(viewModel._dateChanging.value) }
        present_date= viewModel._dateChanging.value!!
        viewModel._dateChanging.observe(this, Observer {
            println("present date:" + it.toString())
            date_change = it
        })
        Column(modifier = Modifier.fillMaxSize()) {
            Display_dates()   /*displays dates 5 circles*/
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
                IconButton(onClick = {  navHostController.navigate("view_notes")}) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "notes")
                }
            }
            finalDisplay(navHostController)    /*displays previous tasks and add new task*/
        }
    }

    @Composable
    fun Display_dates() {
        val context = LocalContext.current
        var change_detected by remember { mutableStateOf(present_date) }
        println("change_detected"+change_detected)
        var list_dates by remember {
            mutableStateOf(dates(present_date,present_month))
        }
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
                            change_detected=present_date_1
                        }
                        if(index==-1){
                            var proxy=list_dates
                            present_date=dates(proxy[0].first,getMonth_name(proxy[0].second))[0].first
                            present_month=getMonth_name(dates(proxy[0].first,getMonth_name(proxy[0].second))[0].second)
                            proxy=dates(present_date,present_month)
                            present_date=proxy[1].first
                            present_month=getMonth_number(proxy[1].second)
                            list_dates=dates(present_date,present_month)
                            viewModel._dateChanging.value=present_date
                            Log.i("MYTAG","dates1:"+list_dates)
                            viewModel.date_highlighted.value=present_date
                            change_detected=present_date
                            viewModel._dateChanging.value=present_date
                             index=2
                        }
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
                                    count++
                                    FirstDisplayItem(int = it.first, onClick = {
                                            Toast.makeText(context,"clicked on $it" , Toast.LENGTH_SHORT).show()
                                             viewModel.date_highlighted.value=it.first
                                            change_detected=it.first
                                    }, change_detected,it.second)
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
                            viewModel.date_highlighted.value=present_date_1
                            change_detected=present_date_1
                        }
                        if(index==5){
                            var proxy=list_dates
                            present_date=dates(proxy[4].first,getMonth_name(proxy[4].second))[4].first
                            present_month=getMonth_name(dates(proxy[4].first,getMonth_name(proxy[4].second))[4].second)
                            proxy=dates(present_date,present_month)
                            present_date=proxy[3].first
                            present_month=getMonth_number(proxy[3].second)
                            list_dates=dates(present_date,present_month)
                            viewModel._dateChanging.value=present_date
                            Log.i("MYTAG","dates2:"+list_dates)
                            viewModel.date_highlighted.value=present_date
                            change_detected=present_date
                            index=2
                        }
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
         onClick: () -> Unit
     ){
         val editedDescription =task_description
         val editedHeading = task_heading
         Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(10.dp)
                .background(Color.Blue)
        ) {
             Row(modifier = Modifier.padding(10.dp)) {
                Checkbox(
                    checked =check,
                    onCheckedChange = {
                         viewModel.change(dayEntitiy,index,it)
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
                        fontSize = 10.sp,
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
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
        var date_change by remember { mutableStateOf( present_date)}
            viewModel.date_highlighted.observe(this, Observer {
            date_change=it
            viewModel.set_date_changed_1(true)
            println("date ki data:"+it)
        })
        Column(modifier = Modifier
            .fillMaxWidth()){
            println("date changed")
            date_change?.let {
                displayTaskView(date = it, month =present_month,navHostController,true)
            }
        }
    }
    @Composable
    fun displayTaskView(date :Int,month: Int,navHostController: NavHostController,check: Boolean){
        var entity1 by remember{ mutableStateOf(viewModel.get_entitiy()) }
        try {
            viewModel.retrieveDayTasks(date, month).observe(this, Observer {
                viewModel.set_entity(it)
                if(it!=null){
                   entity1=it
                }else{
                    if(viewModel.get_date_chaged1()){
                        entity1=null
                        viewModel.set_date_changed_1(false)
                    }
                }
                println("data of highlighted:" + it+date+" "+month)
                })
        }catch (e:  RuntimeException){
            println("error occurred")
        }
        var change by remember { mutableStateOf(false) } /* aimed to recompose if any Task gets deleted*/
        Card (modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navHostController.navigate("floating")
            }
            .padding(10.dp)){
            Text(text = "Work To-Dos", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 25.sp, modifier = Modifier.padding(10.dp))
            if (entity1 == null || entity1!!.tasksList.size == 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No tasks present!!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                    Text(
                        text = "Add tasks by clicking",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            entity1?.let {
                change = showDailyTask(entitiy = it)
            }
        }
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
            add_on()
        }
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
    fun showDailyTask(entitiy: DayEntitiy) : Boolean{
         val tasklist =entitiy.tasksList
         var change by remember { mutableStateOf(false) }
         println("recomposed-3")
         LazyColumn(modifier = Modifier.padding(10.dp)) {
             var count=0
             if (tasklist.size > 0) {
                 items(items = tasklist) {
                     TaskStructure1(
                        task_description = it.description,
                        check = it.complete,
                        task_heading = it.heading,
                         entitiy,
                         count
                    ) {
                         change = !change
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(15.dp)
                    )
                 count++
                 }
            }
        }
    return  change
    }
    fun getTodayMonthDate(): Pair<Int, Int> {
        val date_month = LocalDateTime.now().dayOfMonth
        val month = LocalDateTime.now().monthValue
        println(month)
        return Pair(date_month, month)
    }
    fun show_count(){
        viewModel.all_days.observe(this) {
            println("size of saved entities: " + it.size + " " + it.toString())
        }
    }
}
