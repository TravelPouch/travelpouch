package com.github.se.travelpouch.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// todo: change all the todos after implementing the modelView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var startDate by remember { mutableStateOf("") }
  var endDate by remember { mutableStateOf("") }

  Scaffold(
      modifier = Modifier.testTag("DashboardScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Create a new task", Modifier.testTag("addTodoTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = {
                    // todo with navigation
                  },
                  Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
  ) { paddingValues ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
              "Title_Field", // todo
              modifier = Modifier.testTag("TitleFieldTag"),
              enabled = true,
              onValueChange = { title = it },
              label = { Text("Title") })

          OutlinedTextField(
              "Destination_Field", // todo
              modifier = Modifier.testTag("DestinationFieldTag"),
              enabled = false,
              onValueChange = {},
              label = { Text("Destination") })

          OutlinedTextField(
              "Budget_Field", // todo
              modifier = Modifier.testTag("BudgetFieldTag"),
              enabled = false,
              onValueChange = {},
              label = { Text("Budget") })

          OutlinedTextField(
              "Travel_Companions_Field", // todo
              modifier = Modifier.testTag("ParticipantsFieldTag"),
              enabled = false,
              onValueChange = {},
              label = { Text("Travel Companions") })

          OutlinedTextField( // contains the duration of the travel with start and end dates
              "Start_Date_Field", // todo
              modifier = Modifier.testTag("StartDateFieldTag"),
              enabled = true,
              onValueChange = { startDate = it },
              label = { Text("Start Date") })

          OutlinedTextField( // contains the duration of the travel with start and end dates
              "End_Date_Field", // todo
              modifier = Modifier.testTag("EndDateFieldTag"),
              enabled = true,
              onValueChange = { endDate = it },
              label = { Text("End Date") })

          OutlinedTextField(
              "Description_Field", // todo
              modifier = Modifier.testTag("DescriptionFieldTag"),
              enabled = true,
              onValueChange = { description = it },
              label = { Text("Description") })
        }
  }
}
