package com.github.se.travelpouch.ui.home

import android.icu.util.GregorianCalendar
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.travelpouch.model.ListTravelViewModel
import com.github.se.travelpouch.model.Location
import com.github.se.travelpouch.model.Participant
import com.github.se.travelpouch.model.Role
import com.github.se.travelpouch.model.TravelContainer
import com.github.se.travelpouch.model.location.LocationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

/**
 * The AddTravelScreen composable function displays the UI for adding a new travel entry to the list
 * of travels in the app. It includes input fields for the title, description, location name, start
 * date, and end date.
 *
 * @param listTravelViewModel: The ViewModel that manages the list of travels in the app.
 * @param navigationActions: The navigation actions to handle navigation within the app.
 * @param locationViewModel: The ViewModel that manages the location search functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTravelScreen(
    listTravelViewModel: ListTravelViewModel = viewModel(factory = ListTravelViewModel.Factory),
    navigationActions: NavigationActions,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    profileModelView: ProfileModelView
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var startDate by remember { mutableStateOf("") }
  var endDate by remember { mutableStateOf("") }

  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsState()
  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("addTravelScreen"), // Tag for entire screen
      topBar = {
        TopAppBar(
            title = {
              Text("Create a new travel", modifier = Modifier.semantics { testTag = "travelTitle" })
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton") // Tag for back button
                  ) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Title Input
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Title") },
                  placeholder = { Text("Name your travel") },
                  modifier = Modifier.fillMaxWidth().testTag("inputTravelTitle"))

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  placeholder = { Text("Describe your travel") },
                  modifier = Modifier.fillMaxWidth().testTag("inputTravelDescription"),
                  maxLines = 5, // Allow the text field to grow up to 5 lines
                  minLines = 3 // Ensure the text field shows at least 3 lines
                  )

              // Location Input

              // Location Input with dropdown
              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = {
                      locationViewModel.setQuery(it)
                      showDropdown = true // Show dropdown when user starts typing
                    },
                    label = { Text("Location") },
                    placeholder = { Text("Enter an Address or Location") },
                    modifier = Modifier.fillMaxWidth().testTag("inputTravelLocation"))

                // Dropdown to show location suggestions
                DropdownMenu(
                    expanded = showDropdown && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { showDropdown = false },
                    properties = PopupProperties(focusable = false),
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(
                                max = 200.dp) // Set max height to make it scrollable if more than 3

                    // items
                    ) {
                      locationSuggestions.filterNotNull().take(3).forEach { location ->
                        DropdownMenuItem(
                            text = {
                              Text(
                                  text =
                                      location.name.take(30) +
                                          if (location.name.length > 30) "..."
                                          else "", // Limit name length and add ellipsis
                                  maxLines = 1 // Ensure name doesn't overflow
                                  )
                            },
                            onClick = {
                              locationViewModel.setQuery(location.name)
                              selectedLocation = location // Store the selected location object
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

              // Start Date Input
              OutlinedTextField(
                  value = startDate,
                  onValueChange = { startDate = it },
                  label = { Text("Start Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  modifier = Modifier.fillMaxWidth().testTag("inputTravelStartDate"))

              // End Date Input
              OutlinedTextField(
                  value = endDate,
                  onValueChange = { endDate = it },
                  label = { Text("End Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  modifier = Modifier.fillMaxWidth().testTag("inputTravelEndDate"))

              Spacer(modifier = Modifier.height(16.dp))

              // Save Button
              Button(
                  onClick = {
                    val startCalendar = parseDate(startDate)
                    val endCalendar = parseDate(endDate)

                    Log.d("AddTravelScreen", "Start date: $startDate, End date: $endDate")
                    Log.d(
                        "AddTravelScreen",
                        "Location - Name: ${selectedLocation?.name}, Latitude: ${selectedLocation?.latitude}, Longitude: ${selectedLocation?.longitude}")

                    // Check if date parsing was successful
                    if (startCalendar == null || endCalendar == null) {
                      Log.e("AddTravelScreen", "Invalid date format")
                      Toast.makeText(
                              context,
                              "Invalid date format. Please use DD/MM/YYYY.",
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      Log.d("AddTravelScreen", "All inputs are valid, proceeding to add travel")

                      // Try to create the TravelContainer object
                      val travelContainer: TravelContainer?
                      try {
                        travelContainer =
                            TravelContainer(
                                fsUid = listTravelViewModel.getNewUid(),
                                title = title,
                                description = description,
                                startTime = Timestamp(startCalendar.time),
                                endTime = Timestamp(endCalendar.time),
                                location =
                                    selectedLocation ?: Location(0.0, 0.0, Timestamp.now(), " "),
                                allAttachments = emptyMap(),
                                allParticipants =
                                    mapOf(
                                        Participant(fsUid = profileModelView.profile.value.fsUid) to
                                            Role.OWNER))

                        Log.d("AddTravelScreen", "TravelContainer created successfully.")
                      } catch (e: Exception) {
                        Log.e("AddTravelScreen", "Error creating travel: $e")
                        Toast.makeText(
                                context,
                                "Error creating travel. Please try again.",
                                Toast.LENGTH_SHORT)
                            .show()
                        return@Button // Return early since we can't proceed if creation failed
                      }

                      // Try to save the TravelContainer using the ViewModel
                      try {
                        // Call the ViewModel method to add the travel data
                        listTravelViewModel.addTravel(travelContainer)

                        Toast.makeText(context, "Travel added successfully!", Toast.LENGTH_SHORT)
                            .show()

                        // Optionally navigate back after successful addition
                        navigationActions.goBack()

                        return@Button // Early return only from the button's onClick
                      } catch (e: Exception) {
                        Log.e("AddTravelScreen", "Error adding travel: $e")
                        Toast.makeText(
                                context,
                                "Error adding travel. Please try again.",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag("travelSaveButton"),
                  enabled =
                      title.isNotBlank() &&
                          selectedLocation != null &&
                          startDate.isNotBlank() &&
                          endDate.isNotBlank()) {
                    Text("Save")
                  }
            }
      })
}

// Function to parse a date in DD/MM/YYYY format
fun parseDate(dateString: String): GregorianCalendar? {
  val parts = dateString.split("/")
  return if (parts.size == 3) {
    try {
      val calendar = GregorianCalendar()
      calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), 0, 0, 0)
      calendar
    } catch (e: Exception) {
      null // Return null if there's a parsing issue
    }
  } else {
    null // Return null if the date format is incorrect
  }
}
