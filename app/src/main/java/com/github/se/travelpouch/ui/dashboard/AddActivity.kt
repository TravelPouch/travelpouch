package com.github.se.travelpouch.ui.dashboard

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.activity.Activity
import com.github.se.travelpouch.model.activity.ActivityViewModel
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.github.se.travelpouch.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
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
  val dateFormat =
      SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
        isLenient = false // strict date format
      }

  val gregorianCalendar = GregorianCalendar()

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

              OutlinedTextField(
                  value = dateText,
                  onValueChange = {
                    if (it.isDigitsOnly() && it.length <= 8) {
                      dateText = it
                    }
                  },
                  label = { Text("Date") },
                  placeholder = { Text("01/01/2024") },
                  visualTransformation = DateVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier.testTag("dateField").fillMaxWidth())

              // Time Input Field (Optional)
              Box(
                  modifier =
                      Modifier.testTag("timeField")
                          .fillMaxWidth()
                          .clickable(
                              interactionSource = remember { MutableInteractionSource() },
                              indication = null,
                              onClick = {
                                // Create and show the TimePickerDialog when clicked
                                TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                          timeText =
                                              String.format(
                                                  Locale.getDefault(),
                                                  "%02d:%02d",
                                                  hourOfDay,
                                                  minute)
                                        },
                                        gregorianCalendar.get(Calendar.HOUR_OF_DAY),
                                        gregorianCalendar.get(Calendar.MINUTE),
                                        true)
                                    .show()
                              })) {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {},
                        label = {
                          Text("Time (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        placeholder = {
                          Text(
                              "HH:MM (Optional)",
                              color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth())
                  }

              // Location Input with dropdown (Optional)
              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = {
                      locationViewModel.setQuery(it)
                      showDropdown = true // Show dropdown when user starts typing
                    },
                    label = {
                      Text("Location (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    try {
                      val finalDate =
                          convertStringToDate(dateText + timeText, dateFormat, gregorianCalendar)

                      val activity =
                          Activity(
                              activityModelView.getNewUid(),
                              title,
                              description,
                              selectedLocation ?: Location(0.0, 0.0, finalDate, "Unknown"),
                              finalDate,
                              mapOf())

                      activityModelView.addActivity(activity, context)
                    } catch (e: java.text.ParseException) {
                      Toast.makeText(
                              context,
                              "Invalid format, date must be DD/MM/YYYY and time must be HH:MM.",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                    navigationActions.navigateTo(Screen.TRAVEL_ACTIVITIES)
                  },
                  modifier = Modifier.testTag("saveButton")) {
                    Text("Save")
                  }
            }
      })
}

/**
 * This function converts a String date to a Timestamp using the dd/MM/yyyy HH:mm format.
 *
 * @param stringDate (String) : the string to convert to a date
 * @param dateFormat (SimpleDateFormat) : the format to use to convert the string to a timestamp
 * @param gregorianCalendar (GregorianCalendar) : the calendar to use to set the date
 * @return (Timestamp) : the timestamp got from the string
 * @throws (Exception) : If the formatting fails an exception is thrown
 */
fun convertStringToDate(
    stringDate: String,
    dateFormat: SimpleDateFormat,
    gregorianCalendar: GregorianCalendar
): Timestamp {
  val day = stringDate.substring(0, 2)
  val month = stringDate.substring(2, 4)
  val year = stringDate.substring(4, 8)
  val hour = if (stringDate.length > 8) stringDate.substring(8, 10) else "00"
  val minute = if (stringDate.length > 10) stringDate.substring(11, 13) else "00"

  val finalDateString = "$day/$month/$year $hour:$minute"

  val date = dateFormat.parse(finalDateString)
  val calendar =
      gregorianCalendar.apply {
        time = date!!
        set(Calendar.SECOND, 0)
      }

  return Timestamp(calendar.time)
}
