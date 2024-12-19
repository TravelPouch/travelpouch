// Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
package com.github.se.travelpouch.ui.dashboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.fields.DateTimeInputField
import com.github.se.travelpouch.ui.fields.LocationInputField
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.utils.DateTimeUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * This function displays the screen that allows us to edit an activity
 *
 * @param navigationActions (NavigationActions) : the navigation actions that we use to navigate
 *   between screens
 * @param activityViewModel (ActivityViewModel) : the view model used to manange activities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActivity(
    navigationActions: NavigationActions,
    activityViewModel: ActivityViewModel,
    locationViewModel: LocationViewModel
) {

  val context = LocalContext.current

  val selectedActivity = activityViewModel.selectedActivity.collectAsState()

  val zoneId = ZoneId.systemDefault()
  val timeHour = ZonedDateTime.ofInstant(selectedActivity.value!!.date.toDate().toInstant(), zoneId)

  var title by remember { mutableStateOf(selectedActivity.value!!.title) }
  var description by remember { mutableStateOf(selectedActivity.value!!.description) }
  val time = timeHour.format(DateTimeFormatter.ofPattern("HH:mm"))
  var timeText by remember { mutableStateOf(time) }
  var date by remember {
    mutableStateOf(convertDateToString(selectedActivity.value!!.date.toDate()))
  }
  var selectedLocation by remember { mutableStateOf(selectedActivity.value!!.location) }
  val locationQuery = remember {
    mutableStateOf(selectedActivity.value!!.location.name)
  } // Use mutable state for location query
  // locationViewModel.setQuery(selectedTravel!!.location.name)
  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  val dateTimeUtils = DateTimeUtils("dd/MM/yyyy HH:mm")

  Scaffold(
      modifier = Modifier.testTag("EditActivityScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Edit Activity", Modifier.testTag("travelTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SWIPER) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  enabled = true,
                  label = { Text("Title") },
                  modifier = Modifier.fillMaxWidth().testTag("titleField"))

              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  enabled = true,
                  label = { Text("Description") },
                  modifier = Modifier.fillMaxWidth().testTag("descriptionField"))

              // Date Input
              DateTimeInputField(
                  value = date,
                  onValueChange = { newDate ->
                    if (newDate.isDigitsOnly() && newDate.length <= 8) { // Validate date format
                      date = newDate
                    }
                  },
                  label = "Date",
                  placeholder = "DD/MM/YYYY",
                  visualTransformation = DateVisualTransformation(),
                  keyboardType = KeyboardType.Number,
                  onDatePickerClick = {
                    dateTimeUtils.showDatePicker(context) { selectedDate ->
                      date = selectedDate.replace("/", "") // Set the selected date
                    }
                  },
                  onTimePickerClick = { /* Empty, not needed for date */},
                  isTime = false // Specify this is a date picker
                  )

              // Time Input
              DateTimeInputField(
                  value = timeText,
                  onValueChange = { timeText = it },
                  label = "Time",
                  placeholder = "HH:mm",
                  visualTransformation = VisualTransformation.None,
                  keyboardType = KeyboardType.Number,
                  onDatePickerClick = { /* Empty, not needed for time */},
                  onTimePickerClick = {
                    dateTimeUtils.showTimePicker(context) { selectedTime ->
                      timeText = selectedTime // Set the selected time
                    }
                  },
                  isTime = true // Specify this is a time picker
                  )

              LocationInputField(
                  locationQuery = locationQuery,
                  locationSuggestions = locationSuggestions,
                  showDropdown = showDropdown,
                  setShowDropdown = { showDropdown = it },
                  locationViewModel = locationViewModel,
                  setSelectedLocation = { selectedLocation = it })

              Button(
                  enabled =
                      selectedLocation.name.isNotBlank() &&
                          title.isNotBlank() &&
                          description.isNotBlank() &&
                          date.isNotBlank(),
                  onClick = {

                    // Correctly format dateText from ddMMyyyy to dd/MM/yyyy
                    val formattedDateText =
                        if (date.length == 8) {
                          "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4, 8)}"
                        } else {
                          date // If the length is incorrect, use it as is (to handle any edge
                          // cases)
                        }

                    val finalDate =
                        dateTimeUtils.convertStringToTimestamp("$formattedDateText $timeText")
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

                    if (finalDate == null) {
                      Log.e("EditActivityScreen", "Invalid date or format")
                      Toast.makeText(
                              context,
                              "Invalid date or time format. Please use DD/MM/YYYY or HH:mm.",
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      try {
                        Log.d(
                            "EditActivityScreen",
                            "All inputs are valid, proceeding to add Activity")

                        val activity =
                            Activity(
                                selectedActivity.value!!.uid,
                                title,
                                description,
                                newLocation,
                                finalDate,
                                mapOf())

                        activityViewModel.updateActivity(activity, context)
                        navigationActions.navigateTo(Screen.SWIPER)
                      } catch (e: java.text.ParseException) {
                        Toast.makeText(
                                context,
                                "Error editing Activity. Please try again.",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    }
                  },
                  modifier = Modifier.testTag("saveButton")) {
                    Text("Save")
                  }

              Button(
                  enabled = true,
                  onClick = {
                    activityViewModel.deleteActivityById(selectedActivity.value!!, context)
                    navigationActions.navigateTo(Screen.SWIPER)
                  },
                  modifier = Modifier.testTag("deleteButton")) {
                    Text("Delete")
                  }
            }
      }
}

/**
 * This function converts a date to a String
 *
 * @param date (Date) : the date we want to convert
 * @return (String) : the string obtained from the original date
 */
private fun convertDateToString(date: Date): String {
  val df = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
  return df.format(date)
}
