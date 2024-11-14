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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.github.se.travelpouch.utils.DateTimeUtils
import java.util.*

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
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dateText by remember { mutableStateOf("") }
  var timeText by remember { mutableStateOf("") }

  val locationQuery by locationViewModel.query.collectAsState()
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
                  onClick = { navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

              // Date Input with Visual Transformation
              OutlinedTextField(
                  value = dateText,
                  onValueChange = {
                    if (it.isDigitsOnly() && it.length <= 8) {
                      dateText = it
                    }
                  },
                  label = { Text("Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  visualTransformation = DateVisualTransformation(), // Apply visual transformation
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.testTag("dateField").fillMaxWidth(),
                  trailingIcon = {
                    IconButton(
                        onClick = {
                          dateTimeUtils.showDatePicker(context) { selectedDate ->
                            dateText =
                                selectedDate.replace(
                                    "/", "") // Use the DatePickerDialog to select a date
                          }
                        },
                        modifier = Modifier.testTag("datePickerButton")) {
                          Icon(
                              imageVector = Icons.Default.DateRange,
                              contentDescription = "Select Date")
                        }
                  })

              // Time Input
              OutlinedTextField(
                  value = timeText,
                  onValueChange = { timeText = it }, // Allow manual input
                  label = { Text("Time") },
                  placeholder = { Text("HH:mm") },
                  modifier = Modifier.fillMaxWidth().testTag("timeField"),
                  trailingIcon = {
                    IconButton(
                        onClick = {
                          dateTimeUtils.showTimePicker(context) { selectedDate ->
                            timeText = selectedDate
                          }
                        },
                        modifier = Modifier.testTag("timePickerButton")) {
                          Icon(
                              imageVector = Icons.Filled.AccessTime,
                              contentDescription = "Select Time")
                        }
                  })

              // Location Input with dropdown (Optional)
              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = {
                      locationViewModel.setQuery(it)
                      showDropdown = true // Show dropdown when user starts typing
                    },
                    label = {
                      Text(
                          "Location (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    placeholder = {
                      Text(
                          "Enter an Address or Location",
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("inputTravelLocation"))

                // Dropdown to show location suggestions
                DropdownMenu(
                    expanded = showDropdown && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { showDropdown = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                      locationSuggestions.filterNotNull().take(3).forEach { location ->
                        DropdownMenuItem(
                            text = {
                              Text(
                                  text =
                                      location.name.take(30) +
                                          if (location.name.length > 30) "..." else "",
                                  maxLines = 1 // Ensure name doesn't overflow
                                  )
                            },
                            onClick = {
                              locationViewModel.setQuery(location.name)
                              selectedLocation = location // Store the selected location object
                              showDropdown = false // Close dropdown on selection
                            },
                            modifier =
                                Modifier.padding(8.dp).testTag("suggestion_${location.name}"))
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

                        activityModelView.addActivity(activity, context)

                        navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
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
