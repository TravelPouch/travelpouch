package com.github.se.travelpouch.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.github.se.travelpouch.model.*
import com.github.se.travelpouch.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTravelScreen(
    listTravelViewModel: ListTravelViewModel,
    navigationActions: NavigationActions
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  var startDate by remember { mutableStateOf("") }
  var endDate by remember { mutableStateOf("") }
  val context = LocalContext.current

  var locationName by remember { mutableStateOf("") }
  var latitude by remember { mutableStateOf("") }
  var longitude by remember { mutableStateOf("") }

  Scaffold(
      modifier =
          Modifier.fillMaxSize().semantics { testTag = "addTravelScreen" }, // Tag for entire screen
      topBar = {
        TopAppBar(
            title = {
              Text("Create a new travel", modifier = Modifier.semantics { testTag = "travelTitle" })
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.semantics { testTag = "goBackButton" } // Tag for back button
                  ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              // Title Input
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Title") },
                  placeholder = { Text("Name your travel") },
                  modifier =
                      Modifier.fillMaxWidth().semantics {
                        testTag = "inputTravelTitle"
                      } // Tag for title input
                  )

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  placeholder = { Text("Describe your travel") },
                  modifier =
                      Modifier.fillMaxWidth().height(200.dp).semantics {
                        testTag = "inputTravelDescription"
                      } // Tag for description input
                  )

              // Location Input
              // Location Name Input
              OutlinedTextField(
                  value = locationName,
                  onValueChange = { locationName = it },
                  label = { Text("Location Name") },
                  placeholder = { Text("Enter location name") },
                  modifier =
                      Modifier.fillMaxWidth().semantics { testTag = "inputTravelLocationName" })

              // Latitude Input
              OutlinedTextField(
                  value = latitude,
                  onValueChange = { latitude = it },
                  label = { Text("Latitude") },
                  placeholder = { Text("Enter latitude (e.g. 48.8566)") },
                  modifier = Modifier.fillMaxWidth().semantics { testTag = "inputTravelLatitude" })

              // Longitude Input
              OutlinedTextField(
                  value = longitude,
                  onValueChange = { longitude = it },
                  label = { Text("Longitude") },
                  placeholder = { Text("Enter longitude (e.g. 2.3522)") },
                  modifier = Modifier.fillMaxWidth().semantics { testTag = "inputTravelLongitude" })

              // Start Date Input
              OutlinedTextField(
                  value = startDate,
                  onValueChange = { startDate = it },
                  label = { Text("Start Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  modifier =
                      Modifier.fillMaxWidth().semantics {
                        testTag = "inputTravelStartDate"
                      } // Tag for start date input
                  )

              // End Date Input
              OutlinedTextField(
                  value = endDate,
                  onValueChange = { endDate = it },
                  label = { Text("End Date") },
                  placeholder = { Text("DD/MM/YYYY") },
                  modifier =
                      Modifier.fillMaxWidth().semantics {
                        testTag = "inputTravelEndDate"
                      } // Tag for end date input
                  )

              Spacer(modifier = Modifier.height(16.dp))

              // Save Button
              Button(
                  onClick = {},
                  modifier =
                      Modifier.fillMaxWidth().semantics {
                        testTag = "travelSaveButton"
                      }, // Tag for save button
                  enabled =
                      title.isNotBlank() &&
                          locationName.isNotBlank() &&
                          latitude.isNotBlank() &&
                          longitude.isNotBlank() &&
                          startDate.isNotBlank() &&
                          endDate.isNotBlank()) {
                    Text("Save")
                  }
            }
      })
}
