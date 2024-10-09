package com.github.se.travelpouch.ui.overview

import android.icu.util.GregorianCalendar
import android.widget.Toast
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
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTravelScreen() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var locationQuery by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTag = "addTravelScreen" },  // Tag for entire screen
        topBar = {
            TopAppBar(
                title = { Text("Create a new travel", modifier = Modifier.semantics { testTag = "travelTitle" }) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            //navigationActions.goBack()
                        },
                        modifier = Modifier.semantics { testTag = "goBackButton" } // Tag for back button
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Name your travel") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "inputTravelTitle" }  // Tag for title input
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe your travel") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics { testTag = "inputTravelDescription" }  // Tag for description input
                )

                // Location Selector (assuming future implementation)

                // Start Date Input
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "inputTravelStartDate" }  // Tag for start date input
                )

                // End Date Input
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "inputTravelEndDate" }  // Tag for end date input
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        val startCalendar = GregorianCalendar()
                        val endCalendar = GregorianCalendar()

                        val startParts = startDate.split("/")
                        val endParts = endDate.split("/")

                        if (startParts.size == 3 && endParts.size == 3) {
                            try {
                                startCalendar.set(
                                    startParts[2].toInt(),
                                    startParts[1].toInt() - 1,
                                    startParts[0].toInt(),
                                    0,
                                    0,
                                    0
                                )
                                endCalendar.set(
                                    endParts[2].toInt(),
                                    endParts[1].toInt() - 1,
                                    endParts[0].toInt(),
                                    0,
                                    0,
                                    0
                                )
                                /*
                                                                travelViewModel.addTravel(
                                                                    TravelContainer(
                                                                        fsUid = "6766", //todo : travelViewModel.getNewUid(),
                                                                        title = title,
                                                                        description = description,
                                                                        startTime = Timestamp(startCalendar.time),
                                                                        endTime = Timestamp(endCalendar.time),
                                                                        location = selectedLocation ?: Location(0.0, 0.0, Timestamp.now(), ""),
                                                                        allAttachments = emptyMap(),
                                                                        allParticipants = mapOf(
                                                                            Participant(fsUid = travelViewModel.getNewUid()) to Role.OWNER
                                                                        )
                                                                    )
                                                                )
                                */

                                //navigationActions.goBack()
                                return@Button
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context, "Invalid date format. Please use DD/MM/YYYY.", Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context, "Invalid date format. Please use DD/MM/YYYY.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "travelSaveButton" },  // Tag for save button
                    enabled = title.isNotBlank() && selectedLocation != null
                ) {
                    Text("Save")
                }
            }
        }
    )
}
