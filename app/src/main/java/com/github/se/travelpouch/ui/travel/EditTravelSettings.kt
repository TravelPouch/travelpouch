package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.R
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen.PARTICIPANT_LIST
import com.github.se.travelpouch.utils.DateTimeUtils
import com.google.firebase.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

// parts of this file was generated using Github Copilot

/**
 * A composable function that displays the Edit Travel Settings screen.
 *
 * @param listTravelViewModel The ViewModel that holds the state and logic for the travel list.
 * @param navigationActions The navigation actions to handle navigation events.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTravelSettingsScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions,
) {
    val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val (expandedAddUserDialog, setExpandedAddUserDialog) = remember { mutableStateOf(false) }

    val dateTimeUtils = DateTimeUtils("dd/MM/yyyy")

    Scaffold(
        modifier = Modifier.testTag("editScreen"),
        topBar = {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.travel_settings_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    contentScale = ContentScale.FillWidth, // Display the image from the bottom
                    alignment = Alignment.BottomCenter // Align the image to the bottom
                )
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                    ),
                    title = {
                        Text(
                            "Edit Travel",
                            modifier = Modifier.testTag("editTravelText"),
                            fontWeight = FontWeight.Bold // Set the title to bold
                        )
                    },
                    navigationIcon = {
                        Button(
                            onClick = { navigationActions.goBack() },
                            modifier = Modifier.testTag("goBackButton")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                )
            }
        }.
        floatingActionButton = {
            var toggled by remember { mutableStateOf(false) }

            Column(horizontalAlignment = Alignment.End) {
                if (toggled) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
                        ExtendedFloatingActionButton(
                            text = { Text("Add User", modifier = Modifier.testTag("addUserButtonText")) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Add User") },
                            onClick = {
                                setExpandedAddUserDialog(true)
                                Log.d("EditTravelSettingsScreen", "Add User clicked")
                            },
                            modifier = Modifier.testTag("addUserButton")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExtendedFloatingActionButton(
                            text = { Text("Import Email to Clipboard", modifier = Modifier.testTag("importEmailButtonText")) },
                            icon = { Icon(Icons.Default.UploadFile, contentDescription = "Import Email") },
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString("travelpouchswent+${selectedTravel!!.fsUid}@gmail.com")
                                )
                                Log.d("EditTravelSettingsScreen", "Email copied to clipboard")
                            },
                            modifier = Modifier.testTag("importEmailButton")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
            // Longitude Input
            OutlinedTextField(
                value = longitude.value,
                onValueChange = { longitude.value = it },
                label = { Text("Longitude") },
                placeholder = { Text("Enter longitude (e.g. 2.3522)") },
                modifier = Modifier.testTag("inputTravelLongitude"))
            OutlinedTextField(
                value = startTime.value,
                onValueChange = { keystroke -> startTime.value = keystroke }, // Allow manual input
                label = { Text("Start Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier = Modifier.fillMaxWidth().testTag("inputTravelStartTime"),
                trailingIcon = {
                  IconButton(
                      onClick = {
                        dateTimeUtils.showDatePicker(context) { selectedDate ->
                          startTime.value = selectedDate
                        }
                      },
                      modifier = Modifier.testTag("startDatePickerButton")) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Start Date")
                      }
                })
            OutlinedTextField(
                value = endTimeText.value,
                onValueChange = { keystroke ->
                  endTimeText.value = keystroke
                }, // Allow manual input
                label = { Text("End Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier = Modifier.fillMaxWidth().testTag("inputTravelEndTime"),
                trailingIcon = {
                  IconButton(
                      onClick = {
                        dateTimeUtils.showDatePicker(context) { selectedDate ->
                          endTimeText.value = selectedDate
                        }
                      },
                      modifier = Modifier.testTag("endDatePickerButton")) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select End Date")
                      }
                })
                        FloatingActionButton(
                            onClick = { toggled = !toggled },
                            modifier = Modifier.testTag("dropDownButton")
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Collapse")
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = { toggled = !toggled },
                        modifier = Modifier.testTag("plusButton")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Expand")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    )  { padding ->
        if (selectedTravel != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val titleText = remember { mutableStateOf(selectedTravel!!.title) }
            val descriptionText = remember { mutableStateOf(selectedTravel!!.description) }
            val startTime = remember { mutableStateOf(formatter.format(selectedTravel!!.startTime.toDate())) }
            val locationName = remember { mutableStateOf(selectedTravel!!.location.name) }
            val latitude = remember { mutableStateOf(selectedTravel!!.location.latitude.toString()) }
            val longitude = remember { mutableStateOf(selectedTravel!!.location.longitude.toString()) }
            val endTimeText = remember { mutableStateOf(formatter.format(selectedTravel!!.endTime.toDate())) }

            Column(
                modifier = Modifier.padding(padding)
                    .testTag("editTravelColumn")
                    .verticalScroll(rememberScrollState()),
                Arrangement.Top,
                Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp).testTag("editTravelRow")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Participants",
                        modifier = Modifier.padding(start = 50.dp, end = 8.dp)
                            .size(50.dp)
                            .testTag("editTravelParticipantIcon")
                            .clickable {
                                listTravelViewModel.fetchAllParticipantsInfo()
                                navigationActions.navigateTo(PARTICIPANT_LIST)
                            }
                    )
                    Text(
                        "${selectedTravel!!.allParticipants.size} participants",
                        modifier = Modifier.weight(1f).testTag("inputParticipants")
                    )
                }

                val textFieldModifier = Modifier
                    .testTag("inputField")
                    .width(300.dp) // Set a fixed width for all text fields
                    .padding(vertical = 4.dp)

                OutlinedTextField(
                    value = titleText.value,
                    onValueChange = { keystroke -> titleText.value = keystroke },
                    modifier = textFieldModifier.testTag("inputTravelTitle"),
                    label = { Text("Title") },
                    placeholder = { Text("Name the Travel") },
                    maxLines = 1
                )
                OutlinedTextField(
                    value = descriptionText.value,
                    onValueChange = { keystroke -> descriptionText.value = keystroke },
                    modifier = textFieldModifier.testTag("inputTravelDescription"),
                    label = { Text("Description") },
                    placeholder = { Text("Describe the Travel") },
                    maxLines = 1
                )
                OutlinedTextField(
                    value = locationName.value,
                    onValueChange = { locationName.value = it },
                    label = { Text("Location Name") },
                    placeholder = { Text("Enter location name") },
                    modifier = textFieldModifier.testTag("inputTravelLocationName"),
                    maxLines = 1
                )
                OutlinedTextField(
                    value = latitude.value,
                    onValueChange = { latitude.value = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("Enter latitude (e.g. 48.8566)") },
                    modifier = textFieldModifier.testTag("inputTravelLatitude"),
                    maxLines = 1
                )
                OutlinedTextField(
                    value = longitude.value,
                    onValueChange = { longitude.value = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("Enter longitude (e.g. 2.3522)") },
                    modifier = textFieldModifier.testTag("inputTravelLongitude"),
                    maxLines = 1
                )
                OutlinedTextField(
                    value = startTime.value,
                    onValueChange = { keystroke -> startTime.value = keystroke },
                    modifier = textFieldModifier.testTag("inputTravelStartTime"),
                    label = { Text("StartTime") },
                    placeholder = { Text("StartTime") },
                    maxLines = 1
                )
                OutlinedTextField(
                    value = endTimeText.value,
                    onValueChange = { keystroke -> endTimeText.value = keystroke },
                    modifier = textFieldModifier.testTag("inputTravelEndTime"),
                    label = { Text("EndTime") },
                    placeholder = { Text("--/--/--") },
                    maxLines = 1
                )

                Button(
                    onClick = {
                        try {
                            val fsUid = selectedTravel!!.fsUid
                            val newLocation: Location
                            try {
                                newLocation = Location(
                                    latitude = latitude.value.toDouble(),
                                    longitude = longitude.value.toDouble(),
                                    name = locationName.value,
                                    insertTime = Timestamp.now()
                                )
                            } catch (e: NumberFormatException) {
                                Toast.makeText(
                                    context,
                                    "Error: latitude and longitude must be numbers",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newStart = parseDateToTimestamp(startTime.value)
                            val newEnd = parseDateToTimestamp(endTimeText.value)
                            
                    val newTravel =
                        TravelContainer(
                            fsUid = fsUid,
                            title = titleText.value,
                            description = descriptionText.value,
                            startTime = newStart,
                            endTime = newEnd,
                            location = newLocation,
                            allAttachments = selectedTravel!!.allAttachments,
                            allParticipants = selectedTravel!!.allParticipants,
                            listParticipant = selectedTravel!!.listParticipant)
                    listTravelViewModel.updateTravel(newTravel)
                    Toast.makeText(context, "Save clicked", Toast.LENGTH_SHORT).show()
                  } catch (e: ParseException) {
                    Toast.makeText(context, "Error: due date invalid", Toast.LENGTH_SHORT).show()
                    return@Button
                  } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@Button
                  }
                },
                modifier = Modifier.testTag("travelSaveButton")) {
                  Text("Save")
                }

                Button(
                    onClick = {
                        navigationActions.goBack()
                        listTravelViewModel.deleteTravelById(selectedTravel!!.fsUid)
                        Toast.makeText(context, "Delete clicked", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("travelDeleteButton")
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                "No Travel to be edited was selected. If you read this message an error has occurred.",
                modifier = Modifier.padding(padding).testTag("noTravelSelectedText")
            )
        }
        if (expandedAddUserDialog) {
            val addUserEmail = remember { mutableStateOf("newuser.email@example.org") }
            Dialog(onDismissRequest = { setExpandedAddUserDialog(false) }) {
                Box(Modifier.size(800.dp, 250.dp).background(Color.White).testTag("addUserDialogBox")) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                            .testTag("roleDialogColumn"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Add User by Email",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp).testTag("addUserDialogTitle")
                        )
                        OutlinedTextField(
                            value = addUserEmail.value,
                            onValueChange = { addUserEmail.value = it },
                            label = { Text("Enter User's Email") },
                            placeholder = { Text("Enter User's Email") },
                            modifier = Modifier.testTag("addUserEmailField"),
                            maxLines = 1
                        )
                        Button(
                            onClick = {
                                listTravelViewModel.addUserToTravel(
                                    addUserEmail.value,
                                    selectedTravel!!,
                                    { updatedContainer ->
                                        listTravelViewModel.selectTravel(updatedContainer)
                                        Toast.makeText(context, "User added successfully!", Toast.LENGTH_SHORT).show()
                                        setExpandedAddUserDialog(false)
                                    },
                                    {
                                        Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.testTag("addUserButton")
                        ) {
                            Text("Add User")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses a date string in the format "dd/MM/yyyy" to a `Timestamp`.
 *
 * @param dateString The date string to be parsed.
 * @return A `Timestamp` representing the parsed date.
 * @throws ParseException If the date string is not in the expected format.
 */
fun parseDateToTimestamp(dateString: String): Timestamp {
  val dateFormat =
      SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH).apply {
        isLenient = false // strict date format
      }
  val date = dateFormat.parse(dateString)
  val calendar = GregorianCalendar(Locale.FRENCH)
  val datetime =
      calendar.apply {
        time = date!!
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
      }
  return Timestamp(datetime.time)
}
