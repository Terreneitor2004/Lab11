package com.example.tasklistapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tasklistapp.ui.theme.TaskListAppTheme
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskListAppTheme {
                TaskListScreen()
            }
        }
    }
}

@Composable
fun TaskListScreen() {
    val context = LocalContext.current
    val tasks = remember { mutableStateListOf<Task>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    TaskListContent(
        tasks = tasks,
        onAddTask = { title, imageUri ->
            tasks.add(Task(title, imageUri))
        },
        onEditTask = { index, newTitle, newImageUri ->
            tasks[index] = Task(newTitle, newImageUri)
        },
        onDeleteTask = { index ->
            tasks.removeAt(index)
        }
    )
}

@Composable
fun TaskListContent(
    tasks: List<Task>,
    onAddTask: (String, String?) -> Unit,
    onEditTask: (Int, String, String?) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enhanced Todo", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick Image")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (newTaskTitle.isNotEmpty()) {
                    onAddTask(newTaskTitle, selectedImageUri)
                    newTaskTitle = ""
                    selectedImageUri = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TaskList(tasks, onEditTask, onDeleteTask)
    }
}

@Composable
fun TaskList(tasks: List<Task>, onEditTask: (Int, String, String?) -> Unit, onDeleteTask: (Int) -> Unit) {
    Column {
        tasks.forEachIndexed { index, task ->
            TaskItem(task, index, onEditTask, onDeleteTask)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun TaskItem(task: Task, index: Int, onEditTask: (Int, String, String?) -> Unit, onDeleteTask: (Int) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(task.title) }
    var editedImageUri by remember { mutableStateOf(task.imageUri) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        editedImageUri = uri.toString()
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = editedTitle,
                onValueChange = { editedTitle = it },
                label = { Text("Edit Task Title") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change Image")
            }
            Button(onClick = {
                onEditTask(index, editedTitle, editedImageUri)
                isEditing = false
            }) {
                Text("Save")
            }
        } else {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
            task.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Button(onClick = { isEditing = true }) {
                Text("Editar")
            }
            Button(onClick = { onDeleteTask(index) }) {
                Text("Borrar")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TaskListAppTheme {
        TaskListScreen()
    }
}
data class Task(val title: String, val imageUri: String?)
