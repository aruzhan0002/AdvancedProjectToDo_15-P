package com.example.plannerapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {

    enum class Priority { LOW, MEDIUM, HIGH }

    data class Task(
        var title: String,
        var description: String = "",
        var isDone: Boolean = false,
        var priority: Priority = Priority.LOW
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showStartScreen by remember { mutableStateOf(true) }

            MaterialTheme(colorScheme = darkColorScheme()) {
                if (showStartScreen) {
                    StartScreen(onStartClicked = { showStartScreen = false })
                } else {
                    TaskPlannerApp()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun TaskPlannerApp() {
        val today = remember { LocalDate.now() }
        var selectedDate by remember { mutableStateOf(today) }
        var taskMap by remember { mutableStateOf(mutableMapOf<LocalDate, MutableList<Task>>()) }
        var showDialog by remember { mutableStateOf(false) }
        var newTaskTitle by remember { mutableStateOf("") }
        var newTaskDescription by remember { mutableStateOf("") }
        var newPriority by remember { mutableStateOf(Priority.LOW) }
        var editIndex by remember { mutableStateOf(-1) }

        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            newTaskTitle = ""
                            newTaskDescription = ""
                            newPriority = Priority.LOW
                            editIndex = -1
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "CREATE TASK",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            containerColor = Color(0xFF121212)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                HorizontalCalendar(selectedDate) { selectedDate = it }

                Spacer(modifier = Modifier.height(16.dp))

                Text("To-Do", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White))
                Spacer(modifier = Modifier.height(8.dp))

                val tasks = taskMap[selectedDate] ?: emptyList()

                if (tasks.isEmpty()) {
                    Text(
                        text = "There are no tasks for this day",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(tasks.toList()) { index, task ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (task.priority) {
                                                Priority.HIGH -> Color.Red
                                                Priority.MEDIUM -> Color(0xFFFFA726)
                                                Priority.LOW -> Color(0xFF81C784)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                task.priority.name,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Text("82%", color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(task.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                    if (task.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(task.description, color = Color.LightGray, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("10:00 AM - 06:00 PM", color = Color.Gray, fontSize = 13.sp)

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Due Date: ${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.dayOfMonth}", color = Color.White, fontSize = 13.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = task.isDone,
                                                onCheckedChange = {
                                                    taskMap = taskMap.toMutableMap().apply {
                                                        val updatedList = getOrPut(selectedDate) { mutableListOf() }.toMutableList()
                                                        updatedList[index] = task.copy(isDone = it)
                                                        this[selectedDate] = updatedList
                                                    }
                                                }
                                            )
                                        }
                                        Row {
                                            TextButton(onClick = {
                                                newTaskTitle = task.title
                                                newTaskDescription = task.description
                                                newPriority = task.priority
                                                editIndex = index
                                                showDialog = true
                                            }) {
                                                Text("Edit")
                                            }
                                            TextButton(onClick = {
                                                taskMap = taskMap.toMutableMap().apply {
                                                    val updatedList = getOrPut(selectedDate) { mutableListOf() }.toMutableList()
                                                    if (index in updatedList.indices) {
                                                        updatedList.removeAt(index)
                                                        this[selectedDate] = updatedList
                                                    }
                                                }
                                            }) {
                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(if (editIndex == -1) "Add new to-do" else "Edit to-do") },
                    text = {
                        Column {
                            val priorityOptions = Priority.values().toList()
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                label = { Text("Task title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newTaskDescription,
                                onValueChange = { newTaskDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Priority", color = Color.White)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                priorityOptions.forEach { option ->
                                    val selected = newPriority == option
                                    Button(
                                        onClick = { newPriority = option },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected) Color(0xFFB39DDB) else Color.DarkGray
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(option.name)
                                    }
                                }
                            }

                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                taskMap = taskMap.toMutableMap().apply {
                                    val updatedList = getOrPut(selectedDate) { mutableListOf() }.toMutableList()
                                    if (editIndex == -1) {
                                        updatedList.add(Task(newTaskTitle, newTaskDescription, false, newPriority))
                                    } else if (editIndex in updatedList.indices) {
                                        updatedList[editIndex] = Task(newTaskTitle, newTaskDescription, updatedList[editIndex].isDone, newPriority)
                                    }
                                    this[selectedDate] = updatedList
                                }
                                showDialog = false
                            } else {
                                Toast.makeText(this@MainActivity, "Please enter task title", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(if (editIndex == -1) "Add" else "Save")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun HorizontalCalendar(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
        val dates = (0..30).map { LocalDate.now().minusDays(15).plusDays(it.toLong()) }
        val dayFormatter = DateTimeFormatter.ofPattern("dd")
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy", Locale.ENGLISH)
        val dayNameFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

        val currentMonth = monthFormatter.format(selectedDate)

        Column {
            Text(
                text = currentMonth,
                style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(dates) { _, date ->
                    val isSelected = date == selectedDate
                    val bgColor = if (isSelected) Color.White else Color.DarkGray
                    val textColor = if (isSelected) Color.Black else Color.White

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(4.dp)
                            .width(60.dp)
                            .clickable { onDateSelected(date) }
                    ) {
                        Text(
                            text = dayNameFormatter.format(date),
                            style = TextStyle(color = Color.Gray, fontSize = 12.sp)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(color = bgColor, shape = RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = dayFormatter.format(date),
                                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            )
                        }
                    }
                }
            }
        }
    }


@Composable
fun StartScreen(onStartClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.start_1),
                contentDescription = "Start Illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Task Management &\nTo-Do List",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This productive tool is designed to help\nyou better manage your task\nproject-wise conveniently!",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onStartClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Let's Start", color = Color.White)


            }
        }
    }
}
    }


