package com.github.se.travelpouch.ui.dashboard

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.events.EventViewModel
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.fields.DateTimeInputField
import com.github.se.travelpouch.ui.fields.locationInputField
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.utils.DateTimeUtils

/**
 * This function describes how the screen to add an activity
 *
 * @param activityModelView (ActivityViewModel) : the view model for an activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddActivityScreen(
    navigationActions: NavigationActions,
    activityModelView: ActivityViewModel,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    eventViewModel: EventViewModel
) {

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") }

    val locationQuery = remember { mutableStateOf("") }
    val queryFromViewModel by locationViewModel.query.collectAsState()

    LaunchedEffect(queryFromViewModel) {
        locationQuery.value = queryFromViewModel
    }
  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  var selectedLocation by remember { mutableStateOf<Location?>(null) }

  val context = LocalContext.current

  val dateTimeUtils = DateTimeUtils("dd/MM/yyyy HH:mm")

  Scaffold(
      modifier = Modifier.testTag("AddActivityScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Add Activity", Modifier.testTag("travelTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SWIPER) },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { paddingValues ->
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
                  label = { Text("Title") },
                  placeholder = { Text("Title") },
                  modifier = Modifier.testTag("titleField").fillMaxWidth())

              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  placeholder = { Text("Description") },
                  modifier = Modifier.testTag("descriptionField").fillMaxWidth())

            // Date input
            DateTimeInputField(
                value = dateText,
                onValueChange = { newDate ->
                    if (newDate.isDigitsOnly() && newDate.length <= 8) { // Validate date format
                        dateText = newDate
                    }
                },
                label = "Date",
                placeholder = "DD/MM/YYYY",
                visualTransformation = DateVisualTransformation(),
                keyboardType = KeyboardType.Number,
                onDatePickerClick = {
                    dateTimeUtils.showDatePicker(context) { selectedDate ->
                        dateText = selectedDate.replace("/", "") // Set the selected date
                    }
                },
                onTimePickerClick = { /* Empty, not needed for date */ },
                isTime = false // Specify this is a date picker
            )

            // Time input
            DateTimeInputField(
                value = timeText,
                onValueChange = { timeText = it },
                label = "Time",
                placeholder = "HH:mm",
                visualTransformation = VisualTransformation.None,
                keyboardType = KeyboardType.Number,
                onDatePickerClick = { /* Empty, not needed for time */ },
                onTimePickerClick = {
                    dateTimeUtils.showTimePicker(context) { selectedTime ->
                        timeText = selectedTime // Set the selected time
                    }
                },
                isTime = true // Specify this is a time picker
            )

              locationInputField(
                    locationQuery = locationQuery,
                    locationSuggestions = locationSuggestions,
                    showDropdown = showDropdown,
                    locationViewModel = locationViewModel,
                    setSelectedLocation = { selectedLocation = it },
                    setShowDropdown = { showDropdown = it })

              Button(
                  enabled = title.isNotBlank() && description.isNotBlank() && dateText.isNotBlank(),
                  onClick = {

                    // Correctly format dateText from ddMMyyyy to dd/MM/yyyy
                    val formattedDateText =
                        if (dateText.length == 8) {
                          "${dateText.substring(0, 2)}/${dateText.substring(2, 4)}/${dateText.substring(4, 8)}"
                        } else {
                          dateText // If the length is incorrect, use it as is (to handle any edge
                          // cases)
                        }

                    val t = formattedDateText + timeText
                    Log.d(
                        "AddActivityScreen", "ATT : $title, $description, $dateText, $timeText, $t")
                    val finalDate =
                        dateTimeUtils.convertStringToTimestamp("$formattedDateText $timeText")
                    // Check if date parsing was successful
                    if (finalDate == null) {
                      Log.e("AddActivityScreen", "Invalid date or time format")
                      Toast.makeText(
                              context,
                              "Invalid date or time format. Please use DD/MM/YYYY or HH:mm.",
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      Log.d("AddActivityScreen", "All inputs are valid, proceeding to add Activity")
                      try {
                        val activity =
                            Activity(
                                activityModelView.getNewUid(),
                                title,
                                description,
                                selectedLocation ?: Location(0.0, 0.0, finalDate, "Unknown"),
                                finalDate,
                                mapOf())

                        activityModelView.addActivity(
                            activity, context, eventViewModel.getNewDocumentReference())

                        navigationActions.navigateTo(Screen.SWIPER)
                      } catch (e: java.text.ParseException) {
                        Toast.makeText(
                                context,
                                "Error adding Activity. Please try again.",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    }
                  },
                  modifier = Modifier.testTag("saveButton")) {
                    Text("Save")
                  }
            }
      })
}
