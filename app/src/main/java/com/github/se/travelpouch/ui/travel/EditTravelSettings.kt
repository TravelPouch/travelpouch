package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.TravelContainer
import com.github.se.travelpouch.model.travels.TravelRepository
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
    notificationViewModel: NotificationViewModel,
    profileViewModel: ProfileModelView,
    locationViewModel: LocationViewModel
) {
  val selectedTravel by listTravelViewModel.selectedTravel.collectAsState()
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val (expandedAddUserDialog, setExpandedAddUserDialog) = remember { mutableStateOf(false) }
  val darkTheme = isSystemInDarkTheme()
  val dateTimeUtils = DateTimeUtils("dd/MM/yyyy")

  Scaffold(
      modifier = Modifier.testTag("editScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Edit Travel", modifier = Modifier.testTag("editTravelText")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description")
                  }
            })
      },
      floatingActionButton = {
        var toggled by remember { mutableStateOf(false) }

        Column(horizontalAlignment = Alignment.End) {
          if (toggled) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
              ExtendedFloatingActionButton(
                  text = {
                    Text(
                        "Manage participants",
                        modifier = Modifier.testTag("manageParticipantsText"))
                  },
                  icon = { Icon(Icons.Default.Person, contentDescription = "Manage Participants") },
                  onClick = {
                    listTravelViewModel.fetchAllParticipantsInfo()
                    navigationActions.navigateTo(PARTICIPANT_LIST)
                    toggled = !toggled
                  },
                  modifier = Modifier.testTag("manageParticipantsButton"))

              Spacer(modifier = Modifier.height(8.dp))

              ExtendedFloatingActionButton(
                  text = { Text("Import Email", modifier = Modifier.testTag("importEmailText")) },
                  icon = { Icon(Icons.Default.MailOutline, contentDescription = "Import Email") },
                  onClick = {
                    clipboardManager.setText(
                        AnnotatedString("travelpouchswent+${selectedTravel!!.fsUid}@gmail.com"))
                    Log.d("EditTravelSettingsScreen", "Email copied to clipboard")
                    toggled = !toggled
                  },
                  modifier = Modifier.testTag("importEmailFab"))
            }
          } else {
            FloatingActionButton(
                onClick = { toggled = !toggled }, modifier = Modifier.testTag("plusButton")) {
                  Icon(Icons.Default.Add, contentDescription = "Expend button")
                }
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
      var selectedLocation by remember { mutableStateOf(selectedTravel!!.location) }
      val locationQuery = remember {
        mutableStateOf(selectedTravel!!.location.name)
      } // Use mutable state for location query
      // locationViewModel.setQuery(selectedTravel!!.location.name)
      var showDropdown by remember { mutableStateOf(false) }
      val locationSuggestions by
          locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

      val endTimeText = remember {
        mutableStateOf(formatter.format(selectedTravel!!.endTime.toDate()))
      }

      Column(
          modifier =
              Modifier.padding(padding)
                  .testTag("editTravelColumn")
                  .verticalScroll(rememberScrollState()),
          Arrangement.spacedBy(8.dp),
          Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier =
                    Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .testTag("editTravelRow")) {
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = "Participants",
                      modifier =
                          Modifier.padding(start = 50.dp, end = 8.dp)
                              .size(50.dp)
                              .testTag("editTravelParticipantIcon"))
                  Text(
                      "${selectedTravel!!.allParticipants.size} participants",
                      modifier = Modifier.weight(1f).testTag("inputParticipants"))
                }

            OutlinedTextField(
                value = titleText.value,
                onValueChange = { keystroke -> titleText.value = keystroke },
                modifier =
                    Modifier.testTag("inputTravelTitle").width(300.dp).padding(vertical = 4.dp),
                label = { Text("Title") },
                placeholder = { Text("Name the Travel") },
                shape = RoundedCornerShape(6.dp),
            )
            OutlinedTextField(
                value = descriptionText.value,
                onValueChange = { keystroke -> descriptionText.value = keystroke },
                modifier =
                    Modifier.testTag("inputTravelDescription")
                        .width(300.dp)
                        .padding(vertical = 4.dp),
                label = { Text("Description") },
                placeholder = { Text("Describe the Travel") },
                shape = RoundedCornerShape(6.dp))

            Box(modifier = Modifier.width(300.dp).padding(vertical = 4.dp)) {
              OutlinedTextField(
                  value = locationQuery.value,
                  onValueChange = {
                    locationQuery.value = it
                    locationViewModel.setQuery(it)
                    showDropdown = true
                  },
                  label = { Text("Location") },
                  placeholder = { Text("Enter an Address or Location") },
                  modifier =
                      Modifier.width(300.dp)
                          .padding(vertical = 4.dp)
                          .testTag("inputTravelLocation"))

              // Dropdown for location suggestions
              DropdownMenu(
                  expanded = showDropdown && locationSuggestions.isNotEmpty(),
                  onDismissRequest = { showDropdown = false },
                  properties = PopupProperties(focusable = false),
                  modifier =
                      Modifier.fillMaxWidth()
                          .heightIn(max = 200.dp)
                          .testTag("locationDropdownMenu")) {
                    locationSuggestions.filterNotNull().take(3).forEach { location ->
                      DropdownMenuItem(
                          text = {
                            Text(
                                text =
                                    location.name.take(30) +
                                        if (location.name.length > 30) "..." else "",
                                maxLines = 1,
                                modifier = Modifier.testTag("suggestionText_${location.name}"))
                          },
                          onClick = {
                            locationViewModel.setQuery(location.name)
                            selectedLocation = location // Store the selected location object
                            locationQuery.value =
                                location
                                    .name // Update location query with the selected location name
                            showDropdown = false // Close dropdown on selection
                          },
                          modifier =
                              Modifier.padding(8.dp)
                                  .testTag("suggestion_${location.name}") // Tag each suggestion
                          )
                      HorizontalDivider() // Separate items with a divider
                    }

                    if (locationSuggestions.size > 3) {
                      DropdownMenuItem(
                          text = { Text("More...") },
                          onClick = { /* Optionally show more results */},
                          modifier = Modifier.padding(8.dp).testTag("moreSuggestions"))
                    }
                  }
            }

            OutlinedTextField(
                value = startTime.value,
                onValueChange = { keystroke -> startTime.value = keystroke }, // Allow manual input
                label = { Text("Start Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier =
                    Modifier.testTag("inputTravelStartTime").width(300.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
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
                modifier =
                    Modifier.testTag("inputTravelEndTime").width(300.dp).padding(vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
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

            Button(
                onClick = {
                  try {
                    val fsUid = selectedTravel!!.fsUid
                    val newLocation: Location
                    try {
                      newLocation =
                          Location(
                              latitude = selectedLocation.latitude,
                              longitude = selectedLocation.longitude,
                              name = selectedLocation.name,
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
                            listParticipant = selectedTravel!!.listParticipant)
                    listTravelViewModel.updateTravel(
                        newTravel, TravelRepository.UpdateMode.FIELDS_UPDATE, null)
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
                modifier = Modifier.testTag("travelDeleteButton").padding(vertical = 8.dp)) {
                  Text(
                      text = "Delete",
                  )
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
