package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.github.se.travelpouch.ui.navigation.Screen.PARTICIPANT_LIST
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
      floatingActionButton = {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(150.dp)) {
              FloatingActionButton(
                  onClick = {
                    Toast.makeText(context, "Add User clicked", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("addUserFab").padding(end = 16.dp)) {
                    Text("Add User")
                  }
              FloatingActionButton(
                  onClick = {
                    Toast.makeText(context, "Import Email clicked to clipboard", Toast.LENGTH_SHORT)
                        .show()
                  },
                  modifier = Modifier.testTag("importEmailFab")) {
                    Text("Import Email to Clipboard")
                  }
            }
      },
      floatingActionButtonPosition = FabPosition.End,
  ) { padding ->
    if (selectedTravel != null) {
      val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      val titleText = remember { mutableStateOf(selectedTravel!!.title) }
      val descriptionText = remember { mutableStateOf(selectedTravel!!.description) }
      val startTime = remember {
        mutableStateOf(formatter.format(selectedTravel!!.startTime.toDate()))
      }
      val locationName = remember { mutableStateOf(selectedTravel!!.location.name) }
      val latitude = remember { mutableStateOf(selectedTravel!!.location.latitude.toString()) }
      val longitude = remember { mutableStateOf(selectedTravel!!.location.longitude.toString()) }
      val endTimeText = remember {
        mutableStateOf(formatter.format(selectedTravel!!.endTime.toDate()))
      }

      Column(
          modifier =
              Modifier.padding(padding)
                  .testTag("editTravelColumn")
                  .verticalScroll(rememberScrollState()),
          Arrangement.Top,
          Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp).testTag("editTravelRow")) {
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = "Participants",
                      modifier =
                          Modifier.padding(start = 50.dp, end = 8.dp)
                              .size(50.dp)
                              .testTag("editTravelParticipantIcon")
                              .clickable {
                                listTravelViewModel.fetchAllParticipantsInfo()
                                navigationActions.navigateTo(PARTICIPANT_LIST)
                                Toast.makeText(context, "Icon clicked", Toast.LENGTH_SHORT).show()
                              })
                  Text(
                      "${selectedTravel!!.allParticipants.size} participants",
                      modifier = Modifier.weight(1f).testTag("inputParticipants"))
                }

            OutlinedTextField(
                value = titleText.value,
                onValueChange = { keystroke -> titleText.value = keystroke },
                modifier = Modifier.testTag("inputTravelTitle"),
                label = { Text("Title") },
                placeholder = { Text("Name the Travel") })
            OutlinedTextField(
                value = descriptionText.value,
                onValueChange = { keystroke -> descriptionText.value = keystroke },
                modifier = Modifier.testTag("inputTravelDescription"),
                label = { Text("Description") },
                placeholder = { Text("Describe the Travel") })

            OutlinedTextField(
                value = locationName.value,
                onValueChange = { locationName.value = it },
                label = { Text("Location Name") },
                placeholder = { Text("Enter location name") },
                modifier = Modifier.testTag("inputTravelLocationName"))

            // Latitude Input
            OutlinedTextField(
                value = latitude.value,
                onValueChange = { latitude.value = it },
                label = { Text("Latitude") },
                placeholder = { Text("Enter latitude (e.g. 48.8566)") },
                modifier = Modifier.testTag("inputTravelLatitude"))

            // Longitude Input
            OutlinedTextField(
                value = longitude.value,
                onValueChange = { longitude.value = it },
                label = { Text("Longitude") },
                placeholder = { Text("Enter longitude (e.g. 2.3522)") },
                modifier = Modifier.testTag("inputTravelLongitude"))
            OutlinedTextField(
                value = startTime.value,
                onValueChange = { keystroke -> startTime.value = keystroke },
                modifier = Modifier.testTag("inputTravelStartTime"),
                label = { Text("StartTime") },
                placeholder = { Text("StartTime") })
            OutlinedTextField(
                value = endTimeText.value,
                onValueChange = { keystroke -> endTimeText.value = keystroke },
                modifier = Modifier.testTag("inputTravelEndTime"),
                label = { Text("EndTime") },
                placeholder = { Text("--/--/--") })

            Button(
                onClick = {
                  try {
                    val fsUid = selectedTravel!!.fsUid
                    var newLocation = selectedTravel!!.location
                    try {
                      newLocation =
                          Location(
                              latitude = latitude.value.toDouble(),
                              longitude = longitude.value.toDouble(),
                              name = locationName.value,
                              insertTime = Timestamp.now())
                    } catch (e: NumberFormatException) {
                      Toast.makeText(
                              context,
                              "Error: latitude and longitude must be numbers",
                              Toast.LENGTH_SHORT)
                          .show()
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
                        )
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
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, contentColor = Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier =
                    Modifier.testTag("travelDeleteButton")
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                  Text(
                      // text = "🗑  Delete",
                      text = "Delete",
                      fontWeight = FontWeight.Bold)
                }
          }
    } else {
      Text(
          "No Travel to be edited was selected. If you read this message an error has occurred.",
          modifier = Modifier.padding(padding).testTag("noTravelSelectedText"))
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
