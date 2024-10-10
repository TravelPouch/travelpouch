package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

// parts of this file was generated using Github Copilot
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTravelSettingsScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
) {
    val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()

    Scaffold(
        modifier = Modifier.testTag("editScreen"),
        topBar = {
            MediumTopAppBar(
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Edit Travel", modifier = Modifier.testTag("editTravelText")) },
                navigationIcon = {
                    Button(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("goBackButton")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description")
                    }
                })
        },
    ) {}

    if (selectedTravel != null) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val titleText = remember { mutableStateOf(selectedTravel!!.title) }
        val descriptionText = remember { mutableStateOf(selectedTravel!!.description) }
        val assigneeText = remember { mutableStateOf(selectedTravel!!.description) }
        val location: MutableState<Location> = remember { mutableStateOf(selectedTravel!!.location) }
        val endTimeText = remember {
            mutableStateOf(formatter.format(selectedTravel!!.endTime.toDate()))
        }
        val context = LocalContext.current
        Column(modifier = Modifier.padding(15.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = titleText.value,
                onValueChange = { keystroke -> titleText.value = keystroke },
                modifier = Modifier.testTag("inputTodoTitle"),
                label = { Text("Title") },
                placeholder = { Text("Name the Task") })
            OutlinedTextField(
                value = descriptionText.value,
                onValueChange = { keystroke -> descriptionText.value = keystroke },
                modifier = Modifier.testTag("inputTodoDescription"),
                label = { Text("Description") },
                placeholder = { Text("Describe the task") })
            OutlinedTextField(
                value = assigneeText.value,
                onValueChange = { keystroke -> assigneeText.value = keystroke },
                modifier = Modifier.testTag("inputTodoAssignee"),
                label = { Text("Assignee") },
                placeholder = { Text("Assign a person") })
//            OutlinedTextField(
//                value = locationText.value,
//                onValueChange = { keystroke -> locationText.value = keystroke },
//                modifier = Modifier.testTag("inputTodoLocation"),
//                label = { Text("Enter an address") },
//                placeholder = { Text("Enter an address") }
//            )

            OutlinedTextField(
                value = endTimeText.value,
                onValueChange = { keystroke -> endTimeText.value = keystroke },
                modifier = Modifier.testTag("inputTodoDate"),
                label = { Text("Due Date") },
                placeholder = { Text("--/--/--") })

            Button(
                onClick = {
                    try {
                        val fsUid = selectedTravel!!.fsUid
                        val dateFormat =
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                isLenient = false // strict date format
                            }
                        val date = dateFormat.parse(endTimeText.value)
                        val calendar =
                            GregorianCalendar().apply {
                                time = date!!
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                        val timestamp = Timestamp(calendar.time)
                        // val location =
                        //    Location(name = "Placeholder", latitude = 69.0, longitude = 42.0)
                        val newTodo =
                            TravelContainer(
                                fsUid = fsUid,
                                title = titleText.value,
                                description = descriptionText.value,
                                startTime = selectedTravel!!.startTime,
                                endTime = Timestamp.now(),
                                location = selectedTravel!!.location,
                                allAttachments = selectedTravel!!.allAttachments,
                                allParticipants = selectedTravel!!.allParticipants,
                            )
                        listTravelViewModel.updateTravel(
                            newTodo,
//                            onSuccess = {
//                                navigationActions.goBack()
//                                Toast.makeText(
//                                    context, "Saved todo successfully to firebase", Toast.LENGTH_SHORT)
//                                    .show()
//                            },
//                            onFailure = {
//                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
//                            }
                        )
                    } catch (e: ParseException) {
                        Toast.makeText(context, "Error: due date invalid", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.testTag("travelSave")) {
                Text("Save")
            }

            Button(
                onClick = {
                    navigationActions.goBack()
                    listTravelViewModel.deleteTravelById(selectedTravel!!.fsUid)
                },
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, contentColor = Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("travelDelete").fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    // text = "🗑  Delete",
                    text = "Delete",
                    fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Text("No ToDo to be edited was selected. If you read this message an error has occurred.",
            modifier = Modifier.padding(15.dp).testTag("noTravelSelected"))

    }
}
