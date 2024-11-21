package com.github.se.travelpouch.ui.travel

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.se.travelpouch.model.notifications.Notification
import com.github.se.travelpouch.model.notifications.NotificationContent
import com.github.se.travelpouch.model.notifications.NotificationType
import com.github.se.travelpouch.model.notifications.NotificationViewModel
import com.github.se.travelpouch.model.profile.ProfileModelView
import com.github.se.travelpouch.model.travels.ListTravelViewModel
import com.github.se.travelpouch.model.travels.Location
import com.github.se.travelpouch.model.travels.Role
import com.github.se.travelpouch.model.travels.TravelContainer
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
    profileViewModel: ProfileModelView
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
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(95.dp)) {
              FloatingActionButton(
                  onClick = {
                    setExpandedAddUserDialog(true)
                    Log.d("EditTravelSettingsScreen", "Add User clicked")
                  },
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.testTag("addUserFab").padding(start = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp) // Adjust padding for better alignment
                        ) {
                          Icon(
                              imageVector =
                                  Icons.Default
                                      .PersonAddAlt1, // Replace with your mail icon resource
                              contentDescription = "Mail Icon",
                              modifier = Modifier.size(24.dp) // Adjust size as needed
                              )
                          Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                          Text(
                              text = "Add User",
                              style = MaterialTheme.typography.bodyLarge // Or customize further
                              )
                        }

                    // Text("Add User")
                  }
              FloatingActionButton(
                  onClick = {
                    clipboardManager.setText(
                        AnnotatedString("travelpouchswent+${selectedTravel!!.fsUid}@gmail.com"))
                    Log.d("EditTravelSettingsScreen", "Email copied to clipboard")
                  },
                  containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.testTag("importEmailFab")) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp) // Adjust padding for better alignment
                        ) {
                          Icon(
                              imageVector =
                                  Icons.Default.MailOutline, // Replace with your mail icon resource
                              contentDescription = "Mail Icon",
                              modifier = Modifier.size(24.dp) // Adjust size as needed
                              )
                          Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                          Text(
                              text = "Import Email",
                              style = MaterialTheme.typography.bodyLarge // Or customize further
                              )
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
                              .testTag("editTravelParticipantIcon")
                              .clickable {
                                listTravelViewModel.fetchAllParticipantsInfo()
                                navigationActions.navigateTo(PARTICIPANT_LIST)
                              })
                  Text(
                      "${selectedTravel!!.allParticipants.size} participants",
                      modifier = Modifier.weight(1f).testTag("inputParticipants"))
                }

            OutlinedTextField(
                value = titleText.value,
                onValueChange = { keystroke -> titleText.value = keystroke },
                modifier =
                    Modifier.testTag("inputTravelTitle").fillMaxWidth().padding(horizontal = 10.dp),
                label = { Text("Title") },
                placeholder = { Text("Name the Travel") },
                shape = RoundedCornerShape(6.dp),
            )
            OutlinedTextField(
                value = descriptionText.value,
                onValueChange = { keystroke -> descriptionText.value = keystroke },
                modifier =
                    Modifier.testTag("inputTravelDescription")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                label = { Text("Description") },
                placeholder = { Text("Describe the Travel") },
                shape = RoundedCornerShape(6.dp))

            OutlinedTextField(
                value = locationName.value,
                onValueChange = { locationName.value = it },
                label = { Text("Location Name") },
                placeholder = { Text("Enter location name") },
                modifier =
                    Modifier.testTag("inputTravelLocationName")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(6.dp))

            // Latitude Input
            OutlinedTextField(
                value = latitude.value,
                onValueChange = { latitude.value = it },
                label = { Text("Latitude") },
                placeholder = { Text("Enter latitude (e.g. 48.8566)") },
                modifier =
                    Modifier.testTag("inputTravelLatitude")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(6.dp))

            // Longitude Input
            OutlinedTextField(
                value = longitude.value,
                onValueChange = { longitude.value = it },
                label = { Text("Longitude") },
                placeholder = { Text("Enter longitude (e.g. 2.3522)") },
                modifier =
                    Modifier.testTag("inputTravelLongitude")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(6.dp))
            OutlinedTextField(
                value = startTime.value,
                onValueChange = { keystroke -> startTime.value = keystroke }, // Allow manual input
                label = { Text("Start Date") },
                placeholder = { Text("DD/MM/YYYY") },
                modifier =
                    Modifier.testTag("inputTravelStartTime")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
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
                    Modifier.testTag("inputTravelEndTime")
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
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
                            listParticipant = selectedTravel!!.listParticipant)
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
    if (expandedAddUserDialog) {
      val addUserEmail = remember { mutableStateOf("") }
      Dialog(onDismissRequest = { setExpandedAddUserDialog(false) }) {
        Box(
            Modifier.fillMaxWidth(1f)
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surface)
                .testTag("addUserDialogBox")) {
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(16.dp)
                          .verticalScroll(rememberScrollState())
                          .testTag("roleDialogColumn"),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    Text(
                        "Add User by Email",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp).testTag("addUserDialogTitle"))
                    OutlinedTextField(
                        value = addUserEmail.value,
                        onValueChange = { addUserEmail.value = it },
                        label = { Text("Enter User's Email") },
                        placeholder = { Text("Enter User's Email") },
                        modifier = Modifier.testTag("addUserEmailField"))
                    Button(
                        onClick = {
                          profileViewModel.getFsUidByEmail(
                              addUserEmail.value,
                              onSuccess = { fsUid ->
                                val isUserAlreadyAdded =
                                    selectedTravel!!.allParticipants.keys.any { it.fsUid == fsUid }
                                if (fsUid == profileViewModel.profile.value.fsUid) {
                                  Toast.makeText(
                                          context,
                                          "Error: You can't invite yourself",
                                          Toast.LENGTH_SHORT)
                                      .show()
                                } else if (isUserAlreadyAdded) {
                                  Toast.makeText(
                                          context, "Error: User already added", Toast.LENGTH_SHORT)
                                      .show()
                                } else if (fsUid != null) {
                                  try {
                                    notificationViewModel.sendNotification(
                                        Notification(
                                            notificationViewModel.getNewUid(),
                                            profileViewModel.profile.value.fsUid,
                                            fsUid,
                                            selectedTravel!!.fsUid,
                                            NotificationContent.InvitationNotification(
                                                profileViewModel.profile.value.name,
                                                selectedTravel!!.title,
                                                Role.PARTICIPANT),
                                            NotificationType.INVITATION))
                                  } catch (e: Exception) {
                                    Log.e(
                                        "NotificationError",
                                        "Failed to send notification: ${e.message}")
                                  }
                                  // Go back
                                  setExpandedAddUserDialog(false)
                                } else {
                                  Toast.makeText(
                                          context,
                                          "Error: User with email not found",
                                          Toast.LENGTH_SHORT)
                                      .show()
                                }
                              },
                              onFailure = { e ->
                                Log.e("EditTravelSettingsScreen", "Error getting fsUid by email", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                              })
                        },
                        modifier = Modifier.testTag("addUserButton")) {
                          Text("Add User")
                        }
                  }
            }
      }
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
